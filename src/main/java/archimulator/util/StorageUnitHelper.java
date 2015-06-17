/*******************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
 *
 * This file is part of the PickaPack library.
 *
 * PickaPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PickaPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PickaPack. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package archimulator.util;

import org.apache.commons.io.FileUtils;

/**
 * Storage unit helper.
 *
 * @author Min Cai
 */
public class StorageUnitHelper {
    /**
     * Convert the specified display size to the byte count.
     *
     * @param displaySize the display size
     * @return the byte count corresponding to the specified display size
     */
    public static long displaySizeToByteCount(String displaySize) {
        String[] parts = displaySize.split(" ");
        if (parts.length == 2) {
            double scale = Double.parseDouble(parts[0]);
            String unit = parts[1];

            switch (unit) {
                case "KB":
                    return (long) (scale * FileUtils.ONE_KB);
                case "MB":
                    return (long) (scale * FileUtils.ONE_MB);
                case "GB":
                    return (long) (scale * FileUtils.ONE_GB);
                case "TB":
                    return (long) (scale * FileUtils.ONE_TB);
                case "PB":
                    return (long) (scale * FileUtils.ONE_PB);
                case "EB":
                    return (long) (scale * FileUtils.ONE_EB);
            }
        }

        throw new IllegalArgumentException();
    }
}
