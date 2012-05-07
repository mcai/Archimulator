package archimulator.util.event.future.new2;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CycleAccurateEventQueue2Test {
    public static void main(String[] args) {
        final long maxCycles = 1000000000;

        EventBus channel = new EventBus();

        MemoryHierarchyThread memoryHierarchyThread = new MemoryHierarchyThread(channel);
        CPUThread cpuThread = new CPUThread(channel, memoryHierarchyThread);
        CycleAccurateEventQueue2Thread cycleAccurateEventQueue2Thread = new CycleAccurateEventQueue2Thread(maxCycles, channel, cpuThread, memoryHierarchyThread);

        memoryHierarchyThread.setCycleAccurateEventQueue2Thread(cycleAccurateEventQueue2Thread);

        channel.register(memoryHierarchyThread);
        channel.register(cpuThread);
        channel.register(cycleAccurateEventQueue2Thread);

        cycleAccurateEventQueue2Thread.start();
    }
}
