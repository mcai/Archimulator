/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the PickaPack library.
 * <p>
 * PickaPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * PickaPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with PickaPack. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.util.math;

/**
 * Math helper.
 *
 * @author Min Cai
 */
public class MathHelper {
    /**
     * Get the text representation of the specified byte array.
     *
     * @param buffer the byte array
     * @return the text representation of the specified byte array
     */
    public static String dumpBytes(byte[] buffer) {
        return dumpBytes(buffer, 0, buffer.length);
    }

    /**
     * Get the text representation of the specified byte array.
     *
     * @param buffer the byte array
     * @param offset the offset
     * @param size the size
     * @return the text representation of the specified byte array
     */
    public static String dumpBytes(byte[] buffer, int offset, int size) {
        if (buffer == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = offset; i < size; i++) {
            if (i != 0 && i % 8 == 0) {
                sb.append("\n");
            }
            sb.append(String.format("%08x%s", buffer[i], (i < (size - 1) ? " " : "")));
        }

        return sb.toString();
    }

    /**
     * Get a boolean value indicating whether the specified value contains the specified bit or not.
     *
     * @param value the value
     * @param i the bit to check
     * @return a boolean value indicating whether the specified value contains the specified bit or not
     */
    public static boolean containsBit(int value, int i) {
        return (value & (1 << i)) != 0;
    }

    /**
     * Set the specified bit in the specified value.
     *
     * @param value the value
     * @param i the bit to set
     * @return the new value
     */
    public static int setBit(int value, int i) {
        return value | (1 << i);
    }

    /**
     * Clear the specified bit in the specified value.
     *
     * @param value the value
     * @param i the bit to clear
     * @return the new value
     */
    public static int clearBit(int value, int i) {
        return value & ~(1 << i);
    }

    /**
     * Create the mask of the specified length.
     *
     * @param numBits the length of the mask
     * @return the mask
     */
    public static int mask(int numBits) {
        return (1 << numBits) - 1;
    }

    /**
     * Get the specified bits from the specified value.
     *
     * @param value the value
     * @param first the first bit
     * @param last the last bit
     * @return the specified bits from the specified value
     */
    public static int bits(int value, int first, int last) {
        return (value >> last) & mask(first - last + 1);
    }

    /**
     * Get the specified masked bits from the specified value.
     *
     * @param value the value
     * @param first the first bit
     * @param last the last bit
     * @return the specified masked bits from the specified value
     */
    public static int mbits(int value, int first, int last) {
        return value & (mask(first + 1) & ~mask(last));
    }

    /**
     * Sign extend the specified value.
     *
     * @param value the value
     * @return the sign extended value
     */
    public static int signExtend(int value) {
        return (value << 16) >> 16;
    }

    /**
     * Zero extend the specified value.
     *
     * @param value the value
     * @return the zero extended value
     */
    public static int zeroExtend(int value) {
        return (value & 0xffff);
    }
}
