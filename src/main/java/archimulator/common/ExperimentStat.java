/**
 * ****************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.common;

import java.io.Serializable;

/**
 * Experiment statistic.
 *
 * @author Min Cai
 */
public class ExperimentStat implements Serializable {
    private String prefix;

    private String key;

    private String value;

    /**
     * Create an experiment statistic.
     *
     * @param prefix   the prefix
     * @param key      the key
     * @param value    the value
     */
    public ExperimentStat(String prefix, String key, String value) {
        this.prefix = prefix;
        this.key = key;
        this.value = value;
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

    @Override
    public String toString() {
        return String.format("ExperimentStat{prefix='%s', key='%s', value='%s'}", prefix, key, value);
    }
}
