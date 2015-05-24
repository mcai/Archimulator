/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.os.signal;

/**
 * A set of signal masks.
 *
 * @author Min Cai
 */
public class SignalMasks {
    private SignalMask pending;
    private SignalMask blocked;
    private SignalMask backup;

    /**
     * Create a set of signal masks.
     */
    public SignalMasks() {
        this.pending = new SignalMask();
        this.blocked = new SignalMask();
        this.backup = new SignalMask();
    }

    /**
     * Get the pending signal mask.
     *
     * @return the pending signal mask
     */
    public SignalMask getPending() {
        return pending;
    }

    /**
     * Set the pending signal mask.
     *
     * @param pending the pending signal mask
     */
    public void setPending(SignalMask pending) {
        this.pending = pending;
    }

    /**
     * Get the blocked signal mask.
     *
     * @return the blocked signal mask
     */
    public SignalMask getBlocked() {
        return blocked;
    }

    /**
     * Set the blocked signal mask.
     *
     * @param blocked the blocked signal mask
     */
    public void setBlocked(SignalMask blocked) {
        this.blocked = blocked;
    }

    /**
     * Get the backup signal mask.
     *
     * @return the backup signal mask
     */
    public SignalMask getBackup() {
        return backup;
    }

    /**
     * Set the backup signal mask.
     *
     * @param backup the backup signal mask
     */
    public void setBackup(SignalMask backup) {
        this.backup = backup;
    }
}
