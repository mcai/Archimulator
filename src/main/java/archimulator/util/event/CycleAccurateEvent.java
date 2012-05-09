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

public class CycleAccurateEvent implements Comparable<CycleAccurateEvent> {
    private Object sender;
    private Action action;
    private long when;
    private long id;

    public CycleAccurateEvent(Object sender, Action action, long when) {
        this.id = currentId++;
        this.sender = sender;
        this.action = action;
        this.when = when;
    }

    public int compareTo(CycleAccurateEvent otherEvent) {
        return this.when < otherEvent.when ? -1 : this.when == otherEvent.when ? 0 : 1;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CycleAccurateEvent && this.id == ((CycleAccurateEvent) o).id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    public long getId() {
        return id;
    }

    public long getWhen() {
        return when;
    }

    public void setWhen(long when) {
        this.when = when;
    }

    public Action getAction() {
        return action;
    }

    public Object getSender() {
        return sender;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s: %s", when, sender, action);
    }

    private static long currentId = 0;
}
