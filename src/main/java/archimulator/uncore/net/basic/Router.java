/**
 * ****************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.uncore.net.basic;

import archimulator.util.Reference;

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

    private int id;

    private int x;
    private int y;

    private EnumMap<Port, Router> links;

    private List<Packet> injectionBuffer;

    private EnumMap<Port, List<List<Flit>>> inputBuffers;

    private EnumMap<Port, List<List<Flit>>> outputBuffers;

    private Switch aSwitch;

    private RouteComputation routeComputation;

    private VirtualChannelAllocator virtualChannelAllocator;

    private SwitchAllocator switchAllocator;

    /**
     * Create a router.
     *
     * @param net the net
     * @param id the ID of the router
     */
    public Router(BasicNet net, int id) {
        this.net = net;
        this.id = id;

        this.x = -1;
        this.y = -1;

        this.links = new EnumMap<>(Port.class);

        this.injectionBuffer = new ArrayList<>();

        this.inputBuffers = new EnumMap<>(Port.class);
        this.inputBuffers.put(Port.LOCAL, new ArrayList<>());
        this.inputBuffers.put(Port.LEFT, new ArrayList<>());
        this.inputBuffers.put(Port.RIGHT, new ArrayList<>());
        this.inputBuffers.put(Port.UP, new ArrayList<>());
        this.inputBuffers.put(Port.DOWN, new ArrayList<>());

        for (Port inputPort : Port.values()) {
            for (int i = 0; i < this.net.getNumVirtualChannels(); i++) {
                this.inputBuffers.get(inputPort).add(new ArrayList<>());
            }
        }

        this.outputBuffers = new EnumMap<>(Port.class);
        this.outputBuffers.put(Port.LOCAL, new ArrayList<>());
        this.outputBuffers.put(Port.LEFT, new ArrayList<>());
        this.outputBuffers.put(Port.RIGHT, new ArrayList<>());
        this.outputBuffers.put(Port.UP, new ArrayList<>());
        this.outputBuffers.put(Port.DOWN, new ArrayList<>());

        for (Port outputPort : Port.values()) {
            for (int ovc = 0; ovc < this.net.getNumVirtualChannels(); ovc++) {
                this.outputBuffers.get(outputPort).add(new ArrayList<>());
            }
        }

        this.aSwitch = new Switch(this);

        this.routeComputation = new RouteComputation(this);

        this.virtualChannelAllocator = new VirtualChannelAllocator(this);

        this.switchAllocator = new SwitchAllocator(this);
    }

    /**
     * Advance one cycle.
     */
    public void advanceOneCycle() {
        this.stageLinkTraversal();

        this.switchAllocator.stageSwitchAllocation();

        this.aSwitch.stageSwitchTraversal();

        this.routeComputation.stageRouteCalculation();

        this.virtualChannelAllocator.stageVirtualChannelAllocation();

        this.localPacketInjection();
    }

    /**
     * The link traversal (LT) stage.
     */
    private void stageLinkTraversal() {
        for (Port outputPort : Port.values()) {
            long oldestCycle = Long.MAX_VALUE;
            final Reference<Integer> ovcFoundRef = new Reference<>(-1);

            for (int ovc = 0; ovc < this.net.getNumVirtualChannels(); ovc++) {
                int index = (int) ((ovc + this.net.getCycleAccurateEventQueue().getCurrentCycle())
                        % this.net.getNumVirtualChannels());
                List<Flit> outputBuffer = this.outputBuffers.get(outputPort).get(index);

                if (outputBuffer.isEmpty() ||
                        this.virtualChannelAllocator.getCredits().get(outputPort).get(index).getValue() == 0) {
                    continue;
                }

                Flit flit = outputBuffer.get(0);

                if (flit.getTimestamp() < oldestCycle) {
                    oldestCycle = flit.getTimestamp();
                    ovcFoundRef.set(index);
                }
            }

            Integer ovcFound = ovcFoundRef.get();

            if (ovcFound != -1) {
                List<Flit> outputBuffer = this.outputBuffers.get(outputPort).get(ovcFound);

                Flit flit = outputBuffer.get(0);

                if (outputPort != Port.LOCAL) {
                    this.net.getCycleAccurateEventQueue().schedule(
                            this, () -> this.links.get(outputPort).insertFlit(
                                    flit,
                                    outputPort.opposite(),
                                    ovcFound
                            ), this.net.getLinkLatency() + 1);
                }

                outputBuffer.remove(0);

                if (outputPort != Port.LOCAL) {
                    this.virtualChannelAllocator.getCredits().get(outputPort).get(ovcFound).decrement();
                }

                if (flit.isTail()) {
                    this.virtualChannelAllocator.getOutputVirtualChannelAvailables().get(outputPort).set(ovcFound, true);

                    if (outputPort == Port.LOCAL) {
                        flit.getPacket().getOnCompletedCallback().apply();
                    }
                }
            }
        }
    }

    /**
     * Perform local packet injection.
     */
    private void localPacketInjection() {
        for (; ; ) {
            boolean requestInserted = false;

            for (int ivc = 0; ivc < this.net.getNumVirtualChannels(); ivc++) {
                if (this.injectionBuffer.isEmpty()) {
                    continue;
                }

                Packet packet = this.injectionBuffer.get(0);

                int numFlits = (int) Math.ceil((double) packet.getSize() / this.net.getLinkWidth());

                List<Flit> inputBuffer = this.inputBuffers.get(Port.LOCAL).get(ivc);

                if (inputBuffer.size() + numFlits <= this.net.getInputBufferMaxSize()) {
                    for (int i = 0; i < numFlits; i++) {
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

                        inputBuffer.add(flit);
                    }

                    this.injectionBuffer.remove(0);
                    requestInserted = true;
                    break;
                }
            }

            if (!requestInserted) {
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
        if (this.injectionBuffer.size() < this.net.getInjectionBufferMaxSize()) {
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
        if (x == -1) {
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
        if (y == -1) {
            int width = (int) Math.sqrt(this.net.getRouters().size());
            y = this.id / width;
        }

        return y;
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

    /**
     * Get the map of input buffers.
     *
     * @return the map of input buffers
     */
    public EnumMap<Port, List<List<Flit>>> getInputBuffers() {
        return inputBuffers;
    }

    /**
     * Get the map of output buffers.
     *
     * @return the map of output buffers
     */
    public EnumMap<Port, List<List<Flit>>> getOutputBuffers() {
        return outputBuffers;
    }

    /**
     * Get the switch.
     *
     * @return the switch
     */
    public Switch getSwitch() {
        return aSwitch;
    }

    /**
     * Get the route computation component.
     *
     * @return the route computation component
     */
    public RouteComputation getRouteComputation() {
        return routeComputation;
    }

    /**
     * Get the virtual channel allocator.
     *
     * @return the virtual channel allocator
     */
    public VirtualChannelAllocator getVirtualChannelAllocator() {
        return virtualChannelAllocator;
    }

    /**
     * Get the switch allocator.
     *
     * @return the switch allocator
     */
    public SwitchAllocator getSwitchAllocator() {
        return switchAllocator;
    }
}
