/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
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

/**
 * Cycle accurate event.
 *
 * @author Min Cai
 */
public class CycleAccurateEvent implements Comparable<CycleAccurateEvent> {
    private Object sender;
    private Action action;
    private long scheduledTime;
    private long when;
    private long id;

    /**
     * Create a cycle accurate event.
     *
     * @param parent the parent cycle accurate event queue
     * @param sender the event sender
     * @param action the action that is to be performed when the event takes place
     * @param when   the cycle at which the event takes place
     */
    public CycleAccurateEvent(CycleAccurateEventQueue parent, Object sender, Action action, long when) {
        this.id = parent.currentId++;
        this.sender = sender;
        this.action = action;
        this.when = when;
    }

    /**
     * Get the ID of the event.
     *
     * @return the ID of the event
     */
    public long getId() {
        return id;
    }

    /**
     * Get the cycle at which the event is scheduled.
     *
     * @return the cycle at which the event is scheduled
     */
    public long getScheduledTime() {
        return scheduledTime;
    }

    /**
     * Set the cycle at which the event is scheduled.
     *
     * @param scheduledTime the cycle at which the event is scheduled
     */
    public void setScheduledTime(long scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    /**
     * Get the cycle at which the event takes place.
     *
     * @return the cycle at which the event takes place
     */
    public long getWhen() {
        return when;
    }

    /**
     * Set the cycle at which the event takes place.
     *
     * @param when the cycle at which the event takes place
     */
    public void setWhen(long when) {
        this.when = when;
    }

    /**
     * Get the action that is to be performed when the event takes place.
     *
     * @return the action that is to be performed when the event takes place
     */
    public Action getAction() {
        return action;
    }

    /**
     * Get the event sender.
     *
     * @return the event sender
     */
    public Object getSender() {
        return sender;
    }

    @Override
    public int compareTo(CycleAccurateEvent otherEvent) {
        if (this.when < otherEvent.when) {
            return -1;
        } else if (this.when == otherEvent.when) {
            if (this.id < otherEvent.id) {
                return -1;
            } else if (this.id == otherEvent.id) {
                return 0;
            } else {
                return 1;
            }
        } else {
            return 1;
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CycleAccurateEvent && this.id == ((CycleAccurateEvent) o).id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return String.format("[%d] %s: %s", when, sender, action);
    }
}
