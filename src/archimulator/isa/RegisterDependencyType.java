/*******************************************************************************
 * Copyright (c) 2010-2011 by Min Cai (min.cai.china@gmail.com).
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

public enum RegisterDependencyType {
    INTEGER, FLOAT, MISC;

    public static int toRegisterDependency(RegisterDependencyType type, int num) {
        switch (type) {
            case INTEGER:
                return num;
            case FLOAT:
                return ArchitecturalRegisterFile.NUM_INT_REGS + num;
            case MISC:
                return ArchitecturalRegisterFile.NUM_INT_REGS + ArchitecturalRegisterFile.NUM_FLOAT_REGS + num;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static Pair<RegisterDependencyType, Integer> parse(int dep) {
        if (dep < ArchitecturalRegisterFile.NUM_INT_REGS) {
            return new Pair<RegisterDependencyType, Integer>(RegisterDependencyType.INTEGER, dep);
        } else if (dep < ArchitecturalRegisterFile.NUM_INT_REGS + ArchitecturalRegisterFile.NUM_FLOAT_REGS) {
            return new Pair<RegisterDependencyType, Integer>(RegisterDependencyType.FLOAT, dep - ArchitecturalRegisterFile.NUM_INT_REGS);
        } else {
            return new Pair<RegisterDependencyType, Integer>(RegisterDependencyType.MISC, dep - ArchitecturalRegisterFile.NUM_INT_REGS - ArchitecturalRegisterFile.NUM_FLOAT_REGS);
        }
    }

    public static RegisterDependencyType getType(int dep) {
        return parse(dep).getFirst();
    }

    public static int getNum(int dep) {
        return parse(dep).getSecond();
    }
}
