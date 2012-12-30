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
 * MERCHANpTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package archimulator.sim.os.event;

import archimulator.sim.os.Context;
import archimulator.sim.os.NativeSystemCalls;

/**
 * Time criterion.
 *
 * @author Min Cai
 */
public class TimeCriterion implements SystemEventCriterion {
    private long when;

    public boolean needProcess(Context context) {
        return this.getWhen() <= NativeSystemCalls.clock(context.getKernel().getCurrentCycle());
    }

    /**
     * Get the time when the criterion is time out.
     *
     * @return the time when the criterion is time out
     */
    public long getWhen() {
        return when;
    }

    /**
     * Set the time when the criterion is time out.
     *
     * @param when the time when the criterion is time out
     */
    public void setWhen(long when) {
        this.when = when;
    }
}
