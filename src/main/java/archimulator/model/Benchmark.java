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
package archimulator.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.io.Serializable;

/**
 * Benchmark.
 *
 * @author Min Cai
 */
@Root
public class Benchmark implements Serializable {
    @Attribute
    private String title;

    @Element
    private String workingDirectory;

    @Element
    private String executable;

    @Element(required = false)
    private String arguments;

    @Element(required = false)
    private String standardIn;

    /**
     * Create a benchmark. Reserved for XML mapping only.
     */
    public Benchmark() {
    }

    /**
     * Create a benchmark.
     *
     * @param title            the title
     * @param workingDirectory the working directory
     * @param executable       the executable
     * @param arguments the default arguments
     */
    public Benchmark(String title, String workingDirectory, String executable, String arguments) {
        this(title, workingDirectory, executable, arguments, "");
    }

    /**
     * Create a benchmark.
     *
     * @param title               the title
     * @param workingDirectory    the working directory
     * @param executable          the executable
     * @param arguments    the default arguments
     * @param standardIn          the standard in
     */
    public Benchmark(String title, String workingDirectory, String executable, String arguments, String standardIn) {
        this.title = title;
        this.workingDirectory = workingDirectory;
        this.executable = executable;
        this.arguments = arguments;
        this.standardIn = standardIn;
    }

    /**
     * Get the title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the working directory.
     *
     * @return the working directory
     */
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * Get the executable.
     *
     * @return the executable
     */
    public String getExecutable() {
        return executable;
    }

    /**
     * Get the arguments.
     *
     * @return the arguments
     */
    public String getArguments() {
        return arguments;
    }

    /**
     * Get the standard in.
     *
     * @return the standard in
     */
    public String getStandardIn() {
        if (standardIn == null) {
            standardIn = "";
        }

        return standardIn;
    }

    @Override
    public String toString() {
        return String.format("Benchmark{title='%s', workingDirectory='%s', executable='%s', arguments='%s', standardIn='%s'}", title, workingDirectory, executable, arguments, standardIn);
    }
}
