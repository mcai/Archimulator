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

import archimulator.util.Reference;

/**
 * Switch.
 *
 * @author Min Cai
 */
public class Switch {
    private Router router;

    /**
     * Create a switch.
     *
     * @param router the parent router
     */
    public Switch(Router router) {
        this.router = router;
    }

    /**
     * The switch traversal (ST) stage.
     */
    public void stageSwitchTraversal() {
        for (Port outputPort : Port.values()) {
            for (Port inputPort : Port.values()) {
                if (inputPort == outputPort) {
                    continue;
                }

                for (int ivc = 0; ivc < this.router.getNet().getNumVirtualChannels(); ivc++) {
                    if (this.router.getVirtualChannelAllocator().getOutputVirtualChannels().get(inputPort).get(ivc) != -1 &&
                            this.router.getVirtualChannelAllocator().getOutputPorts().get(inputPort).get(ivc) == outputPort &&
                            !this.router.getInputBuffers().get(inputPort).get(ivc).isEmpty()) {
                        Flit flit = this.router.getInputBuffers().get(inputPort).get(ivc).get(0);
                        if (flit.getState() == FlitState.SWITCH_ALLOCATION) {
                            flit.setState(FlitState.SWITCH_TRAVERSAL);

                            this.router.getOutputBuffers().get(this.router.getVirtualChannelAllocator().getOutputPorts().get(inputPort).get(ivc))
                                    .get(this.router.getVirtualChannelAllocator().getOutputVirtualChannels().get(inputPort).get(ivc)).add(flit);

                            this.router.getInputBuffers().get(inputPort).get(ivc).remove(0);

                            if (flit.isTail()) {
                                this.router.getVirtualChannelAllocator().getOutputPorts().get(inputPort).set(ivc, null);
                                this.router.getVirtualChannelAllocator().getOutputVirtualChannels().get(inputPort).set(ivc, -1);
                            }

                            Reference<Integer> ivcRef = new Reference<>(ivc);

                            if (inputPort != Port.LOCAL) {
                                this.router.getNet().getCycleAccurateEventQueue().schedule(this,
                                        () -> this.router.getLinks().get(inputPort).getVirtualChannelAllocator().getCredits().get(inputPort.opposite()).get(ivcRef.get()).increment(), 1
                                );
                            }

                            break;
                        }
                    }
                }
            }
        }
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
