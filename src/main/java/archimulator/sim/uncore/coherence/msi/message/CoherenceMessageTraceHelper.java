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
package archimulator.sim.uncore.coherence.msi.message;

import archimulator.sim.common.Simulation;
import archimulator.sim.common.SimulationEvent;
import archimulator.sim.common.SimulationObject;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Coherence message trace helper.
 *
 * @author Min Cai
 */
public class CoherenceMessageTraceHelper {
    private Simulation simulation;
    private PrintWriter printWriter;
    private boolean enabled;

    /**
     * Create a coherence message trace helper.
     *
     * @param simulation the simulation
     */
    public CoherenceMessageTraceHelper(Simulation simulation) {
        this.simulation = simulation;

        try {
            this.printWriter = new PrintWriter(new File(simulation.getWorkingDirectory() + "/coherence_message_trace.csv"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        this.printWriter.println("\"BEGIN_CYCLE\",\"CONTROLLER\",\"MESSAGE_TYPE\",\"ADDRESS\",\"DATA\"");

        this.simulation.getBlockingEventDispatcher().addListener(CoherenceMessageTraceEvent.class, param -> {
            if(isEnabled()) {
                printWriter.println(param);
            }
        });
    }

    /**
     * Close the print writer.
     */
    public void close() {
        this.printWriter.close();
    }

    /**
     * Get the simulation.
     *
     * @return the simulation
     */
    public Simulation getSimulation() {
        return simulation;
    }

    /**
     * Get a value indicating whether it is enabled or not.
     *
     * @return a value indicating whether it is enabled or not
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set a value indicating whether it is enabled or not.
     *
     * @param enabled a value indicating whether it is enabled or not
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Coherence message trace event.
     */
    public static class CoherenceMessageTraceEvent extends SimulationEvent {
        private CoherenceMessage message;

        /**
         * Create a simulation event.
         *
         * @param sender the sender simulation object
         */
        public CoherenceMessageTraceEvent(SimulationObject sender, CoherenceMessage message) {
            super(sender);
            this.message = message;
        }

        @Override
        public String toString() {
            return String.format(
                    "\"%d\",\"%s\",\"%s\",\"0x%08x\",\"0x%s\"",
                    this.message.getBeginCycle(),
                    this.message.getGenerator().getName(),
                    this.message.getType(),
                    this.message.getTag(),
                    StringUtils.join(Arrays.asList(ArrayUtils.toObject(this.message.getData())).stream()
                            .map(b -> String.format("%2s", Integer.toHexString(b & 0xFF)).replace(' ', '0'))
                            .collect(Collectors.toList()), "|")
            );
        }

        /**
         * Get the message.
         *
         * @return the message
         */
        public CoherenceMessage getMessage() {
            return message;
        }
    }
}
