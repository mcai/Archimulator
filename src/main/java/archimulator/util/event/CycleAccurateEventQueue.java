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

import archimulator.util.action.Action;

import java.util.PriorityQueue;

public final class CycleAccurateEventQueue {
    private long currentCycle;
    private PriorityQueue<CycleAccurateEvent> events;

    public CycleAccurateEventQueue() {
        this.events = new PriorityQueue<CycleAccurateEvent>();
    }

    public synchronized void advanceOneCycle() {
        while (!this.events.isEmpty()) {
            CycleAccurateEvent event = this.events.peek();

            if (event.getWhen() != this.currentCycle) {
                break;
            }

            event.getAction().apply();
            this.events.remove(event);
        }

        this.currentCycle++;
    }

    public synchronized CycleAccurateEventQueue schedule(Action action, int delay) {
        assert (delay >= 0);

//        System.out.printf("[%d] scheduling %s after %d cycles%n", currentCycle, action instanceof NamedAction ? ((NamedAction) action).getName() : action, delay);

//        if(action instanceof NamedAction) {
//            System.out.printf("[%d] scheduling %s after %d cycles%n", currentCycle, ((NamedAction) action).getName(), delay);
//        }

        this.schedule(new CycleAccurateEvent(this.currentCycle + delay, action));

        return this;
    }

    private void schedule(CycleAccurateEvent event) {
        this.events.add(event);
    }

    public void resetCurrentCycle() {
        for (CycleAccurateEvent event : this.events) {
            event.setWhen(event.getWhen() - this.currentCycle);
        }

        this.currentCycle = 0;
    }

    public long getCurrentCycle() {
        return this.currentCycle;
    }
}