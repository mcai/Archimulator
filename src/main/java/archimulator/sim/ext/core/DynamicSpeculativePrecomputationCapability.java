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
package archimulator.sim.ext.core;

import archimulator.sim.base.experiment.capability.ProcessorCapability;
import archimulator.sim.base.simulation.Logger;
import archimulator.sim.base.simulation.SimulationObject;
import archimulator.sim.core.Core;
import archimulator.sim.core.Processor;
import archimulator.sim.core.Thread;
import archimulator.sim.core.event.InstructionCommittedEvent;
import archimulator.sim.core.event.InstructionDecodedEvent;
import archimulator.sim.ext.uncore.delinquentLoad.AbstractDelinquentLoadIdentificationTable;
import archimulator.sim.ext.uncore.delinquentLoad.DelinquentLoad;
import archimulator.sim.isa.ArchitecturalRegisterFile;
import archimulator.sim.isa.StaticInstruction;
import archimulator.sim.isa.StaticInstructionType;
import archimulator.sim.os.Context;
import archimulator.sim.os.ContextKilledEvent;
import archimulator.sim.os.ContextState;
import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.cache.*;
import archimulator.sim.uncore.cache.eviction.EvictionPolicy;
import archimulator.sim.uncore.cache.eviction.LRUPolicy;
import archimulator.sim.uncore.coherence.event.CoherentCacheBeginCacheAccessEvent;
import archimulator.util.action.Action1;
import archimulator.util.action.Function3;
import archimulator.util.action.Predicate;

import java.util.*;

public class DynamicSpeculativePrecomputationCapability implements ProcessorCapability {
    private SliceCache sliceCache;

    private Map<archimulator.sim.core.Thread, AbstractDelinquentLoadIdentificationTable> dlits;
    private Map<archimulator.sim.core.Thread, RetiredInstructionBuffer> ribs;
    private Map<Thread, SliceInformationTable> sits;

    public DynamicSpeculativePrecomputationCapability(Processor processor) {
        this.sliceCache = new SliceCache(processor, "sliceCache", new CacheGeometry(SLICE_CACHE_CAPACITY, SLICE_CACHE_CAPACITY, 1), LRUPolicy.class, new Function3<Cache<?, ?>, Integer, Integer, SliceCacheLine>() {
            public SliceCacheLine apply(Cache<?, ?> cache, Integer set, Integer way) {
                return new SliceCacheLine(cache, set, way);
            }
        });

        this.dlits = new HashMap<Thread, AbstractDelinquentLoadIdentificationTable>();
        this.ribs = new HashMap<Thread, RetiredInstructionBuffer>();
        this.sits = new HashMap<Thread, SliceInformationTable>();

        for (Core core : processor.getCores()) {
            for (Thread thread : core.getThreads()) {
                this.dlits.put(thread, new DelinquentLoadIdentificationTable(thread));
                this.ribs.put(thread, new RetiredInstructionBuffer(thread));
                this.sits.put(thread, new SliceInformationTable(thread));
            }
        }
    }

    private class SliceCacheLine extends CacheLine<Boolean> {
        private Map<Integer, Integer> machInsts;

        private SliceCacheLine(Cache<?, ?> cache, int set, int way) {
            super(cache, set, way, false);
        }
    }

    private class SliceCache extends EvictableCache<Boolean, SliceCacheLine> {
        private SliceCache(SimulationObject parent, String name, CacheGeometry geometry, Class<? extends EvictionPolicy> evictionPolicyClz, Function3<Cache<?, ?>, Integer, Integer, SliceCacheLine> createLine) {
            super(parent, name, geometry, evictionPolicyClz, createLine);
        }

        private CacheMiss<Boolean, SliceCacheLine> findInvalidLineAndNewMiss(int address, CacheAccessType accessType, int set) {
            CacheReference reference = new CacheReference(null, null, address, this.getTag(address), accessType, set);

            for (int way = 0; way < this.getAssociativity(); way++) {
                SliceCacheLine line = this.getLine(set, way);
                if (line.getState() == line.getInitialState()) {
                    return new CacheMiss<Boolean, SliceCacheLine>(this, reference, way);
                }
            }

            return null;
        }
    }

    private class DelinquentLoadIdentificationTable extends AbstractDelinquentLoadIdentificationTable {
        private DelinquentLoadIdentificationTable(Thread thread) {
            super(thread);
        }

        @Override
        protected void action(DelinquentLoad delinquentLoad) {
            ribs.get(this.getThread()).gatherInstructionFor(delinquentLoad);
        }
    }

    private enum RetiredInstructionBufferState {
        IDLE,
        INSTRUCTION_GATHERING,
        SLICE_BUILDING
    }

    private class RetiredInstructionBuffer {
        private Thread thread;
        private Stack<RetiredInstruction> retiredInstructions;

        private List<StackAddressTableEntry> stackAddressTable;

        private RetiredInstructionBufferState state;

        private DelinquentLoad delinquentLoad;

        private RetiredInstructionBuffer(Thread thread) {
            this.thread = thread;

            this.retiredInstructions = new Stack<RetiredInstruction>();

            this.stackAddressTable = new ArrayList<StackAddressTableEntry>();

            this.state = RetiredInstructionBufferState.IDLE;

            this.thread.getBlockingEventDispatcher().addListener(InstructionCommittedEvent.class, new Action1<InstructionCommittedEvent>() {
                public void apply(InstructionCommittedEvent event) {
                    if (event.getDynamicInst().getThread() == RetiredInstructionBuffer.this.thread) {
                        if (state == RetiredInstructionBufferState.IDLE && delinquentLoad != null && event.getDynamicInst().getPc() == delinquentLoad.getPc() && !event.getDynamicInst().getThread().getContext().getFunctionCallContextStack().isEmpty() && event.getDynamicInst().getThread().getContext().getFunctionCallContextStack().peek().getPc() == delinquentLoad.getFunctionCallPc()) {
                            state = RetiredInstructionBufferState.INSTRUCTION_GATHERING;

                            retiredInstructions.add(new RetiredInstruction(event.getDynamicInst().getPc(), event.getDynamicInst().isUseStackPointerAsEffectiveAddressBase(), event.getDynamicInst().getEffectiveAddressDisplacement(), event.getDynamicInst().getStaticInst()));
                        } else if (state == RetiredInstructionBufferState.INSTRUCTION_GATHERING) {
                            if (retiredInstructions.size() >= CAPACITY) {
                                retiredInstructions.remove(retiredInstructions.firstElement());
                            }
                            retiredInstructions.add(new RetiredInstruction(event.getDynamicInst().getPc(), event.getDynamicInst().isUseStackPointerAsEffectiveAddressBase(), event.getDynamicInst().getEffectiveAddressDisplacement(), event.getDynamicInst().getStaticInst()));

                            if (event.getDynamicInst().getPc() == delinquentLoad.getPc() && !event.getDynamicInst().getThread().getContext().getFunctionCallContextStack().isEmpty() && event.getDynamicInst().getThread().getContext().getFunctionCallContextStack().peek().getPc() == delinquentLoad.getFunctionCallPc()) {
                                buildSlice();
                            }
                        }
                    }
                }
            });
        }

        private void gatherInstructionFor(DelinquentLoad delinquentLoad) {
            if (this.state == RetiredInstructionBufferState.IDLE) {
                this.delinquentLoad = delinquentLoad;
            }
        }

        private void buildSlice() {
            this.state = RetiredInstructionBufferState.SLICE_BUILDING;

            this.stackAddressTable.clear();

            Slice slice = new Slice(this.delinquentLoad);

            //            for (RetiredInstruction retiredInstruction : this.retiredInstructions) {
            //                System.out.println(retiredInstruction);
            //            }

            slice.liveIns.addAll(this.retiredInstructions.lastElement().staticInst.getIdeps());
            this.mark(this.retiredInstructions.lastElement());

            RetiredInstruction lastAnalyzedRetiredInstruction = null;

            for (Iterator<RetiredInstruction> it = new DescendingIterator<RetiredInstruction>(this.retiredInstructions); it.hasNext(); ) {
                RetiredInstruction retiredInstruction = it.next();

                if (this.checkIfHasNoIgnoredStoreLoadDependence(retiredInstruction)) {
                    for (int odep : retiredInstruction.staticInst.getOdeps()) {
                        if (slice.liveIns.contains(odep)) {
                            this.mark(retiredInstruction);
                            slice.liveIns.removeAll(retiredInstruction.staticInst.getOdeps());
                            slice.liveIns.addAll(retiredInstruction.staticInst.getIdeps());
                            break;
                        }
                    }

                    lastAnalyzedRetiredInstruction = retiredInstruction;
                }
            }

            for (StackAddressTableEntry stackAddressTableEntry : this.stackAddressTable) {
                this.unmark(stackAddressTableEntry.retiredInstruction);
            }

            assert (lastAnalyzedRetiredInstruction != null);

            slice.triggerPc = lastAnalyzedRetiredInstruction.pc;

            for (RetiredInstruction retiredInstruction : this.retiredInstructions) {
                if (retiredInstruction.marked) {
                    slice.pcs.add(retiredInstruction.pc);
                }
            }

            sits.get(this.thread).storeSlice(slice);

            this.retiredInstructions.clear();
            this.state = RetiredInstructionBufferState.IDLE;
            this.delinquentLoad = null;
        }

        private void mark(RetiredInstruction retiredInstruction) {
            retiredInstruction.marked = true;

            if (retiredInstruction.staticInst.getMnemonic().getType() == StaticInstructionType.LOAD && retiredInstruction.useStackPointerAsEffectiveAddressBase) {
                int displacement = retiredInstruction.effectiveAddressDisplacement;
                int nonEffectiveAddressBaseDep = retiredInstruction.staticInst.getNonEffectiveAddressBaseDep();

                this.stackAddressTable.add(new StackAddressTableEntry(retiredInstruction, displacement, nonEffectiveAddressBaseDep));
            }
        }

        private boolean checkIfHasNoIgnoredStoreLoadDependence(RetiredInstruction retiredInstruction) {
            for (Iterator<StackAddressTableEntry> iterator = this.stackAddressTable.iterator(); iterator.hasNext(); ) {
                if (retiredInstruction.staticInst.getOdeps().contains(iterator.next().nonEffectiveAddressBaseDep)) {
                    iterator.remove();
                }
            }

            if (retiredInstruction.staticInst.getMnemonic().getType() == StaticInstructionType.STORE && retiredInstruction.useStackPointerAsEffectiveAddressBase) {
                for (StackAddressTableEntry stackAddressTableEntry : this.stackAddressTable) {
                    if (stackAddressTableEntry.displacement == retiredInstruction.effectiveAddressDisplacement) {
                        return false;
                    }
                }
            }

            return true;
        }

        private void unmark(RetiredInstruction retiredInstruction) {
            retiredInstruction.marked = false;
        }

        private class StackAddressTableEntry {
            private RetiredInstruction retiredInstruction;
            private int displacement;
            private int nonEffectiveAddressBaseDep;

            private StackAddressTableEntry(RetiredInstruction retiredInstruction, int displacement, int nonEffectiveAddressBaseDep) {
                this.retiredInstruction = retiredInstruction;
                this.displacement = displacement;
                this.nonEffectiveAddressBaseDep = nonEffectiveAddressBaseDep;
            }
        }

        private class RetiredInstruction {
            private boolean marked;

            private int pc;
            private boolean useStackPointerAsEffectiveAddressBase;
            private int effectiveAddressDisplacement;
            private StaticInstruction staticInst;

            private RetiredInstruction(int pc, boolean useStackPointerAsEffectiveAddressBase, int effectiveAddressDisplacement, StaticInstruction staticInst) {
                this.marked = false;

                this.pc = pc;
                this.useStackPointerAsEffectiveAddressBase = useStackPointerAsEffectiveAddressBase;
                this.effectiveAddressDisplacement = effectiveAddressDisplacement;
                this.staticInst = staticInst;
            }

            @Override
            public String toString() {
                return String.format("RetiredInstruction{marked=%s, pc=%d, useStackPointerAsEffectiveAddressBase=%s, effectiveAddressDisplacement=%d, staticInst=%s}", marked, pc, useStackPointerAsEffectiveAddressBase, effectiveAddressDisplacement, staticInst);
            }
        }

        private static final int CAPACITY = 512;
    }

    private class Slice {
        private DelinquentLoad delinquentLoad;
        private int triggerPc;
        private Set<Integer> liveIns;
        private List<Integer> pcs;

        private int spawningCount;

        private Context spawnedThreadContext;

        private int decodedInstructionCount;
        private int savedL2Misses;

        private boolean ineffective;

        private Slice(DelinquentLoad delinquentLoad) {
            this.delinquentLoad = delinquentLoad;

            this.triggerPc = 0;
            this.liveIns = new HashSet<Integer>();
            this.pcs = new ArrayList<Integer>();

            this.spawningCount = 0;

            this.decodedInstructionCount = 0;
            this.savedL2Misses = 0;

            this.ineffective = false;
        }
    }

    private class SliceInformationTable {
        private Thread thread;
        private List<Slice> slices;

        private SliceInformationTable(Thread thread) {
            this.thread = thread;

            this.slices = new ArrayList<Slice>();

            this.thread.getBlockingEventDispatcher().addListener(InstructionDecodedEvent.class, new Action1<InstructionDecodedEvent>() {
                public void apply(InstructionDecodedEvent event) {
                    for (Slice slice : slices) {
                        if (event.getDynamicInst().getPc() == slice.triggerPc && slice.spawnedThreadContext == null) {
                            spawnPrecomputationThread(slice, event.getDynamicInst().getThread().getContext());
                            break;
                        }
                    }

                    for (Slice slice : slices) {
                        if (slice.spawnedThreadContext != null && event.getDynamicInst().getThread().getContext() == slice.spawnedThreadContext) {
                            slice.decodedInstructionCount++;
                            break;
                        }
                    }
                }
            });

            this.thread.getBlockingEventDispatcher().addListener(InstructionCommittedEvent.class, new Action1<InstructionCommittedEvent>() {
                public void apply(InstructionCommittedEvent event) {
                    if (SliceInformationTable.this.thread.getTotalInsts() % INSTRUCTIONS_PER_PHASE == 0) {
                        for (Iterator<Slice> it = slices.iterator(); it.hasNext(); ) {
                            Slice slice = it.next();
                            if (slice.spawningCount > 0) {
                                if (!evaluateEffectiveness(slice)) {
                                    it.remove();
                                }
                            }
                        }
                    }
                }
            });

            this.thread.getBlockingEventDispatcher().addListener(ContextKilledEvent.class, new Action1<ContextKilledEvent>() {
                public void apply(ContextKilledEvent event) {
                    for (Iterator<Slice> iterator = slices.iterator(); iterator.hasNext(); ) {
                        Slice slice = iterator.next();
                        if (event.getContext() == slice.spawnedThreadContext) {
                            slice.spawnedThreadContext = null;

                            if (slice.ineffective) {
                                prepareRemoveSlice(slice);
                                iterator.remove();
                            }

                            break;
                        }
                    }
                }
            });

            this.thread.getBlockingEventDispatcher().addListener(CoherentCacheBeginCacheAccessEvent.class, new Action1<CoherentCacheBeginCacheAccessEvent>() {
                public void apply(CoherentCacheBeginCacheAccessEvent event) {
                    if (!event.getCacheAccess().isHitInCache() && event.getCache().isLastLevelCache() && event.getCacheAccess().getReference().getAccessType().isDownwardRead()) {
                        for (Slice slice : slices) {
                            if (slice.spawnedThreadContext != null) {
                                if (((Thread) event.getAccess().getThread()).getContext() == slice.spawnedThreadContext) {
                                    slice.savedL2Misses++;
                                    break;
                                }
                            }
                        }
                    }
                }
            });
        }

        private void storeSlice(Slice slice) {
            assert (slice.pcs.size() <= 64); //TODO: how to support longer pcs? e.g., slice chains?

            int set = sliceCache.getSet(slice.triggerPc);

            CacheMiss<Boolean, SliceCacheLine> miss = sliceCache.findInvalidLineAndNewMiss(slice.triggerPc, CacheAccessType.UNKNOWN, set);

            if (miss != null) {
                miss.getLine().setNonInitialState(true);
                miss.commit();

                Map<Integer, Integer> machInsts = new TreeMap<Integer, Integer>();

                int i = FIRST_INSTRUCTION_PC;

                for (int pc : slice.pcs) {
                    machInsts.put(i, pc); //TODO: trick for the moment: store mappedPc instead of machInst to use predecoding
                    i += 4;
                }

                miss.getLine().machInsts = machInsts;

                slices.add(slice);
            } else {
                Logger.infof(Logger.THREAD, "%s: There is no sufficient hardware resource for storing slice, kindly ignored.", this.thread.getCycleAccurateEventQueue().getCurrentCycle(), this.thread.getName());
            }
        }

        private void spawnPrecomputationThread(Slice slice, Context context) {
            ArchitecturalRegisterFile newRegs = new ArchitecturalRegisterFile(context.getProcess().isLittleEndian());

            for (int liveIn : slice.liveIns) {
                newRegs.copyRegFrom(context.getRegs(), liveIn);
            }

            final PrecomputationThreadContext newContext = new PrecomputationThreadContext(context, newRegs, slice.triggerPc);

            if (this.thread.getCore().getProcessor().getKernel().map(newContext, new Predicate<Integer>() {
                public boolean apply(Integer candidateThreadId) {
                    int candidateCoreNum = candidateThreadId / thread.getCore().getProcessor().getConfig().getNumThreadsPerCore();
                    int parentCoreNum = newContext.getParent().getThreadId() / thread.getCore().getProcessor().getConfig().getNumThreadsPerCore();
                    return candidateCoreNum != parentCoreNum;
                }
            })) {
                context.getKernel().getContexts().add(newContext);

                newContext.getRegs().setNpc(FIRST_INSTRUCTION_PC);
                newContext.getRegs().setNnpc(newContext.getRegs().getNpc() + 4);

                slice.spawningCount++;
                slice.spawnedThreadContext = newContext;
            } else {
                Logger.infof(Logger.THREAD, "%s: There is no sufficient hardware resource for spawning precomputation thread, kindly ignored.", this.thread.getCycleAccurateEventQueue().getCurrentCycle(), this.thread.getName());
            }
        }

        private boolean evaluateEffectiveness(Slice slice) {
            if (slice.savedL2Misses * AVERAGE_L2_MISS_LATENCY_SAVING > slice.decodedInstructionCount) {
                return true;
            } else if (slice.spawnedThreadContext != null) {
                slice.ineffective = true;
                return true;
            } else {
                this.prepareRemoveSlice(slice);

                return false;
            }
        }

        private void prepareRemoveSlice(Slice slice) {
            dlits.get(this.thread).removeDelinquentLoad(slice.delinquentLoad);

            SliceCacheLine lineFound = sliceCache.findLine(slice.triggerPc).getLine();
            assert (lineFound != null);
            lineFound.invalidate();
            lineFound.machInsts = null;
        }

        private static final int INSTRUCTIONS_PER_PHASE = 128000;
        private static final int AVERAGE_L2_MISS_LATENCY_SAVING = 110;
        private static final int FIRST_INSTRUCTION_PC = 0x1000;
    }

    private class PrecomputationThreadContext extends Context {
        private int triggerPc;

        private PrecomputationThreadContext(Context parent, ArchitecturalRegisterFile regs, int triggerPc) {
            super(parent, regs, 0);

            this.triggerPc = triggerPc;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected StaticInstruction decode(int pc) {
            SliceCacheLine lineFound = sliceCache.findLine(this.triggerPc).getLine();
            assert (lineFound != null && lineFound.machInsts != null);

            if (lineFound.machInsts.containsKey(pc)) {
                return super.decode(lineFound.machInsts.get(pc));
            } else {
                if (this.getState() != ContextState.FINISHED) {
                    this.finish();
                }
                return StaticInstruction.NOP;
            }
        }

        @Override
        public boolean useICache() {
            return false;
        }
    }

    public static class DescendingIterator<T> implements Iterator<T> {
        private List<T> list;
        private int current;

        public DescendingIterator(List<T> list) {
            this.list = list;
            current = list.size() - 1;
        }

        public boolean hasNext() {
            return current >= 0;
        }

        public T next() {
            return list.get(current--);
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private static final int SLICE_CACHE_CAPACITY = 32;
}
