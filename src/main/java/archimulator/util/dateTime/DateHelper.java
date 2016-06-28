/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.util.dateTime;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Date helper.
 *
 * @author Min Cai
 */
public class DateHelper {
    /**
     * Get the ticks of the current time.
     *
     * @return the ticks of the current time
     */
    public static long toTick() {
        return toTick(new Date());
    }

    /**
     * Get the ticks of the specified time.
     *
     * @param time the time
     * @return the ticks of the specified time
     */
    public static long toTick(Date time) {
        return time.getTime();
    }

    /**
     * Get the date time object from the specified ticks.
     *
     * @param tick the ticks
     * @return the date time object converted from the specified ticks
     */
    public static Date fromTick(long tick) {
        return new Date(tick);
    }

    /**
     * Get the string representation of the specified ticks.
     *
     * @param tick the ticks
     * @return the string representation of the specified ticks
     */
    public static String toString(long tick) {
        return toString(fromTick(tick));
    }

    /**
     * Get the string representation of the specified date time object.
     *
     * @param date the date time object
     * @return the string representation of the specified date time object
     */
    public static String toString(Date date) {
        return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date);
    }

    /**
     * Get the string representation of the specified date time object, which is suitable for use in file names.
     *
     * @param date the date time object
     * @return the string representation of the specified date time object, which is suitable for use in file names
     */
    public static String toFileNameString(Date date) {
        return new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss").format(date);
    }
}
