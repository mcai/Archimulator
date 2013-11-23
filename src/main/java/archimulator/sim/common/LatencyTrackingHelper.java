/*******************************************************************************
 * Copyright (c) 2010-2013 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.sim.common;

import archimulator.sim.common.report.ReportNode;
import archimulator.sim.common.report.Reportable;
import archimulator.sim.core.event.InstructionFetchBeginEvent;
import archimulator.sim.core.event.InstructionFetchEndEvent;

//TODO: track dynamic instructions and memory hierarchy requests' life cycles.
/**
 * Latency tracking helper.
 *
 * @author Min Cai
 */
public class LatencyTrackingHelper implements Reportable {
    private Simulation simulation;

    /**
     * Create a latency tracking helper.
     *
     * @param simulation the parent simulation object
     */
    public LatencyTrackingHelper(Simulation simulation) {
        this.simulation = simulation;

        simulation.getBlockingEventDispatcher().addListener(InstructionFetchBeginEvent.class, System.out::println);
        simulation.getBlockingEventDispatcher().addListener(InstructionFetchEndEvent.class, System.out::println);
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, "latencyTrackingHelper") {
            {
            }
        });
    }

    /**
     * Get the parent simulation object.
     *
     * @return the parent simulation object
     */
    public Simulation getSimulation() {
        return simulation;
    }
}
