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

/**
 *
 * @author Min Cai
 */
@DatabaseTable(tableName = "Benchmark")
public class Benchmark implements ModelElement {
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
    private String defaultArguments;

    @DatabaseField
    private String standardIn;

    @DatabaseField
    private boolean helperThreadEnabled;

    /**
     *
     */
    public Benchmark() {
    }

    /**
     *
     * @param title
     * @param workingDirectory
     * @param executable
     * @param defaultArguments
     */
    public Benchmark(String title, String workingDirectory, String executable, String defaultArguments) {
        this(title, workingDirectory, executable, defaultArguments, "", false);
    }

    /**
     *
     * @param title
     * @param workingDirectory
     * @param executable
     * @param defaultArguments
     * @param standardIn
     * @param helperThreadEnabled
     */
    public Benchmark(String title, String workingDirectory, String executable, String defaultArguments, String standardIn, boolean helperThreadEnabled) {
        this.title = title;
        this.workingDirectory = workingDirectory;
        this.executable = executable;
        this.defaultArguments = defaultArguments;
        this.standardIn = standardIn;

        this.helperThreadEnabled = helperThreadEnabled;

        this.createTime = DateHelper.toTick(new Date());
    }

    /**
     *
     * @return
     */
    public long getId() {
        return id;
    }

    /**
     *
     * @return
     */
    @Override
    public long getParentId() {
        return -1;
    }

    /**
     *
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     *
     * @return
     */
    public long getCreateTime() {
        return createTime;
    }

    /**
     *
     * @return
     */
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     *
     * @return
     */
    public String getExecutable() {
        return executable;
    }

    /**
     *
     * @return
     */
    public String getDefaultArguments() {
        return defaultArguments;
    }

    /**
     *
     * @return
     */
    public String getStandardIn() {
        if(standardIn == null) {
            standardIn = "";
        }

        return standardIn;
    }

    /**
     *
     * @return
     */
    public boolean getHelperThreadEnabled() {
        return helperThreadEnabled;
    }

    @Override
    public String toString() {
        return String.format("Benchmark{id=%d, title='%s', workingDirectory='%s', executable='%s', standardIn='%s', helperThreadEnabled=%s}", id, title, workingDirectory, executable, standardIn, helperThreadEnabled);
    }
}
