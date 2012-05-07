package archimulator.util.event.future.new2;

public class NewCycleAckMessage extends CycleAccurateMessage {
    public NewCycleAckMessage(long currentCycle, long from, long to) {
        super(currentCycle, from, to);
    }
}
