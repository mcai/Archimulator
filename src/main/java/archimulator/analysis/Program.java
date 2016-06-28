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
package archimulator.analysis;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Program.
 *
 * @author Min Cai
 */
public class Program {
    private String fileName;
    private SortedSet<Function> functions;

    /**
     * Create a program.
     *
     * @param fileName the file name
     */
    public Program(String fileName) {
        this.fileName = fileName;
        this.functions = new TreeSet<>();
    }

    /**
     * Get the file name.
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Get the list of constituent functions.
     *
     * @return the list of constituent functions
     */
    public SortedSet<Function> getFunctions() {
        return functions;
    }

    @Override
    public String toString() {
        return String.format("Program{fileName='%s'}", fileName);
    }
}
