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
import archimulator.sim.base.event.ResetStatEvent;
import archimulator.sim.uncore.CacheHierarchy;
import archimulator.sim.uncore.MemoryDevice;
import archimulator.sim.uncore.coherence.LastLevelCache;
import archimulator.sim.uncore.coherence.message.MemReadMessage;
import archimulator.sim.uncore.coherence.message.MemWriteMessage;
import archimulator.sim.uncore.coherence.message.MemoryDeviceMessage;
import archimulator.sim.uncore.net.Net;
import archimulator.util.action.Action;
import archimulator.util.action.Action1;

public abstract class MainMemory extends MemoryDevice {
    private long reads;
    private long writes;

    public MainMemory(CacheHierarchy cacheHierarchy) {
        super(cacheHierarchy, "mainMemory");

        this.getBlockingEventDispatcher().addListener(ResetStatEvent.class, new Action1<ResetStatEvent>() {
            public void apply(ResetStatEvent event) {
                reads = 0;
                writes = 0;
            }
        });

        this.getBlockingEventDispatcher().addListener(DumpStatEvent.class, new Action1<DumpStatEvent>() {
            public void apply(DumpStatEvent event) {
                if (event.getType() == DumpStatEvent.Type.DETAILED_SIMULATION) {
                    event.getStats().put(getName() + ".accesses", String.valueOf(getAccesses()));
                    event.getStats().put(getName() + ".reads", String.valueOf(reads));
                    event.getStats().put(getName() + ".writes", String.valueOf(writes));
                }
            }
        });
    }

    @Override
    protected Net getNet(MemoryDevice to) {
        return this.getCacheHierarchy().getL2ToMemNetwork();
    }

    @Override
    public void receiveRequest(MemoryDevice source, MemoryDeviceMessage message) {
        switch (message.getType()) {
            case MEM_READ:
                this.memReadRequestReceive(source, (MemReadMessage) message);
                break;
            case MEM_WRITE:
                this.memWriteRequestReceive(source, (MemWriteMessage) message);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void memReadRequestReceive(final MemoryDevice source, final MemReadMessage message) {
        this.reads++;

        this.access(message.getTag(), new Action() {
            public void apply() {
                new Action() {
                    public void apply() {
                        sendReply(source, message, ((LastLevelCache) source).getCache().getLineSize() + 8);
                    }
                }.apply();
            }
        });
    }

    private void memWriteRequestReceive(final MemoryDevice source, final MemWriteMessage message) {
        this.writes++;

        this.access(message.getTag(), (new Action() {
            public void apply() {
                new Action() {
                    public void apply() {
                        sendReply(source, message, 8);
                    }
                }.apply();
            }
        }));
    }

    protected abstract void access(int addr, Action onCompletedCallback);

    public void dumpState() {
        //TODO
    }

    private long getAccesses() {
        return this.reads + this.writes;
    }
}



















