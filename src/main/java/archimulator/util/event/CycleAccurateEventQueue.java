/*******************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
 *
 * This file is part of the Archimulator multicore architectural simulator.
 *
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package archimulator.util.event;

import archimulator.util.event.future.CycleAccurateEventQueueInterface;
import archimulator.util.event.future.Future;
import archimulator.util.action.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.Semaphore;

public final class CycleAccurateEventQueue implements CycleAccurateEventQueueInterface {
    private long currentCycle;
    private final PriorityQueue<CycleAccurateEvent> events;
    private List<Semaphore> semsToNotify = new ArrayList<Semaphore>();
    private List<Semaphore> semsToWait = new ArrayList<Semaphore>();

    public CycleAccurateEventQueue() {
        this.events = new PriorityQueue<CycleAccurateEvent>();
    }

    @Override
    public void advanceOneCycle() {
        for(Semaphore semToNotify : this.semsToNotify) {
            semToNotify.release();
        }

        for(Semaphore semToWait : this.semsToWait) {
            try {
                semToWait.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        this.semsToNotify.clear();
        this.semsToWait.clear();

//        synchronized (this.events) {
            while (!this.events.isEmpty()) {
                CycleAccurateEvent event = this.events.peek();

                if (event.getWhen() != this.currentCycle) {
                    break;
                }

                event.getAction().apply();
                this.events.remove(event);
            }
//        }

        this.currentCycle++;
    }

    @Override
    public CycleAccurateEventQueueInterface schedule(Action action, int delay) {
        this.schedule(new CycleAccurateEvent(action, this.currentCycle + delay));
        return this;
    }

    @Override
    public void schedule(CycleAccurateEvent event) {
//        synchronized (this.events) {
            this.events.add(event);
//        }
    }

    @Override
    public Future scheduleAndGetFuture(final Action action, int delay) {
        final Future future = new Future();

        this.schedule(new Action() {
            @Override
            public void apply() {
                try {
                    action.apply();
                    future.notifyCompletion();
                } catch (Exception e) {
                    future.notifyFailure(e);
                }
            }
        }, delay);

        return future;
    }

    @Override
    public AwaitHandleInterface awaitNextCycle() {
        Semaphore semToNotify = new Semaphore(0, true);
        this.semsToNotify.add(semToNotify);

        Semaphore semToWait = new Semaphore(0, true);
        this.semsToWait.add(semToWait);

        try {
            semToNotify.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return new AwaitHandle(semToWait);
    }

    @Override
    public void resetCurrentCycle() {
        for (CycleAccurateEvent event : this.events) {
            event.setWhen(event.getWhen() - this.currentCycle);
        }

        this.currentCycle = 0;
    }

    @Override
    public long getCurrentCycle() {
        return this.currentCycle;
    }

    public static class AwaitHandle implements AwaitHandleInterface {
        private Semaphore semToWait;

        public AwaitHandle(Semaphore semToWait) {
            this.semToWait = semToWait;
        }

        @Override
        public void complete() {
            this.semToWait.release();
        }
    }
}
