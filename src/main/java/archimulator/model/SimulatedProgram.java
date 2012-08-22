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
package archimulator.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.model.ModelElement;

import java.util.Date;

@DatabaseTable(tableName = "SimulatedProgram")
public class SimulatedProgram implements ModelElement {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField
    private String title;

    @DatabaseField
    private long createTime;

    @DatabaseField
    private String workingDirectory;

    @DatabaseField
    private String executable;

    @DatabaseField
    private String arguments;

    @DatabaseField
    private String standardIn;

    @DatabaseField
    private boolean helperThreadEnabled;

    public SimulatedProgram() {
    }

    public SimulatedProgram(String title, String workingDirectory, String executable, String arguments) {
        this(title, workingDirectory, executable, arguments, "", false);
    }

    public SimulatedProgram(String title, String workingDirectory, String executable, String arguments, String standardIn, boolean helperThreadEnabled) {
        this.title = title;
        this.workingDirectory = workingDirectory;
        this.executable = executable;
        this.arguments = arguments;
        this.standardIn = standardIn;

        this.helperThreadEnabled = helperThreadEnabled;

        this.createTime = DateHelper.toTick(new Date());
    }

    public long getId() {
        return id;
    }

    @Override
    public long getParentId() {
        return -1;
    }

    public String getTitle() {
        return title;
    }

    public long getCreateTime() {
        return createTime;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public String getExecutable() {
        return executable;
    }

    public String getArguments() {
        return arguments;
    }

    public String getStandardIn() {
        return standardIn;
    }

    public boolean getHelperThreadEnabled() {
        return helperThreadEnabled;
    }

    @Override
    public String toString() {
        return String.format("SimulatedProgram{id=%d, title='%s', workingDirectory='%s', executable='%s', arguments='%s', standardIn='%s', helperThreadEnabled=%s}", id, title, workingDirectory, executable, arguments, standardIn, helperThreadEnabled);
    }
}
