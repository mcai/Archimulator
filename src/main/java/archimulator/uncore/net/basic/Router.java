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

    private EnumMap<Direction, Router> links;

    private List<Packet> injectionBuffer;
    private int injectionBufferMaxSize;

    private int inputBufferMaxSize;

    private EnumMap<Direction, List<InputVirtualChannel>> inputVirtualChannels;
    private EnumMap<Direction, List<OutputVirtualChannel>> outputVirtualChannels;

    private EnumMap<Direction, List<Counter>> credits;

    private EnumMap<Direction, Boolean> switchAvailables;

    private EnumMap<Direction, Boolean> linkAvailables;

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

        this.links = new EnumMap<>(Direction.class);

        this.injectionBuffer = new ArrayList<>();
        this.injectionBufferMaxSize = 32;

        this.inputBufferMaxSize = 10;

        this.inputVirtualChannels = new EnumMap<>(Direction.class);
        this.inputVirtualChannels.put(Direction.LOCAL, new ArrayList<>());
        this.inputVirtualChannels.put(Direction.LEFT, new ArrayList<>());
        this.inputVirtualChannels.put(Direction.RIGHT, new ArrayList<>());
        this.inputVirtualChannels.put(Direction.UP, new ArrayList<>());
        this.inputVirtualChannels.put(Direction.DOWN, new ArrayList<>());

        for(Direction inputPort : Direction.values()) {
            for(int i = 0; i < this.net.getNumVirtualChannels(); i++) {
                this.inputVirtualChannels.get(inputPort).add(new InputVirtualChannel(inputPort, i));
            }
        }

        this.outputVirtualChannels = new EnumMap<>(Direction.class);
        this.outputVirtualChannels.put(Direction.LOCAL, new ArrayList<>());
        this.outputVirtualChannels.put(Direction.LEFT, new ArrayList<>());
        this.outputVirtualChannels.put(Direction.RIGHT, new ArrayList<>());
        this.outputVirtualChannels.put(Direction.UP, new ArrayList<>());
        this.outputVirtualChannels.put(Direction.DOWN, new ArrayList<>());

        for(Direction outputPort : Direction.values()) {
            for(int i = 0; i < this.net.getNumVirtualChannels(); i++) {
                this.outputVirtualChannels.get(outputPort).add(new OutputVirtualChannel(outputPort, i));
            }
        }

        this.credits = new EnumMap<>(Direction.class);
        this.credits.put(Direction.LOCAL, new ArrayList<>());
        this.credits.put(Direction.LEFT, new ArrayList<>());
        this.credits.put(Direction.RIGHT, new ArrayList<>());
        this.credits.put(Direction.UP, new ArrayList<>());
        this.credits.put(Direction.DOWN, new ArrayList<>());

        for(Direction port : Direction.values()) {
            for(int i = 0; i < this.net.getNumVirtualChannels(); i++) {
                this.credits.get(port).add(new Counter(10));
            }
        }

        this.switchAvailables = new EnumMap<>(Direction.class);
        this.switchAvailables.put(Direction.LOCAL, true);
        this.switchAvailables.put(Direction.LEFT, true);
        this.switchAvailables.put(Direction.RIGHT, true);
        this.switchAvailables.put(Direction.UP, true);
        this.switchAvailables.put(Direction.DOWN, true);

        this.linkAvailables = new EnumMap<>(Direction.class);
        this.linkAvailables.put(Direction.LOCAL, true);
        this.linkAvailables.put(Direction.LEFT, true);
        this.linkAvailables.put(Direction.RIGHT, true);
        this.linkAvailables.put(Direction.UP, true);
        this.linkAvailables.put(Direction.DOWN, true);
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
        for(Direction outputPort : Direction.values()) {
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

                if(outputPort != Direction.LOCAL) {
                    this.net.getCycleAccurateEventQueue().schedule(
                            this, () -> {
                                this.links.get(outputPort).insertFlit(
                                        flit,
                                        outputPort.opposite(),
                                        outputVirtualChannelFoundNum
                                );
                            }, this.net.getLinkLatency() + 1);

                    this.linkAvailables.put(outputPort, false);
                    this.net.getCycleAccurateEventQueue().schedule(
                            this, () -> this.linkAvailables.put(outputPort, true), this.net.getLinkLatency());
                }

                outputVirtualChannelFound.getOutputBuffer().remove(0);

                if(outputPort != Direction.LOCAL) {
                    this.credits.get(outputPort).get(outputVirtualChannelFound.getNum()).decrement();
                }

                if(flit.isTail()) {
                    outputVirtualChannelFound.setAvailable(true);

                    if(outputPort == Direction.LOCAL) {
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
        for(Direction outputPort : Direction.values()) {
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
    private InputVirtualChannel stageSwitchAllocationPickWinner(Direction outputPort) {
        long oldestTimestamp = Long.MAX_VALUE;
        InputVirtualChannel inputVirtualChannelFound = null;

        for(Direction inputPort : Direction.values()) {
            if(inputPort == outputPort) {
                continue;
            }

            for(InputVirtualChannel inputVirtualChannel : this.inputVirtualChannels.get(inputPort)) {
                if(inputVirtualChannel.getFixedRoute() == outputPort &&
                        inputVirtualChannel.getOutputVirtualChannel().getPort() == outputPort &&
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
        for(Direction outputPort : Direction.values()) {
            InputVirtualChannel inputVirtualChannelFound = null;

            for(Direction inputPort : Direction.values()) {
                if(inputPort == outputPort) {
                    continue;
                }

                for(InputVirtualChannel inputVirtualChannel : this.inputVirtualChannels.get(inputPort)) {
                    if(inputVirtualChannel.getFixedRoute() == outputPort &&
                            inputVirtualChannel.getOutputVirtualChannel().getPort() == outputPort &&
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

                inputVirtualChannelFound.getOutputVirtualChannel().getOutputBuffer().add(flit);

                inputVirtualChannelFound.getInputBuffer().remove(0);

                if(flit.isTail()) {
                    inputVirtualChannelFound.setFixedRoute(null);
                    inputVirtualChannelFound.setOutputVirtualChannel(null);
                }

                Direction direction = inputVirtualChannelFound.getPort();
                if(direction != Direction.LOCAL) {
                    int virtualChannel = inputVirtualChannelFound.getNum();
                    this.net.getCycleAccurateEventQueue().schedule(this,
                            () -> this.links.get(direction).credits.get(direction.opposite()).get(virtualChannel).increment(), 1
                    );
                }
            }
        }
    }

    /**
     * The route calculation (RC) stage.
     */
    private void stageRouteCalculation() {
        for(Direction inputPort : Direction.values()) {
            for(InputVirtualChannel inputVirtualChannel : this.inputVirtualChannels.get(inputPort)) {
                if(inputVirtualChannel.getInputBuffer().isEmpty()) {
                    continue;
                }

                Flit flit = inputVirtualChannel.getInputBuffer().get(0);
                if(flit.isHead() && flit.getState() == FlitState.INPUT_BUFFER) {
                    inputVirtualChannel.clearRoutes();
                    if(flit.getDestination() == this) {
                        inputVirtualChannel.setRoute(0, Direction.LOCAL, true);
                        inputVirtualChannel.setRoute(1, Direction.LOCAL, true);
                    }
                    else {
                        int width = (int) Math.sqrt(this.net.getRouters().size());
                        int sourceX = this.id % width;
                        int sourceY = this.id / width;
                        int destinationX = flit.getDestination().getId() % width;
                        int destinationY = flit.getDestination().getId() / width;

                        //adaptive routing
                        if(sourceX > destinationX) {
                            inputVirtualChannel.setRoute(0, Direction.LEFT, true);
                        }
                        else if (sourceX < destinationX) {
                            inputVirtualChannel.setRoute(0, Direction.RIGHT, true);
                        }

                        if(sourceY > destinationY) {
                            inputVirtualChannel.setRoute(0, Direction.UP, true);
                        }
                        else if(sourceY < destinationY) {
                            inputVirtualChannel.setRoute(0, Direction.DOWN, true);
                        }

                        //escape routing
                        if(sourceX > destinationX) {
                            inputVirtualChannel.setRoute(1, Direction.LEFT, true);
                        }
                        else if(sourceX < destinationX) {
                            inputVirtualChannel.setRoute(1, Direction.RIGHT, true);
                        }
                        else if(sourceY > destinationY) {
                            inputVirtualChannel.setRoute(1, Direction.UP, true);
                        }
                        else if(sourceY < destinationY) {
                            inputVirtualChannel.setRoute(1, Direction.DOWN, true);
                        }
                    }
                    flit.setState(FlitState.ROUTE_CALCULATION);
                }
            }
        }
    }

    /**
     * The virtual channel allocation (VCA) stage.
     */
    private void stageVirtualChannelAllocation() {
        for(Direction outputPort : Direction.values()) {
            for(OutputVirtualChannel outputVirtualChannel : this.outputVirtualChannels.get(outputPort)) {
                if(outputVirtualChannel.isAvailable()) {
                    InputVirtualChannel inputVirtualChannel =
                            this.stageVirtualChannelAllocationPickWinner(outputVirtualChannel);
                    if(inputVirtualChannel != null) {
                        Flit flit = inputVirtualChannel.getInputBuffer().get(0);
                        flit.setState(FlitState.VIRTUAL_CHANNEL_ALLOCATION);

                        inputVirtualChannel.setOutputVirtualChannel(outputVirtualChannel);
                        inputVirtualChannel.setFixedRoute(outputPort);

                        outputVirtualChannel.setAvailable(false);
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
    private InputVirtualChannel stageVirtualChannelAllocationPickWinner(OutputVirtualChannel outputVirtualChannel) {
        int routeCalculationIndex = outputVirtualChannel.getNum() / (this.net.getNumVirtualChannels() - 1);

        long oldestTimestamp = Long.MAX_VALUE;
        InputVirtualChannel inputVirtualChannelFound = null;

        for(Direction inputPort : Direction.values()) {
            if(inputPort == outputVirtualChannel.getPort()) {
                continue;
            }

            for(InputVirtualChannel inputVirtualChannel : this.inputVirtualChannels.get(inputPort)) {
                if(inputVirtualChannel.getInputBuffer().isEmpty() ||
                        !inputVirtualChannel.getRoute(routeCalculationIndex, outputVirtualChannel.getPort())) {
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

            for(InputVirtualChannel inputVirtualChannel : this.inputVirtualChannels.get(Direction.LOCAL)) {
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
     * @param direction the direction
     * @param inputVirtualChannelNum the input virtual channel number
     */
    private void insertFlit(Flit flit, Direction direction, int inputVirtualChannelNum) {
        this.inputVirtualChannels.get(direction).get(inputVirtualChannelNum).getInputBuffer().add(flit);
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
    public EnumMap<Direction, Router> getLinks() {
        return links;
    }
}
