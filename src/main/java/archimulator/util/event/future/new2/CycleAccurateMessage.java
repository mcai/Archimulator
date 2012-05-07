package archimulator.util.event.future.new2;

public abstract class CycleAccurateMessage {
    private long id;
    private long currentCycle;
    private long from;
    private long to;

    public CycleAccurateMessage(long currentCycle, long from, long to) {
        this.currentCycle = currentCycle;
        this.id = currentId++;
        this.from = from;
        this.to = to;
    }

    public long getId() {
        return id;
    }

    public long getCurrentCycle() {
        return currentCycle;
    }

    public long getFrom() {
        return from;
    }

    public long getTo() {
        return to;
    }

    private static long currentId = 0;

}
