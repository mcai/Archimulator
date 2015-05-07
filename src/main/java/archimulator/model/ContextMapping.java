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

import archimulator.service.ServiceManager;

import java.io.Serializable;

/**
 * Context mapping, or the assignment of software contexts to hardware threads.
 *
 * @author Min Cai
 */
public class ContextMapping implements Serializable {
    private int threadId;

    private String benchmarkTitle;

    private String arguments;

    private String standardOut;

    private int helperThreadLookahead;

    private int helperThreadStride;

    private transient Benchmark benchmark;

    /**
     * Create a context mapping.
     *
     * @param threadId  the hardware thread ID.
     * @param benchmark the benchmark.
     * @param arguments the command line arguments used in running the software context
     */
    public ContextMapping(int threadId, Benchmark benchmark, String arguments) {
        this(threadId, benchmark, arguments, getDefaultStandardOut(threadId));
    }

    /**
     * Create a context mapping.
     *
     * @param threadId    the hardware thread ID.
     * @param benchmark   the benchmark
     * @param arguments   the command line arguments used in running the software context
     * @param standardOut the standard out
     */
    public ContextMapping(int threadId, Benchmark benchmark, String arguments, String standardOut) {
        this.threadId = threadId;
        this.benchmarkTitle = benchmark.getTitle();
        this.arguments = arguments;
        this.standardOut = standardOut;
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
     * Get the benchmark title.
     *
     * @return the benchmark title
     */
    public String getBenchmarkTitle() {
        return benchmarkTitle;
    }

    /**
     * Get the command line arguments used in running the software context.
     *
     * @return the command line arguments used in running the software context
     */
    public String getArguments() {
        if (arguments == null) {
            arguments = "";
        }

        return arguments;
    }

    /**
     * Set the command line arguments used in running the software context
     *
     * @param arguments the command line arguments used in running the software context
     */
    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    /**
     * Get the standard out.
     *
     * @return the standard out
     */
    public String getStandardOut() {
        if (standardOut == null) {
            standardOut = "";
        }

        return standardOut;
    }

    /**
     * Set the standard out.
     *
     * @param standardOut the standard out
     */
    public void setStandardOut(String standardOut) {
        this.standardOut = standardOut;
    }

    /**
     * Get the dynamic value of the helper thread lookahead.
     *
     * @return the dynamic value of the helper thread lookahead
     */
    public int getHelperThreadLookahead() {
        return helperThreadLookahead;
    }

    /**
     * Set the dynamic value of the helper thread lookahead.
     *
     * @param helperThreadLookahead the dynamic value of the helper thread lookahead
     */
    public void setHelperThreadLookahead(int helperThreadLookahead) {
        this.helperThreadLookahead = helperThreadLookahead;
    }

    /**
     * Get the dynamic value of the helper thread stride.
     *
     * @return the dynamic value of the helper thread stride
     */
    public int getHelperThreadStride() {
        return helperThreadStride;
    }

    /**
     * Set the dynamic value of the helper thread stride.
     *
     * @param helperThreadStride the dynamic value of the helper thread stride
     */
    public void setHelperThreadStride(int helperThreadStride) {
        this.helperThreadStride = helperThreadStride;
    }

    /**
     * Get the benchmark.
     *
     * @return the benchmark
     */
    public Benchmark getBenchmark() {
        if (benchmark == null) {
            benchmark = ServiceManager.getBenchmarkService().getBenchmarkByTitle(benchmarkTitle);
        }

        return benchmark;
    }

    @Override
    public String toString() {
        return String.format("thread #%d->'%s'", threadId, getBenchmark().getTitle() + "_" + arguments + "-lookahead_" + helperThreadLookahead + "-stride_" + helperThreadStride);
    }

    /**
     * Get the default standard out by the hardware thread ID.
     *
     * @param threadId the hardware thread ID
     * @return the default standard out by the hardware thread ID
     */
    public static String getDefaultStandardOut(int threadId) {
        return "ctx" + threadId + "_out.txt";
    }
}
