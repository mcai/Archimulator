/**
 * ****************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.os;

/**
 * System call handler.
 *
 * @author Min Cai
 */
public abstract class SystemCallHandler {
    private int index;
    private String name;

    /**
     * Create a system call handler.
     *
     * @param index the index of the system call to be handled
     * @param name  the name of the system call to be handled
     */
    public SystemCallHandler(int index, String name) {
        this.index = index;
        this.name = name;
    }

    /**
     * Process the system call.
     *
     * @param context the context
     */
    public abstract void run(Context context);

    /**
     * Get the index of the system call to be handled.
     *
     * @return the index of the system call to be handled
     */
    public int getIndex() {
        return index;
    }

    /**
     * Get the name of the system call to be handled.
     *
     * @return the name of the system call to be handled
     */
    public String getName() {
        return name;
    }
}
