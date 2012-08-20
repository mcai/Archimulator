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
package archimulator.util;

import org.apache.commons.io.FileUtils;

public class StorageUnitHelper {
    public static long displaySizeToByteCount(String displaySize) {
        String[] parts = displaySize.split(" ");
        if(parts.length == 2) {
            double scale = Double.parseDouble(parts[0]);
            String unit = parts[1];

            if(unit.equals("KB")) {
                return (long) (scale * FileUtils.ONE_KB);
            }
            else if(unit.equals("MB")) {
                return (long) (scale * FileUtils.ONE_MB);
            }
            else if(unit.equals("GB")) {
                return (long) (scale * FileUtils.ONE_GB);
            }
            else if(unit.equals("TB")) {
                return (long) (scale * FileUtils.ONE_TB);
            }
            else if(unit.equals("PB")) {
                return (long) (scale * FileUtils.ONE_PB);
            }
            else if(unit.equals("EB")) {
                return (long) (scale * FileUtils.ONE_EB);
            }
        }

        throw new IllegalArgumentException();
    }
}
