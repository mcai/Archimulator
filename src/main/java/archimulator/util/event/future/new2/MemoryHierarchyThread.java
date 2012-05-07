package archimulator.util.event.future.new2;

import archimulator.util.action.Action;
import com.google.common.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class MemoryHierarchyThread extends BaseCycleAccurateThread {
    private Random random = new Random();
    private CycleAccurateEventQueue2Thread cycleAccurateEventQueue2Thread;
    private Map<Long, RequestMessage> pendingRequests;
    private long currentCycle;

    public MemoryHierarchyThread(EventBus channel) {
        super(channel);
        this.pendingRequests = new HashMap<Long, RequestMessage>();
    }

    @Override
    protected void handleMessage(final CycleAccurateMessage message) {
        if(message instanceof NewCycleMessage) {
            this.currentCycle = message.getCurrentCycle();

            for(final RequestMessage requestMessage : this.pendingRequests.values()) {
                final int delay = random.nextInt(20) + 1;
//            final int delay = 1;
                ScheduleMessage scheduleMessage = new ScheduleMessage(requestMessage.getCurrentCycle(), this.getId(), cycleAccurateEventQueue2Thread.getId(), delay, new Action() {
                    @Override
                    public void apply() {
                        getChannel().post(new ReplyMessage(requestMessage.getCurrentCycle() + delay, getId(), requestMessage.getFrom(), requestMessage.getId(), delay));
                    }
                });
                this.getChannel().post(scheduleMessage);
            }

            this.pendingRequests.clear();
        }
        else if(message instanceof RequestMessage) {
            this.pendingRequests.put(message.getId(), (RequestMessage) message);
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void run() {
    }

    public void setCycleAccurateEventQueue2Thread(CycleAccurateEventQueue2Thread cycleAccurateEventQueue2Thread) {
        this.cycleAccurateEventQueue2Thread = cycleAccurateEventQueue2Thread;
    }
}
