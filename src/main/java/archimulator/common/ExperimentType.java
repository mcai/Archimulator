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
package archimulator.common;

/**
 * Experiment type.
 *
 * @author Min Cai
 */
public enum ExperimentType {
    /**
     * Functional experiment.
     */
    FUNCTIONAL,

    /**
     * Detailed experiment.
     */
    DETAILED,

    /**
     * Two-phase "fast forward and measurement" experiment.
     */
    TWO_PHASE;

    /**
     * Get the measurement simulation title prefix from the specified experiment type.
     *
     * @return the measurement simulation title prefix from the specified experiment type
     */
    public String getMeasurementTitlePrefix() {
        switch (this) {
            case TWO_PHASE:
                return "twoPhase/phase1";
            case FUNCTIONAL:
                return "functional";
            case DETAILED:
                return "detailed";
            default:
                throw new IllegalArgumentException();
        }
    }
}
