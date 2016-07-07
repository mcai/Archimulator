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
package archimulator.uncore.delinquentLoad;

import archimulator.common.Simulation;
import archimulator.common.report.ReportNode;
import archimulator.common.report.Reportable;
import archimulator.core.Core;
import archimulator.core.Thread;
import archimulator.util.Pair;

import java.util.*;

/**
 * Delinquent load identification helper.
 *
 * @author Min Cai
 */
public class DelinquentLoadIdentificationHelper implements Reportable {
    private Map<Integer, DelinquentLoadIdentificationTable> delinquentLoadIdentificationTables;

    private List<Pair<Integer, Integer>> identifiedDelinquentLoads;

    /**
     * Create a delinquent load identification helper.
     *
     * @param simulation the simulation
     */
    @SuppressWarnings("unchecked")
    public DelinquentLoadIdentificationHelper(Simulation simulation) {
        this.delinquentLoadIdentificationTables = new HashMap<>();

        for (Core core : simulation.getProcessor().getCores()) {
            for (Thread thread : core.getThreads()) {
                this.delinquentLoadIdentificationTables.put(thread.getId(), new DelinquentLoadIdentificationTable(thread));
            }
        }

        this.identifiedDelinquentLoads = new ArrayList<>();

        simulation.getBlockingEventDispatcher().addListener(DelinquentLoadIdentificationTable.DelinquentLoadIdentifiedEvent.class, event -> {
            for (Pair<Integer, Integer> recordedDelinquentLoad : identifiedDelinquentLoads) {
                if (recordedDelinquentLoad.getFirst() == event.getThread().getId() && recordedDelinquentLoad.getSecond() == event.getDelinquentLoad().getPc()) {
                    return;
                }
            }

            identifiedDelinquentLoads.add(new Pair<>(event.getThread().getId(), event.getDelinquentLoad().getPc()));

            identifiedDelinquentLoads.sort(Comparator.<Pair<Integer, Integer>, Comparable>comparing(Pair::getFirst).thenComparing(Pair::getSecond));
        });
    }

    /**
     * Get a value indicating whether the specified program counter (PC) is delinquent or not for the specified thread.
     *
     * @param threadId the ID of the thread
     * @param pc       the value of the program counter (PC)
     * @return a value indicating whether the specified program counter (PC) is delinquent or not for the specified thread
     */
    public boolean isDelinquentPc(int threadId, int pc) {
        for (Pair<Integer, Integer> recordedDelinquentLoad : identifiedDelinquentLoads) {
            if (recordedDelinquentLoad.getFirst() == threadId && recordedDelinquentLoad.getSecond() == pc) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, "delinquentLoadIdentificationHelper") {{
            getChildren().add(
                    new ReportNode(
                            this,
                            "identifiedDelinquentLoads.size",
                            String.format("%d", identifiedDelinquentLoads.size())
                    )
            );

            for (int i = 0; i < identifiedDelinquentLoads.size(); i++) {
                Pair<Integer, Integer> delinquentLoad = identifiedDelinquentLoads.get(i);
                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("identifiedDelinquentLoad/%d/threadId", i),
                                String.format("%d", delinquentLoad.getFirst())
                        )
                );

                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("identifiedDelinquentLoad/%d/pc", i),
                                String.format("0x%08x", delinquentLoad.getSecond())
                        )
                );
            }
        }});
    }
}
