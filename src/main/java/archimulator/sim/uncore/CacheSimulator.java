package archimulator.sim.uncore;

import archimulator.sim.base.event.MyBlockingEventDispatcher;
import archimulator.sim.core.ProcessorConfig;
import archimulator.sim.uncore.cache.eviction.LRUPolicy;
import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;
import net.pickapack.DateHelper;
import net.pickapack.action.Action;
import net.pickapack.event.BlockingEvent;
import net.pickapack.event.BlockingEventDispatcher;
import net.pickapack.event.CycleAccurateEventQueue;
import org.apache.commons.io.FileUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class CacheSimulator {
    public static CycleAccurateEventQueue cycleAccurateEventQueue = new CycleAccurateEventQueue();

    private static Random random = new Random();

    private static int numPendingReads = 0;
    private static int numPendingWrites = 0;

    private static int numCompletedReads = 0;
    private static int numCompletedWrites = 0;

//        public static boolean logEnabled = true;
    public static boolean logEnabled = false;

    private static boolean reached;

    private static int numMaxPendingAccesses;

    private static void issueRequests(final CacheController cacheController) {
        if(!reached && (numPendingReads + numPendingWrites > numMaxPendingAccesses)) {
            System.out.printf("[%s] [%s] reached -> numPendingReads: %d, numPendingWrites: %d%n", DateHelper.toString(new Date()), cycleAccurateEventQueue.getCurrentCycle(), numPendingReads, numPendingWrites);
            reached = true;
        }

        if(!reached) {
            read(cacheController, random.nextInt(10000000));
//        }

            write(cacheController, random.nextInt(10000000));
//        }
        }
    }

    private static void read(final CacheController cacheController, int addr) {
        final int tag = cacheController.getCache().getTag(addr);
        if(cacheController.canAccess(MemoryHierarchyAccessType.LOAD, addr)) {
            cacheController.onLoad(null, tag, new Action() {
                @Override
                public void apply() {
                    numPendingReads--;
                    numCompletedReads++;
                }
            });
            numPendingReads++;
        }
    }

    private static void write(final CacheController cacheController, int addr) {
        final int tag = cacheController.getCache().getTag(addr);
        if(cacheController.canAccess(MemoryHierarchyAccessType.STORE, addr)) {
            cacheController.onStore(null, tag, new Action() {
                @Override
                public void apply() {
                    numPendingWrites--;
                    numCompletedWrites++;
                }
            });
            numPendingWrites++;
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

    private static boolean useTrace = false;
//    private static boolean useTrace = true;

    public static PrintWriter pw;

    public static void main(String[] args) throws IOException {
        if(args.length != 1) {
            numMaxPendingAccesses = 300;
        }
        else {
            numMaxPendingAccesses = Integer.parseInt(args[0]);
        }

        System.out.printf("[%s] numMaxPendingAccesses: %d%n", DateHelper.toString(new Date()),numMaxPendingAccesses);

        pw = new PrintWriter(new FileWriter(FileUtils.getUserDirectoryPath() + "/trace.txt"));

        MemoryHierarchyConfig memoryHierarchyConfig = MemoryHierarchyConfig.createDefaultMemoryHierarchyConfig(64, 1, 64, 1, 64, 1, LRUPolicy.class);
        ProcessorConfig processorConfig = ProcessorConfig.createDefaultProcessorConfig(memoryHierarchyConfig, null, null, 8, 2);
        BasicCacheHierarchy cacheHierarchy = new BasicCacheHierarchy(new MyBlockingEventDispatcher<BlockingEvent>(), cycleAccurateEventQueue, processorConfig);

        CacheController l10 = cacheHierarchy.getDataCaches().get(0);
        CacheController l11 = cacheHierarchy.getDataCaches().get(1);

        List<MemoryAccessLine> memoryAccessLines = new ArrayList<MemoryAccessLine>();

        memoryAccessLines.add(new MemoryAccessLine(0, l10, 0x00100000, false));
        memoryAccessLines.add(new MemoryAccessLine(995, l11, 0x00200000, false));
        memoryAccessLines.add(new MemoryAccessLine(1000, l10, 0x00100000, true));
        memoryAccessLines.add(new MemoryAccessLine(1060, l11, 0x00200000, false));
        memoryAccessLines.add(new MemoryAccessLine(1105, l10, 0x00100000, true));
        memoryAccessLines.add(new MemoryAccessLine(1110, l11, 0x00100000, false));
        memoryAccessLines.add(new MemoryAccessLine(1170, l10, 0x00200000, true));

        memoryAccessLines.add(new MemoryAccessLine(1245, l11, 0x00100000, false));
        memoryAccessLines.add(new MemoryAccessLine(1250, l10, 0x00300000, false));

        int maxCycle = 100000000;

        if(useTrace) {
            for(final MemoryAccessLine memoryAccessLine : memoryAccessLines) {
                cycleAccurateEventQueue.schedule(null, new Action() {
                    @Override
                    public void apply() {
                        if(!memoryAccessLine.isWrite()) {
                            read(memoryAccessLine.getL1(), memoryAccessLine.getTag());
                        }
                        else {
                            write(memoryAccessLine.getL1(), memoryAccessLine.getTag());
                        }
                    }
                }, memoryAccessLine.getBeginCycle());
            }
        }

        for (int i = 0; i < maxCycle; i++) {
            cycleAccurateEventQueue.advanceOneCycle();

            if(!useTrace) {
                if(reached && numPendingReads == 0 && numPendingWrites == 0) {
                    System.out.printf("[%s] [%s] completed -> numPendingReads: %d, numPendingWrites: %d%n", DateHelper.toString(new Date()),cycleAccurateEventQueue.getCurrentCycle(), numPendingReads, numPendingWrites);
                    break;
                }

                for(CacheController dataCache : cacheHierarchy.getDataCaches()) {
                    issueRequests(dataCache);
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

        cacheHierarchy.dumpStats();

        if(numPendingReads != 0 || numPendingWrites != 0) {
            System.out.flush();
            throw new IllegalArgumentException();
        }
    }
}
