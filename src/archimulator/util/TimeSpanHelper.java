/*******************************************************************************
 * Copyright (c) 2010-2011 by Min Cai (min.cai.china@gmail.com).
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

public class TimeSpanHelper {
    private static String zeroNumber(long in) {
        return (in < 10 ? "0" : "") + in;
    }

    public static String toString(long timespanInMilliseconds) {
        long days = 0;
        long hours = 0;
        long minutes = 0;
        long seconds = 0;
        long mseconds = 0;

        if (timespanInMilliseconds > 86400000L) {
            days = timespanInMilliseconds / 86400000L;
            timespanInMilliseconds = timespanInMilliseconds % 86400000L;
        }

        if (timespanInMilliseconds > 3600000L) {
            hours = timespanInMilliseconds / 3600000L;
            timespanInMilliseconds = timespanInMilliseconds % 3600000L;
        }

        if (timespanInMilliseconds > 60000L) {
            minutes = timespanInMilliseconds / 60000L;
            timespanInMilliseconds = timespanInMilliseconds % 60000L;
        }

        if (timespanInMilliseconds > 1000L) {
            seconds = timespanInMilliseconds / 1000L;
            timespanInMilliseconds = timespanInMilliseconds % 1000L;
        }

        mseconds = timespanInMilliseconds;

        StringBuilder sb = new StringBuilder(64);
        boolean hasDays = false;

        if (days > 0) {
            hasDays = true;
            sb.append(days);
            sb.append(days > 1 ? " days" : " day");
        }

        if (hours > 0 || minutes > 0 || seconds > 0 || mseconds > 0) {
            if (hasDays) {
                sb.append(" ");
            }
            sb.append(zeroNumber(hours));
            sb.append(":");
            sb.append(zeroNumber(minutes));
            sb.append(":");
            sb.append(zeroNumber(seconds));
            sb.append(".");
            sb.append(mseconds);
        }

        return (sb.toString());
    }
}