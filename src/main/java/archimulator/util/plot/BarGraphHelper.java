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
package archimulator.util.plot;

import net.pickapack.action.Function1;

import java.util.ArrayList;
import java.util.List;

/**
 * Histogram plot helper.
 *
 * @author Min Cai
 */
public class BarGraphHelper {
    /**
     * Get the value from the specified row and column in the specified CSV file.
     *
     * @param csvFileName the CSV file name
     * @param row         the row
     * @param column      the column
     * @return the value from the specified row and column in the specified CSV file
     */
    public static String getValueFromCsvFile(String csvFileName, int row, int column) {
        return Table.fromCsv(csvFileName).getRows().get(row).get(column);
    }

    /**
     * Get the list of values from the specified rows and column in the specified CSV file.
     *
     * @param csvFileName the CSV file name
     * @param minRow      the min row
     * @param maxRow      the max row
     * @param column      the column
     * @return the list of values from the specified rows and column in the specified CSV file
     */
    public static List<String> getValuesFromCsvFile(String csvFileName, int minRow, int maxRow, int column) {
        List<String> values = new ArrayList<>();

        for (int i = minRow; i <= maxRow; i++) {
            values.add(Table.fromCsv(csvFileName).getRows().get(i).get(column));
        }

        return values;
    }

    /**
     * Get the list of values from the specified rows and columns in the specified CSV file.
     *
     * @param csvFileName the CSV file name
     * @param minRow      the min row
     * @param maxRow      the max row
     * @param minColumn   the min column
     * @param maxColumn   the max column
     * @param func        the mapping function
     * @return the list of values from the specified rows and columns in the specified CSV file
     */
    public static List<String> getValuesFromCsvFile(String csvFileName, int minRow, int maxRow, int minColumn, int maxColumn, Function1<List<String>, String> func) {
        List<String> values = new ArrayList<>();

        for (int i = minRow; i <= maxRow; i++) {
            List<String> valuesPerRow = new ArrayList<>();
            for (int j = minColumn; j <= maxColumn; j++) {
                valuesPerRow.add(Table.fromCsv(csvFileName).getRows().get(i).get(j));
            }
            values.add(func.apply(valuesPerRow));
        }

        return values;
    }

    /**
     * Compare two numbers.
     *
     * @param a the first number
     * @param b the second number
     * @param <T> the type
     * @return the result
     */
    @SuppressWarnings("unchecked")
    public static  <T extends Number> int compare(T a, T b) {
        if (a instanceof Comparable)
            if (a.getClass().equals(b.getClass()))
                return ((Comparable<T>) a).compareTo(b);
        throw new UnsupportedOperationException();
    }

    /**
     * get the min of the two numbers.
     *
     * @param a the first number
     * @param b the second number
     * @param <T> the type
     * @return the result
     */
    public static  <T extends Number> T min(T a, T b) {
        return compare(a, b) < 0 ? a : b;
    }

    /**
     * get the max of the two numbers.
     *
     * @param a the first number
     * @param b the second number
     * @param <T> the type
     * @return the result
     */
    public static  <T extends Number> T max(T a, T b) {
        return compare(a, b) > 0 ? a : b;
    }

    /**
     * Get the lowest value among the specified list of items.
     *
     * @param items the list of items
     * @param func  the mapping function of items to values
     * @param <T>   the type of the items
     * @return the lowest value among the specified list of items
     */
    public static <T> Number getLowest(List<T> items, Function1<T, Number> func) {
        Number lowest = items.isEmpty() ? 0 : func.apply(items.get(0));

        for (T item : items) {
            Number value = func.apply(item);
            if (compare(value, lowest) < 0) {
                lowest = value;
            }
        }

        return lowest;
    }

    /**
     * Get the highest value among the specified list of items.
     *
     * @param items the list of items
     * @param func  the mapping function of items to values
     * @param <T>   the type of the items
     * @return the highest value among the specified list of items
     */
    public static <T> Number getHighest(List<T> items, Function1<T, Number> func) {
        Number highest = items.isEmpty() ? 0 : func.apply(items.get(0));

        for (T item : items) {
            Number value = func.apply(item);
            if (compare(value, highest) > 0) {
                highest = value;
            }
        }

        return highest;
    }

    /**
     * Get the automatically calculated y min value from the specified min and max.
     *
     * @param min the min
     * @param max the max
     * @return the automatically calculated y min value from the specified min and max.
     */
    public static double getAutomaticYMin(double min, double max) {
        double room = (max - min) * 0.05;
        min -= room;
        return Math.max(min, 0);
    }

    /**
     * Get the automatically calculated y max value from the specified min and max.
     *
     * @param min the min
     * @param max the max
     * @return the automatically calculated y max value from the specified min and max.
     */
    public static double getAutomaticYMax(double min, double max) {
        double room = (max - min) * 0.05;
        max += room;
        return Math.max(max, 0);
    }
}
