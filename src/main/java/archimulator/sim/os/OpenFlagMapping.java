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
package archimulator.sim.os;

/**
 * Open flag mapping.
 *
 * @author Min Cai
 */
public class OpenFlagMapping {
    private int targetFlag;
    private int hostFlag;

    /**
     * Create an open flag mapping.
     *
     * @param targetFlag the target flag
     * @param hostFlag the host flag
     */
    public OpenFlagMapping(int targetFlag, int hostFlag) {
        this.targetFlag = targetFlag;
        this.hostFlag = hostFlag;
    }

    /**
     * Get the target flag.
     *
     * @return the target flag
     */
    public int getTargetFlag() {
        return targetFlag;
    }

    /**
     * Get the host flag.
     *
     * @return the host flag
     */
    public int getHostFlag() {
        return hostFlag;
    }
}
