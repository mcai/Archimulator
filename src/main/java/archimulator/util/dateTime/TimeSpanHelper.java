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
package archimulator.util.dateTime;

/**
 * Time span helper.
 *
 * @author Min Cai
 */
public class TimeSpanHelper {
    /**
     * Get the string representation of the specified time span measured in milliseconds.
     *
     * @param timeSpanInMilliseconds the time span measured in milliseconds
     * @return the string representation of the specified time span measured in milliseconds
     */
    public static String toString(long timeSpanInMilliseconds) {
        long days = 0;
        long hours = 0;
        long minutes = 0;
        long seconds = 0;
        long milliSeconds = 0;

        if (timeSpanInMilliseconds > 86400000L) {
            days = timeSpanInMilliseconds / 86400000L;
            timeSpanInMilliseconds = timeSpanInMilliseconds % 86400000L;
        }

        if (timeSpanInMilliseconds > 3600000L) {
            hours = timeSpanInMilliseconds / 3600000L;
            timeSpanInMilliseconds = timeSpanInMilliseconds % 3600000L;
        }

        if (timeSpanInMilliseconds > 60000L) {
            minutes = timeSpanInMilliseconds / 60000L;
            timeSpanInMilliseconds = timeSpanInMilliseconds % 60000L;
        }

        if (timeSpanInMilliseconds > 1000L) {
            seconds = timeSpanInMilliseconds / 1000L;
            timeSpanInMilliseconds = timeSpanInMilliseconds % 1000L;
        }

        milliSeconds = timeSpanInMilliseconds;

        StringBuilder sb = new StringBuilder(64);
        boolean hasDays = false;

        if (days > 0) {
            hasDays = true;
            sb.append(days);
            sb.append(days > 1 ? " days" : " day");
        }

        if (hours > 0 || minutes > 0 || seconds > 0 || milliSeconds > 0) {
            if (hasDays) {
                sb.append(" ");
            }
            sb.append(zeroNumber(hours));
            sb.append(":");
            sb.append(zeroNumber(minutes));
            sb.append(":");
            sb.append(zeroNumber(seconds));
            sb.append(".");
            sb.append(milliSeconds);
        }

        return (sb.toString());
    }

    private static String zeroNumber(long in) {
        return (in < 10 ? "0" : "") + in;
    }
}