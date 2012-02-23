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
package archimulator.model.simulation;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ContextConfig {
    private SimulatedProgram simulatedProgram;
    private String stdout;
    private int threadId;

    private List<String> cmdArgList;

    public ContextConfig(SimulatedProgram simulatedProgram, int threadId) {
        this(simulatedProgram, "ctx" + threadId + ".out", threadId);
    }

    public ContextConfig(SimulatedProgram simulatedProgram, String stdout, int threadId) {
        this.simulatedProgram = simulatedProgram;
        this.stdout = stdout;
        this.threadId = threadId;

        this.cmdArgList = Arrays.asList((simulatedProgram.getCwd() + File.separator + simulatedProgram.getExe() + " " + simulatedProgram.getArgs()).split(" "));
    }

    @Override
    public String toString() {
        return String.format("%s-%s(%s)", simulatedProgram.getSetTitle(), simulatedProgram.getTitle(), simulatedProgram.getArgs());
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

    public List<String> toCmdArgList() {
        return cmdArgList;
    }
}
