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
package archimulator.sim.uncore.coherence.msi.state;

/**
 * Cache controller state.
 *
 * @author Min Cai
 */
public enum CacheControllerState {
    /**
     * I.
     */
    I,

    /**
     * IS_D.
     */
    IS_D,

    /**
     * IM_AD.
     */
    IM_AD,

    /**
     * IM_A.
     */
    IM_A,

    /**
     * S.
     */
    S,

    /**
     * SM_AD.
     */
    SM_AD,

    /**
     * SM_A.
     */
    SM_A,

    /**
     * M.
     */
    M,

    /**
     * MI_A.
     */
    MI_A,

    /**
     * SI_A.
     */
    SI_A,

    /**
     * II_A.
     */
    II_A;

    /**
     * Get a value indicating whether the state is stable or not.
     *
     * @return a value indicating whether the state is stable or not.
     */
    public boolean isStable() {
        return this == I || this == S || this == M;
    }

    /**
     * Get a value indicating whether the state is transient or not.
     *
     * @return a value indicating whether the state is transient or not
     */
    public boolean isTransient() {
        return !isStable();
    }
}
