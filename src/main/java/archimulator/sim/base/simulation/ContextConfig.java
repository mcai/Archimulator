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
package archimulator.sim.base.simulation;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class ContextConfig implements Serializable {
    private SimulatedProgram simulatedProgram;
    private String stdout;
    private int threadId;

    public ContextConfig(SimulatedProgram simulatedProgram, int threadId) {
        this(simulatedProgram, "ctx" + threadId + "_out.txt", threadId);
    }

    public ContextConfig(SimulatedProgram simulatedProgram, String stdout, int threadId) {
        this.simulatedProgram = simulatedProgram;
        this.stdout = stdout;
        this.threadId = threadId;
    }

    public SimulatedProgram getSimulatedProgram() {
        return simulatedProgram;
    }

    public String getStdout() {
        return stdout;
    }

    public int getThreadId() {
        return threadId;
    }

    @Override
    public String toString() {
        return String.format("thread %d -> '%s'", threadId, simulatedProgram.getTitle());
    }
}
