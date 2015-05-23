/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.model;

import java.io.Serializable;

/**
 * Context mapping, or the assignment of software contexts to hardware threads.
 *
 * @author Min Cai
 */
public class ContextMapping implements Serializable {
    private int threadId;
    private Benchmark benchmark;

    /**
     * Create a context mapping.
     *
     * @param threadId    the hardware thread ID.
     * @param benchmark   the benchmark
     */
    public ContextMapping(int threadId, Benchmark benchmark) {
        this.threadId = threadId;
        this.benchmark = benchmark;
    }

    /**
     * Get the hardware thread ID.
     *
     * @return the hardware thread ID
     */
    public int getThreadId() {
        return threadId;
    }

    /**
     * Set the hardware thread ID.
     *
     * @param threadId the hardware thread ID
     */
    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    /**
     * Get the benchmark.
     *
     * @return the benchmark
     */
    public Benchmark getBenchmark() {
        return benchmark;
    }

    @Override
    public String toString() {
        return String.format("thread #%d->'%s'", threadId, getBenchmark().getTitle());
    }
}
