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
package archimulator.mem;

import archimulator.sim.Simulation;
import archimulator.sim.capability.SimulationCapability;
import archimulator.sim.capability.SimulationCapabilityFactory;
import archimulator.util.action.Action1;

import java.io.PrintWriter;

//TODO: Add class CacheAccessTraceGenerationCapability
public class MemoryAccessTraceGenerationCapability implements SimulationCapability {
    private String traceFileName;
    private PrintWriter fileWriter;

    public MemoryAccessTraceGenerationCapability(Simulation simulation) {
        this.traceFileName = simulation.getConfig().getCwd() + "/memTrace";

//        try {
//            this.fileWriter = new PrintWriter(new BufferedWriter(new FileWriter(traceFileName)));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        this.fileWriter = new PrintWriter(System.out);

        simulation.getBlockingEventDispatcher().addListener(MemoryAccessInitiatedEvent.class, new Action1<MemoryAccessInitiatedEvent>() {
            public void apply(MemoryAccessInitiatedEvent event) {
                writeTraceLine(new MemoryAccessTraceLine(event.getThreadId(), event.getVirtualPc(), event.getPhysicalAddress(), event.getType()));
            }
        });
    }

    @Override
    protected void finalize() throws Throwable {
        this.fileWriter.flush();
        this.fileWriter.close();

        super.finalize();
    }

    private void writeTraceLine(MemoryAccessTraceLine memoryAccessTraceLine) {
        this.fileWriter.printf("%s\n", memoryAccessTraceLine);
    }

    public static final SimulationCapabilityFactory FACTORY = new SimulationCapabilityFactory() {
        public SimulationCapability createCapability(Simulation simulation) {
            return new MemoryAccessTraceGenerationCapability(simulation);
        }
    };
}
