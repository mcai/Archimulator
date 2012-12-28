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
package archimulator.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.model.WithCreateTime;
import net.pickapack.model.WithId;

import java.util.Date;

/**
 * System setting.
 *
 * @author Min Cai
 */
@DatabaseTable(tableName = "SystemSetting")
public class SystemSetting implements WithId, WithCreateTime {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField
    private long createTime;

    @DatabaseField
    private boolean runningExperimentsEnabled;

    /**
     * Create a system setting.
     */
    public SystemSetting() {
        this.createTime = DateHelper.toTick(new Date());
    }

    /**
     * Get the system setting's ID.
     *
     * @return the system setting's ID
     */
    @Override
    public long getId() {
        return id;
    }

    /**
     * Get the time in ticks when the system setting is created.
     *
     * @return the time in ticks when the system setting is created
     */
    @Override
    public long getCreateTime() {
        return createTime;
    }

    /**
     * Get a value indicating whether the running of experiments is enabled or not.
     *
     * @return a value indicating whether the running of experiments is enabled or not
     */
    public boolean isRunningExperimentsEnabled() {
        return runningExperimentsEnabled;
    }

    /**
     * Set a value indicating whether the running of experiments is enabled or not.
     *
     * @param runningExperimentsEnabled a value indicating whether the running of experiments is enabled or not
     */
    public void setRunningExperimentsEnabled(boolean runningExperimentsEnabled) {
        this.runningExperimentsEnabled = runningExperimentsEnabled;
    }
}
