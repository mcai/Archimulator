/*******************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
 *
 * This file is part of the Archimulator multicore architectural simulator.
 *
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package archimulator.uncore.net.basic;

import archimulator.util.math.Counter;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * Router.
 *
 * @author Min Cai
 */
public class Router {
    private BasicNet net;

    private RouterType type;
    private int id;

    private int x;
    private int y;

    private EnumMap<Port, Router> links;

    private List<Packet> injectionBuffer;
    private int injectionBufferMaxSize;

    private int inputBufferMaxSize;

    private EnumMap<Port, List<InputVirtualChannel>> inputVirtualChannels;

    private EnumMap<Port, List<OutputVirtualChannel>> outputVirtualChannels;
    private EnumMap<Port, List<Boolean>> outputVirtualChannelAvailables;

    private EnumMap<Port, List<Counter>> credits;

    private EnumMap<Port, Boolean> switchAvailables;

    private EnumMap<Port, Boolean> linkAvailables;

    /**
     * Create a router.
     *
     * @param net the net
     * @param type the type of the router
     * @param id the ID of the router
     */
    public Router(BasicNet net, RouterType type, int id) {
        this.net = net;
        this.type = type;
        this.id = id;

        this.x = -1;
        this.y = -1;

        this.links = new EnumMap<>(Port.class);

        this.injectionBuffer = new ArrayList<>();
        this.injectionBufferMaxSize = 32;

        this.inputBufferMaxSize = 10;

        this.inputVirtualChannels = new EnumMap<>(Port.class);
        this.inputVirtualChannels.put(Port.LOCAL, new ArrayList<>());
        this.inputVirtualChannels.put(Port.LEFT, new ArrayList<>());
        this.inputVirtualChannels.put(Port.RIGHT, new ArrayList<>());
        this.inputVirtualChannels.put(Port.UP, new ArrayList<>());
        this.inputVirtualChannels.put(Port.DOWN, new ArrayList<>());

        for(Port inputPort : Port.values()) {
            for(int i = 0; i < this.net.getNumVirtualChannels(); i++) {
                this.inputVirtualChannels.get(inputPort).add(new InputVirtualChannel(inputPort, i));
            }
        }

        this.outputVirtualChannels = new EnumMap<>(Port.class);
        this.outputVirtualChannels.put(Port.LOCAL, new ArrayList<>());
        this.outputVirtualChannels.put(Port.LEFT, new ArrayList<>());
        this.outputVirtualChannels.put(Port.RIGHT, new ArrayList<>());
        this.outputVirtualChannels.put(Port.UP, new ArrayList<>());
        this.outputVirtualChannels.put(Port.DOWN, new ArrayList<>());

        for(Port outputPort : Port.values()) {
            for(int i = 0; i < this.net.getNumVirtualChannels(); i++) {
                this.outputVirtualChannels.get(outputPort).add(new OutputVirtualChannel(outputPort, i));
            }
        }

        this.outputVirtualChannelAvailables = new EnumMap<>(Port.class);
        this.outputVirtualChannelAvailables.put(Port.LOCAL, new ArrayList<>());
        this.outputVirtualChannelAvailables.put(Port.LEFT, new ArrayList<>());
        this.outputVirtualChannelAvailables.put(Port.RIGHT, new ArrayList<>());
        this.outputVirtualChannelAvailables.put(Port.UP, new ArrayList<>());
        this.outputVirtualChannelAvailables.put(Port.DOWN, new ArrayList<>());

        for(Port outputPort : Port.values()) {
            for(int i = 0; i < this.net.getNumVirtualChannels(); i++) {
                this.outputVirtualChannelAvailables.get(outputPort).add(true);
            }
        }

        this.credits = new EnumMap<>(Port.class);
        this.credits.put(Port.LOCAL, new ArrayList<>());
        this.credits.put(Port.LEFT, new ArrayList<>());
        this.credits.put(Port.RIGHT, new ArrayList<>());
        this.credits.put(Port.UP, new ArrayList<>());
        this.credits.put(Port.DOWN, new ArrayList<>());

        for(Port port : Port.values()) {
            for(int i = 0; i < this.net.getNumVirtualChannels(); i++) {
                this.credits.get(port).add(new Counter(10));
            }
        }

        this.switchAvailables = new EnumMap<>(Port.class);
        this.switchAvailables.put(Port.LOCAL, true);
        this.switchAvailables.put(Port.LEFT, true);
        this.switchAvailables.put(Port.RIGHT, true);
        this.switchAvailables.put(Port.UP, true);
        this.switchAvailables.put(Port.DOWN, true);

        this.linkAvailables = new EnumMap<>(Port.class);
        this.linkAvailables.put(Port.LOCAL, true);
        this.linkAvailables.put(Port.LEFT, true);
        this.linkAvailables.put(Port.RIGHT, true);
        this.linkAvailables.put(Port.UP, true);
        this.linkAvailables.put(Port.DOWN, true);
    }

    /**
     * Advance one cycle.
     */
    public void advanceOneCycle() {
        this.stageLinkTraversal();
        this.stageSwitchAllocation();
        this.stageSwitchTraversal();
        this.stageRouteCalculation();
        this.stageVirtualChannelAllocation();
        this.localPacketInjection();
    }

    /**
     * The link traversal (LT) stage.
     */
    private void stageLinkTraversal() {
        for(Port outputPort : Port.values()) {
            if(!this.linkAvailables.get(outputPort)) {
                continue;
            }

            long oldestCycle = Long.MAX_VALUE;
            OutputVirtualChannel outputVirtualChannelFound = null;

            for(int i = 0; i < this.net.getNumVirtualChannels(); i++) {
                int index = (int) ((i + this.net.getCycleAccurateEventQueue().getCurrentCycle())
                        % this.net.getNumVirtualChannels());
                OutputVirtualChannel outputVirtualChannel = this.outputVirtualChannels.get(outputPort).get(index);
                if (outputVirtualChannel.getOutputBuffer().isEmpty() ||
                        this.credits.get(outputPort).get(index).getValue() == 0) {
                    continue;
                }

                Flit flit = outputVirtualChannel.getOutputBuffer().get(0);

                if(flit.getState() != FlitState.SWITCH_TRAVERSAL) {
                    throw new IllegalArgumentException(flit.getState() + "");
                }

                if(flit.getTimestamp() < oldestCycle) {
                    oldestCycle = flit.getTimestamp();
                    outputVirtualChannelFound = outputVirtualChannel;
                    break;
                }
            }

            if(outputVirtualChannelFound != null) {
                Flit flit = outputVirtualChannelFound.getOutputBuffer().get(0);

                int outputVirtualChannelFoundNum = outputVirtualChannelFound.getNum();

                if(outputPort != Port.LOCAL) {
                    this.net.getCycleAccurateEventQueue().schedule(
                            this, () -> this.links.get(outputPort).insertFlit(
                                    flit,
                                    outputPort.opposite(),
                                    outputVirtualChannelFoundNum
                            ), this.net.getLinkLatency() + 1);

                    this.linkAvailables.put(outputPort, false);
                    this.net.getCycleAccurateEventQueue().schedule(
                            this, () -> this.linkAvailables.put(outputPort, true), this.net.getLinkLatency());
                }

                outputVirtualChannelFound.getOutputBuffer().remove(0);

                if(outputPort != Port.LOCAL) {
                    this.credits.get(outputPort).get(outputVirtualChannelFoundNum).decrement();
                }

                if(flit.isTail()) {
                    this.outputVirtualChannelAvailables.get(outputPort).set(outputVirtualChannelFoundNum, true);

                    if(outputPort == Port.LOCAL) {
                        flit.getPacket().getOnCompletedCallback().apply();
                    }
                }
            }
        }
    }

    /**
     * The switch allocation (SA) stage.
     */
    private void stageSwitchAllocation() {
        for(Port outputPort : Port.values()) {
            if(this.switchAvailables.get(outputPort)) {
                InputVirtualChannel inputVirtualChannel = this.stageSwitchAllocationPickWinner(outputPort);

                if(inputVirtualChannel != null) {
                    Flit flit = inputVirtualChannel.getInputBuffer().get(0);
                    flit.setState(FlitState.SWITCH_ALLOCATION);

                    this.switchAvailables.put(outputPort, false);

                    this.net.getCycleAccurateEventQueue().schedule(
                            this, () -> this.switchAvailables.put(outputPort, true), 1);
                }
            }
        }
    }

    /**
     * Pick a winner input virtual channel for the specified output port in the switch allocation (SA) stage.
     *
     * @param outputPort the output port
     * @return the selected winner input virtual channel
     */
    private InputVirtualChannel stageSwitchAllocationPickWinner(Port outputPort) {
        long oldestTimestamp = Long.MAX_VALUE;
        InputVirtualChannel inputVirtualChannelFound = null;

        for(Port inputPort : Port.values()) {
            if(inputPort == outputPort) {
                continue;
            }

            for(InputVirtualChannel inputVirtualChannel : this.inputVirtualChannels.get(inputPort)) {
                if(inputVirtualChannel.getOutputVirtualChannel() != -1 &&
                        inputVirtualChannel.getOutputPort() == outputPort &&
                        !inputVirtualChannel.getInputBuffer().isEmpty()) {
                    Flit flit = inputVirtualChannel.getInputBuffer().get(0);

                    if ((flit.isHead() && flit.getState() == FlitState.VIRTUAL_CHANNEL_ALLOCATION ||
                            !flit.isHead() && flit.getState() == FlitState.INPUT_BUFFER) &&
                            flit.getTimestamp() < oldestTimestamp) {
                        oldestTimestamp = flit.getTimestamp();
                        inputVirtualChannelFound = inputVirtualChannel;
                    }
                }
            }
        }

        return inputVirtualChannelFound;
    }

    /**
     * The switch traversal (ST) stage.
     */
    private void stageSwitchTraversal() {
        for(Port outputPort : Port.values()) {
            InputVirtualChannel inputVirtualChannelFound = null;

            for(Port inputPort : Port.values()) {
                if(inputPort == outputPort) {
                    continue;
                }

                for(InputVirtualChannel inputVirtualChannel : this.inputVirtualChannels.get(inputPort)) {
                    if(inputVirtualChannel.getOutputVirtualChannel() != -1 &&
                            inputVirtualChannel.getOutputPort() == outputPort &&
                            !inputVirtualChannel.getInputBuffer().isEmpty()) {
                        Flit flit = inputVirtualChannel.getInputBuffer().get(0);
                        if(flit.getState() == FlitState.SWITCH_ALLOCATION) {
                            inputVirtualChannelFound = inputVirtualChannel;
                            break;
                        }
                    }
                }
            }

            if(inputVirtualChannelFound != null) {
                Flit flit = inputVirtualChannelFound.getInputBuffer().get(0);
                flit.setState(FlitState.SWITCH_TRAVERSAL);

                this.outputVirtualChannels.get(inputVirtualChannelFound.getOutputPort())
                        .get(inputVirtualChannelFound.getOutputVirtualChannel()).getOutputBuffer().add(flit);

                inputVirtualChannelFound.getInputBuffer().remove(0);

                if(flit.isTail()) {
                    inputVirtualChannelFound.setOutputPort(null);
                    inputVirtualChannelFound.setOutputVirtualChannel(-1);
                }

                Port port = inputVirtualChannelFound.getPort();
                if(port != Port.LOCAL) {
                    int virtualChannel = inputVirtualChannelFound.getNum();
                    this.net.getCycleAccurateEventQueue().schedule(this,
                            () -> this.links.get(port).credits.get(port.opposite()).get(virtualChannel).increment(), 1
                    );
                }
            }
        }
    }

    /**
     * The route calculation (RC) stage. Calculate the permissible routes for every first flit in each ivc.
     */
    private void stageRouteCalculation() {
        for(Port inputPort : Port.values()) {
            for(InputVirtualChannel inputVirtualChannel : this.inputVirtualChannels.get(inputPort)) {
                if(inputVirtualChannel.getInputBuffer().isEmpty()) {
                    continue;
                }

                Flit flit = inputVirtualChannel.getInputBuffer().get(0);
                if(flit.isHead() && flit.getState() == FlitState.INPUT_BUFFER) {
                    inputVirtualChannel.clearRoutes();
                    if(flit.getDestination() == this) {
                        inputVirtualChannel.setRoute(0, Port.LOCAL, true);
                        inputVirtualChannel.setRoute(1, Port.LOCAL, true);
                    }
                    else {
                        //adaptive routing
                        if(this.getX() > flit.getDestination().getX()) {
                            inputVirtualChannel.setRoute(0, Port.LEFT, true);
                        }
                        else if (this.getX() < flit.getDestination().getX()) {
                            inputVirtualChannel.setRoute(0, Port.RIGHT, true);
                        }

                        if(this.getY() > flit.getDestination().getY()) {
                            inputVirtualChannel.setRoute(0, Port.UP, true);
                        }
                        else if(this.getY() < flit.getDestination().getY()) {
                            inputVirtualChannel.setRoute(0, Port.DOWN, true);
                        }

                        //escape routing
                        if(this.getX() > flit.getDestination().getX()) {
                            inputVirtualChannel.setRoute(1, Port.LEFT, true);
                        }
                        else if(this.getX() < flit.getDestination().getX()) {
                            inputVirtualChannel.setRoute(1, Port.RIGHT, true);
                        }
                        else if(this.getY() > flit.getDestination().getY()) {
                            inputVirtualChannel.setRoute(1, Port.UP, true);
                        }
                        else if(this.getY() < flit.getDestination().getY()) {
                            inputVirtualChannel.setRoute(1, Port.DOWN, true);
                        }
                    }
                    flit.setState(FlitState.ROUTE_CALCULATION);
                }
            }
        }
    }

    /**
     * The virtual channel allocation (VCA) stage. For each ovc, find the suitable ivc.
     */
    private void stageVirtualChannelAllocation() {
        for(Port port : Port.values()) {
            for(int ovc = 0; ovc < this.net.getNumVirtualChannels(); ovc++) {
                if(this.outputVirtualChannelAvailables.get(port).get(ovc)) {
                    InputVirtualChannel inputVirtualChannel = this.stageVirtualChannelAllocationPickWinner(port, ovc);
                    if(inputVirtualChannel != null) {
                        Flit flit = inputVirtualChannel.getInputBuffer().get(0);
                        flit.setState(FlitState.VIRTUAL_CHANNEL_ALLOCATION);

                        inputVirtualChannel.setOutputPort(port);
                        inputVirtualChannel.setOutputVirtualChannel(ovc);

                        this.outputVirtualChannelAvailables.get(port).set(ovc, false);
                    }
                }
            }
        }
    }

    /**
     * Pick a winner input virtual channel for the specified output virtual channel in the virtual channel allocation (VCA) stage.
     *
     * @param outputVirtualChannel the output virtual channel
     * @return the selected winner input virtual channel
     */
    private InputVirtualChannel stageVirtualChannelAllocationPickWinner(Port outputPort, int outputVirtualChannel) {
        int routeCalculationIndex = outputVirtualChannel == this.net.getNumVirtualChannels() - 1 ? 0 : 1;

        long oldestTimestamp = Long.MAX_VALUE;
        InputVirtualChannel inputVirtualChannelFound = null;

        for(Port inputPort : Port.values()) {
            if(inputPort == outputPort) {
                continue;
            }

            for(InputVirtualChannel inputVirtualChannel : this.inputVirtualChannels.get(inputPort)) {
                if(inputVirtualChannel.getInputBuffer().isEmpty() ||
                        !inputVirtualChannel.getRoute(routeCalculationIndex, outputPort)) {
                    continue;
                }

                Flit flit = inputVirtualChannel.getInputBuffer().get(0);

                if(flit.isHead() && flit.getState() == FlitState.ROUTE_CALCULATION &&
                        flit.getTimestamp() < oldestTimestamp) {
                    oldestTimestamp = flit.getTimestamp();
                    inputVirtualChannelFound = inputVirtualChannel;
                }
            }
        }

        return inputVirtualChannelFound;
    }

    /**
     * Perform local packet injection.
     */
    private void localPacketInjection() {
        for(;;) {
            if(this.injectionBuffer.isEmpty()) {
                break;
            }

            boolean requestInserted = false;

            for(InputVirtualChannel inputVirtualChannel : this.inputVirtualChannels.get(Port.LOCAL)) {
                if(this.injectionBuffer.isEmpty()) {
                    continue;
                }

                Packet packet = this.injectionBuffer.get(0);

                int numFlits = 1;

                numFlits += packet.getSize() / this.net.getLinkWidth();

                if(inputVirtualChannel.getInputBuffer().size() + numFlits <= this.inputBufferMaxSize) {
                    for(int i = 0; i < numFlits; i++) {
                        Flit flit = new Flit(
                                this.net,
                                packet,
                                this.net.getRouter(packet.getFrom()),
                                this.net.getRouter(packet.getTo())
                        );

                        flit.setHead(i == 0);
                        flit.setTail(i == numFlits - 1);

                        flit.setState(FlitState.INPUT_BUFFER);
                        flit.setNum(i);

                        inputVirtualChannel.getInputBuffer().add(flit);
                    }

                    this.injectionBuffer.remove(0);
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
     * Inject the specified packet from the network interface (NI).
     *
     * @param packet the packet
     * @return a boolean value indicating whether the packet has been injected or not
     */
    public boolean injectPacket(Packet packet) {
        if(this.injectionBuffer.size() < this.injectionBufferMaxSize) {
            this.injectionBuffer.add(packet);
            return true;
        }

        return false;
    }

    /**
     * Insert the flit.
     *
     * @param flit the flit
     * @param port the direction
     * @param inputVirtualChannelNum the input virtual channel number
     */
    private void insertFlit(Flit flit, Port port, int inputVirtualChannelNum) {
        this.inputVirtualChannels.get(port).get(inputVirtualChannelNum).getInputBuffer().add(flit);
        flit.setState(FlitState.INPUT_BUFFER);
    }

    /**
     * Get the ID.
     *
     * @return the ID
     */
    public int getId() {
        return id;
    }

    /**
     * Get the x coordinate.
     *
     * @return x coordinate
     */
    public int getX() {
        if(x == -1) {
            int width = (int) Math.sqrt(this.net.getRouters().size());
            x = this.id % width;
        }

        return x;
    }

    /**
     * Get the y coordinate.
     *
     * @return y coordinate
     */
    public int getY() {
        if(y == -1) {
            int width = (int) Math.sqrt(this.net.getRouters().size());
            y = this.id / width;
        }

        return y;
    }

    /**
     * Get the type of the router.
     *
     * @return the type of the router
     */
    public RouterType getType() {
        return type;
    }

    /**
     * Get the net.
     *
     * @return the net
     */
    public BasicNet getNet() {
        return net;
    }

    /**
     * Get the map of links.
     *
     * @return the map of links
     */
    public EnumMap<Port, Router> getLinks() {
        return links;
    }
}
