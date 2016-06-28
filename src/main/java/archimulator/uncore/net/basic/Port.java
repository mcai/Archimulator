/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.uncore.net.basic;

/**
 * Port.
 *
 * @author Min Cai
 */
public enum Port {
    /**
     * Local.
     */
    LOCAL,

    /**
     * Left.
     */
    LEFT,

    /**
     * Right.
     */
    RIGHT,

    /**
     * Up.
     */
    UP,

    /**
     * Down.
     */
    DOWN;

    /**
     * Get the opposite port.
     *
     * @return the opposite port
     */
    public Port opposite() {
        switch (this) {
            case LOCAL:
                return LOCAL;
            case LEFT:
                return RIGHT;
            case RIGHT:
                return LEFT;
            case UP:
                return DOWN;
            case DOWN:
                return UP;
            default:
                throw new IllegalArgumentException(this + "");
        }
    }
}
