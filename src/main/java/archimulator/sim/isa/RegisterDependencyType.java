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

/**
 *
 * @author Min Cai
 */
public enum RegisterDependencyType {
    /**
     *
     */
    INTEGER,
    /**
     *
     */
    FLOAT,
    /**
     *
     */
    MISC;

    /**
     *
     * @param type
     * @param num
     * @return
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
     *
     * @param dependency
     * @return
     */
    public static Pair<RegisterDependencyType, Integer> parse(int dependency) {
        if (dependency < ArchitecturalRegisterFile.NUM_INT_REGISTERS) {
            return new Pair<RegisterDependencyType, Integer>(RegisterDependencyType.INTEGER, dependency);
        } else if (dependency < ArchitecturalRegisterFile.NUM_INT_REGISTERS + ArchitecturalRegisterFile.NUM_FLOAT_REGISTERS) {
            return new Pair<RegisterDependencyType, Integer>(RegisterDependencyType.FLOAT, dependency - ArchitecturalRegisterFile.NUM_INT_REGISTERS);
        } else {
            return new Pair<RegisterDependencyType, Integer>(RegisterDependencyType.MISC, dependency - ArchitecturalRegisterFile.NUM_INT_REGISTERS - ArchitecturalRegisterFile.NUM_FLOAT_REGISTERS);
        }
    }

    /**
     *
     * @param dependency
     * @return
     */
    public static RegisterDependencyType getType(int dependency) {
        return parse(dependency).getFirst();
    }

    /**
     *
     * @param dependency
     * @return
     */
    public static int getNum(int dependency) {
        return parse(dependency).getSecond();
    }
}
