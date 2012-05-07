package archimulator.util.event.future.new2;

import archimulator.util.action.Action;
import archimulator.util.event.AwaitHandleInterface;
import archimulator.util.event.CycleAccurateEvent;
import archimulator.util.event.future.CycleAccurateEventQueueInterface;
import archimulator.util.event.future.Future;
import com.google.common.eventbus.EventBus;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import java.util.*;
import java.util.concurrent.Semaphore;

public class CycleAccurateEventQueue2Thread extends BaseCycleAccurateThread implements CycleAccurateEventQueueInterface {
    private final long maxCycles;
    private CPUThread cpuThread;
    private MemoryHierarchyThread memoryHierarchyThread;
    private long currentCycle;
    private final PriorityQueue<CycleAccurateEvent> events;
    private Semaphore semWait = new Semaphore(0, true);

    public CycleAccurateEventQueue2Thread(long maxCycles, EventBus channel, CPUThread cpuThread, MemoryHierarchyThread memoryHierarchyThread) {
        super(channel);
        this.maxCycles = maxCycles;
        this.cpuThread = cpuThread;
        this.memoryHierarchyThread = memoryHierarchyThread;
        this.events = new PriorityQueue<CycleAccurateEvent>();
    }

    @Override
    protected void handleMessage(CycleAccurateMessage message) {
        if(message instanceof ScheduleMessage) {
            this.schedule(((ScheduleMessage) message).getAction(), ((ScheduleMessage) message).getDelay());
//            System.out.printf("[%d] scheduled%n", this.getCurrentCycle());
        }
        else if(message instanceof NewCycleAckMessage) {
            this.semWait.release();
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void run() {
        for (int i = 0; i < maxCycles; i++) {
            this.getChannel().post(new NewCycleMessage(this.getCurrentCycle(), this.getId(), this.cpuThread.getId()));
            try {
                this.semWait.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            this.advanceOneCycle();
        }
    }

    @Override
    public synchronized void advanceOneCycle() {
        while (!this.events.isEmpty()) {
            CycleAccurateEvent event = this.events.peek();

            if (event.getWhen() != this.currentCycle) {
                break;
            }

            event.getAction().apply();
            this.events.remove(event);
        }

        this.currentCycle++;
    }

    @Override
    public CycleAccurateEventQueueInterface schedule(Action action, int delay) {
        this.schedule(new CycleAccurateEvent(action, this.currentCycle + delay));
        return this;
    }

    @Override
    public synchronized void schedule(CycleAccurateEvent event) {
        this.events.add(event);
    }

    @Override
    public Future scheduleAndGetFuture(final Action action, int delay) {
        final Future future = new Future();

        this.schedule(new Action() {
            @Override
            public void apply() {
                try {
                    action.apply();
                    future.notifyCompletion();
                } catch (Exception e) {
                    future.notifyFailure(e);
                }
            }
        }, delay);

        return future;
    }

    @Override
    public AwaitHandleInterface awaitNextCycle() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resetCurrentCycle() {
        for (CycleAccurateEvent event : this.events) {
            event.setWhen(event.getWhen() - this.currentCycle);
        }

        this.currentCycle = 0;
    }

    @Override
    public long getCurrentCycle() {
        return this.currentCycle;
    }
}
