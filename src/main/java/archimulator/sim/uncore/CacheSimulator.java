package archimulator.sim.uncore;

import archimulator.sim.base.event.SimulationEvent;
import archimulator.sim.core.ProcessorConfig;
import archimulator.sim.uncore.cache.eviction.LRUPolicy;
import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;
import archimulator.sim.uncore.ht.HTLLCRequestProfilingCapability;
import archimulator.sim.uncore.tlb.TranslationLookasideBuffer;
import net.pickapack.DateHelper;
import net.pickapack.action.Action;
import net.pickapack.action.UntypedPredicate;
import net.pickapack.event.BlockingEventDispatcher;
import net.pickapack.event.CycleAccurateEventQueue;
import org.apache.commons.io.FileUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class CacheSimulator {
    public static CycleAccurateEventQueue cycleAccurateEventQueue = new CycleAccurateEventQueue();

    private static Random random = new Random();

    private static int numPendingReads = 0;
    private static int numPendingWrites = 0;

    private static int numCompletedReads = 0;
    private static int numCompletedWrites = 0;

//        public static boolean logEnabled = true;
    public static boolean logEnabled = false;

    //    public static boolean logSameState = false;
    public static boolean logSameState = true;

//    public static boolean recordTransitionHistory = true;
    public static boolean recordTransitionHistory = false;

    //    private static boolean useTrace = false;
    private static boolean useTrace = true;

    private static boolean reached;

    private static int numMaxPendingAccesses;

    private static void issueRequests(int threadId, final CacheController cacheController) {
        if(!reached && (numPendingReads + numPendingWrites > numMaxPendingAccesses)) {
            System.out.printf("[%s] [%s] reached -> numPendingReads: %d, numPendingWrites: %d%n", DateHelper.toString(new Date()), cycleAccurateEventQueue.getCurrentCycle(), numPendingReads, numPendingWrites);
            reached = true;
        }

        if(!reached) {
            read(threadId, cacheController, random.nextInt(10000000));
//        }

            write(threadId, cacheController, random.nextInt(10000000));
//        }
        }
    }

    private static void read(int threadId, final CacheController cacheController, int addr) {
        final int tag = cacheController.getCache().getTag(addr);
        if(cacheController.canAccess(MemoryHierarchyAccessType.LOAD, addr)) {
            cacheController.onLoad(new MemoryHierarchyAccess(null, new MockThread(threadId), MemoryHierarchyAccessType.LOAD, 0, addr, tag, null, cycleAccurateEventQueue.getCurrentCycle()), tag, new Action() {
                @Override
                public void apply() {
                    numPendingReads--;
                    numCompletedReads++;
                }
            });
            numPendingReads++;
        }
    }

    private static void write(int threadId, final CacheController cacheController, int addr) {
        final int tag = cacheController.getCache().getTag(addr);
        if(cacheController.canAccess(MemoryHierarchyAccessType.STORE, addr)) {
            cacheController.onStore(new MemoryHierarchyAccess(null, new MockThread(threadId), MemoryHierarchyAccessType.STORE, 0, addr, tag, null, cycleAccurateEventQueue.getCurrentCycle()), tag, new Action() {
                @Override
                public void apply() {
                    numPendingWrites--;
                    numCompletedWrites++;
                }
            });
            numPendingWrites++;
        }
    }

    public static class CacheAssertionLine {
        private int beginCycle;
        private UntypedPredicate pred;

        public CacheAssertionLine(int beginCycle, UntypedPredicate pred) {
            this.beginCycle = beginCycle;
            this.pred = pred;
        }

        public int getBeginCycle() {
            return beginCycle;
        }

        public UntypedPredicate getPred() {
            return pred;
        }
    }

    public static class MemoryAccessLine {
        private int beginCycle;
        private CacheController l1;
        private int tag;
        private boolean write;

        public MemoryAccessLine(int beginCycle, CacheController l1, int tag, boolean write) {
            this.beginCycle = beginCycle;
            this.l1 = l1;
            this.tag = tag;
            this.write = write;
        }

        public int getBeginCycle() {
            return beginCycle;
        }

        public CacheController getL1() {
            return l1;
        }

        public int getTag() {
            return tag;
        }

        public boolean isWrite() {
            return write;
        }
    }

    public static PrintWriter pw;

    static {
        try {
            pw = new PrintWriter(new FileWriter(FileUtils.getUserDirectoryPath() + "/trace.txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        if(args.length != 1) {
            numMaxPendingAccesses = 300;
        }
        else {
            numMaxPendingAccesses = Integer.parseInt(args[0]);
        }

        System.out.printf("[%s] numMaxPendingAccesses: %d%n", DateHelper.toString(new Date()),numMaxPendingAccesses);

        MemoryHierarchyConfig memoryHierarchyConfig = MemoryHierarchyConfig.createDefaultMemoryHierarchyConfig(64, 1, 64, 1, 64, 1, LRUPolicy.class);
        ProcessorConfig processorConfig = ProcessorConfig.createDefaultProcessorConfig(memoryHierarchyConfig, 2, 2);
        BasicCacheHierarchy cacheHierarchy = new BasicCacheHierarchy(new BlockingEventDispatcher<SimulationEvent>(), cycleAccurateEventQueue, processorConfig);

        HTLLCRequestProfilingCapability htllcRequestProfilingCapability = new HTLLCRequestProfilingCapability(cacheHierarchy.getL2CacheController());

        final DirectoryController l2 = cacheHierarchy.getL2CacheController();
        final CacheController mt = cacheHierarchy.getL1DCacheControllers().get(0);
        final CacheController ht = cacheHierarchy.getL1DCacheControllers().get(1);

        final Map<CacheController, Integer> cacheControllerToThreadIdMap = new HashMap<CacheController, Integer>();
        cacheControllerToThreadIdMap.put(mt, 0);
        cacheControllerToThreadIdMap.put(ht, 2);

        List<CacheAssertionLine> cacheAssertionLines = new ArrayList<CacheAssertionLine>();
        List<MemoryAccessLine> memoryAccessLines = new ArrayList<MemoryAccessLine>();

//        cacheAssertionLines.add(new CacheAssertionLine(5000, new UntypedPredicate() {
//            @Override
//            public boolean apply() {
//                return mt.getCache().findLine(0x00200000) == null && mt.getCache().findLine(0x00300000) == null && ht.getCache().findLine(0x00300000) != null && l2.getCache().findLine(0x00300000) != null;
//            }
//        }));

        int tag1 = 0x00100000;
        int tag2 = 0x00200000;
        int tag3 = 0x00300000;

//        memoryAccessLines.add(new MemoryAccessLine(0, ht, tag1, false));
        memoryAccessLines.add(new MemoryAccessLine(5, mt, tag1, false));

        memoryAccessLines.add(new MemoryAccessLine(2000, ht, tag2, false));

        memoryAccessLines.add(new MemoryAccessLine(3000, mt, tag1, false));

//        memoryAccessLines.add(new MemoryAccessLine(4000, mt, tag3, false));
//        memoryAccessLines.add(new MemoryAccessLine(4050, ht, tag3, false));

        int maxCycle = 100000000;

        if(useTrace) {
            for(final MemoryAccessLine memoryAccessLine : memoryAccessLines) {
                cycleAccurateEventQueue.schedule(memoryAccessLine, new Action() {
                    @Override
                    public void apply() {
                        if(!memoryAccessLine.isWrite()) {
                            read(cacheControllerToThreadIdMap.get(memoryAccessLine.getL1()), memoryAccessLine.getL1(), memoryAccessLine.getTag());
                        }
                        else {
                            write(cacheControllerToThreadIdMap.get(memoryAccessLine.getL1()), memoryAccessLine.getL1(), memoryAccessLine.getTag());
                        }
                    }
                }, memoryAccessLine.getBeginCycle());
            }

            for(final CacheAssertionLine cacheAssertionLine : cacheAssertionLines) {
                cycleAccurateEventQueue.schedule(cacheAssertionLine, new Action() {
                    @Override
                    public void apply() {
                        if(!cacheAssertionLine.getPred().apply()) {
                            throw new IllegalArgumentException();
                        }
                    }
                }, cacheAssertionLine.getBeginCycle());
            }
        }

        for (int i = 0; i < maxCycle; i++) {
            cycleAccurateEventQueue.advanceOneCycle();

            if(!useTrace) {
                if(reached && numPendingReads == 0 && numPendingWrites == 0) {
                    System.out.printf("[%s] [%s] completed -> numPendingReads: %d, numPendingWrites: %d%n", DateHelper.toString(new Date()),cycleAccurateEventQueue.getCurrentCycle(), numPendingReads, numPendingWrites);
                    break;
                }

                for(CacheController l1DCacheController : cacheHierarchy.getL1DCacheControllers()) {
                    issueRequests(cacheControllerToThreadIdMap.get(l1DCacheController), l1DCacheController);
                }

                if(!reached) {
                    if(cycleAccurateEventQueue.getCurrentCycle() % 1000 == 0) {
                        System.out.printf("[%s] [%s] pending -> numPendingReads: %d, numPendingWrites: %d, numCompletedReads: %d, numCompletedWrites: %d%n", DateHelper.toString(new Date()),cycleAccurateEventQueue.getCurrentCycle(), numPendingReads, numPendingWrites, numCompletedReads, numCompletedWrites);
                    }
                }

                if (reached) {
                    if(cycleAccurateEventQueue.getCurrentCycle() % 1000 == 0) {
                        System.out.printf("[%s] [%s] reached -> numPendingReads: %d, numPendingWrites: %d%n", DateHelper.toString(new Date()),cycleAccurateEventQueue.getCurrentCycle(), numPendingReads, numPendingWrites);
                    }
                }
            }
        }

        for (int i = 0; i < 10000; i++) {
            if(numPendingReads == 0 && numPendingWrites == 0) {
                break;
            }

            cycleAccurateEventQueue.advanceOneCycle();
        }

        System.out.printf("[%s] ended -> numPendingReads: %d, numPendingWrites: %d%n%n", DateHelper.toString(new Date()),numPendingReads, numPendingWrites);

        pw.close();

        CacheCoherenceFlow.dumpTree();

        cacheHierarchy.dumpCacheControllerFsmStats();

//        htllcRequestProfilingCapability.dumpCacheControllerFsmStats(); //TODO

        if(numPendingReads != 0 || numPendingWrites != 0) {
            System.out.flush();
            throw new IllegalArgumentException();
        }
    }

    private static class MockThread implements MemoryHierarchyThread {
        private int threadId;

        public MockThread(int threadId) {
            this.threadId = threadId;
        }

        @Override
        public int getNum() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getId() {
            return threadId;
        }

        @Override
        public String getName() {
            return "T" + threadId + "";
        }

        @Override
        public TranslationLookasideBuffer getItlb() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setItlb(TranslationLookasideBuffer itlb) {
            throw new UnsupportedOperationException();
        }

        @Override
        public TranslationLookasideBuffer getDtlb() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setDtlb(TranslationLookasideBuffer dtlb) {
            throw new UnsupportedOperationException();
        }

        @Override
        public MemoryHierarchyCore getCore() {
            throw new UnsupportedOperationException();
        }

        @Override
        public BlockingEventDispatcher<SimulationEvent> getBlockingEventDispatcher() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CycleAccurateEventQueue getCycleAccurateEventQueue() {
            throw new UnsupportedOperationException();
        }
    }
}
