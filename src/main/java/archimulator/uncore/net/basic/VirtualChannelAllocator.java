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
package archimulator.uncore.net.basic;

import archimulator.util.Pair;
import archimulator.util.math.Counter;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * Virtual channel allocator.
 *
 * @author Min Cai
 */
public class VirtualChannelAllocator {
    private Router router;

    private EnumMap<Port, List<Port>> outputPorts;
    private EnumMap<Port, List<Integer>> outputVirtualChannels;

    private EnumMap<Port, List<Boolean>> outputVirtualChannelAvailables;

    private EnumMap<Port, List<Counter>> credits;

    /**
     * Create a virtual channel allocator.
     *
     * @param router the router
     */
    public VirtualChannelAllocator(Router router) {
        this.router = router;

        this.outputPorts = new EnumMap<>(Port.class);
        this.outputPorts.put(Port.LOCAL, new ArrayList<>());
        this.outputPorts.put(Port.LEFT, new ArrayList<>());
        this.outputPorts.put(Port.RIGHT, new ArrayList<>());
        this.outputPorts.put(Port.UP, new ArrayList<>());
        this.outputPorts.put(Port.DOWN, new ArrayList<>());

        for (Port inputPort : Port.values()) {
            for (int i = 0; i < this.router.getNet().getNumVirtualChannels(); i++) {
                this.outputPorts.get(inputPort).add(null);
            }
        }

        this.outputVirtualChannels = new EnumMap<>(Port.class);
        this.outputVirtualChannels.put(Port.LOCAL, new ArrayList<>());
        this.outputVirtualChannels.put(Port.LEFT, new ArrayList<>());
        this.outputVirtualChannels.put(Port.RIGHT, new ArrayList<>());
        this.outputVirtualChannels.put(Port.UP, new ArrayList<>());
        this.outputVirtualChannels.put(Port.DOWN, new ArrayList<>());

        for (Port inputPort : Port.values()) {
            for (int ivc = 0; ivc < this.router.getNet().getNumVirtualChannels(); ivc++) {
                this.outputVirtualChannels.get(inputPort).add(-1);
            }
        }

        this.outputVirtualChannelAvailables = new EnumMap<>(Port.class);
        this.outputVirtualChannelAvailables.put(Port.LOCAL, new ArrayList<>());
        this.outputVirtualChannelAvailables.put(Port.LEFT, new ArrayList<>());
        this.outputVirtualChannelAvailables.put(Port.RIGHT, new ArrayList<>());
        this.outputVirtualChannelAvailables.put(Port.UP, new ArrayList<>());
        this.outputVirtualChannelAvailables.put(Port.DOWN, new ArrayList<>());

        for (Port outputPort : Port.values()) {
            for (int ovc = 0; ovc < this.router.getNet().getNumVirtualChannels(); ovc++) {
                this.outputVirtualChannelAvailables.get(outputPort).add(true);
            }
        }

        this.credits = new EnumMap<>(Port.class);
        this.credits.put(Port.LOCAL, new ArrayList<>());
        this.credits.put(Port.LEFT, new ArrayList<>());
        this.credits.put(Port.RIGHT, new ArrayList<>());
        this.credits.put(Port.UP, new ArrayList<>());
        this.credits.put(Port.DOWN, new ArrayList<>());

        for (Port port : Port.values()) {
            for (int vc = 0; vc < this.router.getNet().getNumVirtualChannels(); vc++) {
                this.credits.get(port).add(new Counter(10));
            }
        }
    }

    /**
     * The virtual channel allocation (VCA) stage. For each ovc, find the suitable ivc.
     */
    public void stageVirtualChannelAllocation() {
        for (Port outputPort : Port.values()) {
            for (int ovc = 0; ovc < this.router.getNet().getNumVirtualChannels(); ovc++) {
                if (this.outputVirtualChannelAvailables.get(outputPort).get(ovc)) {
                    Pair<Port, Integer> pair = this.stageVirtualChannelAllocationPickWinner(outputPort);

                    Port inputPortFound = pair.getFirst();
                    int inputVirtualChannelFound = pair.getSecond();

                    if (inputPortFound != null) {
                        Flit flit = this.router.getInputBuffers().get(inputPortFound).get(inputVirtualChannelFound).get(0);
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
     * @return the selected winner input virtual channel
     */
    private Pair<Port, Integer> stageVirtualChannelAllocationPickWinner(Port outputPort) {
        long oldestTimestamp = Long.MAX_VALUE;
        Port inputPortFound = null;
        int inputVirtualChannelFound = -1;

        for (Port inputPort : Port.values()) {
            if (inputPort == outputPort) {
                continue;
            }

            for (int ivc = 0; ivc < this.router.getNet().getNumVirtualChannels(); ivc++) {
                List<Flit> inputBuffer = this.router.getInputBuffers().get(inputPort).get(ivc);
                if (inputBuffer.isEmpty() ||
                        this.router.getRouteComputation().getRoute(inputPort, ivc) != outputPort) {
                    continue;
                }

                Flit flit = inputBuffer.get(0);

                if (flit.isHead() && flit.getState() == FlitState.ROUTE_CALCULATION &&
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
     * Get the router.
     *
     * @return the router
     */
    public Router getRouter() {
        return router;
    }

    /**
     * Get the map of input virtual channels to output ports.
     *
     * @return the map of input virtual channels to output ports
     */
    public EnumMap<Port, List<Port>> getOutputPorts() {
        return outputPorts;
    }

    /**
     * Get the map of input virtual channels to output virtual channels.
     *
     * @return the map of input virtual channels to output virtual channels
     */
    public EnumMap<Port, List<Integer>> getOutputVirtualChannels() {
        return outputVirtualChannels;
    }

    /**
     * Get a map of boolean values indicating the availability of output virtual channels.
     *
     * @return a map of boolean values indicating the availability of output virtual channels
     */
    public EnumMap<Port, List<Boolean>> getOutputVirtualChannelAvailables() {
        return outputVirtualChannelAvailables;
    }

    /**
     * Get a map of credits.
     *
     * @return a map of credits
     */
    public EnumMap<Port, List<Counter>> getCredits() {
        return credits;
    }
}

