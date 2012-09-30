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

import archimulator.model.ContextMapping;
import archimulator.sim.common.BasicSimulationObject;
import archimulator.sim.common.SimulationObject;
import archimulator.sim.isa.BitField;
import archimulator.sim.isa.Memory;
import archimulator.sim.isa.Mnemonic;
import archimulator.sim.isa.StaticInstruction;
import archimulator.util.ExperimentHelper;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.io.cmd.CommandLineHelper;
import net.pickapack.io.cmd.SedHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.RandomUtils;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class Process extends BasicSimulationObject implements SimulationObject, Serializable {
    private List<String> environments;

    private int standardInFileDescriptor;
    private int standardOutFileDescriptor;

    private int stackBase;
    private int stackSize;
    private int textSize;

    private int environmentBase;
    private int heapTop;
    private int dataTop;

    private int programEntry;

    private boolean littleEndian;

    private Memory memory;

    private int id;

    private ContextMapping contextMapping;

    public Process(Kernel kernel, String simulationDirectory, ContextMapping contextMapping) {
        super(kernel);

        this.contextMapping = contextMapping;

        this.id = getExperiment().currentProcessId++;
        kernel.getProcesses().add(this);

        this.standardInFileDescriptor = contextMapping.getBenchmark().getStandardIn().length() > 0 ? NativeSystemCalls.LIBC.open(simulationDirectory + File.separator + contextMapping.getBenchmark().getStandardIn(), OpenFlags.O_RDONLY) : 0;
        this.standardOutFileDescriptor = contextMapping.getStandardOut().length() > 0 ? NativeSystemCalls.LIBC.open(simulationDirectory + File.separator + contextMapping.getStandardOut(), OpenFlags.O_CREAT | OpenFlags.O_APPEND | OpenFlags.O_TRUNC | OpenFlags.O_WRONLY, 0660) : 1;

        this.environments = new ArrayList<String>();

        this.littleEndian = false;

        this.memory = new Memory(kernel, this.littleEndian, this.id);

        try {
            File file = new File(getTransformedBenchmarkWorkingDirectory(contextMapping.getBenchmark().getWorkingDirectory()) + "/archimulator_lock");
            FileChannel channel = new RandomAccessFile(file, "rw").getChannel();

            FileLock lock = null;

            for(;;) {
                try {
                    lock = channel.tryLock();
                } catch (OverlappingFileLockException e) {
                    // File is already locked in this thread or virtual machine
                }

                if(lock != null) {
                    break;
                }

                Thread.sleep(RandomUtils.nextInt(10) * 1000);
            }

            buildBenchmark(contextMapping.getBenchmark().getWorkingDirectory(), contextMapping.getBenchmark().getHelperThreadEnabled(), contextMapping.getHelperThreadLookahead(), contextMapping.getHelperThreadStride());
            this.loadProgram(kernel, simulationDirectory, contextMapping);

            lock.release();

            channel.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    protected abstract void loadProgram(Kernel kernel, String simulationDirectory, ContextMapping contextMapping);

    public int translateFileDescriptor(int fileDescriptor) {
        if (fileDescriptor == 1 || fileDescriptor == 2) {
            return this.standardOutFileDescriptor;
        } else if (fileDescriptor == 0) {
            return this.standardInFileDescriptor;
        } else {
            return fileDescriptor;
        }
    }

    public void closeProgram() {
        if (this.standardInFileDescriptor != 0) {
            NativeSystemCalls.LIBC.close(this.standardInFileDescriptor);
        }
        if (this.standardOutFileDescriptor > 2) {
            NativeSystemCalls.LIBC.close(this.standardOutFileDescriptor);
        }
    }

    protected StaticInstruction decode(int machineInstruction) {
        for (Mnemonic mnemonic : StaticInstruction.machineInstructionDecoderInfos) {
            BitField extraBitField = mnemonic.getExtraBitField();
            if ((machineInstruction & mnemonic.getMask()) == mnemonic.getBits() && (extraBitField == null || extraBitField.valueOf(machineInstruction) == mnemonic.getExtraBitFieldValue())) {
                return new StaticInstruction(mnemonic, machineInstruction);
            }
        }

        throw new IllegalArgumentException();
    }

    public abstract StaticInstruction getStaticInstruction(int pc);

    public int getId() {
        return id;
    }

    public int getStandardInFileDescriptor() {
        return standardInFileDescriptor;
    }

    public int getStandardOutFileDescriptor() {
        return standardOutFileDescriptor;
    }

    public List<String> getEnvironments() {
        return environments;
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

    public int getEnvironmentBase() {
        return environmentBase;
    }

    public void setEnvironmentBase(int environmentBase) {
        this.environmentBase = environmentBase;
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

    public ContextMapping getContextMapping() {
        return contextMapping;
    }

    public static final int TEXT_BASE = 0x00400000;
    public static final int DATA_BASE = 0x10000000;
    public static final int STACK_BASE = 0x7fffc000;
    public static final int STACK_SIZE = 1024 * 1024;
    public static final int MAX_ENVIRON = 16 * 1024;

    protected static int roundUp(int n, int alignment) {
        return (n + alignment - 1) & ~(alignment - 1);
    }

    private static void buildBenchmark(String workingDirectory, boolean helperThreadEnabled, int helperThreadLookahead, int helperThreadStride) {
        if (helperThreadEnabled) {
            pushMacroDefineArg(workingDirectory, "push_params.h", "LOOKAHEAD", helperThreadLookahead + "");
            pushMacroDefineArg(workingDirectory, "push_params.h", "STRIDE", helperThreadStride + "");
        }
        buildWithMakeFile(workingDirectory);
    }

    private static void pushMacroDefineArg(String workingDirectory, String fileName, String key, String value) {
        fileName = getTransformedBenchmarkWorkingDirectory(workingDirectory) + "/" + fileName;
        System.err.printf("[%s] Pushing Macro Define Arg in %s: %s, %s\n", DateHelper.toString(new Date()), fileName, key, value);
        List<String> result = SedHelper.sedInPlace(fileName, "#define " + key, "#define " + key + " " + value);
        for (String line : result) {
            System.err.println(line);
        }
    }

    private static void buildWithMakeFile(String workingDirectory) {
        System.err.printf("[%s] Building with Makefile\n", DateHelper.toString(new Date()));
        List<String> result = CommandLineHelper.invokeShellCommandAndGetResult("sh -c 'cd " + getTransformedBenchmarkWorkingDirectory(workingDirectory) + ";make -f Makefile.mips -B'");
        for (String line : result) {
            System.err.println(line);
        }
    }

    private static String getTransformedBenchmarkWorkingDirectory(String workingDirectory) {
        return workingDirectory.replaceAll(ExperimentHelper.USER_HOME_TEMPLATE_ARG, FileUtils.getUserDirectoryPath());
    }
}
