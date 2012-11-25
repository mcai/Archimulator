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
package archimulator.model.metric;

import com.Ostermiller.util.CSVPrinter;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.List;

/**
 *
 * @author Min Cai
 */
public class Table implements Serializable {
    private List<String> columns;
    private List<List<String>> rows;

    public Table(List<String> columns, List<List<String>> rows) {
        this.columns = columns;
        this.rows = rows;
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<List<String>> getRows() {
        return rows;
    }

    public String toCsv() {
        try {
            StringWriter sw = new StringWriter();
            CSVPrinter csvPrinter = new CSVPrinter(sw);

            csvPrinter.println(getColumns().toArray(new String[getColumns().size()]));

            for(List<String> row : getRows()) {
                csvPrinter.println(row.toArray(new String[row.size()]));
            }

            csvPrinter.close();

            return sw.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
