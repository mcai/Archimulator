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
package archimulator.util.comparator;

import net.pickapack.util.StorageUnitHelper;

import java.util.Comparator;

/**
 * Storage unit comparator.
 *
 * @author Min Cai
 */
public class StorageUnitComparator implements Comparator<String> {
    @Override
    public int compare(String o1, String o2) {
        String str1 = o1.replaceAll("KB", " KB").replaceAll("MB", " MB").replaceAll("GB", " GB");
        String str2 = o2.replaceAll("KB", " KB").replaceAll("MB", " MB").replaceAll("GB", " GB");

        return ((Long) StorageUnitHelper.displaySizeToByteCount(str1))
                .compareTo(StorageUnitHelper.displaySizeToByteCount(str2));
    }
}
