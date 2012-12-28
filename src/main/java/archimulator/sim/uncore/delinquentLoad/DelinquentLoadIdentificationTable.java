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

import archimulator.sim.common.SimulationEvent;
import archimulator.sim.core.Thread;
import archimulator.sim.core.event.InstructionCommittedEvent;
import archimulator.sim.os.FunctionCallContext;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerServiceNonblockingRequestEvent;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import net.pickapack.action.Action1;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * Delinquent load identification table.
 *
 * @author Min Cai
 */
public class DelinquentLoadIdentificationTable {
    private Thread thread;
    private List<DelinquentLoad> delinquentLoads;

    /**
     * Create a delinquent load identification table.
     *
     * @param thread the thread
     */
    public DelinquentLoadIdentificationTable(final Thread thread) {
        this.thread = thread;
        this.delinquentLoads = new ArrayList<DelinquentLoad>();

        thread.getBlockingEventDispatcher().addListener(InstructionCommittedEvent.class, new Action1<InstructionCommittedEvent>() {
            public void apply(InstructionCommittedEvent event) {
                if (event.getDynamicInstruction().getThread() == DelinquentLoadIdentificationTable.this.thread) {
                    Stack<FunctionCallContext> functionCallContextStack = event.getDynamicInstruction().getThread().getContext().getFunctionCallContextStack();

                    if (functionCallContextStack.size() > 0) {
                        boolean delinquentLoadFound = false;

                        for (DelinquentLoad delinquentLoad : delinquentLoads) {
                            if (delinquentLoad.getPc() == event.getDynamicInstruction().getPc() && delinquentLoad.getFunctionCallPc() == functionCallContextStack.peek().getPc()) {
                                delinquentLoad.setNumExecutions(delinquentLoad.getNumExecutions() + 1);
                                delinquentLoad.setNumCyclesSpentAtHeadOfReorderBuffer(delinquentLoad.getNumCyclesSpentAtHeadOfReorderBuffer() + event.getDynamicInstruction().getNumCyclesSpentAtHeadOfReorderBuffer());
                                delinquentLoadFound = true;
                                break;
                            }
                        }

                        if (!delinquentLoadFound && event.getDynamicInstruction().isMissedInL2Cache() && delinquentLoads.size() < CAPACITY) {
                            DelinquentLoad delinquentLoad = new DelinquentLoad(event.getDynamicInstruction().getPc(), functionCallContextStack.peek().getPc());
                            delinquentLoad.setNumExecutions(1);
                            delinquentLoad.setNumCyclesSpentAtHeadOfReorderBuffer(event.getDynamicInstruction().getNumCyclesSpentAtHeadOfReorderBuffer());
                            delinquentLoads.add(delinquentLoad);
                        }
                    }

                    for (Iterator<DelinquentLoad> iterator = delinquentLoads.iterator(); iterator.hasNext(); ) {
                        DelinquentLoad delinquentLoad = iterator.next();
                        delinquentLoad.setNumInstructions(delinquentLoad.getNumInstructions() + 1);

                        if (delinquentLoad.getNumInstructions() >= INSTRUCTIONS_PER_PHASE) {
                            if (delinquentLoad.getNumExecutions() >= EXECUTION_COUNT_THRESHOLD && delinquentLoad.getNumCyclesSpentAtHeadOfReorderBuffer() / EXECUTION_COUNT_THRESHOLD >= 4) {
                                thread.getBlockingEventDispatcher().dispatch(new DelinquentLoadIdentifiedEvent(thread, delinquentLoad));

                                delinquentLoad.setSteady(true);

                                delinquentLoad.setNumInstructions(0);
                                delinquentLoad.setNumExecutions(0);
                                delinquentLoad.setNumCyclesSpentAtHeadOfReorderBuffer(0);
                            } else {
                                iterator.remove();
                            }
                        }
                    }
                }
            }
        });

        thread.getBlockingEventDispatcher().addListener(GeneralCacheControllerServiceNonblockingRequestEvent.class, new Action1<GeneralCacheControllerServiceNonblockingRequestEvent>() {
            @Override
            public void apply(GeneralCacheControllerServiceNonblockingRequestEvent event) {
                if(!event.isHitInCache() && event.getAccess().getThread() == DelinquentLoadIdentificationTable.this.thread && event.getCacheController() instanceof DirectoryController && event.getAccess().getType().isRead()) {
                    if(event.getAccess().getDynamicInstruction() != null) {
                        event.getAccess().getDynamicInstruction().setMissedInL2Cache(true);
                    }
                }
            }
        });
    }

    /**
     * Remove the specified delinquent load.
     *
     * @param delinquentLoadToRemove the delinquent load to be removed
     */
    public void removeDelinquentLoad(DelinquentLoad delinquentLoadToRemove) {
        this.delinquentLoads.remove(delinquentLoadToRemove);
    }

    /**
     * Get a value indicating whether the specified program counter (PC) is delinquent or not.
     *
     * @param pc the value of the program counter (PC)
     * @return a value indicating whether the specified program counter (PC) is delinquent or not
     */
    public boolean isDelinquentPc(int pc) {
        for (DelinquentLoad delinquentLoad : this.delinquentLoads) {
            if (delinquentLoad.getPc() == pc && delinquentLoad.isSteady()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the thread.
     *
     * @return the thread
     */
    public Thread getThread() {
        return thread;
    }

    /**
     * Get the list of steady delinquent loads.
     *
     * @return the list of steady delinquent loads
     */
    public List<DelinquentLoad> getSteadyDelinquentLoads() {
        List<DelinquentLoad> steadyDelinquentLoads = new ArrayList<DelinquentLoad>();

        for (DelinquentLoad delinquentLoad : this.delinquentLoads) {
            if (delinquentLoad.isSteady()) {
                steadyDelinquentLoads.add(delinquentLoad);
            }
        }

        return steadyDelinquentLoads;
    }

    /**
     * The event when a delinquent load is identified.
     */
    public class DelinquentLoadIdentifiedEvent extends SimulationEvent {
        private Thread thread;
        private DelinquentLoad delinquentLoad;

        /**
         * Create a delinquent load identified event.
         *
         * @param thread the thread
         * @param delinquentLoad the delinquent load
         */
        public DelinquentLoadIdentifiedEvent(Thread thread, DelinquentLoad delinquentLoad) {
            super(thread);
            this.thread = thread;
            this.delinquentLoad = delinquentLoad;
        }

        /**
         * Get the thread.
         *
         * @return the thread
         */
        public Thread getThread() {
            return thread;
        }

        /**
         * Get the delinquent load.
         *
         * @return the delinquent load
         */
        public DelinquentLoad getDelinquentLoad() {
            return delinquentLoad;
        }
    }

    private static final int CAPACITY = 64;
    //    private static final int CAPACITY = 2;
    private static final int INSTRUCTIONS_PER_PHASE = 128000;
    private static final int EXECUTION_COUNT_THRESHOLD = 100;
}
