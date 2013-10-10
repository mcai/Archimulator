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

import archimulator.service.ServiceManager;

import java.io.Serializable;

/**
 * Experiment statistic.
 *
 * @author Min Cai
 */
public class ExperimentStat implements Serializable {
    private long parentId;

    private String prefix;

    private String key;

    private String value;

    /**
     * Create an experiment statistic. Reserved for ORM only.
     */
    public ExperimentStat() {
    }

    /**
     * Create an experiment statistic.
     *
     * @param parentId  the parent experiment ID
     * @param prefix  the prefix
     * @param key     the key
     * @param value   the value
     */
    public ExperimentStat(long parentId, String prefix, String key, String value) {
        this.parentId = parentId;
        this.prefix = prefix;
        this.key = key;
        this.value = value;
    }

    /**
     * Get the parent experiment object's ID.
     *
     * @return the parent experiment object's ID
     */
    public long getParentId() {
        return parentId;
    }

    /**
     * Get the prefix.
     *
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Get the key.
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Get the value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Get the parent experiment object.
     *
     * @return the parent experiment object
     */
    public Experiment getParent() {
        return ServiceManager.getExperimentService().getExperimentById(parentId);
    }

    @Override
    public String toString() {
        return String.format("ExperimentStat{parentId=%d, prefix='%s', key='%s', value='%s'}", parentId, prefix, key, value);
    }
}
