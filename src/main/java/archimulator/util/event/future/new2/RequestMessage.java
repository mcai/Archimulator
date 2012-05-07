package archimulator.util.event.future.new2;

public class RequestMessage extends CycleAccurateMessage {
    public RequestMessage(long currentCycle, long from, long to) {
        super(currentCycle, from, to);
    }
}
