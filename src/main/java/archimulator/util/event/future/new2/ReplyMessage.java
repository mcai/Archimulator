package archimulator.util.event.future.new2;

public class ReplyMessage extends CycleAccurateMessage {
    private long requestId;
    private int delay;

    public ReplyMessage(long currentCycle, long from, long to, long requestId, int delay) {
        super(currentCycle, from, to);
        this.requestId = requestId;
        this.delay = delay;
    }

    public long getRequestId() {
        return requestId;
    }

    public int getDelay() {
        return delay;
    }
}
