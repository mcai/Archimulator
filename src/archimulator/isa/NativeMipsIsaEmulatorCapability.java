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
package archimulator.isa;

import archimulator.os.Kernel;
import archimulator.os.KernelCapability;
import archimulator.os.KernelCapabilityFactory;
import com.sun.jna.Native;

import java.io.IOException;
import java.io.ObjectInputStream;

public class NativeMipsIsaEmulatorCapability implements KernelCapability {
    private transient NativeMipsIsaEmulatorInterface nativeMipsIsaEmulator;

    //WARN: should keep JNACallback as a field to prevent from garbage collected!!!
    private transient NativeMipsIsaEmulatorInterface.GetGprCallback getGprCallback;
    private transient NativeMipsIsaEmulatorInterface.SetGprCallback setGprCallback;
    private transient NativeMipsIsaEmulatorInterface.GetFprsCallback getFprsCallback;
    private transient NativeMipsIsaEmulatorInterface.SetFprsCallback setFprsCallback;
    private transient NativeMipsIsaEmulatorInterface.GetFprdCallback getFprdCallback;
    private transient NativeMipsIsaEmulatorInterface.SetFprdCallback setFprdCallback;
    private transient NativeMipsIsaEmulatorInterface.GetFirCallback getFirCallback;
    private transient NativeMipsIsaEmulatorInterface.SetFirCallback setFirCallback;
    private transient NativeMipsIsaEmulatorInterface.GetFcsrCallback getFcsrCallback;
    private transient NativeMipsIsaEmulatorInterface.SetFcsrCallback setFcsrCallback;
    private transient NativeMipsIsaEmulatorInterface.GetHiCallback getHiCallback;
    private transient NativeMipsIsaEmulatorInterface.SetHiCallback setHiCallback;
    private transient NativeMipsIsaEmulatorInterface.GetLoCallback getLoCallback;
    private transient NativeMipsIsaEmulatorInterface.SetLoCallback setLoCallback;
    private transient NativeMipsIsaEmulatorInterface.GetPcCallback getPcCallback;
    private transient NativeMipsIsaEmulatorInterface.SetPcCallback setPcCallback;
    private transient NativeMipsIsaEmulatorInterface.GetNpcCallback getNpcCallback;
    private transient NativeMipsIsaEmulatorInterface.SetNpcCallback setNpcCallback;
    private transient NativeMipsIsaEmulatorInterface.GetNnpcCallback getNnpcCallback;
    private transient NativeMipsIsaEmulatorInterface.SetNnpcCallback setNnpcCallback;
    private transient NativeMipsIsaEmulatorInterface.MemReadByteCallback memReadByteCallback;
    private transient NativeMipsIsaEmulatorInterface.MemReadHalfWordCallback memReadHalfWordCallback;
    private transient NativeMipsIsaEmulatorInterface.MemReadWordCallback memReadWordCallback;
    private transient NativeMipsIsaEmulatorInterface.MemReadDoubleWordCallback memReadDoubleWordCallback;
    private transient NativeMipsIsaEmulatorInterface.MemWriteByteCallback memWriteByteCallback;
    private transient NativeMipsIsaEmulatorInterface.MemWriteHalfWordCallback memWriteHalfWordCallback;
    private transient NativeMipsIsaEmulatorInterface.MemWriteWordCallback memWriteWordCallback;
    private transient NativeMipsIsaEmulatorInterface.MemWriteDoubleWordCallback memWriteDoubleWordCallback;
    private transient NativeMipsIsaEmulatorInterface.GetStackBaseCallback getStackBaseCallback;
    private transient NativeMipsIsaEmulatorInterface.SetStackBaseCallback setStackBaseCallback;
    private transient NativeMipsIsaEmulatorInterface.GetStackSizeCallback getStackSizeCallback;
    private transient NativeMipsIsaEmulatorInterface.SetStackSizeCallback setStackSizeCallback;
    private transient NativeMipsIsaEmulatorInterface.GetTextSizeCallback getTextSizeCallback;
    private transient NativeMipsIsaEmulatorInterface.SetTextSizeCallback setTextSizeCallback;
    private transient NativeMipsIsaEmulatorInterface.GetEnvironBaseCallback getEnvironBaseCallback;
    private transient NativeMipsIsaEmulatorInterface.SetEnvironBaseCallback setEnvironBaseCallback;
    private transient NativeMipsIsaEmulatorInterface.GetHeapTopCallback getHeapTopCallback;
    private transient NativeMipsIsaEmulatorInterface.SetHeapTopCallback setHeapTopCallback;
    private transient NativeMipsIsaEmulatorInterface.GetDataTopCallback getDataTopCallback;
    private transient NativeMipsIsaEmulatorInterface.SetDataTopCallback setDataTopCallback;
    private transient NativeMipsIsaEmulatorInterface.GetProgramEntryCallback getProgramEntryCallback;
    private transient NativeMipsIsaEmulatorInterface.SetProgramEntryCallback setProgramEntryCallback;
    private transient NativeMipsIsaEmulatorInterface.GetProcessIdFromContextIdCallback getProcessIdFromContextIdCallback;

    private Kernel kernel;

    public NativeMipsIsaEmulatorCapability(Kernel kernel) {
        this.kernel = kernel;

        this.init();
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();

        this.init();
    }

    private void init() {
        this.nativeMipsIsaEmulator = (NativeMipsIsaEmulatorInterface) Native.loadLibrary(LIBRARY_NAME, NativeMipsIsaEmulatorInterface.class);

        this.getGprCallback = new NativeMipsIsaEmulatorInterface.GetGprCallback() {
            public int invoke(int ctx, int index) {
                return kernel.getContextFromId(ctx).getRegs().getGpr(index);
            }
        };
        this.nativeMipsIsaEmulator.initGetGprCallback(getGprCallback);

        this.setGprCallback = new NativeMipsIsaEmulatorInterface.SetGprCallback() {
            public void invoke(int ctx, int index, int value) {
                kernel.getContextFromId(ctx).getRegs().setGpr(index, value);
            }
        };
        this.nativeMipsIsaEmulator.initSetGprCallback(setGprCallback);

        this.getFprsCallback = new NativeMipsIsaEmulatorInterface.GetFprsCallback() {
            public float invoke(int ctx, int index) {
                return kernel.getContextFromId(ctx).getRegs().getFpr().getFloat(index);
            }
        };
        this.nativeMipsIsaEmulator.initGetFprsCallback(getFprsCallback);

        this.setFprsCallback = new NativeMipsIsaEmulatorInterface.SetFprsCallback() {
            public void invoke(int ctx, int index, float value) {
                kernel.getContextFromId(ctx).getRegs().getFpr().setFloat(index, value);
            }
        };
        this.nativeMipsIsaEmulator.initSetFprsCallback(setFprsCallback);

        this.getFprdCallback = new NativeMipsIsaEmulatorInterface.GetFprdCallback() {
            public double invoke(int ctx, int index) {
                return kernel.getContextFromId(ctx).getRegs().getFpr().getDouble(index);
            }
        };
        this.nativeMipsIsaEmulator.initGetFprdCallback(getFprdCallback);

        this.setFprdCallback = new NativeMipsIsaEmulatorInterface.SetFprdCallback() {
            public void invoke(int ctx, int index, double value) {
                kernel.getContextFromId(ctx).getRegs().getFpr().setDouble(index, value);
            }
        };
        this.nativeMipsIsaEmulator.initSetFprdCallback(setFprdCallback);

        this.getFirCallback = new NativeMipsIsaEmulatorInterface.GetFirCallback() {
            public int invoke(int ctx) {
                throw new UnsupportedOperationException(); //TODO
            }
        };
        this.nativeMipsIsaEmulator.initGetFirCallback(getFirCallback);

        this.setFirCallback = new NativeMipsIsaEmulatorInterface.SetFirCallback() {
            public void invoke(int ctx, int value) {
                throw new UnsupportedOperationException(); //TODO
            }
        };
        this.nativeMipsIsaEmulator.initSetFirCallback(setFirCallback);

        this.getFcsrCallback = new NativeMipsIsaEmulatorInterface.GetFcsrCallback() {
            public int invoke(int ctx) {
                return kernel.getContextFromId(ctx).getRegs().getFcsr();
            }
        };
        this.nativeMipsIsaEmulator.initGetFcsrCallback(getFcsrCallback);

        this.setFcsrCallback = new NativeMipsIsaEmulatorInterface.SetFcsrCallback() {
            public void invoke(int ctx, int value) {
                kernel.getContextFromId(ctx).getRegs().setFcsr(value);
            }
        };
        this.nativeMipsIsaEmulator.initSetFcsrCallback(setFcsrCallback);

        this.getHiCallback = new NativeMipsIsaEmulatorInterface.GetHiCallback() {
            public int invoke(int ctx) {
                return kernel.getContextFromId(ctx).getRegs().getHi();
            }
        };
        this.nativeMipsIsaEmulator.initGetHiCallback(getHiCallback);

        this.setHiCallback = new NativeMipsIsaEmulatorInterface.SetHiCallback() {
            public void invoke(int ctx, int value) {
                kernel.getContextFromId(ctx).getRegs().setHi(value);
            }
        };
        this.nativeMipsIsaEmulator.initSetHiCallback(setHiCallback);

        this.getLoCallback = new NativeMipsIsaEmulatorInterface.GetLoCallback() {
            public int invoke(int ctx) {
                return kernel.getContextFromId(ctx).getRegs().getLo();
            }
        };
        this.nativeMipsIsaEmulator.initGetLoCallback(getLoCallback);

        this.setLoCallback = new NativeMipsIsaEmulatorInterface.SetLoCallback() {
            public void invoke(int ctx, int value) {
                kernel.getContextFromId(ctx).getRegs().setLo(value);
            }
        };
        this.nativeMipsIsaEmulator.initSetLoCallback(setLoCallback);

        this.getPcCallback = new NativeMipsIsaEmulatorInterface.GetPcCallback() {
            public int invoke(int ctx) {
                return kernel.getContextFromId(ctx).getRegs().getPc();
            }
        };
        this.nativeMipsIsaEmulator.initGetPcCallback(getPcCallback);

        this.setPcCallback = new NativeMipsIsaEmulatorInterface.SetPcCallback() {
            public void invoke(int ctx, int value) {
                kernel.getContextFromId(ctx).getRegs().setPc(value);
            }
        };
        this.nativeMipsIsaEmulator.initSetPcCallback(setPcCallback);

        this.getNpcCallback = new NativeMipsIsaEmulatorInterface.GetNpcCallback() {
            public int invoke(int ctx) {
                return kernel.getContextFromId(ctx).getRegs().getNpc();
            }
        };
        this.nativeMipsIsaEmulator.initGetNpcCallback(getNpcCallback);

        this.setNpcCallback = new NativeMipsIsaEmulatorInterface.SetNpcCallback() {
            public void invoke(int ctx, int value) {
                kernel.getContextFromId(ctx).getRegs().setNpc(value);
            }
        };
        this.nativeMipsIsaEmulator.initSetNpcCallback(setNpcCallback);

        this.getNnpcCallback = new NativeMipsIsaEmulatorInterface.GetNnpcCallback() {
            public int invoke(int ctx) {
                return kernel.getContextFromId(ctx).getRegs().getNnpc();
            }
        };
        this.nativeMipsIsaEmulator.initGetNnpcCallback(getNnpcCallback);

        this.setNnpcCallback = new NativeMipsIsaEmulatorInterface.SetNnpcCallback() {
            public void invoke(int ctx, int value) {
                kernel.getContextFromId(ctx).getRegs().setNnpc(value);
            }
        };
        this.nativeMipsIsaEmulator.initSetNnpcCallback(setNnpcCallback);

        this.memReadByteCallback = new NativeMipsIsaEmulatorInterface.MemReadByteCallback() {
            public byte invoke(int processId, int addr) {
                return kernel.getProcessFromId(processId).getMemory().readByte(addr);
            }
        };
        this.nativeMipsIsaEmulator.initMemReadByteCallback(memReadByteCallback);

        this.memReadHalfWordCallback = new NativeMipsIsaEmulatorInterface.MemReadHalfWordCallback() {
            public short invoke(int processId, int addr) {
                return kernel.getProcessFromId(processId).getMemory().readHalfWord(addr);
            }
        };
        this.nativeMipsIsaEmulator.initMemReadHalfWordCallback(memReadHalfWordCallback);

        this.memReadWordCallback = new NativeMipsIsaEmulatorInterface.MemReadWordCallback() {
            public int invoke(int processId, int addr) {
                return kernel.getProcessFromId(processId).getMemory().readWord(addr);
            }
        };
        this.nativeMipsIsaEmulator.initMemReadWordCallback(memReadWordCallback);

        this.memReadDoubleWordCallback = new NativeMipsIsaEmulatorInterface.MemReadDoubleWordCallback() {
            public long invoke(int processId, int addr) {
                return kernel.getProcessFromId(processId).getMemory().readDoubleWord(addr);
            }
        };
        this.nativeMipsIsaEmulator.initMemReadDoubleWordCallback(memReadDoubleWordCallback);

        this.memWriteByteCallback = new NativeMipsIsaEmulatorInterface.MemWriteByteCallback() {
            public void invoke(int processId, int addr, byte data) {
                kernel.getProcessFromId(processId).getMemory().writeByte(addr, data);
            }
        };
        this.nativeMipsIsaEmulator.initMemWriteByteCallback(memWriteByteCallback);

        this.memWriteHalfWordCallback = new NativeMipsIsaEmulatorInterface.MemWriteHalfWordCallback() {
            public void invoke(int processId, int addr, short data) {
                kernel.getProcessFromId(processId).getMemory().writeHalfWord(addr, data);
            }
        };
        this.nativeMipsIsaEmulator.initMemWriteHalfWordCallback(memWriteHalfWordCallback);

        this.memWriteWordCallback = new NativeMipsIsaEmulatorInterface.MemWriteWordCallback() {
            public void invoke(int processId, int addr, int data) {
                kernel.getProcessFromId(processId).getMemory().writeWord(addr, data);
            }
        };
        this.nativeMipsIsaEmulator.initMemWriteWordCallback(memWriteWordCallback);

        this.memWriteDoubleWordCallback = new NativeMipsIsaEmulatorInterface.MemWriteDoubleWordCallback() {
            public void invoke(int processId, int addr, long data) {
                kernel.getProcessFromId(processId).getMemory().writeDoubleWord(addr, data);
            }
        };
        this.nativeMipsIsaEmulator.initMemWriteDoubleWordCallback(memWriteDoubleWordCallback);

        this.getStackBaseCallback = new NativeMipsIsaEmulatorInterface.GetStackBaseCallback() {
            public int invoke(int processId) {
                return kernel.getProcessFromId(processId).getStackBase();
            }
        };
        this.nativeMipsIsaEmulator.initGetStackBaseCallback(getStackBaseCallback);

        this.setStackBaseCallback = new NativeMipsIsaEmulatorInterface.SetStackBaseCallback() {
            public void invoke(int processId, int value) {
                kernel.getProcessFromId(processId).setStackBase(value);
            }
        };
        this.nativeMipsIsaEmulator.initSetStackBaseCallback(setStackBaseCallback);

        this.getStackSizeCallback = new NativeMipsIsaEmulatorInterface.GetStackSizeCallback() {
            public int invoke(int processId) {
                return kernel.getProcessFromId(processId).getStackSize();
            }
        };
        this.nativeMipsIsaEmulator.initGetStackSizeCallback(getStackSizeCallback);

        this.setStackSizeCallback = new NativeMipsIsaEmulatorInterface.SetStackSizeCallback() {
            public void invoke(int processId, int value) {
                kernel.getProcessFromId(processId).setStackSize(value);
            }
        };
        this.nativeMipsIsaEmulator.initSetStackSizeCallback(setStackSizeCallback);

        this.getTextSizeCallback = new NativeMipsIsaEmulatorInterface.GetTextSizeCallback() {
            public int invoke(int processId) {
                return kernel.getProcessFromId(processId).getTextSize();
            }
        };
        this.nativeMipsIsaEmulator.initGetTextSizeCallback(getTextSizeCallback);

        this.setTextSizeCallback = new NativeMipsIsaEmulatorInterface.SetTextSizeCallback() {
            public void invoke(int processId, int value) {
                kernel.getProcessFromId(processId).setTextSize(value);
            }
        };
        this.nativeMipsIsaEmulator.initSetTextSizeCallback(setTextSizeCallback);

        this.getEnvironBaseCallback = new NativeMipsIsaEmulatorInterface.GetEnvironBaseCallback() {
            public int invoke(int processId) {
                return kernel.getProcessFromId(processId).getEnvironBase();
            }
        };
        this.nativeMipsIsaEmulator.initGetEnvironBaseCallback(getEnvironBaseCallback);

        this.setEnvironBaseCallback = new NativeMipsIsaEmulatorInterface.SetEnvironBaseCallback() {
            public void invoke(int processId, int value) {
                kernel.getProcessFromId(processId).setEnvironBase(value);
            }
        };
        this.nativeMipsIsaEmulator.initSetEnvironBaseCallback(setEnvironBaseCallback);

        this.getHeapTopCallback = new NativeMipsIsaEmulatorInterface.GetHeapTopCallback() {
            public int invoke(int processId) {
                return kernel.getProcessFromId(processId).getHeapTop();
            }
        };
        this.nativeMipsIsaEmulator.initGetHeapTopCallback(getHeapTopCallback);

        this.setHeapTopCallback = new NativeMipsIsaEmulatorInterface.SetHeapTopCallback() {
            public void invoke(int processId, int value) {
                kernel.getProcessFromId(processId).setHeapTop(value);
            }
        };
        this.nativeMipsIsaEmulator.initSetHeapTopCallback(setHeapTopCallback);

        this.getDataTopCallback = new NativeMipsIsaEmulatorInterface.GetDataTopCallback() {
            public int invoke(int processId) {
                return kernel.getProcessFromId(processId).getDataTop();
            }
        };
        this.nativeMipsIsaEmulator.initGetDataTopCallback(getDataTopCallback);

        this.setDataTopCallback = new NativeMipsIsaEmulatorInterface.SetDataTopCallback() {
            public void invoke(int processId, int value) {
                kernel.getProcessFromId(processId).setDataTop(value);
            }
        };
        this.nativeMipsIsaEmulator.initSetDataTopCallback(setDataTopCallback);

        this.getProgramEntryCallback = new NativeMipsIsaEmulatorInterface.GetProgramEntryCallback() {
            public int invoke(int processId) {
                return kernel.getProcessFromId(processId).getProgramEntry();
            }
        };
        this.nativeMipsIsaEmulator.initGetProgramEntryCallback(getProgramEntryCallback);

        this.setProgramEntryCallback = new NativeMipsIsaEmulatorInterface.SetProgramEntryCallback() {
            public void invoke(int processId, int value) {
                kernel.getProcessFromId(processId).setProgramEntry(value);
            }
        };
        this.nativeMipsIsaEmulator.initSetProgramEntryCallback(setProgramEntryCallback);

        this.getProcessIdFromContextIdCallback = new NativeMipsIsaEmulatorInterface.GetProcessIdFromContextIdCallback() {
            public int invoke(int ctx) {
                return kernel.getContextFromId(ctx).getProcess().getId();
            }
        };
        this.nativeMipsIsaEmulator.initGetProcessIdFromContextIdCallback(getProcessIdFromContextIdCallback);
    }

    public NativeMipsIsaEmulatorInterface decode(int machInst) {
        this.nativeMipsIsaEmulator.decode_inst(machInst);
        return this.nativeMipsIsaEmulator;
    }

    public void load_prog(int processId, int argc, String[] argv) {
        this.nativeMipsIsaEmulator.load_prog(processId, argc, argv);
    }

    public static final KernelCapabilityFactory FACTORY = new KernelCapabilityFactory() {
        public KernelCapability createCapability(Kernel kernel) {
            return new NativeMipsIsaEmulatorCapability(kernel);
        }
    };

    public NativeMipsIsaEmulatorInterface getNativeMipsIsaEmulator() {
        return nativeMipsIsaEmulator;
    }

    public static final String LIBRARY_NAME = "mips_isa_emulator";
}
