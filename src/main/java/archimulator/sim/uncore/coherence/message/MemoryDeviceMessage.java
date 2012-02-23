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
package archimulator.sim.uncore.coherence.message;

import archimulator.sim.uncore.MemoryDeviceMessageType;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.util.action.Action1;

public abstract class MemoryDeviceMessage<MessageT extends MemoryDeviceMessage<?>> {
    private MemoryDeviceMessageType type;
    private MemoryHierarchyAccess access;
    private int tag;
    private Action1<MessageT> onReplyCallback;
    private boolean hasError;

    public MemoryDeviceMessage(MemoryDeviceMessageType type, MemoryHierarchyAccess access, int tag, Action1<MessageT> onReplyCallback) {
        this.type = type;
        this.access = access;
        this.tag = tag;
        this.onReplyCallback = onReplyCallback;
    }

    @SuppressWarnings("unchecked")
    public void reply() {
        if (this.onReplyCallback != null) {
            this.onReplyCallback.apply((MessageT) this);
            this.onReplyCallback = null;
        }
    }

    public MemoryDeviceMessageType getType() {
        return type;
    }

    public MemoryHierarchyAccess getAccess() {
        return access;
    }

    public int getTag() {
        return tag;
    }

    public boolean isHasError() {
        return hasError;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }
}
