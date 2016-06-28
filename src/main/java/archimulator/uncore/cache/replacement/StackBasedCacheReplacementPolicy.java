/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.uncore.cache.replacement;

import archimulator.uncore.cache.CacheLine;
import archimulator.uncore.cache.EvictableCache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Stack based cache replacement policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public abstract class StackBasedCacheReplacementPolicy<StateT extends Serializable> extends AbstractCacheReplacementPolicy<StateT> {
    private List<List<Integer>> stackEntries;

    /**
     * Create a stack based replacement policy for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     */
    public StackBasedCacheReplacementPolicy(EvictableCache<StateT> cache) {
        super(cache);

        this.stackEntries = new ArrayList<>();

        for (int set = 0; set < this.getCache().getNumSets(); set++) {
            this.stackEntries.add(new ArrayList<>());

            for (int way = 0; way < this.getCache().getAssociativity(); way++) {
                this.stackEntries.get(set).add(way);
            }
        }
    }

    /**
     * Get the way in the MRU position in the specified set.
     *
     * @param set the set index
     * @return the way in the MRU position in the specified set
     */
    public int getMRU(int set) {
        return this.getCacheLineInStackPosition(set, 0).getWay();
    }

    /**
     * Get the way in the LRU position in the specified set.
     *
     * @param set the set index
     * @return the way in the LRU position in the specified set
     */
    public int getLRU(int set) {
        return this.getCacheLineInStackPosition(set, this.getCache().getAssociativity() - 1).getWay();
    }

    /**
     * Set the specified way in the MRU position in the specified set.
     *
     * @param set the set index
     * @param way the way
     */
    public void setMRU(int set, int way) {
        this.setStackPosition(set, way, 0);
    }

    /**
     * Set the specified way in the LRU position in the specified set.
     *
     * @param set the set index
     * @param way the way
     */
    public void setLRU(int set, int way) {
        this.setStackPosition(set, way, this.getCache().getAssociativity() - 1);
    }

    /**
     * Get the way in the specified stack position (distance) in the specified set.
     *
     * @param set           the set index
     * @param stackPosition the stack position (distance)
     * @return the way in the specified stack position in the specified set
     */
    public int getWayInStackPosition(int set, int stackPosition) {
        return this.getCacheLineInStackPosition(set, stackPosition).getWay();
    }

    /**
     * Get the cache line in the specified stack position in the specified set.
     *
     * @param set           the set index
     * @param stackPosition the stack position
     * @return the cache line in the specified stack position in the specified set
     */
    public CacheLine<StateT> getCacheLineInStackPosition(int set, int stackPosition) {
        return this.getCache().getLine(set, this.stackEntries.get(set).get(stackPosition));
    }

    /**
     * Get the stack position (distance) for the specified way in the specified set.
     *
     * @param set the set index
     * @param way the way
     * @return the stack position (distance) for the specified way in the specified set
     */
    public int getStackPosition(int set, int way) {
        Integer stackEntry = this.getStackEntry(set, way);
        return this.stackEntries.get(set).indexOf(stackEntry);
    }

    /**
     * Set the stack position (distance) for the specified way in the specified set.
     *
     * @param set              the set index
     * @param way              the way
     * @param newStackPosition the new stack position (distance)
     */
    public void setStackPosition(int set, int way, int newStackPosition) {
        Integer stackEntry = this.getStackEntry(set, way);
        this.stackEntries.get(set).remove(stackEntry);
        this.stackEntries.get(set).add(newStackPosition, stackEntry);
    }

    /**
     * Get the stack entry at the specified set and way.
     *
     * @param set the set index
     * @param way the way
     * @return the stack entry at the specified set and way
     */
    private Integer getStackEntry(int set, int way) {
        return this.stackEntries.get(set).stream().filter(stackEntry -> stackEntry == way).findFirst().get();
    }
}
