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
package archimulator.sim.isa;

import net.pickapack.Pair;

import java.util.Arrays;

/**
 *
 * @author Min Cai
 */
public final class ArchitecturalRegisterFile implements Cloneable {
    /**
     *
     */
    public static class Fpr implements Cloneable {
        private int[] data = new int[32];
        private boolean littleEndian;

        /**
         *
         * @param littleEndian
         */
        public Fpr(boolean littleEndian) {
            this.littleEndian = littleEndian;
        }

        /**
         *
         * @param littleEndian
         * @param data
         */
        public Fpr(boolean littleEndian, int[] data) {
            this.littleEndian = littleEndian;
            this.data = data;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return new Fpr(this.littleEndian, this.data.clone());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Fpr fpr = (Fpr) o;

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
         *
         * @param index
         * @return
         */
        public int getInt(int index) {
            return this.data[index];
        }

        /**
         *
         * @param index
         * @param i
         */
        public void setInt(int index, int i) {
            this.data[index] = i;
        }

        /**
         *
         * @param index
         * @return
         */
        public long getLong(int index) {
            int i = (index / 2) * 2;
            int lo = this.getInt(i);
            int hi = this.getInt(i + 1);

            return ((long) (hi) << 32) + (lo & 0xFFFFFFFFL);
        }

        /**
         *
         * @param index
         * @param d
         */
        public void setLong(int index, long d) {
            int hi = ((int) (d >> 32));
            int lo = ((int) ((d << 32) >> 32));

            int i = (index / 2) * 2;
            this.setInt(i, lo);
            this.setInt(i + 1, hi);
        }

        /**
         *
         * @param index
         * @return
         */
        public float getFloat(int index) {
            return Float.intBitsToFloat(this.getInt(index));
        }

        /**
         *
         * @param index
         * @param f
         */
        public void setFloat(int index, float f) {
            this.setInt(index, Float.floatToRawIntBits(f));
        }

        /**
         *
         * @param index
         * @return
         */
        public double getDouble(int index) {
            return Double.longBitsToDouble(this.getLong(index));
        }

        /**
         *
         * @param index
         * @param d
         */
        public void setDouble(int index, double d) {
            this.setLong(index, Double.doubleToRawLongBits(d));
        }

        /**
         *
         * @return
         */
        public boolean isLittleEndian() {
            return littleEndian;
        }
    }

    private boolean littleEndian;

    private int pc;
    private int npc;
    private int nnpc;

    private int[] gpr = new int[32];

    private Fpr fpr;

    private int hi;
    private int lo;
    private int fcsr;

    /**
     *
     * @param littleEndian
     */
    public ArchitecturalRegisterFile(boolean littleEndian) {
        this.littleEndian = littleEndian;

        setPc(0);
        setNpc(0);
        setNnpc(0);

        setFpr(new Fpr(this.littleEndian));

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

        obj.gpr = this.gpr.clone();

        obj.setFpr((Fpr) this.getFpr().clone());

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
        if (!fpr.equals(that.fpr)) return false;
        if (!Arrays.equals(gpr, that.gpr)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (littleEndian ? 1 : 0);
        result = 31 * result + pc;
        result = 31 * result + npc;
        result = 31 * result + nnpc;
        result = 31 * result + Arrays.hashCode(gpr);
        result = 31 * result + fpr.hashCode();
        result = 31 * result + hi;
        result = 31 * result + lo;
        result = 31 * result + fcsr;
        return result;
    }

    /**
     *
     * @param theOtherRegisterFile
     * @param dep
     */
    public void copyRegFrom(ArchitecturalRegisterFile theOtherRegisterFile, int dep) {
        Pair<RegisterDependencyType, Integer> depTypeNumPair = RegisterDependencyType.parse(dep);

        RegisterDependencyType depType = depTypeNumPair.getFirst();
        int num = depTypeNumPair.getSecond();

        switch (depType) {
            case INTEGER:
                this.setGpr(num, theOtherRegisterFile.getGpr(num));
                break;
            case FLOAT:
                this.fpr.setFloat(num, theOtherRegisterFile.fpr.getFloat(num));
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
     *
     * @param index
     * @return
     */
    public int getGpr(int index) {
        return this.gpr[index];
    }

    /**
     *
     * @param index
     * @param value
     */
    public void setGpr(int index, int value) {
        this.gpr[index] = value;
    }

    /**
     *
     * @return
     */
    public String dump() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 32; i++) {
            sb.append(String.format("%s = 0x%08x, \n", gprNames[i], this.gpr[i]));
        }

        sb.append(String.format("pc = 0x%08x, npc = 0x%08x, nnpc = 0x%08x", this.pc, this.npc, this.nnpc));

        return sb.toString();
    }

    /**
     *
     * @return
     */
    public boolean isLittleEndian() {
        return littleEndian;
    }

    /**
     *
     * @return
     */
    public int getPc() {
        return pc;
    }

    /**
     *
     * @param pc
     */
    public void setPc(int pc) {
        this.pc = pc;
    }

    /**
     *
     * @return
     */
    public int getNpc() {
        return npc;
    }

    /**
     *
     * @param npc
     */
    public void setNpc(int npc) {
        this.npc = npc;
    }

    /**
     *
     * @return
     */
    public int getNnpc() {
        return nnpc;
    }

    /**
     *
     * @param nnpc
     */
    public void setNnpc(int nnpc) {
        this.nnpc = nnpc;
    }

    /**
     *
     * @return
     */
    public int getHi() {
        return hi;
    }

    /**
     *
     * @param hi
     */
    public void setHi(int hi) {
        this.hi = hi;
    }

    /**
     *
     * @return
     */
    public int getLo() {
        return lo;
    }

    /**
     *
     * @param lo
     */
    public void setLo(int lo) {
        this.lo = lo;
    }

    /**
     *
     * @return
     */
    public int getFcsr() {
        return fcsr;
    }

    /**
     *
     * @param fcsr
     */
    public void setFcsr(int fcsr) {
        this.fcsr = fcsr;
    }

    /**
     *
     * @return
     */
    public Fpr getFpr() {
        return fpr;
    }

    /**
     *
     * @param fpr
     */
    public void setFpr(Fpr fpr) {
        this.fpr = fpr;
    }

    private static final String[] gprNames = new String[]{
            "zero", "at", "v0", "v1", "a0", "a1", "a2", "a3",
            "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t6",
            "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7",
            "t8", "t9", "k0", "k1", "gp", "sp", "fp", "ra"
    };

    /**
     *
     */
    public static final int NUM_INT_REGISTERS = 32;
    /**
     *
     */
    public static final int NUM_FLOAT_REGISTERS = 32;
    /**
     *
     */
    public static final int NUM_MISC_REGISTERS = 3;

    /**
     *
     */
    public static final int REGISTER_ZERO = 0;
    /**
     *
     */
    public static final int REGISTER_AT = 1;
    /**
     *
     */
    public static final int REGISTER_V0 = 2;
    /**
     *
     */
    public static final int REGISTER_V1 = 3;
    /**
     *
     */
    public static final int REGISTER_A0 = 4;
    /**
     *
     */
    public static final int REGISTER_A1 = 5;
    /**
     *
     */
    public static final int REGISTER_A2 = 6;
    /**
     *
     */
    public static final int REGISTER_A3 = 7;
    /**
     *
     */
    public static final int REGISTER_T0 = 8;
    /**
     *
     */
    public static final int REGISTER_T1 = 9;
    /**
     *
     */
    public static final int REGISTER_T2 = 10;
    /**
     *
     */
    public static final int REGISTER_T3 = 11;
    /**
     *
     */
    public static final int REGISTER_T4 = 12;
    /**
     *
     */
    public static final int REGISTER_T5 = 13;
    /**
     *
     */
    public static final int REGISTER_T6 = 14;
    /**
     *
     */
    public static final int REGISTER_T7 = 15;
    /**
     *
     */
    public static final int REGISTER_S0 = 16;
    /**
     *
     */
    public static final int REGISTER_S1 = 17;
    /**
     *
     */
    public static final int REGISTER_S2 = 18;
    /**
     *
     */
    public static final int REGISTER_S3 = 19;
    /**
     *
     */
    public static final int REGISTER_S4 = 20;
    /**
     *
     */
    public static final int REGISTER_S5 = 21;
    /**
     *
     */
    public static final int REGISTER_S6 = 22;
    /**
     *
     */
    public static final int REGISTER_S7 = 23;
    /**
     *
     */
    public static final int REGISTER_T8 = 24;
    /**
     *
     */
    public static final int REGISTER_T9 = 25;
    /**
     *
     */
    public static final int REGISTER_K0 = 26;
    /**
     *
     */
    public static final int REGISTER_K1 = 27;
    /**
     *
     */
    public static final int REGISTER_GP = 28;
    /**
     *
     */
    public static final int REGISTER_SP = 29;
    /**
     *
     */
    public static final int REGISTER_FP = 30;
    /**
     *
     */
    public static final int REGISTER_RA = 31;

    /**
     *
     */
    public static final int REGISTER_MISC_LO = 0;
    /**
     *
     */
    public static final int REGISTER_MISC_HI = 1;
    /**
     *
     */
    public static final int REGISTER_MISC_FCSR = 2;
}
