/**
 * ****************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the PickaPack library.
 * <p>
 * PickaPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * PickaPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with PickaPack. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.util.event;

import archimulator.util.action.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Cycle accurate event queue.
 *
 * @author Min Cai
 */
public class CycleAccurateEventQueue {
    private long currentCycle;
    private PriorityBlockingQueue<CycleAccurateEvent> events;
    private List<Action> perCycleEvents;

    /**
     * Create a cycle accurate event queue.
     */
    public CycleAccurateEventQueue() {
        this.events = new PriorityBlockingQueue<>();
        this.perCycleEvents = new ArrayList<>();
    }

    /**
     * Advance for one cycle.
     */
    public void advanceOneCycle() {
        while (!this.events.isEmpty()) {
            CycleAccurateEvent event = this.events.peek();

            if (event.getWhen() > this.currentCycle) {
                break;
            }

            event.getAction().apply();
            this.events.remove(event);
        }

        this.perCycleEvents.forEach(Action::apply);

        this.currentCycle++;
    }

    /**
     * Schedule the specified action to be performed after the specified delay in cycles.
     *
     * @param sender the event sender
     * @param action the action that is to be performed when the specified event takes place
     * @param delay  the delay in cycles after which the the specified event takes place
     * @return the newly created cycle accurate event
     */
    public CycleAccurateEventQueue schedule(Object sender, Action action, int delay) {
        this.schedule(new CycleAccurateEvent(this, sender, action, this.currentCycle + delay));
        return this;
    }

    /**
     * Schedule the specified cycle accurate event.
     *
     * @param event the cycle accurate event that is to be scheduled
     */
    private void schedule(CycleAccurateEvent event) {
        event.setScheduledTime(this.getCurrentCycle());
        this.events.add(event);
    }

    /**
     * Reset the current cycle to 0.
     */
    public void resetCurrentCycle() {
        for (CycleAccurateEvent event : this.events) {
            event.setWhen(event.getWhen() - this.currentCycle);
        }

        this.currentCycle = 0;
    }

    /**
     * Get the current cycle.
     *
     * @return the current cycle
     */
    public long getCurrentCycle() {
        return this.currentCycle;
    }

    /**
     * Get the list of actions that is to be performed at each cycle.
     *
     * @return the list of actions that is to be performed at each cycle
     */
    public List<Action> getPerCycleEvents() {
        return perCycleEvents;
    }

    @Override
    public String toString() {
        return String.format("CycleAccurateEventQueue{currentCycle=%d, events=%s}", currentCycle, events);
    }

    /**
     * Current maximum ID of the cycle accurate event.
     */
    public long currentId = 0;
}
