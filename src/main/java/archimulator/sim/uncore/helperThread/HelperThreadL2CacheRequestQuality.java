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
package archimulator.sim.uncore.helperThread;

/**
 * Helper thread L2 cache request quality.
 *
 * @author Min Cai
 */
public enum HelperThreadL2CacheRequestQuality {
    /**
     * Redundant hit to transient tag.
     */
    REDUNDANT_HIT_TO_TRANSIENT_TAG,

    /**
     * Redundant hit to cache.
     */
    REDUNDANT_HIT_TO_CACHE,

    /**
     * Timely.
     */
    TIMELY,

    /**
     * Late.
     */
    LATE,

    /**
     * Bad.
     */
    BAD,

    /**
     * Ugly.
     */
    UGLY,

    /**
     * Invalid.
     */
    INVALID;

    /**
     * Get a value indicating whether the current state of the quality is modifiable or not.
     *
     * @return a value indicating whether the current state of the quality is modifiable or not
     */
    public boolean isModifiable() {
        return this == UGLY;
    }

    /**
     * Get a value indicating whether the current state of the quality is useful or not.
     *
     * @return a value indicating whether the current state of the quality is useful or not
     */
    public boolean isUseful() {
        return this == TIMELY || this == LATE;
    }
}
