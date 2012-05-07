package archimulator.util.event.future.new2;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public abstract class BaseCycleAccurateThread extends Thread {
    private long id;
    private EventBus channel;

    public BaseCycleAccurateThread(EventBus channel) {
        this.id = currentThreadId++;
        this.channel = channel;
    }

    @Subscribe
    public void receiveMessage(CycleAccurateMessage message) {
        long to = message.getTo();
        if(to == this.getId() || to == -1) {
            this.handleMessage(message);
        }
    }

    protected abstract void handleMessage(CycleAccurateMessage message);

    public long getId() {
        return id;
    }

    public EventBus getChannel() {
        return channel;
    }

    private static long currentThreadId;
}
