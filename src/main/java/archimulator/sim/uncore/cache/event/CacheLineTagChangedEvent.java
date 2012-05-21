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
package archimulator.sim.uncore.cache.event;

import archimulator.sim.uncore.cache.CacheLine;
import net.pickapack.event.BlockingEvent;

public class CacheLineTagChangedEvent implements BlockingEvent {
    private CacheLine<?> line;
    private int previousTag;

    public CacheLineTagChangedEvent(CacheLine<?> line, int previousTag) {
        this.line = line;
        this.previousTag = previousTag;
    }

    public CacheLine<?> getLine() {
        return line;
    }

    public int getPreviousTag() {
        return previousTag;
    }

    @Override
    public String toString() {
        return String.format("CacheLineTagChangedEvent{line=%s, previousTag=%s}", line, previousTag == -1 ? "<INVALID>" : String.format("0x%08x", previousTag));
    }
}
