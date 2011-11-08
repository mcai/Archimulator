/*******************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.os.event;

import archimulator.os.Context;
import archimulator.os.Kernel;

public class SignalCriterion implements SystemEventCriterion {
    public boolean needProcess(Context context) {
        for (int sig = 1; sig <= Kernel.MAX_SIGNAL; sig++) {
            if (context.getKernel().mustProcessSignal(context, sig)) {
                return true;
            }
        }

        return false;
    }
}
