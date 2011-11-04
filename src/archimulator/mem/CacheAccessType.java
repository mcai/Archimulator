/*******************************************************************************
 * Copyright (c) 2010-2011 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.mem;

public enum CacheAccessType {
    LOAD,
    STORE,
    DOWNWARD_READ,
    DOWNWARD_WRITE,

    EVICT,

    UPWARD_READ,
    UPWARD_WRITE,

    PREFETCH,
    UNKNOWN;

    public boolean isDownwardRead() {
        return this == LOAD || this == DOWNWARD_READ;
    }

    public boolean isDownwardWrite() {
        return this == STORE || this == DOWNWARD_WRITE;
    }

    public boolean isDownwardReadOrWrite() {
        return this.isDownwardRead() || this.isDownwardWrite();
    }

    public boolean isUpward() {
        return this == UPWARD_READ || this == UPWARD_WRITE;
    }

    public boolean isDownward() {
        return !this.isUpward();
    }

    public boolean isRead() {
        return this == LOAD || this == DOWNWARD_READ || this == UPWARD_READ;
    }

    public boolean isWriteback() {
        return this == EVICT;
    }
}
