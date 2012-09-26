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
package archimulator.model;

import archimulator.service.ServiceManager;

import java.io.Serializable;

public class ContextMapping implements Serializable {
    private int threadId;

    private long benchmarkId;

    private String arguments;

    private String standardOut;

    private int helperThreadLookahead;

    private int helperThreadStride;

    private boolean dynamicHelperThreadParams;

    private transient Benchmark benchmark;

    public ContextMapping(int threadId, Benchmark benchmark, String arguments) {
        this(threadId, benchmark, arguments, "ctx" + threadId + "_out.txt");
    }

    public ContextMapping(int threadId, Benchmark benchmark, String arguments, String standardOut) {
        this.threadId = threadId;
        this.benchmarkId = benchmark.getId();
        this.arguments = arguments;
        this.standardOut = standardOut;
    }

    public int getThreadId() {
        return threadId;
    }

    public long getBenchmarkId() {
        return benchmarkId;
    }

    public Benchmark getBenchmark() {
        if (benchmark == null) {
            benchmark = ServiceManager.getBenchmarkService().getBenchmarkById(benchmarkId);
        }

        return benchmark;
    }

    public String getArguments() {
        return arguments;
    }

    public String getStandardOut() {
        return standardOut;
    }

    public int getHelperThreadLookahead() {
        return helperThreadLookahead;
    }

    public void setHelperThreadLookahead(int helperThreadLookahead) {
        this.helperThreadLookahead = helperThreadLookahead;
    }

    public int getHelperThreadStride() {
        return helperThreadStride;
    }

    public void setHelperThreadStride(int helperThreadStride) {
        this.helperThreadStride = helperThreadStride;
    }

    public boolean getDynamicHelperThreadParams() {
        return dynamicHelperThreadParams;
    }

    public void setDynamicHelperThreadParams(boolean dynamicHelperThreadParams) {
        this.dynamicHelperThreadParams = dynamicHelperThreadParams;
    }

    @Override
    public String toString() {
        return String.format("thread #%d->'%s'", threadId, getBenchmark().getTitle() + "_" + arguments + "-lookahead_" + helperThreadLookahead + "-stride_" + helperThreadStride);
    }
}
