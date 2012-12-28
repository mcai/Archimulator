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

import com.Ostermiller.util.CSVPrinter;
import net.pickapack.Pair;
import net.pickapack.action.Function1;
import net.pickapack.util.CollectionHelper;
import org.apache.commons.io.IOUtils;
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.DataContextFactory;
import org.eobjects.metamodel.csv.CsvConfiguration;
import org.eobjects.metamodel.data.DataSet;
import org.eobjects.metamodel.data.Row;
import org.eobjects.metamodel.query.FilterItem;
import org.eobjects.metamodel.query.LogicalOperator;
import org.eobjects.metamodel.query.OperatorType;
import org.eobjects.metamodel.query.SelectItem;
import org.eobjects.metamodel.query.builder.SatisfiedSelectBuilder;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * CSV-mappable table object.
 *
 * @author Min Cai
 */
public class Table implements Serializable {
    private List<String> columns;
    private List<List<String>> rows;

    /**
     * Create a table object.
     *
     * @param columns a list of columns
     * @param rows a two-dimensional list of rows
     */
    public Table(List<String> columns, List<List<String>> rows) {
        this.columns = columns;
        this.rows = rows;
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
     * Get the two-dimensional list of rows.
     *
     * @return the two-dimensional list of rows
     */
    public List<List<String>> getRows() {
        return rows;
    }

    /**
     * Create a table object from the specified CSV file.
     *
     * @param csvFileName the CSV file name
     * @return a table object created from the specified CSV file
     */
    public static Table fromCsv(String csvFileName) {
        DataContext dataContext = DataContextFactory.createCsvDataContext(new File(csvFileName), CsvConfiguration.DEFAULT_SEPARATOR_CHAR, CsvConfiguration.DEFAULT_QUOTE_CHAR);
        org.eobjects.metamodel.schema.Table table = dataContext.getDefaultSchema().getTables()[0];
        DataSet dataSet = dataContext.query()
                .from(table)
                .select(table.getColumnNames()).execute();
        return fromDataSet(dataSet);
    }

    /**
     * Get the converted CSV file content.
     *
     * @return the converted CSV file content
     */
    public String toCsv() {
        return toCsv(true);
    }

    /**
     * Get the converted CSV file content.
     *
     * @param printColumnHeaders a value indicating whether the column headers should be included or not
     * @return the converted CSV file content
     */
    public String toCsv(boolean printColumnHeaders) {
        try {
            StringWriter sw = new StringWriter();
            CSVPrinter csvPrinter = new CSVPrinter(sw, '#', CsvConfiguration.DEFAULT_QUOTE_CHAR, CsvConfiguration.DEFAULT_SEPARATOR_CHAR, true, true);

            if(printColumnHeaders) {
                csvPrinter.println(getColumns().toArray(new String[getColumns().size()]));
            }

            for(List<String> row : getRows()) {
                csvPrinter.println(row.toArray(new String[row.size()]));
            }

            csvPrinter.close();

            return sw.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Filter the table based on the specified criteria.
     *
     * @param criteria the filter criteria
     * @return a new table created based on the specified criteria from the original table
     */
    public Table filter(TableFilterCriteria criteria) {
        try {
            DataContext dataContext = DataContextFactory.createCsvDataContext(IOUtils.toInputStream(toCsv(), "UTF-8"), CsvConfiguration.DEFAULT_SEPARATOR_CHAR, CsvConfiguration.DEFAULT_QUOTE_CHAR);

            final org.eobjects.metamodel.schema.Table table = dataContext.getDefaultSchema().getTables()[0];

            SatisfiedSelectBuilder<?> select = dataContext.query()
                    .from(table)
                    .select(criteria.getColumns().toArray(new String[criteria.getColumns().size()]));

            for(final Pair<String, List<String>> condition : criteria.getConditions()) {
                select.where(new FilterItem(LogicalOperator.OR, CollectionHelper.transform(condition.getSecond(), new Function1<String, FilterItem>() {
                    @Override
                    public FilterItem apply(String conditionLine) {
                        return new FilterItem(new SelectItem(table.getColumnByName(condition.getFirst())), OperatorType.EQUALS_TO, conditionLine);
                    }
                })));
            }

            DataSet dataSet = select.execute();
            return fromDataSet(dataSet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a table object from the specified data set object.
     *
     * @param dataSet the data set object
     * @return a table object created from the specified data set object
     */
    public static Table fromDataSet(DataSet dataSet) {
        List<String> columns = new ArrayList<String>();

        for(SelectItem selectedItem : dataSet.getSelectItems()) {
            columns.add(selectedItem.getColumn().getName());
        }

        List<List<String>> rows = new ArrayList<List<String>>();

        for(Row row : dataSet) {
            rows.add(CollectionHelper.transform(Arrays.asList(row.getValues()), new Function1<Object, String>() {
                @Override
                public String apply(Object obj) {
                    return obj + "";
                }
            }));
        }

        return new Table(columns, rows);
    }
}
