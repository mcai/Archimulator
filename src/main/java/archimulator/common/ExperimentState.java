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
 * Experiment state.
 *
 * @author Min Cai
 */
public enum ExperimentState {
    /**
     * Pending.
     */
    PENDING,

    /**
     * Ready to run, i.e., enqueued.
     */
    READY_TO_RUN,

    /**
     * Running.
     */
    RUNNING,

    /**
     * Completed simulation.
     */
    COMPLETED,

    /**
     * Aborted, due to some error(s) occurred during simulation.
     */
    ABORTED
}
