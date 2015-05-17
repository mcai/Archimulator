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
package archimulator.sim.isa;

import net.pickapack.util.Pair;

import java.util.Arrays;

/**
 * Architectural register file.
 *
 * @author Min Cai
 */
public final class ArchitecturalRegisterFile implements Cloneable {
    /**
     * Floating point registers.
     */
    public static class FloatingPointRegisters implements Cloneable {
        private int[] data = new int[32];
        private boolean littleEndian;

        /**
         * Create a set of floating point registers.
         *
         * @param littleEndian a value indicating whether the set of floating point registers is little endian or not.
         */
        public FloatingPointRegisters(boolean littleEndian) {
            this.littleEndian = littleEndian;
        }

        /**
         * Create a set of floating point registers.
         *
         * @param littleEndian a value indicating whether the set of floating point registers is little endian or not.
         * @param data         the data array
         */
        public FloatingPointRegisters(boolean littleEndian, int[] data) {
            this.littleEndian = littleEndian;
            this.data = data;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return new FloatingPointRegisters(this.littleEndian, this.data.clone());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FloatingPointRegisters fpr = (FloatingPointRegisters) o;

            if (littleEndian != fpr.littleEndian) return false;
            if (!Arrays.equals(data, fpr.data)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = Arrays.hashCode(data);
            result = 31 * result + (littleEndian ? 1 : 0);
            return result;
        }

        /**
         * Get the value of a floating point register as an integer.
         *
         * @param index the index
         * @return the value of a floating point register as an integer
         */
        public int getInt(int index) {
            return this.data[index];
        }

        /**
         * Set the value of a floating point register as an integer.
         *
         * @param index the index
         * @param i     the value of a floating point register as an integer
         */
        public void setInt(int index, int i) {
            this.data[index] = i;
        }

        /**
         * Get the value of a floating point register as a long integer.
         *
         * @param index the index
         * @return the value of a floating point register as a long integer
         */
        public long getLong(int index) {
            int i = (index / 2) * 2;
            int lo = this.getInt(i);
            int hi = this.getInt(i + 1);

            return ((long) (hi) << 32) + (lo & 0xFFFFFFFFL);
        }

        /**
         * Set the value of a floating point register as a long integer.
         *
         * @param index the index
         * @param d     the value of a floating point register as a long integer
         */
        public void setLong(int index, long d) {
            int hi = ((int) (d >> 32));
            int lo = ((int) ((d << 32) >> 32));

            int i = (index / 2) * 2;
            this.setInt(i, lo);
            this.setInt(i + 1, hi);
        }

        /**
         * Get the value of a floating point register as a float.
         *
         * @param index the index
         * @return the value of a floating point register as a float
         */
        public float getFloat(int index) {
            return Float.intBitsToFloat(this.getInt(index));
        }

        /**
         * Set the value of a floating point register as a float.
         *
         * @param index the index
         * @param f     the value of a floating point register as a float
         */
        public void setFloat(int index, float f) {
            this.setInt(index, Float.floatToRawIntBits(f));
        }

        /**
         * Get the value of a floating point register as a double.
         *
         * @param index the index
         * @return the value of a floating point register as a double
         */
        public double getDouble(int index) {
            return Double.longBitsToDouble(this.getLong(index));
        }

        /**
         * Set the value of a floating point register as a double.
         *
         * @param index the index
         * @param d     the value of a floating point register as a double
         */
        public void setDouble(int index, double d) {
            this.setLong(index, Double.doubleToRawLongBits(d));
        }

        /**
         * Get a value indicating whether the set of floating point registers is little endian or not.
         *
         * @return a value indicating whether the set of floating point registers is little endian or not
         */
        public boolean isLittleEndian() {
            return littleEndian;
        }
    }

    private boolean littleEndian;

    private int pc;
    private int npc;
    private int nnpc;

    private int[] gprs = new int[32];

    private FloatingPointRegisters fprs;

    private int hi;
    private int lo;
    private int fcsr;

    /**
     * Create an architectural register file.
     *
     * @param littleEndian a value indicating whether the architectural register file is little endian or not
     */
    public ArchitecturalRegisterFile(boolean littleEndian) {
        this.littleEndian = littleEndian;

        setPc(0);
        setNpc(0);
        setNnpc(0);

        setFprs(new FloatingPointRegisters(this.littleEndian));

        setHi(0);
        setLo(0);
        setFcsr(0);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        ArchitecturalRegisterFile obj = new ArchitecturalRegisterFile(this.littleEndian);

        obj.setPc(getPc());
        obj.setNpc(this.getNpc());
        obj.setNnpc(this.getNnpc());

        obj.gprs = this.gprs.clone();

        obj.setFprs((FloatingPointRegisters) this.getFprs().clone());

        obj.setHi(this.getHi());
        obj.setLo(this.getLo());
        obj.setFcsr(this.getFcsr());

        return obj;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArchitecturalRegisterFile that = (ArchitecturalRegisterFile) o;

        if (fcsr != that.fcsr) return false;
        if (hi != that.hi) return false;
        if (littleEndian != that.littleEndian) return false;
        if (lo != that.lo) return false;
        if (nnpc != that.nnpc) return false;
        if (npc != that.npc) return false;
        if (pc != that.pc) return false;
        if (!fprs.equals(that.fprs)) return false;
        if (!Arrays.equals(gprs, that.gprs)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (littleEndian ? 1 : 0);
        result = 31 * result + pc;
        result = 31 * result + npc;
        result = 31 * result + nnpc;
        result = 31 * result + Arrays.hashCode(gprs);
        result = 31 * result + fprs.hashCode();
        result = 31 * result + hi;
        result = 31 * result + lo;
        result = 31 * result + fcsr;
        return result;
    }

    /**
     * Copy the value of a register from the other architectural register file.
     *
     * @param theOtherRegisterFile the other architectural register file
     * @param dep                  the dependency index
     */
    public void copyRegisterFrom(ArchitecturalRegisterFile theOtherRegisterFile, int dep) {
        Pair<RegisterDependencyType, Integer> depTypeNumPair = RegisterDependencyType.parse(dep);

        RegisterDependencyType depType = depTypeNumPair.getFirst();
        int num = depTypeNumPair.getSecond();

        switch (depType) {
            case INTEGER:
                this.setGpr(num, theOtherRegisterFile.getGpr(num));
                break;
            case FLOAT:
                this.fprs.setFloat(num, theOtherRegisterFile.fprs.getFloat(num));
                break;
            case MISC:
                if (num == REGISTER_MISC_LO) {
                    this.lo = theOtherRegisterFile.lo;
                } else if (num == REGISTER_MISC_HI) {
                    this.hi = theOtherRegisterFile.hi;
                } else if (num == REGISTER_MISC_FCSR) {
                    this.fcsr = theOtherRegisterFile.fcsr;
                }
                break;
        }
    }

    /**
     * Get the value of a general purpose register (GPR).
     *
     * @param index the index
     * @return the value of a general purpose register (GPR)
     */
    public int getGpr(int index) {
        return this.gprs[index];
    }

    /**
     * Set the value of a general purpose register (GPR).
     *
     * @param index the index
     * @param value the value of a general purpose register (GPR)
     */
    public void setGpr(int index, int value) {
        this.gprs[index] = value;
    }

    /**
     * Dump the architectural register file.
     *
     * @return the text representation of the architectural register file
     */
    public String dump() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 32; i++) {
            sb.append(String.format("%s = 0x%08x, \n", gprNames[i], this.gprs[i]));
        }

        sb.append(String.format("pc = 0x%08x, npc = 0x%08x, nnpc = 0x%08x", this.pc, this.npc, this.nnpc));

        return sb.toString();
    }

    /**
     * Get a value indicating whether the architectural register file is little endian or not.
     *
     * @return a value indicating whether the architectural register file is little endian or not
     */
    public boolean isLittleEndian() {
        return littleEndian;
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
     * Set the value of the program counter (PC).
     *
     * @param pc the value of the program counter (PC)
     */
    public void setPc(int pc) {
        this.pc = pc;
    }

    /**
     * Get the value of the next program counter (NPC).
     *
     * @return the value of the next program counter (NPC)
     */
    public int getNpc() {
        return npc;
    }

    /**
     * Set the value of the next program counter (NPC).
     *
     * @param npc the value of the next program counter (NPC)
     */
    public void setNpc(int npc) {
        this.npc = npc;
    }

    /**
     * Get the value of the next next program counter (NNPC).
     *
     * @return the value of the next next program counter (NNPC)
     */
    public int getNnpc() {
        return nnpc;
    }

    /**
     * Set the value of the next next program counter (NNPC).
     *
     * @param nnpc the value of the next next program counter (NNPC)
     */
    public void setNnpc(int nnpc) {
        this.nnpc = nnpc;
    }

    /**
     * Get the value of the HI register.
     *
     * @return the value of the HI register
     */
    public int getHi() {
        return hi;
    }

    /**
     * Set the value of the HI register.
     *
     * @param hi the value of the HI register
     */
    public void setHi(int hi) {
        this.hi = hi;
    }

    /**
     * Get the value of the LO register.
     *
     * @return the value of the LO register
     */
    public int getLo() {
        return lo;
    }

    /**
     * Set the value of the LO register.
     *
     * @param lo the value of the LO register
     */
    public void setLo(int lo) {
        this.lo = lo;
    }

    /**
     * Get the value of the FCSR register.
     *
     * @return the value of the FCSR register
     */
    public int getFcsr() {
        return fcsr;
    }

    /**
     * Set the value of the FCSR register.
     *
     * @param fcsr the value of the FCSR register
     */
    public void setFcsr(int fcsr) {
        this.fcsr = fcsr;
    }

    /**
     * Get the set of the floating point registers.
     *
     * @return the set of the floating point registers
     */
    public FloatingPointRegisters getFprs() {
        return fprs;
    }

    /**
     * Set the set of the floating point registers.
     *
     * @param fprs the set of the floating point registers
     */
    public void setFprs(FloatingPointRegisters fprs) {
        this.fprs = fprs;
    }

    /**
     * (General purpose register) GPR names.
     */
    private static final String[] gprNames = new String[]{
            "zero", "at", "v0", "v1", "a0", "a1", "a2", "a3",
            "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t6",
            "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7",
            "t8", "t9", "k0", "k1", "gp", "sp", "fp", "ra"
    };

    /**
     * Number of integer registers.
     */
    public static final int NUM_INT_REGISTERS = 32;

    /**
     * Number of floating point registers.
     */
    public static final int NUM_FLOAT_REGISTERS = 32;

    /**
     * Number of miscellaneous registers.
     */
    public static final int NUM_MISC_REGISTERS = 3;

    /**
     * ZERO.
     */
    public static final int REGISTER_ZERO = 0;

    /**
     * AT.
     */
    public static final int REGISTER_AT = 1;

    /**
     * V0.
     */
    public static final int REGISTER_V0 = 2;

    /**
     * V1.
     */
    public static final int REGISTER_V1 = 3;

    /**
     * A0.
     */
    public static final int REGISTER_A0 = 4;

    /**
     * A1.
     */
    public static final int REGISTER_A1 = 5;

    /**
     * A2.
     */
    public static final int REGISTER_A2 = 6;

    /**
     * A3.
     */
    public static final int REGISTER_A3 = 7;

    /**
     * T0.
     */
    public static final int REGISTER_T0 = 8;

    /**
     * T1.
     */
    public static final int REGISTER_T1 = 9;

    /**
     * T2.
     */
    public static final int REGISTER_T2 = 10;

    /**
     * T3.
     */
    public static final int REGISTER_T3 = 11;

    /**
     * T4.
     */
    public static final int REGISTER_T4 = 12;

    /**
     * T5.
     */
    public static final int REGISTER_T5 = 13;

    /**
     * T6.
     */
    public static final int REGISTER_T6 = 14;

    /**
     * T7.
     */
    public static final int REGISTER_T7 = 15;

    /**
     * S0.
     */
    public static final int REGISTER_S0 = 16;

    /**
     * S1.
     */
    public static final int REGISTER_S1 = 17;

    /**
     * S2.
     */
    public static final int REGISTER_S2 = 18;

    /**
     * S3.
     */
    public static final int REGISTER_S3 = 19;

    /**
     * S4.
     */
    public static final int REGISTER_S4 = 20;

    /**
     * S5.
     */
    public static final int REGISTER_S5 = 21;

    /**
     * S6.
     */
    public static final int REGISTER_S6 = 22;

    /**
     * S7.
     */
    public static final int REGISTER_S7 = 23;

    /**
     * T8.
     */
    public static final int REGISTER_T8 = 24;

    /**
     * T9.
     */
    public static final int REGISTER_T9 = 25;

    /**
     * K0.
     */
    public static final int REGISTER_K0 = 26;

    /**
     * K1.
     */
    public static final int REGISTER_K1 = 27;

    /**
     * (Global pointer) GP.
     */
    public static final int REGISTER_GP = 28;

    /**
     * (Stack pointer) SP.
     */
    public static final int REGISTER_SP = 29;

    /**
     * (Frame pointer) FP.
     */
    public static final int REGISTER_FP = 30;

    /**
     * (Return address) RA.
     */
    public static final int REGISTER_RA = 31;

    /**
     * LO.
     */
    public static final int REGISTER_MISC_LO = 0;

    /**
     * HI.
     */
    public static final int REGISTER_MISC_HI = 1;

    /**
     * FCSR.
     */
    public static final int REGISTER_MISC_FCSR = 2;
}
