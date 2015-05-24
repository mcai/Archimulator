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
package archimulator.isa;

import archimulator.util.Pair;

/**
 * Register dependency type.
 *
 * @author Min Cai
 */
public enum RegisterDependencyType {
    /**
     * Integer.
     */
    INTEGER,

    /**
     * Floating point.
     */
    FLOAT,

    /**
     * Miscellaneous.
     */
    MISC;

    /**
     * Get the register dependency for the specified type and number.
     *
     * @param type the type of the register dependency
     * @param num  the number of the register dependency
     * @return the register dependency for the specified type and number
     */
    public static int toRegisterDependency(RegisterDependencyType type, int num) {
        switch (type) {
            case INTEGER:
                return num;
            case FLOAT:
                return ArchitecturalRegisterFile.NUM_INT_REGISTERS + num;
            case MISC:
                return ArchitecturalRegisterFile.NUM_INT_REGISTERS + ArchitecturalRegisterFile.NUM_FLOAT_REGISTERS + num;
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Parse the total ordered dependency into a pair of dependency type and number.
     *
     * @param dependency the total ordered dependency
     * @return a pair of dependency type and number for the specified total ordered dependency
     */
    public static Pair<RegisterDependencyType, Integer> parse(int dependency) {
        if (dependency < ArchitecturalRegisterFile.NUM_INT_REGISTERS) {
            return new Pair<>(RegisterDependencyType.INTEGER, dependency);
        } else if (dependency < ArchitecturalRegisterFile.NUM_INT_REGISTERS + ArchitecturalRegisterFile.NUM_FLOAT_REGISTERS) {
            return new Pair<>(RegisterDependencyType.FLOAT, dependency - ArchitecturalRegisterFile.NUM_INT_REGISTERS);
        } else {
            return new Pair<>(RegisterDependencyType.MISC, dependency - ArchitecturalRegisterFile.NUM_INT_REGISTERS - ArchitecturalRegisterFile.NUM_FLOAT_REGISTERS);
        }
    }

    /**
     * Get the dependency type for the specified total ordered dependency.
     *
     * @param dependency the total ordered dependency
     * @return the dependency type for the specified total ordered dependency
     */
    public static RegisterDependencyType getType(int dependency) {
        return parse(dependency).getFirst();
    }

    /**
     * Get the dependency number for the specified total ordered dependency.
     *
     * @param dependency the total ordered dependency
     * @return the dependency number for the specified total ordered dependency
     */
    public static int getNum(int dependency) {
        return parse(dependency).getSecond();
    }
}
