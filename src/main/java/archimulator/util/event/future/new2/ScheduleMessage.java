package archimulator.util.event.future.new2;

import archimulator.util.action.Action;

public class ScheduleMessage extends CycleAccurateMessage {
    private int delay;
    private Action action;

    public ScheduleMessage(long currentCycle, long from, long to, int delay, Action action) {
        super(currentCycle, from, to);
        this.delay = delay;
        this.action = action;
    }

    public int getDelay() {
        return delay;
    }

    public Action getAction() {
        return action;
    }
}
