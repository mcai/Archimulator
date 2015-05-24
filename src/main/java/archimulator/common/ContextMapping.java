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
package archimulator.common;

import java.io.Serializable;

/**
 * Context mapping, or the assignment of software contexts to hardware threads.
 *
 * @author Min Cai
 */
public class ContextMapping implements Serializable {
    private int threadId;
    private String executable;
    private String arguments;

    /**
     * Create a context mapping.
     * @param threadId    the hardware thread ID.
     * @param executable the path to the executable
     * @param arguments arguments
     */
    public ContextMapping(int threadId, String executable, String arguments) {
        this.threadId = threadId;
        this.executable = executable;
        this.arguments = arguments;
    }

    /**
     * Get the hardware thread ID.
     *
     * @return the hardware thread ID
     */
    public int getThreadId() {
        return threadId;
    }

    /**
     * Set the hardware thread ID.
     *
     * @param threadId the hardware thread ID
     */
    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    /**
     * Get the path to the executable.
     *
     * @return the path to hte executable
     */
    public String getExecutable() {
        return executable;
    }

    /**
     * Arguments passed to the executable.
     *
     * @return arguments passed to the executable
     */
    public String getArguments() {
        return arguments;
    }
}
