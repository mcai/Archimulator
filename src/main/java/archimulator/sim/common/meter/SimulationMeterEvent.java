package archimulator.sim.common.meter;

import archimulator.sim.common.SimulationEvent;
import archimulator.sim.common.SimulationObject;

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
