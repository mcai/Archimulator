/**
 * ****************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.uncore.cache.partitioning;

import java.util.Stack;

/**
 * Per-thread, per-set LRU stack.
 *
 * @author Min Cai
 */
public class LRUStack {
    private int threadId;
    private int set;
    private int associativity;
    private Stack<Integer> tags;

    /**
     * Create a per-thread, per-set LRU stack.
     *
     * @param threadId the thread ID
     * @param set                the set index
     * @param associativity      the associativity
     */
    public LRUStack(int threadId, int set, int associativity) {
        this.threadId = threadId;
        this.set = set;
        this.associativity = associativity;
        this.tags = new Stack<>();
    }

    /**
     * Access the specified tag.
     *
     * @param tag the tag
     * @return the stack distance of the access
     */
    public int access(int tag) {
        int stackDistance = this.tags.search(tag);

        if (stackDistance != -1) {
            this.tags.remove((Integer) tag);
            stackDistance--;
        }

        this.tags.push(tag);

        if (this.tags.size() > this.associativity) {
            this.tags.remove(this.tags.size() - 1);
        }

        return stackDistance;
    }

    /**
     * Get the thread ID.
     *
     * @return the thread ID
     */
    public int getThreadId() {
        return threadId;
    }

    /**
     * Get the set index.
     *
     * @return the set index
     */
    public int getSet() {
        return set;
    }
}
