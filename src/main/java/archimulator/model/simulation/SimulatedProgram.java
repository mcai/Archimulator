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
package archimulator.model.simulation;

import java.io.Serializable;

public class SimulatedProgram implements Serializable {
    private String setTitle;
    private String title;
    private String cwd;
    private String exe;
    private String args;
    private String stdin;

    public SimulatedProgram(String setTitle, String title, String cwd, String exe, String args) {
        this(setTitle, title, cwd, exe, args, "");
    }

    public SimulatedProgram(String setTitle, String title, String cwd, String exe, String args, String stdin) {
        this.setTitle = setTitle;
        this.title = title;
        this.cwd = cwd;
        this.exe = exe;
        this.args = args;
        this.stdin = stdin;
    }

    public String getSetTitle() {
        return setTitle;
    }

    public String getTitle() {
        return title;
    }

    public String getCwd() {
        return cwd;
    }

    public String getExe() {
        return exe;
    }

    public String getArgs() {
        return args;
    }

    public String getStdin() {
        return stdin;
    }
}
