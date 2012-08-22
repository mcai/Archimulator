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

    private long simulatedProgramId;

    private transient SimulatedProgram simulatedProgram;

    private String standardOut;

    private int helperThreadLookahead;

    private int helperThreadStride;

    private boolean dynamicHelperThreadParams;

    public ContextMapping(int threadId, SimulatedProgram simulatedProgram) {
        this(threadId, simulatedProgram, "ctx" + threadId + "_out.txt");
    }

    public ContextMapping(int threadId, SimulatedProgram simulatedProgram, String standardOut) {
        this.simulatedProgramId = simulatedProgram.getId();
        this.threadId = threadId;
        this.standardOut = standardOut;
    }

    public int getThreadId() {
        return threadId;
    }

    public long getSimulatedProgramId() {
        return simulatedProgramId;
    }

    public SimulatedProgram getSimulatedProgram() {
        if (simulatedProgram == null) {
            simulatedProgram = ServiceManager.getSimulatedProgramService().getSimulatedProgramById(simulatedProgramId);
        }

        return simulatedProgram;
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
        return String.format("thread #%d->'%s'", threadId, getSimulatedProgram().getTitle());
    }
}
