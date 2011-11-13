/*******************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.ext.uncore.delinquentLoad;

import archimulator.core.Core;
import archimulator.core.Processor;
import archimulator.core.Thread;
import archimulator.sim.capability.ProcessorCapability;
import archimulator.sim.capability.ProcessorCapabilityFactory;
import archimulator.sim.event.DumpStatEvent;
import archimulator.sim.event.PollStatsEvent;
import archimulator.util.action.Action1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DelinquentLoadIdentificationCapability implements ProcessorCapability {
    private Map<Integer, AbstractDelinquentLoadIdentificationTable> dlits;
    private Processor processor;

    public DelinquentLoadIdentificationCapability(Processor processor) {
        this.processor = processor;

        this.dlits = new HashMap<Integer, AbstractDelinquentLoadIdentificationTable>();

        for (Core core : processor.getCores()) {
            for (Thread thread : core.getThreads()) {
                this.dlits.put(thread.getId(), new DelinquentLoadIdentificationTable(thread));
            }
        }

        processor.getBlockingEventDispatcher().addListener(PollStatsEvent.class, new Action1<PollStatsEvent>() {
            public void apply(PollStatsEvent event) {
                dumpStats(event.getStats());
            }
        });

        processor.getBlockingEventDispatcher().addListener(DumpStatEvent.class, new Action1<DumpStatEvent>() {
            public void apply(DumpStatEvent event) {
                if (event.getType() == DumpStatEvent.Type.DETAILED_SIMULATION) {
                    dumpStats(event.getStats());
                }
            }
        });
    }

    private void dumpStats(Map<String, Object> stats) {
        for (Core core : this.processor.getCores()) {
            for (Thread thread : core.getThreads()) {
                final List<DelinquentLoad> steadyDelinquentLoads = this.dlits.get(thread.getId()).getSteadyDelinquentLoads();

                stats.put("DelinquentLoadIdentificationCapability." + thread.getName() + ".steadyDelinquentLoads.size", steadyDelinquentLoads.size());

                for (int i = 0, steadyDelinquentLoadsSize = steadyDelinquentLoads.size(); i < steadyDelinquentLoadsSize; i++) {
                    DelinquentLoad delinquentLoad = steadyDelinquentLoads.get(i);
                    stats.put("DelinquentLoadIdentificationCapability." + thread.getName() + ".steadyDelinquentLoad_" + i,
                            String.format("PC: 0x%08x, functionCallPC: 0x%08x", delinquentLoad.getPc(), delinquentLoad.getFunctionCallPc()));
                }
            }
        }
    }

    public boolean isDelinquentPc(int threadId, int pc) {
        return this.dlits.get(threadId).isDelinquentPc(pc);
    }

    private class DelinquentLoadIdentificationTable extends AbstractDelinquentLoadIdentificationTable {
        public DelinquentLoadIdentificationTable(Thread thread) {
            super(thread);
        }

        @Override
        protected void action(DelinquentLoad delinquentLoad) {
//            System.out.printf("%s: Delinquent load {pc = 0x%08x, functionCallPc = 0x%08x}\n", this.getThread().getName(), delinquentLoad.getPc(), delinquentLoad.getFunctionCallPc());
        }
    }

    public static final ProcessorCapabilityFactory FACTORY = new ProcessorCapabilityFactory() {
        public ProcessorCapability createCapability(Processor processor) {
            return new DelinquentLoadIdentificationCapability(processor);
        }
    };
}
