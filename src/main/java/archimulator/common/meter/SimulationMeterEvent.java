/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.common.meter;

import archimulator.common.SimulationEvent;
import archimulator.common.SimulationObject;

/**
 * Simulation meter event.
 *
 * @author Min Cai
 */
public class SimulationMeterEvent<T> extends SimulationEvent {
    private String type;

    private int pc;

    private int address;

    private int threadId;

    private String functionName;

    private long currentCycle;

    private T value;

    /**
     * Create an simulation meter event.
     *
     * @param sender       the sender simulation object
     * @param type         the type
     * @param pc           the value of the program counter (PC)
     * @param address      the data access address
     * @param threadId     the thread ID
     * @param functionName the function symbol name
     * @param value        the value
     */
    public SimulationMeterEvent(SimulationObject sender, String type, int pc, int address, int threadId, String functionName, T value) {
        super(sender);

        this.type = type;
        this.pc = pc;
        this.address = address;
        this.threadId = threadId;
        this.functionName = functionName;
        this.currentCycle = getSender().getCycleAccurateEventQueue().getCurrentCycle();
        this.value = value;
    }

    /**
     * Get the type of the simulation meter event.
     *
     * @return the type of the simulation meter event.
     */
    public String getType() {
        return type;
    }

    /**
     * Get the value of the program counter (PC).
     *
     * @return the value of the program counter (PC)
     */
    public int getPc() {
        return pc;
    }

    /**
     * Get the data access address.
     *
     * @return the data access address
     */
    public int getAddress() {
        return address;
    }

    /**
     * Get the thread ID.
     *
     * @return the thread ID
     */
    public int getThreadId() {
        return threadId;
    }

    /**
     * Get the function symbol name.
     *
     * @return the function symbol name
     */
    public String getFunctionName() {
        return functionName;
    }

    /**
     * Get the current time in cycles.
     *
     * @return the current time in cycles
     */
    public long getCurrentCycle() {
        return currentCycle;
    }

    /**
     * Get the value.
     *
     * @return the value
     */
    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("%s{pc=0x%08x, address=0x%08x, threadId=%d, functionName='%s', currentCycle=%d, value=%s}", type, pc, address, threadId, functionName, currentCycle, value);
    }
}
