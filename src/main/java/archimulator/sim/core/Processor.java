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
package archimulator.sim.core;

import archimulator.sim.common.SimulationObject;
import archimulator.sim.common.report.ReportNode;
import archimulator.sim.common.report.Reportable;
import archimulator.sim.os.Kernel;
import archimulator.sim.uncore.MemoryHierarchy;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Processor.
 *
 * @author Min Cai
 */
public interface Processor extends SimulationObject, Reportable {
    /**
     * Get the list of cores.
     *
     * @return the list of cores
     */
    List<Core> getCores();

    /**
     * Get the list of threads.
     *
     * @return the list of threads
     */
    default List<Thread> getThreads() {
        return this.getCores().stream().flatMap(core -> core.getThreads().stream()).collect(Collectors.toList());
    }

    /**
     * Get the kernel.
     *
     * @return the kernel
     */
    Kernel getKernel();

    /**
     * Update the assignments of contexts to threads.
     */
    void updateContextToThreadAssignments();

    /**
     * Get the memory hierarchy.
     *
     * @return the memory hierarchy
     */
    MemoryHierarchy getMemoryHierarchy();

    /**
     * Get the number of instructions executed on all the threads.
     *
     * @return the number of instructions executed on all the threads.
     */
    default long getNumInstructions() {
        return this.getCores().stream().mapToLong(Core::getNumInstructions).sum();
    }

    /**
     * Get the number of instructions executed on the thread C0T0.
     *
     * @return the number of instructions executed on the thread C0T0
     */
    default long getC0t0NumInstructions() {
        return this.getCores().get(0).getThreads().get(0).getNumInstructions();
    }

    /**
     * Get the number of instructions executed on the thread C1T0.
     *
     * @return the number of instructions executed on the thread C1T0
     */
    default long getC1t0NumInstructions() {
        if(this.getCores().size() < 2) {
            return 0;
        }

        return this.getCores().get(1).getThreads().get(0).getNumInstructions();
    }

    /**
     * Get the IPC (instructions per cycle) value.
     *
     * @return the IPC (instructions per cycle) value
     */
    default double getInstructionsPerCycle() {
        return (double) this.getNumInstructions() / this.getCycleAccurateEventQueue().getCurrentCycle();
    }

    /**
     * Get the IPC (instructions per cycle) value for the thread C0T0.
     *
     * @return the IPC (instructions per cycle) value for the thread C0T0
     */
    default double getC0t0InstructionsPerCycle() {
        return (double) this.getCores().get(0).getThreads().get(0).getNumInstructions() / this.getCycleAccurateEventQueue().getCurrentCycle();
    }

    /**
     * Get the IPC (instructions per cycle) value for the thread C1T0.
     *
     * @return the IPC (instructions per cycle) value for the thread C1T0
     */
    default double getC1t0InstructionsPerCycle() {
        if(this.getCores().size() < 2) {
            return 0;
        }

        return (double) this.getCores().get(1).getThreads().get(0).getNumInstructions() / this.getCycleAccurateEventQueue().getCurrentCycle();
    }

    /**
     * Get the CPI (cycles per instruction) value.
     *
     * @return the CPI (cycles per instruction) value
     */
    default double getCyclesPerInstruction() {
        return (double) this.getCycleAccurateEventQueue().getCurrentCycle() / this.getNumInstructions();
    }

    /**
     * Get the simulation speed expressed as the CPS (cycles per second) value.
     *
     * @return the CPS (cycles per second) value
     */
    default double getCyclesPerSecond() {
        return (double) this.getCycleAccurateEventQueue().getCurrentCycle() / this.getSimulation().getDurationInSeconds();
    }

    /**
     * Get the simulation speed expressed as the IPS (instructions per second) value.
     *
     * @return the IPS (instructions per second) value
     */
    default double getInstructionsPerSecond() {
        return (double) this.getNumInstructions() / this.getSimulation().getDurationInSeconds();
    }

    default void dumpStats(ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, "simulation") {{
            getChildren().add(new ReportNode(this, "numInstructions", getNumInstructions() + ""));
            getChildren().add(new ReportNode(this, "c0t0NumInstructions", getC0t0NumInstructions() + ""));
            getChildren().add(new ReportNode(this, "c1t0NumInstructions", getC1t0NumInstructions() + ""));
            getChildren().add(new ReportNode(this, "instructionsPerCycle", getInstructionsPerCycle() + ""));
            getChildren().add(new ReportNode(this, "c0t0InstructionsPerCycle", getC0t0InstructionsPerCycle() + ""));
            getChildren().add(new ReportNode(this, "c1t0InstructionsPerCycle", getC1t0InstructionsPerCycle() + ""));
            getChildren().add(new ReportNode(this, "cyclesPerInstruction", getCyclesPerInstruction() + ""));
            getChildren().add(new ReportNode(this, "cyclesPerSecond", getCyclesPerSecond() + ""));
            getChildren().add(new ReportNode(this, "instructionsPerSecond", getInstructionsPerSecond() + ""));
        }});
    }
}
