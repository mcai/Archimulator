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
package archimulator.util.math;

public class MathHelper {
    public static String dumpBytes(byte[] buffer) {
        return dumpBytes(buffer, 0, buffer.length);
    }
    
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

    public static boolean containsBit(int value, int i) {
        return (value & (1 << i)) != 0;
    }

    public static int setBit(int value, int i) {
        return value | (1 << i);
    }

    public static int clearBit(int value, int i) {
        return value & ~(1 << i);
    }

    public static int mask(int nbits) {
        return (1 << nbits) - 1;
    }

    public static int bits(int val, int first, int last) {
        return (val >> last) & mask(first - last + 1);
    }

    public static int mbits(int val, int first, int last) {
        return val & (mask(first + 1) & ~mask(last));
    }

    public static int signExtend(int value) {
        return (value << 16) >> 16;
    }

    public static int zeroExtend(int value) {
        return (value & 0xffff);
    }
}
