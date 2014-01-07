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

import java.io.Serializable;
import java.util.List;

/**
 * Experiment pack variable.
 *
 * @author Min Cai
 */
public class ExperimentPackVariable implements Serializable {
    private String name;
    private List<String> values;

    /**
     * Get the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the values.
     *
     * @return the values
     */
    public List<String> getValues() {
        return values;
    }

    /**
     * Set the values.
     *
     * @param values the values
     */
    public void setValues(List<String> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return String.format("ExperimentPackVariable{name='%s', values=%s}", name, values);
    }
}
