package archimulator.util.event.future.new2;

import archimulator.util.action.Action;
import com.google.common.eventbus.EventBus;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class CPUThread extends BaseCycleAccurateThread {
    private MemoryHierarchyThread memoryHierarchyThread;
    private Map<Long, RequestMessage> pendingRequests;
    private long currentCycle;

    private DescriptiveStatistics stat = new DescriptiveStatistics();

    private int i = 0;
    private StopWatch stopWatch;

    public CPUThread(EventBus channel, MemoryHierarchyThread memoryHierarchyThread) {
        super(channel);
        this.memoryHierarchyThread = memoryHierarchyThread;
        this.pendingRequests = new HashMap<Long, RequestMessage>();
    }

    @Override
    protected void handleMessage(final CycleAccurateMessage message) {
        if(message instanceof NewCycleMessage) {
            if(stopWatch == null) {
                stopWatch = new StopWatch();
                stopWatch.start();
            }

            this.getChannel().post(new NewCycleAckMessage(message.getCurrentCycle(), this.getId(), message.getFrom()));

            this.currentCycle = message.getCurrentCycle();

//            if(this.pendingRequests.size() < 5 && i < maxInsts - 1000) {
            if(this.currentCycle % (1000 / 150) == 0 && this.currentCycle < maxInsts - 1000) {
//                int j = 0;
//
//                for(; j < 1000000;) {
//                    j++;
//                }

                RequestMessage requestMessage = new RequestMessage(this.currentCycle, this.getId(), this.memoryHierarchyThread.getId());
                this.getChannel().post(requestMessage);
                this.pendingRequests.put(requestMessage.getId(), requestMessage);
//                System.out.printf("[%d] requested%n", this.currentCycle);

                i++;
            }

            if(!this.pendingRequests.isEmpty()) {
                this.getChannel().post(new NewCycleMessage(this.currentCycle, this.getId(), this.memoryHierarchyThread.getId()));
            }

            if(this.currentCycle % 100 == 0) {
                System.out.printf("[%d]: %s, %d%n", currentCycle, getCyclesPerSecond(stopWatch),  this.pendingRequests.size());
                System.out.printf(String.format("n: %d, sum: %.4f, std dev: %.4f, mean: %.4f, min: %.4f, max: %.4f%n", stat.getN(), stat.getSum(), stat.getStandardDeviation(), stat.getMean(), stat.getMin(), stat.getMax()));
            }

//            System.out.println("ok..." + this.pendingRequests.size());
        }
        else if(message instanceof ReplyMessage) {
            long error = this.currentCycle - message.getCurrentCycle();
            stat.addValue(error);
            this.pendingRequests.remove(((ReplyMessage) message).getRequestId());
//            System.out.printf("[%d] replied%n", this.getCurrentCycle());

//            if(this.pendingRequests.size() < 5 && wait) {
//                this.semWait.release();
//            }
        }
        else {
            throw new IllegalArgumentException();
        }
    }
    int maxInsts = 1000000000;

    @Override
    public void run() {
    }

    public double getCyclesPerSecond(StopWatch stopWatch) {
        return (double) this.currentCycle / this.getDurationInSeconds(stopWatch);
    }

    public long getDurationInSeconds(StopWatch stopWatch) {
        return stopWatch.getTime() / 1000;
    }
}
