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
package archimulator.uncore.coherence.message;

import archimulator.uncore.MemoryDeviceMessageType;
import archimulator.uncore.MemoryHierarchyAccess;
import archimulator.util.action.Action1;

public class DownwardReadMessage extends MemoryDeviceMessage<DownwardReadMessage> {
    private boolean shared;

    public DownwardReadMessage(MemoryHierarchyAccess access, int tag, Action1<DownwardReadMessage> onCompletedCallback) {
        super(MemoryDeviceMessageType.DOWNWARD_READ, access, tag, onCompletedCallback);
    }

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }
}
