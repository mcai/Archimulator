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
package archimulator.sim.core;

import java.util.ArrayList;
import java.util.List;

public class PipelineBuffer<EntryT> {
    private int capacity;
    private List<EntryT> entries;

    public PipelineBuffer(int capacity) {
        this.capacity = capacity;
        this.entries = new ArrayList<EntryT>();
    }

    public boolean isFull() {
        return this.entries.size() >= this.capacity;
    }

    public boolean isEmpty() {
        return this.entries.isEmpty();
    }

    public int getCapacity() {
        return capacity;
    }

    public List<EntryT> getEntries() {
        return entries;
    }
}
