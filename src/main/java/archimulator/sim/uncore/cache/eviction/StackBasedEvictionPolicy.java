package archimulator.sim.uncore.cache.eviction; /*******************************************************************************
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

import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.cache.EvictableCache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class StackBasedEvictionPolicy<StateT extends Serializable> extends EvictionPolicy<StateT> {
    private List<List<Integer>> stackEntries;

    public StackBasedEvictionPolicy(EvictableCache<StateT> cache) {
        super(cache);

        this.stackEntries = new ArrayList<List<Integer>>();

        for (int set = 0; set < this.getCache().getNumSets(); set++) {
            this.stackEntries.add(new ArrayList<Integer>());

            for (int way = 0; way < this.getCache().getAssociativity(); way++) {
                this.stackEntries.get(set).add(way);
            }
        }
    }

    public int getMRU(int set) {
        return this.getCacheLineInStackPosition(set, 0).getWay();
    }

    public int getLRU(int set) {
        return this.getCacheLineInStackPosition(set, this.getCache().getAssociativity() - 1).getWay();
    }

    public void setMRU(int set, int way) {
        this.setStackPosition(set, way, 0);
    }

    public void setLRU(int set, int way) {
        this.setStackPosition(set, way, this.getCache().getAssociativity() - 1);
    }

    public int getWayInStackPosition(int set, int stackPosition) {
        return this.getCacheLineInStackPosition(set, stackPosition).getWay();
    }

    public CacheLine<StateT> getCacheLineInStackPosition(int set, int stackPosition) {
        return this.getCache().getLine(set, this.stackEntries.get(set).get(stackPosition));
    }

    public int getStackPosition(int set, int way) {
        Integer stackEntryFound = this.getStackEntry(set, way);
        return this.stackEntries.get(set).indexOf(stackEntryFound);
    }

    public void setStackPosition(int set, int way, int newStackPosition) {
        Integer stackEntryFound = this.getStackEntry(set, way);
        this.stackEntries.get(set).remove(stackEntryFound);
        this.stackEntries.get(set).add(newStackPosition, stackEntryFound);
    }

    private Integer getStackEntry(int set, int way) {
        List<Integer> stackEntriesPerSet = this.stackEntries.get(set);

        for (Integer stackEntry : stackEntriesPerSet) {
            if (stackEntry == way) {
                return stackEntry;
            }
        }

        throw new IllegalArgumentException();
    }
}
