/*******************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.core.bpred;

/**
 * Branch target buffer entry.
 *
 * @author Min Cai
 */
public class BranchTargetBufferEntry {
    private int source;
    private int target;

    /**
     * Create a branch target buffer entry.
     */
    public BranchTargetBufferEntry() {
    }

    /**
     * Get the source address.
     *
     * @return the source address.
     */
    public int getSource() {
        return source;
    }

    /**
     * Set the source address.
     *
     * @param source the source address
     */
    public void setSource(int source) {
        this.source = source;
    }

    /**
     * Get the target address.
     *
     * @return the target address
     */
    public int getTarget() {
        return target;
    }

    /**
     * Set the target address.
     *
     * @param target the target address
     */
    public void setTarget(int target) {
        this.target = target;
    }
}
