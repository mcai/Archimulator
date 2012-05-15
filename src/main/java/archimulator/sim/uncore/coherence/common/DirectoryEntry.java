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
package archimulator.sim.uncore.coherence.common;

import java.util.ArrayList;
import java.util.List;

public class DirectoryEntry {
    private boolean dirty;
    private List<FirstLevelCache> sharers;

    public DirectoryEntry() {
        this.dirty = false;
        this.sharers = new ArrayList<FirstLevelCache>();
    }

    public void reset() {
        this.dirty = false;
        this.sharers.clear();
    }

    public boolean isShared() {
        return this.getSharers().size() > 1;
    }

    public boolean isOwned() {
        return this.getSharers().size() == 1;
    }

    public boolean isOwnedOrShared() {
        return this.getSharers().size() > 0;
    }

    public FirstLevelCache getOwnerOrFirstSharer() {
        return this.getSharers().get(0);
    }

    public boolean isDirty() {
        return dirty;
    }

    public List<FirstLevelCache> getSharers() {
        return sharers;
    }
}
