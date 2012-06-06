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
package archimulator.sim.uncore.dram;

import archimulator.sim.base.event.DumpStatEvent;
import archimulator.sim.base.event.PollStatsEvent;
import archimulator.sim.base.event.ResetStatEvent;
import archimulator.sim.uncore.CacheHierarchy;
import archimulator.sim.uncore.MemoryDevice;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.sim.uncore.net.Net;
import net.pickapack.action.Action;
import net.pickapack.action.Action1;

import java.util.Map;

public abstract class MemoryController extends MemoryDevice {
    private long reads;
    private long writes;

    public MemoryController(CacheHierarchy cacheHierarchy) {
        super(cacheHierarchy, "mainMemory");

        this.getBlockingEventDispatcher().addListener(ResetStatEvent.class, new Action1<ResetStatEvent>() {
            public void apply(ResetStatEvent event) {
                reads = 0;                     writes = 0;
            }
        });

        this.getBlockingEventDispatcher().addListener(PollStatsEvent.class, new Action1<PollStatsEvent>() {
            public void apply(PollStatsEvent event) {
                dumpStats(event.getStats());
            }
        });

        this.getBlockingEventDispatcher().addListener(DumpStatEvent.class, new Action1<DumpStatEvent>() {
            public void apply(DumpStatEvent event) {
                if (event.getType() == DumpStatEvent.Type.DETAILED_SIMULATION) {
                    dumpStats(event.getStats());
                }
            }
        });
    }

    private void dumpStats(Map<String, Object> stats) {
        if(getAccesses() > 0) {
            stats.put(getName() + ".accesses", String.valueOf(getAccesses()));
            stats.put(getName() + ".reads", String.valueOf(reads));
            stats.put(getName() + ".writes", String.valueOf(writes));
        }
    }

    @Override
    protected Net getNet(MemoryDevice to) {
        return this.getCacheHierarchy().getL2ToMemNetwork();
    }

    public void memReadRequestReceive(final MemoryDevice source, int tag, final Action onSuccessCallback) {
        this.reads++;

        this.access(tag, new Action() {
            public void apply() {
                new Action() {
                    public void apply() {
                        transfer(source, ((DirectoryController) source).getCache().getLineSize() + 8, onSuccessCallback);
                    }
                }.apply();
            }
        });
    }

    public void memWriteRequestReceive(final MemoryDevice source, int tag, final Action onSuccessCallback) {
        this.writes++;

        this.access(tag, (new Action() {
            public void apply() {
                new Action() {
                    public void apply() {
                        transfer(source, 8, onSuccessCallback);
                    }
                }.apply();
            }
        }));
    }

    protected abstract void access(int addr, Action onCompletedCallback);

    private long getAccesses() {
        return this.reads + this.writes;
    }

    @Override
    public String toString() {
        return getName();
    }
}



















