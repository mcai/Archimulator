package archimulator.uncore.net.noc.routers;

import archimulator.common.SimulationType;
import archimulator.uncore.net.noc.Direction;
import archimulator.uncore.net.noc.Node;
import archimulator.uncore.net.noc.Packet;

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

    private Map<FlitState, Integer> numInflightFlits;

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

        this.numInflightFlits = new TreeMap<>();
        for(FlitState flitState : FlitState.values()) {
            this.numInflightFlits.put(flitState, 0);
        }

        this.node.getNetwork().getCycleAccurateEventQueue().getPerCycleEvents().add(this::advanceOneCycle);
    }

    private void advanceOneCycle() {
        if (this.getNode().getNetwork().getMemoryHierarchy().getSimulation().getType() == SimulationType.MEASUREMENT
                || this.getNode().getNetwork().getMemoryHierarchy().getSimulation().getType() == SimulationType.WARMUP) {
            this.stageLinkTraversal();

            this.stageSwitchTraversal();

            this.stageSwitchAllocation();

            this.stageVirtualChannelAllocation();

            this.stageRouteComputation();

            this.localPacketInjection();
        }
    }

    private void stageLinkTraversal() {
        if(this.numInflightFlits.get(FlitState.SWITCH_TRAVERSAL) == 0) {
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
                            }, this.node.getNetwork().getMemoryHierarchy().getExperiment().getConfig().getLinkDelay());
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
                                this.node.handleDestArrived(flit.getPacket(), inputVirtualChannel);
                            }
                        }
                    }
                }
            }
        }
    }

    private void nextHopArrived(Flit flit, int nextHop, Direction ip, int ivc) {
        InputBuffer inputBuffer =
                this.node.getNetwork().getNodes().get(nextHop).getRouter().getInputPorts().get(ip).getVirtualChannels().get(ivc).getInputBuffer();

        if(inputBuffer.size() + 1 <= this.node.getNetwork().getMemoryHierarchy().getExperiment().getConfig().getMaxInputBufferSize()) {
            this.node.getNetwork().getNodes().get(nextHop).getRouter().insertFlit(flit, ip, ivc);
        } else {
            this.node.getNetwork().getCycleAccurateEventQueue().schedule(this, () -> this.nextHopArrived(flit, nextHop, ip, ivc), 1);
        }
    }

    private void stageSwitchTraversal() {
        if(this.numInflightFlits.get(FlitState.SWITCH_ALLOCATION) == 0) {
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

    private void stageSwitchAllocation() {
        if(this.numInflightFlits.get(FlitState.VIRTUAL_CHANNEL_ALLOCATION) == 0
                && this.numInflightFlits.get(FlitState.INPUT_BUFFER) == 0) {
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

    private void stageVirtualChannelAllocation() {
        if(this.numInflightFlits.get(FlitState.ROUTE_COMPUTATION) == 0) {
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

    private void stageRouteComputation() {
        if(this.numInflightFlits.get(FlitState.INPUT_BUFFER) == 0) {
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
                                this.node.doRouteCalculation(
                                        flit.getPacket(), inputVirtualChannel
                                )
                        );
                    }

                    flit.setNodeAndState(this.node, FlitState.ROUTE_COMPUTATION);
                }
            }
        }
    }

    private void localPacketInjection() {
        while (true) {
            boolean requestInserted = false;

            for(int ivc = 0; ivc < this.node.getNetwork().getMemoryHierarchy().getExperiment().getConfig().getNumVirtualChannels(); ivc++) {
                if(this.injectionBuffer.isEmpty()) {
                    return;
                }

                Packet packet = this.injectionBuffer.get(0);

                int numFlits = (int) Math.ceil((double)(packet.getSize()) / this.node.getNetwork().getMemoryHierarchy().getExperiment().getConfig().getLinkWidth());

                InputBuffer inputBuffer = this.inputPorts.get(Direction.LOCAL).getVirtualChannels().get(ivc).getInputBuffer();

                if(inputBuffer.size() + numFlits <= this.node.getNetwork().getMemoryHierarchy().getExperiment().getConfig().getMaxInputBufferSize()) {
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

    public boolean injectPacket(Packet packet) {
        if(this.injectionBuffer.size() < this.node.getNetwork().getMemoryHierarchy().getExperiment().getConfig().getMaxInjectionBufferSize()) {
            this.injectionBuffer.add(packet);
            return true;
        }

        return false;
    }

    public void insertFlit(Flit flit, Direction ip, int ivc) {
        this.inputPorts.get(ip).getVirtualChannels().get(ivc).getInputBuffer().append(flit);

        flit.setNodeAndState(this.node, FlitState.INPUT_BUFFER);
    }

    public int freeSlots(Direction ip, int ivc) {
        return this.node.getNetwork().getMemoryHierarchy().getExperiment().getConfig().getMaxInputBufferSize()
                - this.inputPorts.get(ip).getVirtualChannels().get(ivc).getInputBuffer().size();
    }

    @Override
    public String toString() {
        return String.format("Router{node=%s}", node);
    }

    public Node getNode() {
        return node;
    }

    public List<Packet> getInjectionBuffer() {
        return injectionBuffer;
    }

    public Map<Direction, InputPort> getInputPorts() {
        return inputPorts;
    }

    public Map<Direction, OutputPort> getOutputPorts() {
        return outputPorts;
    }

    public Map<FlitState, Integer> getNumInflightFlits() {
        return numInflightFlits;
    }
}
