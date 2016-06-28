/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.uncore.helperThread;

/**
 * Helper thread L2 cache request quality.
 *
 * @author Min Cai
 */
public enum HelperThreadL2RequestQuality {
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
     * Early.
     */
    EARLY,

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
        return this == UGLY || this == BAD;
    }

    /**
     * Get a value indicating whether the current state of the quality is useful or not.
     *
     * @return a value indicating whether the current state of the quality is useful or not
     */
    public boolean isUseful() {
        return this == TIMELY || this == LATE;
    }

    /**
     * Get a value indicating whether the current state of the quality is polluting or not.
     *
     * @return a value indicating whether the current state of the quality is polluting or not
     */
    public boolean isPolluting() {
        return this == BAD;
    }
}
