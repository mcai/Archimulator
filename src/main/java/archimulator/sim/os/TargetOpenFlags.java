/*******************************************************************************
 * Copyright (c) 2010-2013 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.sim.os;

/**
 *
 * @author Min Cai
 */
public class TargetOpenFlags {
    /**
     *
     */
    public static final int O_RDONLY = 0;
    /**
     *
     */
    public static final int O_WRONLY = 1;
    /**
     *
     */
    public static final int O_RDWR = 2;
    /**
     *
     */
    public static final int O_CREAT = 0x100;
    /**
     *
     */
    public static final int O_EXCL = 0x400;
    /**
     *
     */
    public static final int O_NOCTTY = 0x800;
    /**
     *
     */
    public static final int O_TRUNC = 0x200;
    /**
     *
     */
    public static final int O_APPEND = 8;
    /**
     *
     */
    public static final int O_NONBLOCK = 0x80;
    /**
     *
     */
    public static final int O_SYNC = 0x10;
}
