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
    private long when;
    private Action action;

    public CycleAccurateEvent(long when, Action action) {
        this.when = when;
        this.action = action;
    }

    public int compareTo(CycleAccurateEvent otherEvent) {
        return new Long(when).compareTo(otherEvent.when);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CycleAccurateEvent that = (CycleAccurateEvent) o;

        if (when != that.when) return false;
        if (action != null ? !action.equals(that.action) : that.action != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (when ^ (when >>> 32));
        result = 31 * result + (action != null ? action.hashCode() : 0);
        return result;
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
}
