/**
 * ****************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the PickaPack library.
 * <p>
 * PickaPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * PickaPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with PickaPack. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.util;

import java.text.NumberFormat;

/**
 * Storage unit.
 *
 * @author Min Cai
 */
public enum StorageUnit {
    /**
     * Byte.
     */
    BYTE("B", 1L),

    /**
     * Kilobyte.
     */
    KILOBYTE("KB", 1L << 10),

    /**
     * Megabyte.
     */
    MEGABYTE("MB", 1L << 20),

    /**
     * Gigabyte.
     */
    GIGABYTE("GB", 1L << 30),

    /**
     * Terabyte.
     */
    TERABYTE("TB", 1L << 40),

    /**
     * Petabyte.
     */
    PETABYTE("PB", 1L << 50),

    /**
     * Exabyte.
     */
    EXABYTE("EB", 1L << 60);

    private String symbol;
    private long divider;

    /**
     * Create a storage unit.
     *
     * @param symbol the symbol
     * @param divider the divider
     */
    StorageUnit(String symbol, long divider) {
        this.symbol = symbol;
        this.divider = divider;
    }

    /**
     * Get the storage unit for the specified number.
     *
     * @param number the number
     * @return the storage unit for the specified number
     */
    public static StorageUnit of(long number) {
        final long n = number > 0 ? -number : number;
        if (n > -(1L << 10)) {
            return BYTE;
        } else if (n > -(1L << 20)) {
            return KILOBYTE;
        } else if (n > -(1L << 30)) {
            return MEGABYTE;
        } else if (n > -(1L << 40)) {
            return GIGABYTE;
        } else if (n > -(1L << 50)) {
            return TERABYTE;
        } else if (n > -(1L << 60)) {
            return PETABYTE;
        } else {
            return EXABYTE;
        }
    }

    /**
     * Get the formatted string representation for the specified number.
     *
     * @param number the number
     * @return the formatted string representation for the specified number
     */
    public String format(long number) {
        return getValue(number) + " " + symbol;
    }

    /**
     * Get the value in the storage unit of the specified number
     *
     * @param number the number
     * @return the value in the storage unit of the specified number
     */
    public String getValue(double number) {
        return NUMBER_FORMAT.format(number / divider);
    }

    /**
     * Get the symbol.
     *
     * @return the symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Get the divider.
     *
     * @return the divider
     */
    public long getDivider() {
        return divider;
    }

    /**
     * Get the text representation in storage unit of the specified number.
     *
     * @param number the number
     * @return the text representation in storage unit of the specified number
     */
    public static String toString(long number) {
        return of(number).format(number);
    }

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    /**
     * Static constructor.
     */
    static {
        NUMBER_FORMAT.setGroupingUsed(false);
        NUMBER_FORMAT.setMinimumFractionDigits(0);
        NUMBER_FORMAT.setMaximumFractionDigits(1);
    }
}
