package archimulator.util.akka;

import akka.actor.*;
import akka.routing.RoundRobinRouter;
import akka.util.Duration;
import org.apache.commons.lang.time.StopWatch;
import org.jfree.data.statistics.Statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MapReduce {
    public static void main(String[] args) {
        List<Long> times = new ArrayList<Long>();

        for (int i = 0; i < 10; i++) {
            times.add(execute());
        }

        System.out.println(Statistics.calculateMean(times));
    }

    private static long execute() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        ActorSystem system = ActorSystem.create("experiment");

        ActorRef master = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new Master(100);
            }
        }), "master");

        for (int i = 1; i < 1000; i++) {
            master.tell(new Task(1000));
        }

        system.awaitTermination();

        stopWatch.stop();
        System.out.println(stopWatch.getTime());

        return stopWatch.getTime();
    }

    static class Task {
        private long id;
        private long n;

        private double result;
        private long numResults;
        private final long start = System.currentTimeMillis();

        public Task(long n) {
            this.n = n;
            this.id = currentCalculateId++;
        }

        public long getId() {
            return id;
        }

        public long getN() {
            return n;
        }
    }

    private static long currentCalculateId = 0;

    static class Work {
        private long calculateId;
        private long number;

        Work(long calculateId, long number) {
            this.calculateId = calculateId;
            this.number = number;
        }

        public long getCalculateId() {
            return calculateId;
        }

        public long getNumber() {
            return number;
        }
    }

    static class Result {
        private long calculateId;
        private long value;

        Result(long calculateId, long value) {
            this.calculateId = calculateId;
            this.value = value;
        }

        public long getCalculateId() {
            return calculateId;
        }

        public long getValue() {
            return value;
        }
    }

    public static class Worker extends UntypedActor {
        private long calculatePiFor(long number) {
            return number * number;
        }

        public void onReceive(Object message) {
            if (message instanceof Work) {
                Work work = (Work) message;
                long result = calculatePiFor(work.getNumber());
                getSender().tell(new Result(work.getCalculateId(), result), getSelf());
            } else {
                unhandled(message);
            }
        }
    }

    public static class Master extends UntypedActor {
        private final ActorRef workerRouter;

        private Map<Long, Task> calculates = new HashMap<Long, Task>();

        public Master(final int nrOfWorkers) {
            this.workerRouter = this.getContext().actorOf(new Props(Worker.class).withRouter(new RoundRobinRouter(nrOfWorkers)), "workerRouter");
        }

        public void onReceive(Object message) {
            if (message instanceof Task) {
                Task task = (Task) message;
                this.calculates.put(task.getId(), task);
                for (long start = 0; start < task.getN(); start++) {
                    workerRouter.tell(new Work(task.getId(), start), getSelf());
                }
            } else if (message instanceof Result) {
                Result result = (Result) message;
                Task task = this.calculates.get(result.getCalculateId());
                task.result += result.getValue();
                task.numResults++;
                if (task.numResults == task.n) {
                    this.calculates.remove(task.getId());
                    Duration duration = Duration.create(System.currentTimeMillis() - task.start, TimeUnit.MILLISECONDS);
                    System.out.println(String.format("\n\tResult: \t\t%s\n\ttime used: \t%s", task.result, duration));

                    if (this.calculates.isEmpty()) {
                        getContext().system().shutdown();
                    }
                }
            } else {
                unhandled(message);
            }
        }
    }
}