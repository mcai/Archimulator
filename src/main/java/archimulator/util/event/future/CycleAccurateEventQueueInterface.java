package archimulator.util.event.future;

import archimulator.util.action.Action;
import archimulator.util.event.AwaitHandleInterface;
import archimulator.util.event.CycleAccurateEvent;
import archimulator.util.event.CycleAccurateEventQueue;

public interface CycleAccurateEventQueueInterface {
    void advanceOneCycle();

    CycleAccurateEventQueueInterface schedule(Action action, int delay);

    void schedule(CycleAccurateEvent event);

    Future scheduleAndGetFuture(Action action, int delay);

    AwaitHandleInterface awaitNextCycle();

    void resetCurrentCycle();

    long getCurrentCycle();
}
