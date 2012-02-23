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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package archimulator.sim.os.signal;

import java.io.Serializable;

public class SignalMasks implements Serializable {
    private SignalMask pending;
    private SignalMask blocked;
    private SignalMask backup;

    public SignalMasks() {
        this.pending = new SignalMask();
        this.blocked = new SignalMask();
        this.backup = new SignalMask();
    }

    public SignalMask getPending() {
        return pending;
    }

    public void setPending(SignalMask pending) {
        this.pending = pending;
    }

    public SignalMask getBlocked() {
        return blocked;
    }

    public void setBlocked(SignalMask blocked) {
        this.blocked = blocked;
    }

    public SignalMask getBackup() {
        return backup;
    }

    public void setBackup(SignalMask backup) {
        this.backup = backup;
    }
}
