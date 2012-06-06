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
package archimulator.sim.ext.uncore.delinquentLoad;

import archimulator.sim.core.Thread;
import archimulator.sim.core.event.InstructionCommittedEvent;
import archimulator.sim.os.FunctionCallContext;
import archimulator.sim.uncore.coherence.event.CoherentCacheBeginCacheAccessEvent;
import net.pickapack.action.Action1;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public abstract class AbstractDelinquentLoadIdentificationTable {
    private Thread thread;
    private List<DelinquentLoadImpl> delinquentLoads;

    public AbstractDelinquentLoadIdentificationTable(Thread thread) {
        this.thread = thread;
        this.delinquentLoads = new ArrayList<DelinquentLoadImpl>();

        thread.getBlockingEventDispatcher().addListener(InstructionCommittedEvent.class, new Action1<InstructionCommittedEvent>() {
            public void apply(InstructionCommittedEvent event) {
                if (event.getDynamicInst().getThread() == AbstractDelinquentLoadIdentificationTable.this.thread) {
                    Stack<FunctionCallContext> functionCallContextStack = event.getDynamicInst().getThread().getContext().getFunctionCallContextStack();

                    if (functionCallContextStack.size() > 0) {
                        boolean delinquentLoadFound = false;

                        for (DelinquentLoadImpl delinquentLoad : delinquentLoads) {
                            if (delinquentLoad.getPc() == event.getDynamicInst().getPc() && delinquentLoad.getFunctionCallPc() == functionCallContextStack.peek().getPc()) {
                                delinquentLoad.executionCount++;
                                delinquentLoad.cyclesSpentInFirstEntryOfReorderBuffer += event.getDynamicInst().getCyclesSpentAtHeadOfReorderBuffer();
                                delinquentLoadFound = true;
                                break;
                            }
                        }

                        if (!delinquentLoadFound && event.getDynamicInst().isHasL2Miss() && delinquentLoads.size() < CAPACITY) {
                            DelinquentLoadImpl delinquentLoad = new DelinquentLoadImpl(event.getDynamicInst().getPc(), functionCallContextStack.peek().getPc());
                            delinquentLoad.executionCount = 1;
                            delinquentLoad.cyclesSpentInFirstEntryOfReorderBuffer = event.getDynamicInst().getCyclesSpentAtHeadOfReorderBuffer();
                            delinquentLoads.add(delinquentLoad);
                        }
                    }

                    for (Iterator<DelinquentLoadImpl> iterator = delinquentLoads.iterator(); iterator.hasNext(); ) {
                        DelinquentLoadImpl delinquentLoad = iterator.next();
                        delinquentLoad.totalInsts++;

                        if (delinquentLoad.totalInsts >= INSTRUCTIONS_PER_PHASE) {
                            if (delinquentLoad.executionCount >= EXECUTION_COUNT_THRESHOLD && delinquentLoad.cyclesSpentInFirstEntryOfReorderBuffer / EXECUTION_COUNT_THRESHOLD >= 4) {

                                action(delinquentLoad);

                                delinquentLoad.steady = true;

                                delinquentLoad.totalInsts = 0;
                                delinquentLoad.executionCount = 0;
                                delinquentLoad.cyclesSpentInFirstEntryOfReorderBuffer = 0;
                            } else {
                                iterator.remove();
                            }
                        }
                    }
                }
            }
        });

        thread.getBlockingEventDispatcher().addListener(CoherentCacheBeginCacheAccessEvent.class, new Action1<CoherentCacheBeginCacheAccessEvent>() {
            public void apply(CoherentCacheBeginCacheAccessEvent event) {
                if (!event.getCacheAccess().isHitInCache() && event.getAccess().getThread() == AbstractDelinquentLoadIdentificationTable.this.thread && event.getCacheController().isLastLevelCache() && event.getCacheAccess().getReference().getAccessType().isDownwardRead()) {
                    if (event.getAccess().getDynamicInst() != null) {
                        event.getAccess().getDynamicInst().setHasL2Miss(true);
                    }
                }
            }
        });
    }

    public void removeDelinquentLoad(DelinquentLoad delinquentLoadToRemove) {
        this.delinquentLoads.remove((DelinquentLoadImpl) delinquentLoadToRemove);
    }

    public boolean isDelinquentPc(int pc) {
        for (DelinquentLoadImpl delinquentLoad : this.delinquentLoads) {
            if (delinquentLoad.getPc() == pc && delinquentLoad.steady) {
                return true;
            }
        }

        return false;
    }

    protected abstract void action(DelinquentLoad delinquentLoad);

    public Thread getThread() {
        return thread;
    }

    public List<DelinquentLoad> getSteadyDelinquentLoads() {
        List<DelinquentLoad> steadyDelinquentLoads = new ArrayList<DelinquentLoad>();

        for (DelinquentLoadImpl delinquentLoad : this.delinquentLoads) {
            if (delinquentLoad.steady) {
                steadyDelinquentLoads.add(delinquentLoad);
            }
        }

        return steadyDelinquentLoads;
    }

    protected class DelinquentLoadImpl implements DelinquentLoad {
        private int pc;
        private int functionCallPc;

        private int executionCount;
        private int totalInsts;
        private int cyclesSpentInFirstEntryOfReorderBuffer;

        private boolean steady;

        public DelinquentLoadImpl(int pc, int functionCallPc) {
            this.pc = pc;
            this.functionCallPc = functionCallPc;
        }

        public int getPc() {
            return pc;
        }

        public int getFunctionCallPc() {
            return functionCallPc;
        }
    }

    private static final int CAPACITY = 64;
    //    private static final int CAPACITY = 2;
    private static final int INSTRUCTIONS_PER_PHASE = 128000;
    private static final int EXECUTION_COUNT_THRESHOLD = 100;
}
