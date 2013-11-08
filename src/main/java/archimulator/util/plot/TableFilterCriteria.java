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
package archimulator.util.plot;

import net.pickapack.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Table filter criteria.
 *
 * @author Min Cai
 */
public class TableFilterCriteria {
    private List<String> columns;
    private List<Pair<String, List<String>>> conditions;

    private boolean preserveColumns;

    /**
     * Create a table filter criteria.
     *
     * @param columns an array of columns
     */
    public TableFilterCriteria(String... columns) {
        this(Arrays.asList(columns));
    }

    /**
     * Create a table filter criteria.
     *
     * @param columns a list of columns
     */
    public TableFilterCriteria(List<String> columns) {
        this.columns = columns;
        this.conditions = new ArrayList<>();
    }

    /**
     * Get the list of columns.
     *
     * @return the list of columns
     */
    public List<String> getColumns() {
        return columns;
    }

    /**
     * Get the list of conditions.
     *
     * @return the list of conditions
     */
    public List<Pair<String, List<String>>> getConditions() {
        return conditions;
    }

    /**
     * Get a value indicating whether the columns of the original table should be preserved.
     *
     * @return a value indicating whether the columns of the original table should be preserved
     */
    public boolean isPreserveColumns() {
        return preserveColumns;
    }

    /**
     * Set a value indicating whether the columns of the original table should be preserved.
     *
     * @param preserveColumns a value indicating whether the columns of the original table should be preserved
     */
    public void setPreserveColumns(boolean preserveColumns) {
        this.preserveColumns = preserveColumns;
    }
}
