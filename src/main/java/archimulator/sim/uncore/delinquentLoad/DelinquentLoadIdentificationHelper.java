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
package archimulator.sim.uncore.delinquentLoad;

import archimulator.sim.common.Simulation;
import archimulator.sim.common.report.ReportNode;
import archimulator.sim.common.report.Reportable;
import archimulator.sim.core.Core;
import archimulator.sim.core.Processor;
import archimulator.sim.core.Thread;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Delinquent load identification helper.
 *
 * @author Min Cai
 */
public class DelinquentLoadIdentificationHelper implements Reportable {
    private Map<Integer, DelinquentLoadIdentificationTable> delinquentLoadIdentificationTables;
    private Processor processor;

    /**
     * Create a delinquent load identification helper.
     *
     * @param simulation the simulation
     */
    public DelinquentLoadIdentificationHelper(Simulation simulation) {
        this.processor = simulation.getProcessor();

        this.delinquentLoadIdentificationTables = new HashMap<Integer, DelinquentLoadIdentificationTable>();

        for (Core core : processor.getCores()) {
            for (Thread thread : core.getThreads()) {
                this.delinquentLoadIdentificationTables.put(thread.getId(), new DelinquentLoadIdentificationTable(thread));
            }
        }
    }

    /**
     * Get a value indicating whether the specified program counter (PC) is delinquent or not for the specified thread.
     *
     * @param threadId the ID of the thread
     * @param pc       the value of the program counter (PC)
     * @return a value indicating whether the specified program counter (PC) is delinquent or not for the specified thread
     */
    public boolean isDelinquentPc(int threadId, int pc) {
        return this.delinquentLoadIdentificationTables.get(threadId).isDelinquentPc(pc);
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, "delinquentLoadIdentificationHelper") {{
            for (Core core : processor.getCores()) {
                for (Thread thread : core.getThreads()) {
                    final List<DelinquentLoad> steadyDelinquentLoads = delinquentLoadIdentificationTables.get(thread.getId()).getSteadyDelinquentLoads();

                    getChildren().add(new ReportNode(this, thread.getName() + ".steadyDelinquentLoads.size", steadyDelinquentLoads.size() + ""));

                    for (int i = 0, steadyDelinquentLoadsSize = steadyDelinquentLoads.size(); i < steadyDelinquentLoadsSize; i++) {
                        DelinquentLoad delinquentLoad = steadyDelinquentLoads.get(i);
                        getChildren().add(new ReportNode(this, thread.getName() + ".steadyDelinquentLoad_" + i,
                                String.format("PC: 0x%08x, functionCallPC: 0x%08x", delinquentLoad.getPc(), delinquentLoad.getFunctionCallPc())));
                    }
                }
            }
        }});
    }
}
