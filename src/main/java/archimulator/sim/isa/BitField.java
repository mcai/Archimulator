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

import net.pickapack.math.MathHelper;

import java.lang.reflect.Field;

/**
 * Bit field.
 *
 * @author Min Cai
 */
public class BitField {
    private int hi;
    private int lo;

    /**
     * Create a bit field.
     *
     * @param hi the HI value
     * @param lo the LO value
     */
    private BitField(int hi, int lo) {
        this.hi = hi;
        this.lo = lo;
    }

    /**
     * Get the value of the field in the specified machine instruction.
     *
     * @param machineInstruction the machine instruction
     * @return the value of the field in the specified machine instruction
     */
    public int valueOf(int machineInstruction) {
        return MathHelper.bits(machineInstruction, this.hi, this.lo);
    }

    /**
     * Opcode.
     */
    public static final BitField OPCODE = new BitField(31, 26);
    /**
     * Opcode HI.
     */
    public static final BitField OPCODE_HI = new BitField(31, 29);
    /**
     * Opcode LO.
     */
    public static final BitField OPCODE_LO = new BitField(28, 26);

    /**
     * RS.
     */
    public static final BitField RS = new BitField(25, 21);
    /**
     * RT.
     */
    public static final BitField RT = new BitField(20, 16);
    /**
     * RD.
     */
    public static final BitField RD = new BitField(15, 11);
    /**
     * Shift.
     */
    public static final BitField SHIFT = new BitField(10, 6);

    /**
     * Func.
     */
    public static final BitField FUNC = new BitField(5, 0);
    /**
     * Func HI.
     */
    public static final BitField FUNC_HI = new BitField(5, 3);
    /**
     * Func LO.
     */
    public static final BitField FUNC_LO = new BitField(2, 0);

    /**
     * Condition
     */
    public static final BitField COND = new BitField(3, 0);

    /**
     * Integer immediate.
     */
    public static final BitField INTIMM = new BitField(15, 0);
    /**
     * Target.
     */
    public static final BitField TARGET = new BitField(25, 0);

    /**
     * FMT.
     */
    public static final BitField FMT = new BitField(25, 21);
    /**
     * FMT3.
     */
    public static final BitField FMT3 = new BitField(2, 0);
    /**
     * FT.
     */
    public static final BitField FT = new BitField(20, 16);
    /**
     * FR.
     */
    public static final BitField FR = new BitField(25, 21);
    /**
     * FS.
     */
    public static final BitField FS = new BitField(15, 11);
    /**
     * FD.
     */
    public static final BitField FD = new BitField(10, 6);
    /**
     * Branch CC.
     */
    public static final BitField BRANCH_CC = new BitField(20, 18);

    /**
     * CC.
     */
    public static final BitField CC = new BitField(10, 8);

    /**
     * Get a bit field by the specified name.
     *
     * @param name the name to be searched
     * @return a bit field matching the specified name
     */
    public static BitField get(String name) {
        try {
            Field field = BitField.class.getField(name.toUpperCase());
            return (BitField) field.get(null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
