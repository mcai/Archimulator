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
package archimulator.core.speculativePrecomputation;

import archimulator.common.Logger;
import archimulator.common.Simulation;
import archimulator.core.Core;
import archimulator.core.Processor;
import archimulator.core.Thread;
import archimulator.core.event.DynamicInstructionCommittedEvent;
import archimulator.core.event.DynamicInstructionDecodedEvent;
import archimulator.isa.ArchitecturalRegisterFile;
import archimulator.isa.StaticInstruction;
import archimulator.isa.StaticInstructionType;
import archimulator.os.Context;
import archimulator.os.ContextKilledEvent;
import archimulator.uncore.MemoryHierarchyAccess;
import archimulator.uncore.MemoryHierarchyAccessType;
import archimulator.uncore.cache.*;
import archimulator.uncore.cache.replacement.CacheReplacementPolicyType;
import archimulator.uncore.coherence.event.GeneralCacheControllerServiceNonblockingRequestEvent;
import archimulator.uncore.coherence.msi.controller.DirectoryController;
import archimulator.uncore.delinquentLoad.DelinquentLoad;
import archimulator.uncore.delinquentLoad.DelinquentLoadIdentificationTable;
import archimulator.util.ValueProvider;
import org.apache.commons.collections.iterators.ReverseListIterator;

import java.util.*;

/**
 * Dynamic speculative precomputation helper.
 *
 * @author Min Cai
 */
public class DynamicSpeculativePrecomputationHelper {
    private EvictableCache<Boolean> sliceCache;

    private Map<Thread, DelinquentLoadIdentificationTable> delinquentLoadIdentificationTables;
    private Map<Thread, RetiredInstructionBuffer> retiredInstructionBuffers;
    private Map<Thread, SliceInformationTable> sliceInformationTables;

    /**
     * Create a dynamic speculative precomputation helper.
     *
     * @param simulation the simulation
     */
    public DynamicSpeculativePrecomputationHelper(Simulation simulation) {
        Processor processor = simulation.getProcessor();

        this.sliceCache = new BasicEvictableCache<>(
                processor,
                "sliceCache",
                new CacheGeometry(SLICE_CACHE_CAPACITY, SLICE_CACHE_CAPACITY, 1),
                CacheReplacementPolicyType.LRU,
                args -> new BooleanValueProvider()
        );

        this.delinquentLoadIdentificationTables = new HashMap<>();
        this.retiredInstructionBuffers = new HashMap<>();
        this.sliceInformationTables = new HashMap<>();

        for (Core core : processor.getCores()) {
            for (Thread thread : core.getThreads()) {
                this.getDelinquentLoadIdentificationTables().put(thread, new DelinquentLoadIdentificationTable(thread));
                this.getRetiredInstructionBuffers().put(thread, new RetiredInstructionBuffer(this, thread));
                this.getSliceInformationTables().put(thread, new SliceInformationTable(this, thread));
            }
        }

        processor.getBlockingEventDispatcher().addListener(
                DelinquentLoadIdentificationTable.DelinquentLoadIdentifiedEvent.class,
                event -> getRetiredInstructionBuffers().get(event.getThread()).gatherInstructionsFor(event.getDelinquentLoad())
        );
    }

    /**
     * Get the slice cache.
     *
     * @return the slice cache
     */
    public EvictableCache<Boolean> getSliceCache() {
        return sliceCache;
    }

    /**
     * Get the map of delinquent load identification tables.
     *
     * @return the map of delinquent load identification tables
     */
    public Map<Thread, DelinquentLoadIdentificationTable> getDelinquentLoadIdentificationTables() {
        return delinquentLoadIdentificationTables;
    }

    /**
     * Get the map of retired instruction buffers.
     *
     * @return the map of retired instruction buffers
     */
    public Map<Thread, RetiredInstructionBuffer> getRetiredInstructionBuffers() {
        return retiredInstructionBuffers;
    }

    /**
     * Get the map of slice information tables.
     *
     * @return the map of slice information tables
     */
    public Map<Thread, SliceInformationTable> getSliceInformationTables() {
        return sliceInformationTables;
    }

    /**
     * Boolean value provider.
     */
    public class BooleanValueProvider implements ValueProvider<Boolean> {
        private boolean state;
        private Map<Integer, Integer> machineInstructions;

        /**
         * Create a boolean value provider.
         */
        private BooleanValueProvider() {
            this.state = false;
        }

        /**
         * Get the value.
         *
         * @return the value
         */
        @Override
        public Boolean get() {
            return state;
        }

        /**
         * Get the initial value.
         *
         * @return the initial value
         */
        @Override
        public Boolean getInitialValue() {
            return false;
        }

        public Map<Integer, Integer> getMachineInstructions() {
            return machineInstructions;
        }
    }

    /**
     * Find an invalid line and create a new miss for the specified address.
     *
     * @param thread  the thread
     * @param address the address
     * @param set     the set index
     * @return an invalid line and the newly created miss for the specified address
     */
    private CacheAccess<Boolean> findInvalidLineAndNewMiss(Thread thread, int address, int set) {
        int tag = this.getSliceCache().getTag(address);

        for (int way = 0; way < this.getSliceCache().getAssociativity(); way++) {
            CacheLine<Boolean> line = this.getSliceCache().getLine(set, way);
            if (line.getState() == line.getInitialState()) {
                return new CacheAccess<>(this.getSliceCache(), new MemoryHierarchyAccess(null, thread, MemoryHierarchyAccessType.UNKNOWN, -1, address, tag, null), set, way, tag);
            }
        }

        return null;
    }

    /**
     * Retired instruction buffer.
     */
    public static class RetiredInstructionBuffer {
        private DynamicSpeculativePrecomputationHelper dynamicSpeculativePrecomputationHelper;
        private Thread thread;
        private Stack<RetiredInstruction> retiredInstructions;

        private List<StackAddressTableEntry> stackAddressTable;

        private RetiredInstructionBufferState state;

        private DelinquentLoad delinquentLoad;

        /**
         * Create a retired instruction buffer.
         *
         * @param dynamicSpeculativePrecomputationHelper
         *               the dynamic speculative precomputation helper
         * @param thread the thread
         */
        private RetiredInstructionBuffer(DynamicSpeculativePrecomputationHelper dynamicSpeculativePrecomputationHelper, Thread thread) {
            this.dynamicSpeculativePrecomputationHelper = dynamicSpeculativePrecomputationHelper;
            this.thread = thread;

            this.retiredInstructions = new Stack<>();

            this.stackAddressTable = new ArrayList<>();

            this.state = RetiredInstructionBufferState.IDLE;

            this.thread.getBlockingEventDispatcher().addListener(DynamicInstructionCommittedEvent.class, event -> {
                if (event.getDynamicInstruction().getThread() == RetiredInstructionBuffer.this.thread) {
                    if (state == RetiredInstructionBufferState.IDLE && delinquentLoad != null && event.getDynamicInstruction().getPc() == delinquentLoad.getPc() && !event.getDynamicInstruction().getThread().getContext().getFunctionCallContextStack().isEmpty() && event.getDynamicInstruction().getThread().getContext().getFunctionCallContextStack().peek().getPc() == delinquentLoad.getFunctionCallPc()) {
                        state = RetiredInstructionBufferState.INSTRUCTION_GATHERING;

                        retiredInstructions.add(new RetiredInstruction(event.getDynamicInstruction().getPc(), event.getDynamicInstruction().isUseStackPointerAsEffectiveAddressBase(), event.getDynamicInstruction().getEffectiveAddressDisplacement(), event.getDynamicInstruction().getStaticInstruction()));
                    } else if (state == RetiredInstructionBufferState.INSTRUCTION_GATHERING) {
                        if (retiredInstructions.size() >= CAPACITY) {
                            retiredInstructions.remove(retiredInstructions.firstElement());
                        }
                        retiredInstructions.add(new RetiredInstruction(event.getDynamicInstruction().getPc(), event.getDynamicInstruction().isUseStackPointerAsEffectiveAddressBase(), event.getDynamicInstruction().getEffectiveAddressDisplacement(), event.getDynamicInstruction().getStaticInstruction()));

                        if (event.getDynamicInstruction().getPc() == delinquentLoad.getPc() && !event.getDynamicInstruction().getThread().getContext().getFunctionCallContextStack().isEmpty() && event.getDynamicInstruction().getThread().getContext().getFunctionCallContextStack().peek().getPc() == delinquentLoad.getFunctionCallPc()) {
                            buildSlice();
                        }
                    }
                }
            });
        }

        /**
         * Gather the instructions for the specified delinquent load.
         *
         * @param delinquentLoad the delinquent load
         */
        private void gatherInstructionsFor(DelinquentLoad delinquentLoad) {
            if (this.state == RetiredInstructionBufferState.IDLE) {
                this.delinquentLoad = delinquentLoad;
            }
        }

        /**
         * Build the slice.
         */
        private void buildSlice() {
            this.state = RetiredInstructionBufferState.SLICE_BUILDING;

            this.stackAddressTable.clear();

            Slice slice = new Slice(this.delinquentLoad);

            //            for (RetiredInstruction retiredInstruction : this.retiredInstructions) {
            //                System.out.println(retiredInstruction);
            //            }

            slice.getLiveIns().addAll(this.retiredInstructions.lastElement().staticInstruction.getInputDependencies());
            this.mark(this.retiredInstructions.lastElement());

            RetiredInstruction lastAnalyzedRetiredInstruction = null;

            for (Iterator it = new ReverseListIterator(this.retiredInstructions); it.hasNext(); ) {
                RetiredInstruction retiredInstruction = (RetiredInstruction) it.next();

                if (this.checkIfNoIgnoredStoreLoadDependence(retiredInstruction)) {
                    for (int outputDependency : retiredInstruction.staticInstruction.getOutputDependencies()) {
                        if (slice.getLiveIns().contains(outputDependency)) {
                            this.mark(retiredInstruction);
                            slice.getLiveIns().removeAll(retiredInstruction.staticInstruction.getOutputDependencies());
                            slice.getLiveIns().addAll(retiredInstruction.staticInstruction.getInputDependencies());
                            break;
                        }
                    }

                    lastAnalyzedRetiredInstruction = retiredInstruction;
                }
            }

            for (StackAddressTableEntry stackAddressTableEntry : this.stackAddressTable) {
                this.unmark(stackAddressTableEntry.retiredInstruction);
            }

            if (lastAnalyzedRetiredInstruction == null) {
                throw new IllegalArgumentException();
            }

            slice.setTriggerPc(lastAnalyzedRetiredInstruction.pc);

            this.retiredInstructions.stream().filter(retiredInstruction -> retiredInstruction.marked).forEach(retiredInstruction -> {
                slice.getPcs().add(retiredInstruction.pc);
            });

            this.dynamicSpeculativePrecomputationHelper.getSliceInformationTables().get(this.thread).storeSlice(slice);

            this.retiredInstructions.clear();
            this.state = RetiredInstructionBufferState.IDLE;
            this.delinquentLoad = null;
        }

        /**
         * Mark the specified retired instruction.
         *
         * @param retiredInstruction the retired instruction to be marked
         */
        private void mark(RetiredInstruction retiredInstruction) {
            retiredInstruction.marked = true;

            if (retiredInstruction.staticInstruction.getMnemonic().getType() == StaticInstructionType.LOAD && retiredInstruction.useStackPointerAsEffectiveAddressBase) {
                int displacement = retiredInstruction.effectiveAddressDisplacement;
                int nonEffectiveAddressBaseDep = retiredInstruction.staticInstruction.getNonEffectiveAddressBaseDependency();

                this.stackAddressTable.add(new StackAddressTableEntry(retiredInstruction, displacement, nonEffectiveAddressBaseDep));
            }
        }

        /**
         * Check whether the retired instruction has no ignored store-load dependence.
         *
         * @param retiredInstruction the retired instruction
         * @return a value indicating whether the specified retired instruction has no ignored store-load dependence
         */
        private boolean checkIfNoIgnoredStoreLoadDependence(RetiredInstruction retiredInstruction) {
            for (Iterator<StackAddressTableEntry> iterator = this.stackAddressTable.iterator(); iterator.hasNext(); ) {
                if (retiredInstruction.staticInstruction.getOutputDependencies().contains(iterator.next().nonEffectiveAddressBaseDependency)) {
                    iterator.remove();
                }
            }

            if (retiredInstruction.staticInstruction.getMnemonic().getType() == StaticInstructionType.STORE && retiredInstruction.useStackPointerAsEffectiveAddressBase) {
                for (StackAddressTableEntry stackAddressTableEntry : this.stackAddressTable) {
                    if (stackAddressTableEntry.displacement == retiredInstruction.effectiveAddressDisplacement) {
                        return false;
                    }
                }
            }

            return true;
        }

        /**
         * Unmark the specified retired instruction.
         *
         * @param retiredInstruction the retired instruction
         */
        private void unmark(RetiredInstruction retiredInstruction) {
            retiredInstruction.marked = false;
        }

        /**
         * Stack address table entry.
         */
        private class StackAddressTableEntry {
            private RetiredInstruction retiredInstruction;
            private int displacement;
            private int nonEffectiveAddressBaseDependency;

            /**
             * Create a stack address table entry.
             *
             * @param retiredInstruction the retired instruction
             * @param displacement       the displacement
             * @param nonEffectiveAddressBaseDependency
             *                           the non-effective address base dependency
             */
            private StackAddressTableEntry(RetiredInstruction retiredInstruction, int displacement, int nonEffectiveAddressBaseDependency) {
                this.retiredInstruction = retiredInstruction;
                this.displacement = displacement;
                this.nonEffectiveAddressBaseDependency = nonEffectiveAddressBaseDependency;
            }
        }

        /**
         * Retired instruction.
         */
        private class RetiredInstruction {
            private boolean marked;

            private int pc;
            private boolean useStackPointerAsEffectiveAddressBase;
            private int effectiveAddressDisplacement;
            private StaticInstruction staticInstruction;

            /**
             * Create a retired instruction.
             *
             * @param pc                           the value of the program counter (PC)
             * @param useStackPointerAsEffectiveAddressBase
             *                                     a value indicating whether using the stack pointer as the effective address base or not
             * @param effectiveAddressDisplacement the effective address displacement
             * @param staticInstruction            the static instruction
             */
            private RetiredInstruction(int pc, boolean useStackPointerAsEffectiveAddressBase, int effectiveAddressDisplacement, StaticInstruction staticInstruction) {
                this.marked = false;

                this.pc = pc;
                this.useStackPointerAsEffectiveAddressBase = useStackPointerAsEffectiveAddressBase;
                this.effectiveAddressDisplacement = effectiveAddressDisplacement;
                this.staticInstruction = staticInstruction;
            }

            @Override
            public String toString() {
                return String.format("RetiredInstruction{marked=%s, pc=%d, useStackPointerAsEffectiveAddressBase=%s, effectiveAddressDisplacement=%d, staticInstruction=%s}", marked, pc, useStackPointerAsEffectiveAddressBase, effectiveAddressDisplacement, staticInstruction);
            }
        }

        private static final int CAPACITY = 512;
    }

    /**
     * Slice information table.
     */
    public static class SliceInformationTable {
        private DynamicSpeculativePrecomputationHelper dynamicSpeculativePrecomputationHelper;
        private Thread thread;
        private List<Slice> slices;

        /**
         * Create a slice information table for the specified thread.
         *
         * @param dynamicSpeculativePrecomputationHelper
         *               the dynamic speculative precomputation helper
         * @param thread the thread
         */
        private SliceInformationTable(DynamicSpeculativePrecomputationHelper dynamicSpeculativePrecomputationHelper, Thread thread) {
            this.dynamicSpeculativePrecomputationHelper = dynamicSpeculativePrecomputationHelper;
            this.thread = thread;

            this.slices = new ArrayList<>();

            this.thread.getBlockingEventDispatcher().addListener(DynamicInstructionDecodedEvent.class, event -> {
                for (Slice slice : slices) {
                    if (event.getDynamicInstruction().getPc() == slice.getTriggerPc() && slice.getSpawnedThreadContext() == null) {
                        spawnPrecomputationThread(slice, event.getDynamicInstruction().getThread().getContext());
                        break;
                    }
                }

                for (Slice slice : slices) {
                    if (slice.getSpawnedThreadContext() != null && event.getDynamicInstruction().getThread().getContext() == slice.getSpawnedThreadContext()) {
                        slice.setNumDecodedInstructions(slice.getNumDecodedInstructions() + 1);
                        break;
                    }
                }
            });

            this.thread.getBlockingEventDispatcher().addListener(DynamicInstructionCommittedEvent.class, event -> {
                if (SliceInformationTable.this.thread.getNumInstructions() % INSTRUCTIONS_PER_PHASE == 0) {
                    for (Iterator<Slice> it = slices.iterator(); it.hasNext(); ) {
                        Slice slice = it.next();
                        if (slice.getNumSpawnings() > 0) {
                            if (!evaluateEffectiveness(slice)) {
                                it.remove();
                            }
                        }
                    }
                }
            });

            this.thread.getBlockingEventDispatcher().addListener(ContextKilledEvent.class, event -> {
                for (Iterator<Slice> iterator = slices.iterator(); iterator.hasNext(); ) {
                    Slice slice = iterator.next();
                    if (event.getContext() == slice.getSpawnedThreadContext()) {
                        slice.setSpawnedThreadContext(null);

                        if (slice.isIneffective()) {
                            prepareRemoveSlice(slice);
                            iterator.remove();
                        }

                        break;
                    }
                }
            });

            this.thread.getBlockingEventDispatcher().addListener(GeneralCacheControllerServiceNonblockingRequestEvent.class, event -> {
                if (!event.isHitInCache() && event.getCacheController() instanceof DirectoryController && event.getAccess().getType().isRead()) {
                    for (Slice slice : slices) {
                        if (slice.getSpawnedThreadContext() != null) {
                            if (event.getAccess().getThread().getContext() == slice.getSpawnedThreadContext()) {
                                slice.setNumSavedL2Misses(slice.getNumSavedL2Misses() + 1);
                                break;
                            }
                        }
                    }
                }
            });
        }

        /**
         * Store the specified slice.
         *
         * @param slice the slice to be stored
         */
        private void storeSlice(Slice slice) {
            //TODO: how to support longer pcs? e.g., slice chains?
            if (slice.getPcs().size() > 64) {
                throw new IllegalArgumentException(slice.getPcs().size() + "");
            }

            int set = dynamicSpeculativePrecomputationHelper.getSliceCache().getSet(slice.getTriggerPc());

            CacheAccess<Boolean> cacheAccess = dynamicSpeculativePrecomputationHelper.findInvalidLineAndNewMiss(thread, slice.getTriggerPc(), set);

            if (cacheAccess != null) {
                CacheLine<Boolean> line = cacheAccess.getLine();
                BooleanValueProvider stateProvider = (BooleanValueProvider) line.getStateProvider();
                stateProvider.state = true;
                line.setTag(slice.getTriggerPc());
                dynamicSpeculativePrecomputationHelper.getSliceCache().getReplacementPolicy().handleInsertionOnMiss(null, set, cacheAccess.getWay());

                Map<Integer, Integer> machInsts = new TreeMap<>();

                int i = FIRST_INSTRUCTION_PC;

                for (int pc : slice.getPcs()) {
                    machInsts.put(i, pc); //TODO: trick for the moment: store mappedPc instead of machInst to use predecoding
                    i += 4;
                }

                stateProvider.machineInstructions = machInsts;

                slices.add(slice);
            } else {
                Logger.infof(Logger.THREAD, "%s: There is no sufficient hardware resource for storing slice, kindly ignored.", this.thread.getCycleAccurateEventQueue().getCurrentCycle(), this.thread.getName());
            }
        }

        /**
         * Spawn a precomputation thread for the specified slice and context.
         *
         * @param slice   the slice
         * @param context the context
         */
        private void spawnPrecomputationThread(Slice slice, Context context) {
            ArchitecturalRegisterFile newRegs = new ArchitecturalRegisterFile(context.getProcess().isLittleEndian());

            for (int liveIn : slice.getLiveIns()) {
                newRegs.copyRegisterFrom(context.getRegisterFile(), liveIn);
            }

            final PrecomputationContext newContext = new PrecomputationContext(dynamicSpeculativePrecomputationHelper, context, newRegs, slice.getTriggerPc());

            if (this.thread.getCore().getProcessor().getKernel().map(newContext, candidateThreadId -> {
                int candidateCoreNum = candidateThreadId / thread.getCore().getProcessor().getExperiment().getNumThreadsPerCore();
                int parentCoreNum = newContext.getParent().getThreadId() / thread.getCore().getProcessor().getExperiment().getNumThreadsPerCore();
                return candidateCoreNum != parentCoreNum;
            })) {
                context.getKernel().getContexts().add(newContext);

                newContext.getRegisterFile().setNpc(FIRST_INSTRUCTION_PC);
                newContext.getRegisterFile().setNnpc(newContext.getRegisterFile().getNpc() + 4);

                slice.setNumSpawnings(slice.getNumSpawnings() + 1);
                slice.setSpawnedThreadContext(newContext);
            } else {
                Logger.infof(Logger.THREAD, "%s: There is no sufficient hardware resource for spawning precomputation thread, kindly ignored.", this.thread.getCycleAccurateEventQueue().getCurrentCycle(), this.thread.getName());
            }
        }

        /**
         * Evaluate the effectiveness of the specified slice.
         *
         * @param slice the slice to be evaluated
         * @return TODO
         */
        private boolean evaluateEffectiveness(Slice slice) {
            if (slice.getNumSavedL2Misses() * AVERAGE_L2_MISS_LATENCY_SAVING > slice.getNumDecodedInstructions()) {
                return true;
            } else if (slice.getSpawnedThreadContext() != null) {
                slice.setIneffective(true);
                return true;
            } else {
                this.prepareRemoveSlice(slice);
                return false;
            }
        }

        /**
         * Prepare to remove the specified slice.
         *
         * @param slice the slice to be removed
         */
        private void prepareRemoveSlice(Slice slice) {
            dynamicSpeculativePrecomputationHelper.getDelinquentLoadIdentificationTables().get(this.thread).removeDelinquentLoad(slice.getDelinquentLoad());

            CacheLine<Boolean> lineFound = dynamicSpeculativePrecomputationHelper.getSliceCache().findLine(slice.getTriggerPc());
            BooleanValueProvider stateProvider = (BooleanValueProvider) lineFound.getStateProvider();
            stateProvider.state = false;
            lineFound.setTag(CacheLine.INVALID_TAG);
            stateProvider.machineInstructions = null;
        }

        private static final int INSTRUCTIONS_PER_PHASE = 128000;
        private static final int AVERAGE_L2_MISS_LATENCY_SAVING = 110;
        private static final int FIRST_INSTRUCTION_PC = 0x1000;
    }

    private static final int SLICE_CACHE_CAPACITY = 32;
}
