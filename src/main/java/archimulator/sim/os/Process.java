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
package archimulator.sim.os;

import archimulator.model.ContextMapping;
import archimulator.service.ServiceManager;
import archimulator.sim.analysis.BasicBlock;
import archimulator.sim.analysis.ElfAnalyzer;
import archimulator.sim.analysis.Function;
import archimulator.sim.analysis.Instruction;
import archimulator.sim.common.BasicSimulationObject;
import archimulator.sim.common.SimulationObject;
import archimulator.sim.isa.*;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.io.cmd.CommandLineHelper;
import net.pickapack.io.cmd.SedHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.RandomUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Process.
 *
 * @author Min Cai
 */
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

    /**
     * Create a process.
     *
     * @param kernel              the kernel
     * @param outputDirectory     the output directory
     * @param contextMapping      the context mapping
     */
    public Process(Kernel kernel, String outputDirectory, ContextMapping contextMapping) {
        super(kernel);

        this.contextMapping = contextMapping;

        this.id = getExperiment().currentProcessId++;
        kernel.getProcesses().add(this);

        this.standardInFileDescriptor = contextMapping.getBenchmark().getStandardIn().length() > 0 ? NativeSystemCalls.LIBC.open(outputDirectory + File.separator + contextMapping.getBenchmark().getStandardIn(), OpenFlags.O_RDONLY) : 0;
        this.standardOutFileDescriptor = contextMapping.getStandardOut().length() > 0 ? NativeSystemCalls.LIBC.open(outputDirectory + File.separator + contextMapping.getStandardOut(), OpenFlags.O_CREAT | OpenFlags.O_APPEND | OpenFlags.O_TRUNC | OpenFlags.O_WRONLY, 0660) : 1;

        this.environments = new ArrayList<>();

        this.littleEndian = false;

        this.memory = new Memory(kernel, this.littleEndian, this.id);

        try {
            File file = new File(getTransformedBenchmarkWorkingDirectory(contextMapping.getBenchmark().getWorkingDirectory()) + "/archimulator_lock");
            FileChannel channel = new RandomAccessFile(file, "rw").getChannel();

            FileLock lock = null;

            for (; ; ) {
                try {
                    lock = channel.tryLock();
                } catch (OverlappingFileLockException e) {
                    // File is already locked in this thread or virtual machine
                }

                if (lock != null) {
                    break;
                }

                Thread.sleep(RandomUtils.nextInt(10) * 1000);
            }

            buildBenchmark(
                    contextMapping.getBenchmark().getWorkingDirectory(),
                    contextMapping.getBenchmark().getHelperThreadEnabled(),
                    contextMapping.getHelperThreadLookahead(), contextMapping.getHelperThreadStride()
            );
            this.loadProgram(kernel, contextMapping);

            lock.release();

            channel.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Load the program.
     *  @param kernel              the kernel
     * @param contextMapping      the context mapping
     */
    protected abstract void loadProgram(Kernel kernel, ContextMapping contextMapping);

    /**
     * Translate the specified file descriptor number.
     *
     * @param fileDescriptor the file descriptor number
     * @return the translated file descriptor number
     */
    public int translateFileDescriptor(int fileDescriptor) {
        if (fileDescriptor == 1 || fileDescriptor == 2) {
            return this.standardOutFileDescriptor;
        } else if (fileDescriptor == 0) {
            return this.standardInFileDescriptor;
        } else {
            return fileDescriptor;
        }
    }

    /**
     * Close the program.
     */
    public void closeProgram() {
        if (this.standardInFileDescriptor != 0) {
            NativeSystemCalls.LIBC.close(this.standardInFileDescriptor);
        }
        if (this.standardOutFileDescriptor > 2) {
            NativeSystemCalls.LIBC.close(this.standardOutFileDescriptor);
        }
    }

    /**
     * Decode the specified machine instruction.
     *
     * @param machineInstruction the machine instruction
     * @return the decoded static instruction
     */
    protected StaticInstruction decode(int machineInstruction) {
        for (Mnemonic mnemonic : StaticInstruction.MNEMONICS) {
            BitField extraBitField = mnemonic.getExtraBitField();
            if ((machineInstruction & mnemonic.getMask()) == mnemonic.getBits() && (extraBitField == null || extraBitField.valueOf(machineInstruction) == mnemonic.getExtraBitFieldValue())) {
                return new StaticInstruction(mnemonic, machineInstruction);
            }
        }

        throw new IllegalArgumentException();
    }

    /**
     * Get the static instruction at the specified program counter (PC).
     *
     * @param pc the program counter (PC)
     * @return the static instruction at the specified program counter (PC)
     */
    public abstract StaticInstruction getStaticInstruction(int pc);

    /**
     * Get the ID of the process.
     *
     * @return the ID of the process
     */
    public int getId() {
        return id;
    }

    /**
     * Get the standard in file descriptor.
     *
     * @return the standard in file descriptor
     */
    public int getStandardInFileDescriptor() {
        return standardInFileDescriptor;
    }

    /**
     * Get the standard out file descriptor.
     *
     * @return the standard out file descriptor
     */
    public int getStandardOutFileDescriptor() {
        return standardOutFileDescriptor;
    }

    /**
     * Get the list of environment variables.
     *
     * @return the list of environment variables
     */
    public List<String> getEnvironments() {
        return environments;
    }

    /**
     * Get the stack base.
     *
     * @return the stack base
     */
    public int getStackBase() {
        return stackBase;
    }

    /**
     * Set the stack base.
     *
     * @param stackBase the stack base
     */
    public void setStackBase(int stackBase) {
        this.stackBase = stackBase;
    }

    /**
     * Get the stack size.
     *
     * @return the stack size
     */
    public int getStackSize() {
        return stackSize;
    }

    /**
     * Set the stack size.
     *
     * @param stackSize the stack size
     */
    public void setStackSize(int stackSize) {
        this.stackSize = stackSize;
    }

    /**
     * Get the text size.
     *
     * @return the text size
     */
    public int getTextSize() {
        return textSize;
    }

    /**
     * Set the text size.
     *
     * @param textSize the text size
     */
    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    /**
     * Get the environment base.
     *
     * @return the environment base
     */
    public int getEnvironmentBase() {
        return environmentBase;
    }

    /**
     * Set the environment base.
     *
     * @param environmentBase the environment base
     */
    public void setEnvironmentBase(int environmentBase) {
        this.environmentBase = environmentBase;
    }

    /**
     * Get the heap top.
     *
     * @return the heap top
     */
    public int getHeapTop() {
        return heapTop;
    }

    /**
     * Set the heap top.
     *
     * @param heapTop the heap top
     */
    public void setHeapTop(int heapTop) {
        this.heapTop = heapTop;
    }

    /**
     * Get the data top.
     *
     * @return the data top
     */
    public int getDataTop() {
        return dataTop;
    }

    /**
     * Set the data top.
     *
     * @param dataTop the data top
     */
    public void setDataTop(int dataTop) {
        this.dataTop = dataTop;
    }

    /**
     * Get the program entry.
     *
     * @return the program entry
     */
    public int getProgramEntry() {
        return programEntry;
    }

    /**
     * Set the program entry.
     *
     * @param programEntry the program entry
     */
    public void setProgramEntry(int programEntry) {
        this.programEntry = programEntry;
    }

    /**
     * Get a value indicating whether the process is little endian or not.
     *
     * @return a value indicating whether the process is little endian or not
     */
    public boolean isLittleEndian() {
        return littleEndian;
    }

    /**
     * Get the memory.
     *
     * @return the memory
     */
    public Memory getMemory() {
        return memory;
    }

    /**
     * Get the context mapping.
     *
     * @return the context mapping
     */
    public ContextMapping getContextMapping() {
        return contextMapping;
    }

    /**
     * Get the name of the process.
     *
     * @return the name of the process
     */
    @Override
    public String getName() {
        return "ctx" + getContextMapping().getThreadId() + "/process";
    }

    /**
     * Text base.
     */
    public static final int TEXT_BASE = 0x00400000;

    /**
     * Data base.
     */
    public static final int DATA_BASE = 0x10000000;

    /**
     * Stack base.
     */
    public static final int STACK_BASE = 0x7fffc000;

    /**
     * Stack size.
     */
    public static final int STACK_SIZE = 1024 * 1024;

    /**
     * Maximum environment.
     */
    public static final int MAX_ENVIRON = 16 * 1024;

    /**
     * Round up.
     *
     * @param n         the number
     * @param alignment th alignment
     * @return the result
     */
    protected static int roundUp(int n, int alignment) {
        return (n + alignment - 1) & ~(alignment - 1);
    }

    /**
     * Build the benchmark.
     *
     * @param workingDirectory      the working directory
     * @param helperThreadEnabled   a value indicating whether the helper threading is enabled or not
     * @param helperThreadLookahead the helper thread lookahead
     * @param helperThreadStride    the helper thread stride
     */
    private static void buildBenchmark(String workingDirectory, boolean helperThreadEnabled, int helperThreadLookahead, int helperThreadStride) {
        if (helperThreadEnabled) {
            pushMacroDefineArg(workingDirectory, "push_params.h", "LOOKAHEAD", helperThreadLookahead + "");
            pushMacroDefineArg(workingDirectory, "push_params.h", "STRIDE", helperThreadStride + "");
        }
        buildWithMakeFile(workingDirectory);
    }

    /**
     * Push the macro #define argument.
     *
     * @param workingDirectory the working directory
     * @param fileName         the file name
     * @param key              the key
     * @param value            the value
     */
    private static void pushMacroDefineArg(String workingDirectory, String fileName, String key, String value) {
        fileName = getTransformedBenchmarkWorkingDirectory(workingDirectory) + "/" + fileName;
        System.err.printf("[%s] Pushing Macro Define Arg in %s: %s, %s\n", DateHelper.toString(new Date()), fileName, key, value);
        List<String> result = SedHelper.sedInPlace(fileName, "#define " + key, "#define " + key + " " + value);
        for (String line : result) {
            System.err.println(line);
        }
    }

    /**
     * Build the benchmark using a Makefile.
     *
     * @param workingDirectory the working directory
     */
    private static void buildWithMakeFile(String workingDirectory) {
        System.err.printf("[%s] Building with Makefile\n", DateHelper.toString(new Date()));
        String transformedBenchmarkWorkingDirectory = getTransformedBenchmarkWorkingDirectory(workingDirectory);
        System.err.printf("cd %s\n", transformedBenchmarkWorkingDirectory);
        List<String> result = CommandLineHelper.invokeShellCommandAndGetResult("sh -c 'cd " + transformedBenchmarkWorkingDirectory +
                ";make -f Makefile.mips -B CROSS_COMPILER_ROOT=" + getCurrentDirectory() + "/tools/cross_compiler'");
        for (String line : result) {
            System.err.println(line);
        }
    }

    /**
     * Get the transformed benchmark's working directory with parameters filled in.
     *
     * @param workingDirectory the working directory
     * @return the transformed benchmark's working directory with parameters filled in
     */
    private static String getTransformedBenchmarkWorkingDirectory(String workingDirectory) {
        return workingDirectory.replaceAll(ServiceManager.USER_HOME_TEMPLATE_ARG, FileUtils.getUserDirectoryPath());
    }

    /**
     * Get the current directory.
     *
     * @return the current directory
     */
    private static String getCurrentDirectory() {
        try {
            return new File(".").getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the ELF file analyzer.
     *
     * @return the ELF file analyzer
     */
    public abstract ElfAnalyzer getElfAnalyzer();

    /**
     * Get the function name from the process by PC.
     *
     * @param pc the program counter (PC) value
     * @return the function name from the process if any exists; otherwise null
     */
    public abstract String getFunctionNameFromPc(int pc);

    /**
     * Get the list of hotspot functions in the current process.
     *
     * @return the list of hotspot functions in the current process
     */
    public List<Function> getHotspotFunctions() {
        return this.getElfAnalyzer().getProgram().getFunctions().stream().filter(this::isHotspotFunction).collect(Collectors.toList());
    }

    /**
     * Get a value indicating whether the specified function is a hotspot function or not.
     *
     * @param function the function
     * @return a value indicating whether the specified function is a hotspot function or not
     */
    public boolean isHotspotFunction(Function function) {
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            for (Instruction instruction : basicBlock.getInstructions()) {
                PseudoCall pseudoCall = StaticInstruction.getPseudoCall(instruction.getStaticInstruction().getMachineInstruction());
                if (pseudoCall != null && pseudoCall.getImm() == PseudoCall.PSEUDOCALL_HOTSPOT_FUNCTION_BEGIN) {
                    return true;
                }
            }
        }

        return false;
    }
}
