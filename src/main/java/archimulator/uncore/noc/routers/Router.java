/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.uncore.noc.routers;

import archimulator.uncore.noc.Direction;
import archimulator.uncore.noc.Node;
import archimulator.uncore.noc.Packet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Router.
 *
 * @author Min Cai
 */
public class Router {
    private Node node;

    private List<Packet> injectionBuffer;

    private Map<Direction, InputPort> inputPorts;

    private Map<Direction, OutputPort> outputPorts;

    private Map<FlitState, Integer> numInflightHeadFlits;

    private Map<FlitState, Integer> numInflightNonHeadFlits;

    /**
     * Create a router.
     *
     * @param node the parent node
     */
    public Router(Node node) {
        this.node = node;

        this.injectionBuffer = new ArrayList<>();

        this.inputPorts = new TreeMap<>();
        for (Direction direction : Direction.values()) {
            this.inputPorts.put(direction, new InputPort(this, direction));
        }

        this.outputPorts = new TreeMap<>();
        for (Direction direction : Direction.values()) {
            this.outputPorts.put(direction, new OutputPort(this, direction));
        }

        this.numInflightHeadFlits = new TreeMap<>();
        for(FlitState flitState : FlitState.values()) {
            this.numInflightHeadFlits.put(flitState, 0);
        }

        this.numInflightNonHeadFlits = new TreeMap<>();
        for(FlitState flitState : FlitState.values()) {
            this.numInflightNonHeadFlits.put(flitState, 0);
        }

        this.node.getNetwork().getCycleAccurateEventQueue().getPerCycleEvents().add(this::advanceOneCycle);
    }

    /**
     * Advance one cycle.
     */
    private void advanceOneCycle() {
        if (this.getNode().getNetwork().getEnvironment().isInDetailedSimulationMode()) {
            this.stageLinkTraversal();

            this.stageSwitchTraversal();

            this.stageSwitchAllocation();

            this.stageVirtualChannelAllocation();

            this.stageRouteComputation();

            this.localPacketInjection();
        }
    }

    /**
     * The link traversal stage.
     */
    private void stageLinkTraversal() {
        if(this.numInflightHeadFlits.get(FlitState.SWITCH_TRAVERSAL) == 0
                && this.numInflightNonHeadFlits.get(FlitState.SWITCH_TRAVERSAL) == 0) {
            return;
        }

        for(OutputPort outputPort : this.outputPorts.values()) {
            for(OutputVirtualChannel outputVirtualChannel : outputPort.getVirtualChannels()) {
                InputVirtualChannel inputVirtualChannel = outputVirtualChannel.getInputVirtualChannel();
                if(inputVirtualChannel != null && outputVirtualChannel.getCredits() > 0) {
                    Flit flit = inputVirtualChannel.getInputBuffer().peek();
                    if(flit != null && flit.getState() == FlitState.SWITCH_TRAVERSAL) {
                        if(outputPort.getDirection() != Direction.LOCAL) {
                            flit.setNodeAndState(this.node, FlitState.LINK_TRAVERSAL);

                            int nextHop = this.node.getNeighbors().get(outputPort.getDirection());
                            Direction ip = outputPort.getDirection().getReflexDirection();
                            int ivc = outputVirtualChannel.getId();

                            this.node.getNetwork().getCycleAccurateEventQueue().schedule(this, () -> {
                                nextHopArrived(flit, nextHop, ip, ivc);
                            }, this.node.getNetwork().getEnvironment().getConfig().getLinkDelay());
                        }

                        inputVirtualChannel.getInputBuffer().pop();

                        if(outputPort.getDirection() != Direction.LOCAL) {
                            outputVirtualChannel.setCredits(outputVirtualChannel.getCredits() - 1);
                        } else {
                            flit.setNodeAndState(this.node, FlitState.DESTINATION_ARRIVED);
                        }

                        if(flit.isTail()) {
                            inputVirtualChannel.setOutputVirtualChannel(null);
                            outputVirtualChannel.setInputVirtualChannel(null);

                            if(outputPort.getDirection() == Direction.LOCAL) {
                                this.node.getSelectionAlgorithm().handleDestArrived(flit.getPacket(), inputVirtualChannel);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Handle the event when a flit arrives at the next hop node.
     *
     * @param flit the flit
     * @param nextHop the next hop node ID
     * @param ip the input port direction
     * @param ivc the input virtual channel
     */
    private void nextHopArrived(Flit flit, int nextHop, Direction ip, int ivc) {
        InputBuffer inputBuffer =
                this.node.getNetwork().getNodes().get(nextHop).getRouter().getInputPorts().get(ip).getVirtualChannels().get(ivc).getInputBuffer();

        if(inputBuffer.size() + 1 <= this.node.getNetwork().getEnvironment().getConfig().getMaxInputBufferSize()) {
            this.node.getNetwork().getNodes().get(nextHop).getRouter().insertFlit(flit, ip, ivc);
        } else {
            this.node.getNetwork().getCycleAccurateEventQueue().schedule(this, () -> this.nextHopArrived(flit, nextHop, ip, ivc), 1);
        }
    }

    /**
     * The switch traversal stage.
     */
    private void stageSwitchTraversal() {
        if(this.numInflightHeadFlits.get(FlitState.SWITCH_ALLOCATION) == 0
                && this.numInflightNonHeadFlits.get(FlitState.SWITCH_ALLOCATION) == 0) {
            return;
        }

        for (OutputPort outputPort: this.outputPorts.values()) {
            for (InputPort inputPort : this.inputPorts.values()) {
                if (outputPort.getDirection() == inputPort.getDirection()) {
                    continue;
                }

                for(InputVirtualChannel inputVirtualChannel : inputPort.getVirtualChannels()) {
                    if(inputVirtualChannel.getOutputVirtualChannel() != null
                            && inputVirtualChannel.getOutputVirtualChannel().getOutputPort() == outputPort) {
                        Flit flit = inputVirtualChannel.getInputBuffer().peek();
                        if(flit != null && flit.getState() == FlitState.SWITCH_ALLOCATION) {
                            flit.setNodeAndState(this.node, FlitState.SWITCH_TRAVERSAL);

                            if(inputPort.getDirection() != Direction.LOCAL) {
                                Node parent = this.node.getNetwork().getNodes().get(
                                        this.node.getNeighbors().get(inputPort.getDirection())
                                );

                                OutputVirtualChannel outputVirtualChannelAtParent =
                                        parent.getRouter().getOutputPorts().get(inputPort.getDirection().getReflexDirection())
                                                .getVirtualChannels().get(inputVirtualChannel.getId());

                                outputVirtualChannelAtParent.setCredits(outputVirtualChannelAtParent.getCredits() + 1);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * The switch allocation stage.
     */
    private void stageSwitchAllocation() {
        if(this.numInflightHeadFlits.get(FlitState.VIRTUAL_CHANNEL_ALLOCATION) == 0
                && this.numInflightNonHeadFlits.get(FlitState.INPUT_BUFFER) == 0) {
            return;
        }

        for(OutputPort outputPort : this.outputPorts.values()) {
            InputVirtualChannel winnerInputVirtualChannel = outputPort.getArbiter().next();

            if(winnerInputVirtualChannel != null) {
                Flit flit = winnerInputVirtualChannel.getInputBuffer().peek();
                flit.setNodeAndState(this.node, FlitState.SWITCH_ALLOCATION);
            }
        }
    }

    /**
     * The virtual channel allocation stage.
     */
    private void stageVirtualChannelAllocation() {
        if(this.numInflightHeadFlits.get(FlitState.ROUTE_COMPUTATION) == 0) {
            return;
        }

        for(OutputPort outputPort : this.outputPorts.values()) {
            for(OutputVirtualChannel outputVirtualChannel : outputPort.getVirtualChannels()) {
                if(outputVirtualChannel.getInputVirtualChannel() == null) {
                    InputVirtualChannel winnerInputVirtualChannel = outputVirtualChannel.getArbiter().next();

                    if(winnerInputVirtualChannel != null) {
                        Flit flit = winnerInputVirtualChannel.getInputBuffer().peek();
                        flit.setNodeAndState(this.node, FlitState.VIRTUAL_CHANNEL_ALLOCATION);

                        winnerInputVirtualChannel.setOutputVirtualChannel(outputVirtualChannel);
                        outputVirtualChannel.setInputVirtualChannel(winnerInputVirtualChannel);
                    }
                }
            }
        }
    }

    /**
     * The route computation stage.
     */
    private void stageRouteComputation() {
        if(this.numInflightHeadFlits.get(FlitState.INPUT_BUFFER) == 0) {
            return;
        }

        for(InputPort inputPort : this.inputPorts.values()) {
            for(InputVirtualChannel inputVirtualChannel : inputPort.getVirtualChannels()) {
                Flit flit = inputVirtualChannel.getInputBuffer().peek();

                if(flit != null && flit.isHead() && flit.getState() == FlitState.INPUT_BUFFER) {
                    if(flit.getPacket().getDest() == this.node.getId()) {
                        inputVirtualChannel.setRoute(Direction.LOCAL);
                    } else {
                        inputVirtualChannel.setRoute(
                                this.node.getSelectionAlgorithm().doRouteCalculation(
                                        flit.getPacket(), inputVirtualChannel
                                )
                        );
                    }

                    flit.setNodeAndState(this.node, FlitState.ROUTE_COMPUTATION);
                }
            }
        }
    }

    /**
     * Handle the local packet injection.
     */
    private void localPacketInjection() {
        while (true) {
            boolean requestInserted = false;

            for(int ivc = 0; ivc < this.node.getNetwork().getEnvironment().getConfig().getNumVirtualChannels(); ivc++) {
                if(this.injectionBuffer.isEmpty()) {
                    return;
                }

                Packet packet = this.injectionBuffer.get(0);

                int numFlits = (int) Math.ceil((double)(packet.getSize()) / this.node.getNetwork().getEnvironment().getConfig().getLinkWidth());

                InputBuffer inputBuffer = this.inputPorts.get(Direction.LOCAL).getVirtualChannels().get(ivc).getInputBuffer();

                if(inputBuffer.size() + numFlits <= this.node.getNetwork().getEnvironment().getConfig().getMaxInputBufferSize()) {
                    for(int i = 0; i < numFlits; i++) {
                        Flit flit = new Flit(packet, i, i == 0, i == numFlits - 1);
                        this.insertFlit(flit, Direction.LOCAL, ivc);
                    }

                    this.injectionBuffer.remove(packet);
                    requestInserted = true;
                    break;
                }
            }

            if(!requestInserted) {
                break;
            }
        }
    }

    /**
     * Inject the specified packet into the router.
     *
     * @param packet the packet
     * @return whether the injection succeeds or not
     */
    public boolean injectPacket(Packet packet) {
        if(this.injectionBuffer.size() < this.node.getNetwork().getEnvironment().getConfig().getMaxInjectionBufferSize()) {
            this.injectionBuffer.add(packet);
            return true;
        }

        return false;
    }

    /**
     * Insert the flit into the router.
     *
     * @param flit the flit
     * @param ip the input port direction
     * @param ivc the input virtual channel number
     */
    public void insertFlit(Flit flit, Direction ip, int ivc) {
        this.inputPorts.get(ip).getVirtualChannels().get(ivc).getInputBuffer().append(flit);

        flit.setNodeAndState(this.node, FlitState.INPUT_BUFFER);
    }

    /**
     * Get the number of free slots in the specified input virtual channel.
     *
     * @param ip the input port direction
     * @param ivc the input virtual channel number
     * @return the number of free slots in the specified input virtual channel
     */
    public int getFreeSlots(Direction ip, int ivc) {
        return this.inputPorts.get(ip).getVirtualChannels().get(ivc).getInputBuffer().getFreeSlots();
    }

    @Override
    public String toString() {
        return String.format("Router{node=%s}", node);
    }

    /**
     * Get the node.
     *
     * @return the node
     */
    public Node getNode() {
        return node;
    }

    /**
     * Get the injection buffer.
     *
     * @return the injection buffer
     */
    public List<Packet> getInjectionBuffer() {
        return injectionBuffer;
    }

    /**
     * Get the map of input ports.
     *
     * @return the map of input ports
     */
    public Map<Direction, InputPort> getInputPorts() {
        return inputPorts;
    }

    /**
     * Get the map of output ports.
     *
     * @return the map of output ports
     */
    public Map<Direction, OutputPort> getOutputPorts() {
        return outputPorts;
    }

    /**
     * Get the number of inflight head flits.
     *
     * @return the number of inflight head flits
     */
    public Map<FlitState, Integer> getNumInflightHeadFlits() {
        return numInflightHeadFlits;
    }

    /**
     * Get the number of inflight non-head flits.
     *
     * @return the number of inflight non-head flits
     */
    public Map<FlitState, Integer> getNumInflightNonHeadFlits() {
        return numInflightNonHeadFlits;
    }
}
