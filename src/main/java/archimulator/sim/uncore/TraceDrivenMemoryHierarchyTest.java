///*******************************************************************************
//* Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
//*
//* This file is part of the Archimulator multicore architectural simulator.
//*
//* Archimulator is free software: you can redistribute it and/or modify
//* it under the terms of the GNU General Public License as published by
//* the Free Software Foundation, either version 3 of the License, or
//* (at your option) any later version.
//*
//* Archimulator is distributed in the hope that it will be useful,
//* but WITHOUT ANY WARRANTY; without even the implied warranty of
//* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//* GNU General Public License for more details.
//*
//* You should have received a copy of the GNU General Public License
//* along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
//******************************************************************************/
//package archimulator.sim.uncore;
//
//import archimulator.sim.uncore.cache.CacheGeometry;
//import archimulator.sim.uncore.cache.eviction.LRUPolicy;
//import archimulator.sim.uncore.coherence.common.FirstLevelCache;
//import archimulator.sim.uncore.coherence.common.LastLevelCache;
//import archimulator.sim.uncore.dram.*;
//import archimulator.sim.uncore.tlb.TranslationLookasideBuffer;
//import archimulator.sim.uncore.tlb.TranslationLookasideBufferConfig;
//import archimulator.sim.base.simulation.BasicSimulationObject;
//import archimulator.sim.base.simulation.Logger;
//import net.pickapack.action.Action;
//import net.pickapack.event.BlockingEvent;
//import net.pickapack.event.BlockingEventDispatcher;
//import net.pickapack.event.CycleAccurateEventQueue;
//import net.pickapack.io.file.IterableBigTextFile;
//import net.pickapack.math.Counter;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class TraceDrivenMemoryHierarchyTest implements CacheHierarchy {
//    private String title;
//    private List<TestMemoryHierarchyCore> cores;
//
//    private BlockingEventDispatcher<BlockingEvent> blockingEventDispatcher;
//
//    private CycleAccurateEventQueue cycleAccurateEventQueue;
//
//    private MainMemory mainMemory;
//    private LastLevelCache l2Cache;
//
//    public TraceDrivenMemoryHierarchyTest(String title, MemoryHierarchyConfig memoryHierarchyConfig, int numCores, int numThreadsPerCore) {
//        this.blockingEventDispatcher = new BlockingEventDispatcher<BlockingEvent>();
//        this.cycleAccurateEventQueue = new CycleAccurateEventQueue();
//
//        this.title = title;
//
//        this.cores = new ArrayList<TestMemoryHierarchyCore>();
//
//        switch (memoryHierarchyConfig.getMainMemory().getType()) {
//            case SIMPLE:
//                this.mainMemory = new SimpleMainMemory(this, (SimpleMainMemoryConfig) memoryHierarchyConfig.getMainMemory());
//                break;
//            case BASIC:
//                this.mainMemory = new BasicMainMemory(this, (BasicMainMemoryConfig) memoryHierarchyConfig.getMainMemory());
//                break;
//            default:
//                this.mainMemory = new FixedLatencyMainMemory(this, (FixedLatencyMainMemoryConfig) memoryHierarchyConfig.getMainMemory());
//                break;
//        }
//
//        this.l2Cache = new LastLevelCache(this, "llc", memoryHierarchyConfig.getL2Cache());
//        this.l2Cache.setNext(this.mainMemory);
//
//        CacheGeometry tlbGeometry = new CacheGeometry(32768, 4, 64);
//
//        TranslationLookasideBufferConfig tlbConfig = new TranslationLookasideBufferConfig(tlbGeometry, 2, 30);
//
//        for (int i = 0; i < numCores; i++) {
//            TestMemoryHierarchyCore core = new TestMemoryHierarchyCore(i);
//
//            for (int j = 0; j < numThreadsPerCore; j++) {
//                TestMemoryHierarchyThread thread = new TestMemoryHierarchyThread(core, j, numThreadsPerCore, tlbConfig);
//                core.getThreads().add(thread);
//            }
//
//            FirstLevelCache instructionCache = new FirstLevelCache(this, core.getName() + ".icache", memoryHierarchyConfig.getInstructionCache());
//            instructionCache.setNext(this.l2Cache);
//            this.l2Cache.addShadowTagDirectoryForL1(instructionCache);
//            core.setInstructionCache(instructionCache);
//
//            FirstLevelCache dataCache = new FirstLevelCache(this, core.getName() + ".dcache", memoryHierarchyConfig.getDataCache());
//            dataCache.setNext(this.l2Cache);
//            this.l2Cache.addShadowTagDirectoryForL1(dataCache);
//            core.setDataCache(dataCache);
//
//            this.cores.add(core);
//        }
//    }
//
//    private boolean canIssueAccess(MemoryAccessTraceLine line) {
//        int coreNum = line.getThreadId() / this.cores.size();
//        int threadNum = line.getThreadId() % this.cores.size();
//
//        TestMemoryHierarchyThread thread = this.cores.get(coreNum).getThreads().get(threadNum);
//
//        switch (line.getType()) {
//            case IFETCH:
//                return thread.getCore().canIfetch(line.getPhysicalAddress());
//            case LOAD:
//                return thread.getCore().canLoad(line.getPhysicalAddress());
//            case STORE:
//                return thread.getCore().canStore(line.getPhysicalAddress());
//            default:
//                throw new IllegalArgumentException();
//        }
//    }
//
//    private void issueAccess(final MemoryAccessTraceLine line, final Counter counterAccessIssued, final Counter counterAccessCompleted) {
//        int coreNum = line.getThreadId() / this.cores.size();
//        int threadNum = line.getThreadId() % this.cores.size();
//
//        TestMemoryHierarchyThread thread = this.cores.get(coreNum).getThreads().get(threadNum);
//
//        line.setBeginCycle(this.getCycleAccurateEventQueue().getCurrentCycle());
//
//        switch (line.getType()) {
//            case IFETCH:
//                this.printBeginDetail(line);
//                thread.getCore().ifetch(thread, line.getPhysicalAddress(), line.getVirtualPc(), new Action() {
//                    public void apply() {
//                        counterAccessCompleted.inc();
//
//                        line.setEndCycle(getCycleAccurateEventQueue().getCurrentCycle());
//
//                        printEndDetail(line);
//                    }
//                });
//                counterAccessIssued.inc();
//                break;
//            case LOAD:
//                this.printBeginDetail(line);
//                thread.getCore().load(thread, line.getPhysicalAddress(), line.getVirtualPc(), new Action() {
//                    public void apply() {
//                        counterAccessCompleted.inc();
//
//                        line.setEndCycle(getCycleAccurateEventQueue().getCurrentCycle());
//
//                        printEndDetail(line);
//                    }
//                });
//                counterAccessIssued.inc();
//                break;
//            case STORE:
//                this.printBeginDetail(line);
//                thread.getCore().store(thread, line.getPhysicalAddress(), line.getVirtualPc(), new Action() {
//                    public void apply() {
//                        counterAccessCompleted.inc();
//
//                        line.setEndCycle(getCycleAccurateEventQueue().getCurrentCycle());
//
//                        printEndDetail(line);
//                    }
//                });
//                counterAccessIssued.inc();
//                break;
//            default:
//                throw new IllegalArgumentException();
//        }
//    }
//
//    private void printBeginDetail(MemoryAccessTraceLine line) {
//        System.out.printf("\n%d: BEGIN %s\n", this.getCycleAccurateEventQueue().getCurrentCycle(), line);
//    }
//
//    private void printEndDetail(MemoryAccessTraceLine line) {
//        System.out.printf("%d: END %s, cycles=%d\n", this.getCycleAccurateEventQueue().getCurrentCycle(), line, line.getCycles());
//    }
//
//    public void run(String filePath) {
//        System.out.println(this.title);
//
//        final Counter counterAccessIssued = new Counter(0);
//        final Counter counterAccessCompleted = new Counter(0);
//
//        IterableBigTextFile file = new IterableBigTextFile(filePath);
//
//        for (String rawLine : file) {
//            if (rawLine.trim().length() > 0) {
//                MemoryAccessTraceLine line = this.parse(rawLine.trim());
//
//                while (!this.canIssueAccess(line)) {
//                    this.getCycleAccurateEventQueue().advanceOneCycle();
//                }
//
//                this.issueAccess(line, counterAccessIssued, counterAccessCompleted);
//
//                while (counterAccessIssued.getValue() != counterAccessCompleted.getValue()) {
//                    this.getCycleAccurateEventQueue().advanceOneCycle();
//                }
//            }
//        }
//
//        file.close();
//
//        for (int i = 0; i < 100000; i++) {
//            if (counterAccessIssued.getValue() == counterAccessCompleted.getValue()) {
//                break;
//            }
//
//            this.getCycleAccurateEventQueue().advanceOneCycle();
//        }
//
//        assert (counterAccessIssued.getValue() == counterAccessCompleted.getValue());
//
//        System.out.printf("\n%d: %d accesses issued and completed\n", this.getCycleAccurateEventQueue().getCurrentCycle(), counterAccessIssued.getValue());
//    }
//
//    private MemoryAccessTraceLine parse(String line) {
//        String[] fields = line.split(" ");
//
//        assert (fields.length == 4);
//
//        int threadId = Integer.parseInt(fields[0]);
//        long virtualPc = Long.decode(fields[1]);
//        long physicalAddress = Long.decode(fields[2]);
//        MemoryHierarchyAccessType type = Enum.valueOf(MemoryHierarchyAccessType.class, fields[3]);
//
//        return new MemoryAccessTraceLine(threadId, (int) virtualPc, (int) physicalAddress, type);
//    }
//
//    public BlockingEventDispatcher<BlockingEvent> getBlockingEventDispatcher() {
//        return this.blockingEventDispatcher;
//    }
//
//    public CycleAccurateEventQueue getCycleAccurateEventQueue() {
//        return this.cycleAccurateEventQueue;
//    }
//
//    public MainMemory getMainMemory() {
//        return mainMemory;
//    }
//
//    public LastLevelCache getL2Cache() {
//        return l2Cache;
//    }
//
//    private class TestMemoryHierarchyThread extends BasicSimulationObject implements MemoryHierarchyThread {
//        private TestMemoryHierarchyCore core;
//        private int num;
//
//        private int numThreadsPerCore;
//
//        private TranslationLookasideBuffer itlb;
//        private TranslationLookasideBuffer dtlb;
//
//        public TestMemoryHierarchyThread(TestMemoryHierarchyCore core, int num, int numThreadsPerCore, TranslationLookasideBufferConfig tlbConfig) {
//            super(core);
//
//            this.core = core;
//            this.num = num;
//
//            this.numThreadsPerCore = numThreadsPerCore;
//
//            this.setItlb(new TranslationLookasideBuffer(TraceDrivenMemoryHierarchyTest.this, this.getName() + ".itlb", tlbConfig));
//            this.setDtlb(new TranslationLookasideBuffer(TraceDrivenMemoryHierarchyTest.this, this.getName() + ".dtlb", tlbConfig));
//        }
//
//        public int getNum() {
//            return num;
//        }
//
//        public int getId() {
//            return this.core.getNum() * this.numThreadsPerCore + this.num;
//        }
//
//        public String getName() {
//            return "c" + this.core.getNum() + "t" + this.num;
//        }
//
//        public TranslationLookasideBuffer getItlb() {
//            return itlb;
//        }
//
//        public TranslationLookasideBuffer getDtlb() {
//            return dtlb;
//        }
//
//        public void setItlb(TranslationLookasideBuffer itlb) {
//            this.itlb = itlb;
//        }
//
//        public void setDtlb(TranslationLookasideBuffer dtlb) {
//            this.dtlb = dtlb;
//        }
//
//        public TestMemoryHierarchyCore getCore() {
//            return core;
//        }
//    }
//
//    private class TestMemoryHierarchyCore extends BasicSimulationObject implements MemoryHierarchyCore {
//        private int num;
//        private List<TestMemoryHierarchyThread> threads;
//
//        private FirstLevelCache instructionCache;
//        private FirstLevelCache dataCache;
//
//        public TestMemoryHierarchyCore(int num) {
//            super(TraceDrivenMemoryHierarchyTest.this);
//
//            this.num = num;
//            this.threads = new ArrayList<TestMemoryHierarchyThread>();
//        }
//
//        public boolean canIfetch(int addr) {
//            return this.instructionCache.canAccess(MemoryHierarchyAccessType.IFETCH, this.instructionCache.getCache().getTag(addr));
//        }
//
//        public boolean canLoad(int addr) {
//            return this.dataCache.canAccess(MemoryHierarchyAccessType.LOAD, this.dataCache.getCache().getTag(addr));
//        }
//
//        public boolean canStore(int addr) {
//            return this.dataCache.canAccess(MemoryHierarchyAccessType.STORE, this.dataCache.getCache().getTag(addr));
//        }
//
//        public void ifetch(TestMemoryHierarchyThread thread, final int addr, int pc, final Action onCompletedCallback) {
//            int lineAddress = this.instructionCache.getCache().getTag(addr);
//
//            final Counter counterPending = new Counter(0);
//
//            counterPending.inc();
//
//            MemoryHierarchyAccess alias = this.instructionCache.findAccess(lineAddress);
//            MemoryHierarchyAccess access = this.instructionCache.beginAccess(null, thread, MemoryHierarchyAccessType.IFETCH, pc, addr, lineAddress, new Action() {
//                public void apply() {
//                    counterPending.dec();
//
//                    if (counterPending.getValue() == 0) {
//                        onCompletedCallback.apply();
//                    }
//                }
//            });
//
//            if (alias == null) {
//                counterPending.inc();
//
//                thread.getItlb().access(access, new Action() {
//                    public void apply() {
//                        counterPending.dec();
//
//                        if (counterPending.getValue() == 0) {
//                            onCompletedCallback.apply();
//                        }
//                    }
//                });
//
//                this.instructionCache.receiveIfetch(access, new Action() {
//                    public void apply() {
//                        instructionCache.endAccess(instructionCache.getCache().getTag(addr));
//                    }
//                });
//            }
//        }
//
//        public void load(TestMemoryHierarchyThread thread, final int addr, int pc, final Action onCompletedCallback) {
//            int lineAddress = this.dataCache.getCache().getTag(addr);
//
//            final Counter counterPending = new Counter(0);
//
//            counterPending.inc();
//
//            MemoryHierarchyAccess alias = this.dataCache.findAccess(lineAddress);
//            MemoryHierarchyAccess access = this.dataCache.beginAccess(null, thread, MemoryHierarchyAccessType.LOAD, pc, addr, lineAddress, new Action() {
//                public void apply() {
//                    counterPending.dec();
//
//                    if (counterPending.getValue() == 0) {
//                        onCompletedCallback.apply();
//                    }
//                }
//            });
//
//            if (alias == null) {
//                counterPending.inc();
//
//                thread.getDtlb().access(access, new Action() {
//                    public void apply() {
//                        counterPending.dec();
//
//                        if (counterPending.getValue() == 0) {
//                            onCompletedCallback.apply();
//                        }
//                    }
//                });
//
//                this.dataCache.receiveLoad(access, new Action() {
//                    public void apply() {
//                        dataCache.endAccess(dataCache.getCache().getTag(addr));
//                    }
//                });
//            }
//        }
//
//        public void store(TestMemoryHierarchyThread thread, final int addr, int pc, final Action onCompletedCallback) {
//            int lineAddress = this.dataCache.getCache().getTag(addr);
//
//            final Counter counterPending = new Counter(0);
//
//            counterPending.inc();
//
//            MemoryHierarchyAccess alias = this.dataCache.findAccess(lineAddress);
//            MemoryHierarchyAccess access = this.dataCache.beginAccess(null, thread, MemoryHierarchyAccessType.STORE, pc, addr, lineAddress, new Action() {
//                public void apply() {
//                    counterPending.dec();
//
//                    if (counterPending.getValue() == 0) {
//                        onCompletedCallback.apply();
//                    }
//                }
//            });
//
//            if (alias == null) {
//                counterPending.inc();
//
//                thread.getDtlb().access(access, new Action() {
//                    public void apply() {
//                        counterPending.dec();
//
//                        if (counterPending.getValue() == 0) {
//                            onCompletedCallback.apply();
//                        }
//                    }
//                });
//
//                this.dataCache.receiveStore(access, new Action() {
//                    public void apply() {
//                        dataCache.endAccess(dataCache.getCache().getTag(addr));
//                    }
//                });
//            }
//        }
//
//        public int getNum() {
//            return this.num;
//        }
//
//        public List<TestMemoryHierarchyThread> getThreads() {
//            return threads;
//        }
//
//        public FirstLevelCache getInstructionCache() {
//            return instructionCache;
//        }
//
//        public void setInstructionCache(FirstLevelCache instructionCache) {
//            this.instructionCache = instructionCache;
//        }
//
//        public FirstLevelCache getDataCache() {
//            return dataCache;
//        }
//
//        public void setDataCache(FirstLevelCache dataCache) {
//            this.dataCache = dataCache;
//        }
//
//        public String getName() {
//            return "c" + this.getNum();
//        }
//    }
//
//    public static void main(String[] args) {
//        String filePath = args.length == 0 ? "traces/little.trace" : args[0];
//        new TraceDrivenMemoryHierarchyTest("lru", MemoryHierarchyConfig.createDefaultMemoryHierarchyConfig(LRUPolicy.FACTORY), 2, 2).run(filePath);
//    }
//}
