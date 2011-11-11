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
package archimulator.mem.cache.event;

import archimulator.mem.cache.CacheLine;
import archimulator.util.event.BlockingEvent;

import java.io.Serializable;

public class CacheLineInvalidatedEvent implements BlockingEvent {
    private CacheLine<?> line;
    private int previousTag;
    private Serializable previousState;

    public CacheLineInvalidatedEvent(CacheLine<?> line, int previousTag, Serializable previousState) {
        this.line = line;
        this.previousTag = previousTag;
        this.previousState = previousState;
    }

    public CacheLine<?> getLine() {
        return line;
    }

    public int getPreviousTag() {
        return previousTag;
    }

    public Serializable getPreviousState() {
        return previousState;
    }

    @Override
    public String toString() {
        return String.format("CacheLineInvalidatedEvent{line=%s, previousTag=%s, previousState=%s}", line, previousTag == -1 ? "<INVALID>" : String.format("0x%08x", previousTag), previousState);
    }
}
