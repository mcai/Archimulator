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
package archimulator.util;

import net.pickapack.StorageUnit;

import java.text.MessageFormat;

/**
 *
 * @author Min Cai
 */
public class RuntimeHelper {
    /**
     *
     * @return max memory
     */
    public String getMaxMemory() {
        return MessageFormat.format("{0}", StorageUnit.toString(Runtime.getRuntime().maxMemory()));
    }

    /**
     *
     * @return total memory
     */
    public String getTotalMemory() {
        return MessageFormat.format("{0}", StorageUnit.toString(Runtime.getRuntime().totalMemory()));
    }

    /**
     *
     * @return used memory
     */
    public String getUsedMemory() {
        return MessageFormat.format("{0}", StorageUnit.toString(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
    }
}
