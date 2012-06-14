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
package archimulator.sim.os;

import archimulator.sim.base.simulation.BasicSimulationObject;
import archimulator.sim.base.simulation.ContextConfig;
import archimulator.sim.base.simulation.Simulation;
import archimulator.sim.base.simulation.SimulationObject;
import archimulator.sim.isa.BitField;
import archimulator.sim.isa.Mnemonic;
import archimulator.sim.isa.StaticInstruction;
import archimulator.sim.isa.memory.BasicMemory;
import archimulator.sim.isa.memory.Memory;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Process extends BasicSimulationObject implements SimulationObject, Serializable {
    private List<String> envs;

    private int stdinFileDescriptor;
    private int stdoutFileDescriptor;

    private int stackBase;
    private int stackSize;
    private int textSize;

    private int environBase;
    private int heapTop;
    private int dataTop;

    private int programEntry;

    private boolean littleEndian;

    private Memory memory;

    private int id;

    private ContextConfig contextConfig;

    public Process(Kernel kernel, String simulationDirectory, ContextConfig contextConfig) {
        super(kernel);

        this.contextConfig = contextConfig;

        this.id = Simulation.currentProcessId++;
        kernel.getProcesses().add(this);

        this.stdinFileDescriptor = contextConfig.getSimulatedProgram().getStdin().length() > 0 ? NativeSyscalls.LIBC.open(simulationDirectory + File.separator + contextConfig.getSimulatedProgram().getStdin(), OpenFlags.O_RDONLY) : 0;
        this.stdoutFileDescriptor = contextConfig.getStdout().length() > 0 ? NativeSyscalls.LIBC.open(simulationDirectory + File.separator + contextConfig.getStdout(), OpenFlags.O_CREAT | OpenFlags.O_APPEND | OpenFlags.O_TRUNC | OpenFlags.O_WRONLY, 0660) : 1;

        this.envs = new ArrayList<String>();

//        ElfFile elfFile = new ElfFile(contextConfig.toCmdArgList().get(0)); //TODO

        this.littleEndian = false;

        this.memory = new BasicMemory(kernel, simulationDirectory, this.littleEndian, this.id);

        contextConfig.getSimulatedProgram().build();

        this.loadProgram(kernel, simulationDirectory, contextConfig);
    }

    protected abstract void loadProgram(Kernel kernel, String simulationDirectory, ContextConfig contextConfig);

    public int translateFileDescriptor(int fileDescriptor) {
        if (fileDescriptor == 1 || fileDescriptor == 2) {
            return this.stdoutFileDescriptor;
        } else if (fileDescriptor == 0) {
            return this.stdinFileDescriptor;
        } else {
            return fileDescriptor;
        }
    }

    public void closeProgram() {
        if (this.stdinFileDescriptor != 0) {
            NativeSyscalls.LIBC.close(this.stdinFileDescriptor);
        }
        if (this.stdoutFileDescriptor > 2) {
            NativeSyscalls.LIBC.close(this.stdoutFileDescriptor);
        }
    }

    protected StaticInstruction decode(int machInst) {
        for (Mnemonic mnemonic : StaticInstruction.machInstDecoderInfos) {
            BitField extraBitField = mnemonic.getExtraBitField();
            if ((machInst & mnemonic.getMask()) == mnemonic.getBits() && (extraBitField == null || extraBitField.valueOf(machInst) == mnemonic.getExtraBitFieldValue())) {
                return new StaticInstruction(mnemonic, machInst);
            }
        }

        throw new IllegalArgumentException();
    }

    public abstract StaticInstruction getStaticInst(int pc);

    public int getId() {
        return id;
    }

    public int getStdinFileDescriptor() {
        return stdinFileDescriptor;
    }

    public int getStdoutFileDescriptor() {
        return stdoutFileDescriptor;
    }

    public List<String> getEnvs() {
        return envs;
    }

    public int getStackBase() {
        return stackBase;
    }

    public void setStackBase(int stackBase) {
        this.stackBase = stackBase;
    }

    public int getStackSize() {
        return stackSize;
    }

    public void setStackSize(int stackSize) {
        this.stackSize = stackSize;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public int getEnvironBase() {
        return environBase;
    }

    public void setEnvironBase(int environBase) {
        this.environBase = environBase;
    }

    public int getHeapTop() {
        return heapTop;
    }

    public void setHeapTop(int heapTop) {
        this.heapTop = heapTop;
    }

    public int getDataTop() {
        return dataTop;
    }

    public void setDataTop(int dataTop) {
        this.dataTop = dataTop;
    }

    public int getProgramEntry() {
        return programEntry;
    }

    public void setProgramEntry(int programEntry) {
        this.programEntry = programEntry;
    }

    public boolean isLittleEndian() {
        return littleEndian;
    }

    public Memory getMemory() {
        return memory;
    }

    public ContextConfig getContextConfig() {
        return contextConfig;
    }

    public static final int TEXT_BASE = 0x00400000;
    public static final int DATA_BASE = 0x10000000;
    public static final int STACK_BASE = 0x7fffc000;
    public static final int STACK_SIZE = 1024 * 1024;
    public static final int MAX_ENVIRON = 16 * 1024;

    protected static int roundUp(int n, int alignment) {
        return (n + alignment - 1) & ~(alignment - 1);
    }
}
