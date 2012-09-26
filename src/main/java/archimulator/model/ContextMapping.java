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
    private Integer threadId;

    private long benchmarkId;

    private String arguments;

    private String standardOut;

    private int helperThreadLookahead;

    private int helperThreadStride;

    private boolean dynamicHelperThreadParams;

    private transient Benchmark benchmark;

    public ContextMapping(int threadId, Benchmark benchmark, String arguments) {
        this(threadId, benchmark, arguments, getDefaultStandardOut(threadId));
    }

    public ContextMapping(int threadId, Benchmark benchmark, String arguments, String standardOut) {
        this.threadId = threadId;
        this.benchmarkId = benchmark.getId();
        this.arguments = arguments;
        this.standardOut = standardOut;
    }

    public Integer getThreadId() {
        return threadId;
    }

    public void setThreadId(Integer threadId) {
        this.threadId = threadId;
    }

    public long getBenchmarkId() {
        return benchmarkId;
    }

    public void setBenchmarkId(long benchmarkId) {
        this.benchmarkId = benchmarkId;
        this.benchmark = ServiceManager.getBenchmarkService().getBenchmarkById(benchmarkId);
    }

    public String getArguments() {
        if(arguments == null) {
            arguments = "";
        }

        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    public String getStandardOut() {
        if(standardOut == null) {
            standardOut = "";
        }

        return standardOut;
    }

    public void setStandardOut(String standardOut) {
        this.standardOut = standardOut;
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

    public Benchmark getBenchmark() {
        if (benchmark == null) {
            benchmark = ServiceManager.getBenchmarkService().getBenchmarkById(benchmarkId);
        }

        return benchmark;
    }

    @Override
    public String toString() {
        return String.format("thread #%d->'%s'", threadId, getBenchmark().getTitle() + "_" + arguments + "-lookahead_" + helperThreadLookahead + "-stride_" + helperThreadStride);
    }

    public static String getDefaultStandardOut(Integer threadId) {
        return "ctx" + threadId + "_out.txt";
    }
}
