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

import archimulator.util.Pair;
import archimulator.util.Reference;
import archimulator.util.math.Counter;

import java.util.*;

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

    //per input port
    private EnumMap<Port, List<List<Flit>>> inputBuffers;
    private EnumMap<Port, List<Port>> outputPorts;
    private EnumMap<Port, List<Integer>> outputVirtualChannels;
    private EnumMap<Port, List<Map<Integer, Set<Port>>>> routes;

    //per output port
    private EnumMap<Port, List<List<Flit>>> outputBuffers;
    private EnumMap<Port, List<Boolean>> outputVirtualChannelAvailables;

    //per channel
    private EnumMap<Port, List<Counter>> credits;

    //per switch
    private EnumMap<Port, Boolean> switchAvailables;

    //per link
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

        this.inputBuffers = new EnumMap<>(Port.class);
        this.inputBuffers.put(Port.LOCAL, new ArrayList<>());
        this.inputBuffers.put(Port.LEFT, new ArrayList<>());
        this.inputBuffers.put(Port.RIGHT, new ArrayList<>());
        this.inputBuffers.put(Port.UP, new ArrayList<>());
        this.inputBuffers.put(Port.DOWN, new ArrayList<>());

        for(Port inputPort : Port.values()) {
            for(int i = 0; i < this.net.getNumVirtualChannels(); i++) {
                this.inputBuffers.get(inputPort).add(new ArrayList<>());
            }
        }

        this.outputPorts = new EnumMap<>(Port.class);
        this.outputPorts.put(Port.LOCAL, new ArrayList<>());
        this.outputPorts.put(Port.LEFT, new ArrayList<>());
        this.outputPorts.put(Port.RIGHT, new ArrayList<>());
        this.outputPorts.put(Port.UP, new ArrayList<>());
        this.outputPorts.put(Port.DOWN, new ArrayList<>());

        for(Port inputPort : Port.values()) {
            for(int i = 0; i < this.net.getNumVirtualChannels(); i++) {
                this.outputPorts.get(inputPort).add(null);
            }
        }

        this.outputVirtualChannels = new EnumMap<>(Port.class);
        this.outputVirtualChannels.put(Port.LOCAL, new ArrayList<>());
        this.outputVirtualChannels.put(Port.LEFT, new ArrayList<>());
        this.outputVirtualChannels.put(Port.RIGHT, new ArrayList<>());
        this.outputVirtualChannels.put(Port.UP, new ArrayList<>());
        this.outputVirtualChannels.put(Port.DOWN, new ArrayList<>());

        for(Port inputPort : Port.values()) {
            for(int ivc = 0; ivc < this.net.getNumVirtualChannels(); ivc++) {
                this.outputVirtualChannels.get(inputPort).add(-1);
            }
        }

        this.routes = new EnumMap<>(Port.class);
        this.routes.put(Port.LOCAL, new ArrayList<>());
        this.routes.put(Port.LEFT, new ArrayList<>());
        this.routes.put(Port.RIGHT, new ArrayList<>());
        this.routes.put(Port.UP, new ArrayList<>());
        this.routes.put(Port.DOWN, new ArrayList<>());

        for(Port inputPort : Port.values()) {
            for(int ivc = 0; ivc < this.net.getNumVirtualChannels(); ivc++) {
                Map<Integer, Set<Port>> routes = new HashMap<>();
                routes.put(0, new HashSet<>());
                routes.put(1, new HashSet<>());
                this.routes.get(inputPort).add(routes);
            }
        }

        this.outputBuffers = new EnumMap<>(Port.class);
        this.outputBuffers.put(Port.LOCAL, new ArrayList<>());
        this.outputBuffers.put(Port.LEFT, new ArrayList<>());
        this.outputBuffers.put(Port.RIGHT, new ArrayList<>());
        this.outputBuffers.put(Port.UP, new ArrayList<>());
        this.outputBuffers.put(Port.DOWN, new ArrayList<>());

        for(Port outputPort : Port.values()) {
            for(int ovc = 0; ovc < this.net.getNumVirtualChannels(); ovc++) {
                this.outputBuffers.get(outputPort).add(new ArrayList<>());
            }
        }

        this.outputVirtualChannelAvailables = new EnumMap<>(Port.class);
        this.outputVirtualChannelAvailables.put(Port.LOCAL, new ArrayList<>());
        this.outputVirtualChannelAvailables.put(Port.LEFT, new ArrayList<>());
        this.outputVirtualChannelAvailables.put(Port.RIGHT, new ArrayList<>());
        this.outputVirtualChannelAvailables.put(Port.UP, new ArrayList<>());
        this.outputVirtualChannelAvailables.put(Port.DOWN, new ArrayList<>());

        for(Port outputPort : Port.values()) {
            for(int ovc = 0; ovc < this.net.getNumVirtualChannels(); ovc++) {
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
            for(int vc = 0; vc < this.net.getNumVirtualChannels(); vc++) {
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
            final Reference<Integer> outputVirtualChannelFoundRef = new Reference<>(-1);

            for(int ovc = 0; ovc < this.net.getNumVirtualChannels(); ovc++) {
                int index = (int) ((ovc + this.net.getCycleAccurateEventQueue().getCurrentCycle())
                        % this.net.getNumVirtualChannels());
                List<Flit> outputBuffer = this.outputBuffers.get(outputPort).get(index);

                if (outputBuffer.isEmpty() ||
                        this.credits.get(outputPort).get(index).getValue() == 0) {
                    continue;
                }

                Flit flit = outputBuffer.get(0);

                if(flit.getState() != FlitState.SWITCH_TRAVERSAL) {
                    throw new IllegalArgumentException(flit.getState() + "");
                }

                if(flit.getTimestamp() < oldestCycle) {
                    oldestCycle = flit.getTimestamp();
                    outputVirtualChannelFoundRef.set(index);
                    break;
                }
            }

            Integer outputVirtualChannelFound = outputVirtualChannelFoundRef.get();

            if(outputVirtualChannelFound != -1) {
                List<Flit> outputBuffer = this.outputBuffers.get(outputPort).get(outputVirtualChannelFound);

                Flit flit = outputBuffer.get(0);

                if(outputPort != Port.LOCAL) {
                    this.net.getCycleAccurateEventQueue().schedule(
                            this, () -> this.links.get(outputPort).insertFlit(
                                    flit,
                                    outputPort.opposite(),
                                    outputVirtualChannelFound
                            ), this.net.getLinkLatency() + 1);

                    this.linkAvailables.put(outputPort, false);
                    this.net.getCycleAccurateEventQueue().schedule(
                            this, () -> this.linkAvailables.put(outputPort, true), this.net.getLinkLatency());
                }

                outputBuffer.remove(0);

                if(outputPort != Port.LOCAL) {
                    this.credits.get(outputPort).get(outputVirtualChannelFound).decrement();
                }

                if(flit.isTail()) {
                    this.outputVirtualChannelAvailables.get(outputPort).set(outputVirtualChannelFound, true);

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
                Pair<Port, Integer> pair = this.stageSwitchAllocationPickWinner(outputPort);
                if(pair.getFirst() != null) {
                    Flit flit = this.inputBuffers.get(pair.getFirst()).get(pair.getSecond()).get(0);
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
    private Pair<Port, Integer> stageSwitchAllocationPickWinner(Port outputPort) {
        long oldestTimestamp = Long.MAX_VALUE;
        Port inputPortFound = null;
        int inputVirtualChannelFound = -1;

        for(Port inputPort : Port.values()) {
            if(inputPort == outputPort) {
                continue;
            }

            for(int ivc = 0; ivc < this.net.getNumVirtualChannels(); ivc++) {
                if(this.outputVirtualChannels.get(inputPort).get(ivc) != -1 &&
                        this.outputPorts.get(inputPort).get(ivc) == outputPort &&
                        !this.inputBuffers.get(inputPort).get(ivc).isEmpty()) {
                    Flit flit = this.inputBuffers.get(inputPort).get(ivc).get(0);

                    if ((flit.isHead() && flit.getState() == FlitState.VIRTUAL_CHANNEL_ALLOCATION ||
                            !flit.isHead() && flit.getState() == FlitState.INPUT_BUFFER) &&
                            flit.getTimestamp() < oldestTimestamp) {
                        oldestTimestamp = flit.getTimestamp();
                        inputPortFound = inputPort;
                        inputVirtualChannelFound = ivc;
                    }
                }
            }
        }

        return new Pair<>(inputPortFound, inputVirtualChannelFound);
    }

    /**
     * The switch traversal (ST) stage.
     */
    private void stageSwitchTraversal() {
        for(Port outputPort : Port.values()) {
            Reference<Port> inputPortFoundRef = new Reference<>(null);
            Reference<Integer> inputVirtualChannelFoundRef = new Reference<>(-1);

            for(Port inputPort : Port.values()) {
                if(inputPort == outputPort) {
                    continue;
                }

                for(int ivc = 0; ivc < this.net.getNumVirtualChannels(); ivc++) {
                    if(this.outputVirtualChannels.get(inputPort).get(ivc) != -1 &&
                            this.outputPorts.get(inputPort).get(ivc) == outputPort &&
                            !this.inputBuffers.get(inputPort).get(ivc).isEmpty()) {
                        Flit flit = this.inputBuffers.get(inputPort).get(ivc).get(0);
                        if(flit.getState() == FlitState.SWITCH_ALLOCATION) {
                            inputPortFoundRef.set(inputPort);
                            inputVirtualChannelFoundRef.set(ivc);
                            break;
                        }
                    }
                }
            }

            Port inputPortFound = inputPortFoundRef.get();
            int inputVirtualChannelFound = inputVirtualChannelFoundRef.get();

            if(inputPortFound != null) {
                Flit flit = this.inputBuffers.get(inputPortFound).get(inputVirtualChannelFound).get(0);
                flit.setState(FlitState.SWITCH_TRAVERSAL);

                this.outputBuffers.get(this.outputPorts.get(inputPortFound).get(inputVirtualChannelFound))
                        .get(this.outputVirtualChannels.get(inputPortFound).get(inputVirtualChannelFound)).add(flit);

                this.inputBuffers.get(inputPortFound).get(inputVirtualChannelFound).remove(0);

                if(flit.isTail()) {
                    this.outputPorts.get(inputPortFound).set(inputVirtualChannelFound, null);
                    this.outputVirtualChannels.get(inputPortFound).set(inputVirtualChannelFound, -1);
                }

                if(inputPortFound != Port.LOCAL) {
                    this.net.getCycleAccurateEventQueue().schedule(this,
                            () -> this.links.get(inputPortFound).credits.get(inputPortFound.opposite()).get(inputVirtualChannelFound).increment(), 1
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
            for(int ivc = 0; ivc < this.net.getNumVirtualChannels(); ivc++) {
                if(this.inputBuffers.get(inputPort).get(ivc).isEmpty()) {
                    continue;
                }

                Flit flit = this.inputBuffers.get(inputPort).get(ivc).get(0);
                if(flit.isHead() && flit.getState() == FlitState.INPUT_BUFFER) {
                    this.routes.get(inputPort).get(ivc).get(0).clear();
                    this.routes.get(inputPort).get(ivc).get(1).clear();
                    if(flit.getDestination() == this) {
                        this.routes.get(inputPort).get(ivc).get(0).add(Port.LOCAL);
                        this.routes.get(inputPort).get(ivc).get(1).add(Port.LOCAL);
                    }
                    else {
                        //adaptive routing
                        if(this.getX() > flit.getDestination().getX()) {
                            this.routes.get(inputPort).get(ivc).get(0).add(Port.LEFT);
                        }
                        else if (this.getX() < flit.getDestination().getX()) {
                            this.routes.get(inputPort).get(ivc).get(0).add(Port.RIGHT);
                        }

                        if(this.getY() > flit.getDestination().getY()) {
                            this.routes.get(inputPort).get(ivc).get(0).add(Port.UP);
                        }
                        else if(this.getY() < flit.getDestination().getY()) {
                            this.routes.get(inputPort).get(ivc).get(0).add(Port.DOWN);
                        }

                        //escape routing
                        if(this.getX() > flit.getDestination().getX()) {
                            this.routes.get(inputPort).get(ivc).get(1).add(Port.LEFT);
                        }
                        else if(this.getX() < flit.getDestination().getX()) {
                            this.routes.get(inputPort).get(ivc).get(1).add(Port.RIGHT);
                        }
                        else if(this.getY() > flit.getDestination().getY()) {
                            this.routes.get(inputPort).get(ivc).get(1).add(Port.UP);
                        }
                        else if(this.getY() < flit.getDestination().getY()) {
                            this.routes.get(inputPort).get(ivc).get(1).add(Port.DOWN);
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
        for(Port outputPort : Port.values()) {
            for(int ovc = 0; ovc < this.net.getNumVirtualChannels(); ovc++) {
                if(this.outputVirtualChannelAvailables.get(outputPort).get(ovc)) {
                    Pair<Port, Integer> pair = this.stageVirtualChannelAllocationPickWinner(outputPort, ovc);

                    Port inputPortFound = pair.getFirst();
                    int inputVirtualChannelFound = pair.getSecond();

                    if(inputPortFound != null) {
                        Flit flit = this.inputBuffers.get(inputPortFound).get(inputVirtualChannelFound).get(0);
                        flit.setState(FlitState.VIRTUAL_CHANNEL_ALLOCATION);

                        this.outputPorts.get(inputPortFound).set(inputVirtualChannelFound, outputPort);
                        this.outputVirtualChannels.get(inputPortFound).set(inputVirtualChannelFound, ovc);

                        this.outputVirtualChannelAvailables.get(outputPort).set(ovc, false);
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
    private Pair<Port, Integer> stageVirtualChannelAllocationPickWinner(Port outputPort, int outputVirtualChannel) {
        int routeCalculationIndex = outputVirtualChannel == this.net.getNumVirtualChannels() - 1 ? 0 : 1;

        long oldestTimestamp = Long.MAX_VALUE;
        Port inputPortFound = null;
        int inputVirtualChannelFound = -1;

        for(Port inputPort : Port.values()) {
            if(inputPort == outputPort) {
                continue;
            }

            for (int ivc = 0; ivc < this.net.getNumVirtualChannels(); ivc++) {
                List<Flit> inputBuffer = this.inputBuffers.get(inputPort).get(ivc);
                if(inputBuffer.isEmpty() ||
                        !this.routes.get(inputPort).get(ivc).get(routeCalculationIndex).contains(outputPort)) {
                    continue;
                }

                Flit flit = inputBuffer.get(0);

                if(flit.isHead() && flit.getState() == FlitState.ROUTE_CALCULATION &&
                        flit.getTimestamp() < oldestTimestamp) {
                    oldestTimestamp = flit.getTimestamp();
                    inputPortFound = inputPort;
                    inputVirtualChannelFound = ivc;
                }
            }
        }

        return new Pair<>(inputPortFound, inputVirtualChannelFound);
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

            for(int ivc = 0; ivc < this.net.getNumVirtualChannels(); ivc++) {
                if(this.injectionBuffer.isEmpty()) {
                    continue;
                }

                Packet packet = this.injectionBuffer.get(0);

                int numFlits = 1;

                numFlits += packet.getSize() / this.net.getLinkWidth();

                if(this.inputBuffers.get(Port.LOCAL).get(ivc).size() + numFlits <= this.inputBufferMaxSize) {
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

                        this.inputBuffers.get(Port.LOCAL).get(ivc).add(flit);
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
     * @param inputVirtualChannel the input virtual channel
     */
    private void insertFlit(Flit flit, Port port, int inputVirtualChannel) {
        this.inputBuffers.get(port).get(inputVirtualChannel).add(flit);
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
