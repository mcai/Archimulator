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

import archimulator.util.Pair;

import java.util.EnumMap;

/**
 * Switch allocator.
 *
 * @author Min Cai
 */
public class SwitchAllocator {
    private Router router;

    private EnumMap<Port, Boolean> switchAvailables;

    /**
     * Create a switch allocator.
     *
     * @param router the router
     */
    public SwitchAllocator(Router router) {
        this.router = router;

        this.switchAvailables = new EnumMap<>(Port.class);
        this.switchAvailables.put(Port.LOCAL, true);
        this.switchAvailables.put(Port.LEFT, true);
        this.switchAvailables.put(Port.RIGHT, true);
        this.switchAvailables.put(Port.UP, true);
        this.switchAvailables.put(Port.DOWN, true);
    }

    /**
     * The switch allocation (SA) stage.
     */
    public void stageSwitchAllocation() {
        for (Port outputPort : Port.values()) {
            if (this.switchAvailables.get(outputPort)) {
                Pair<Port, Integer> pair = this.stageSwitchAllocationPickWinner(outputPort);
                if (pair.getFirst() != null) {
                    Flit flit = this.router.getInputBuffers().get(pair.getFirst()).get(pair.getSecond()).get(0);
                    flit.setState(FlitState.SWITCH_ALLOCATION);

                    this.switchAvailables.put(outputPort, false);

                    this.router.getNet().getCycleAccurateEventQueue().schedule(
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
        int ivcFound = -1;

        for (Port inputPort : Port.values()) {
            if (inputPort == outputPort) {
                continue;
            }

            for (int ivc = 0; ivc < this.router.getNet().getNumVirtualChannels(); ivc++) {
                if (this.router.getVirtualChannelAllocator().getOutputVirtualChannels().get(inputPort).get(ivc) != -1 &&
                        this.router.getVirtualChannelAllocator().getOutputPorts().get(inputPort).get(ivc) == outputPort &&
                        !this.router.getInputBuffers().get(inputPort).get(ivc).isEmpty()) {
                    Flit flit = this.router.getInputBuffers().get(inputPort).get(ivc).get(0);

                    if ((flit.isHead() && flit.getState() == FlitState.VIRTUAL_CHANNEL_ALLOCATION ||
                            !flit.isHead() && flit.getState() == FlitState.INPUT_BUFFER) &&
                            flit.getTimestamp() < oldestTimestamp) {
                        oldestTimestamp = flit.getTimestamp();
                        inputPortFound = inputPort;
                        ivcFound = ivc;
                    }
                }
            }
        }

        return new Pair<>(inputPortFound, ivcFound);
    }

    /**
     * Get the parent router.
     *
     * @return the parent router
     */
    public Router getRouter() {
        return router;
    }
}
