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

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable
public class SimulatedProgram implements Serializable {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField
    private String cwd;

    @DatabaseField
    private String exe;

    @DatabaseField
    private String args;

    @DatabaseField
    private String stdin;
    
    public SimulatedProgram() {
    }

    public SimulatedProgram(String cwd, String exe, String args) {
        this(cwd, exe, args, "");
    }

    public SimulatedProgram(String cwd, String exe, String args, String stdin) {
        this.cwd = cwd;
        this.exe = exe;
        this.args = args;
        this.stdin = stdin;
    }

    public long getId() {
        return id;
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
