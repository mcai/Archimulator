package archimulator.util.event.future;

import archimulator.util.Reference;
import archimulator.util.action.Action;
import archimulator.util.action.Action1;
import archimulator.util.event.AwaitHandleInterface;
import archimulator.util.event.CycleAccurateEventQueue;
import com.Ostermiller.util.Parallelizer;

import java.util.Random;

public class ParallelizerHelper {
    public static void main(String[] args) throws InterruptedException {
        final Parallelizer pll = new Parallelizer();
        final Random random = new Random();

        final CycleAccurateEventQueue cycleAccurateEventQueue = new CycleAccurateEventQueue();

        final int maxCycles = 10000;

        final Reference<Boolean> halted = new Reference<Boolean>(false);

        pll.run(
                new Runnable(){
                    @Override
                    public void run() {
                        for(int i = 0; i < maxCycles; i++) {
                            if(i % 100 == 0) {
                                System.out.printf("[%d]%n", i);
                            }

                            cycleAccurateEventQueue.advanceOneCycle();

                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        halted.set(true);
                    }
                }
        );
        pll.run(
                new Runnable(){
                    @Override
                    public void run() {
                        for(;!halted.get();) {
                            AwaitHandleInterface awaitHandle = cycleAccurateEventQueue.awaitNextCycle();

                            final long scheduledCycle = cycleAccurateEventQueue.getCurrentCycle();

//                            System.out.printf("[scheduled at %d] running task.%n", scheduledCycle);
                            Future future = cycleAccurateEventQueue.scheduleAndGetFuture(new Action() {
                                @Override
                                public void apply() {
                                    throw new RuntimeException("fdfdfdrrerre");
                                }
                            }, 1).addOnCompletedCallback(new Action() {
                                @Override
                                public void apply() {
//                                    System.out.printf("[%d] task completed!!!!!!!!!!%n", cycleAccurateEventQueue.getCurrentCycle());
                                }
                            }).addOnFailedCallback(new Action1<Exception>() {
                                @Override
                                public void apply(Exception param) {
//                                    System.out.printf("[%d] task failed: %s!!!!!!!!!!%n", cycleAccurateEventQueue.getCurrentCycle(), param);
                                }
                            });

                            awaitHandle.complete();

                            future.awaitForCompletion();
                        }
                    }
                }
        );

        pll.join();
    }
}
