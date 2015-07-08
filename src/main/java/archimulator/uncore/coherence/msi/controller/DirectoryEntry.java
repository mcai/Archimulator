/**
 * ****************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.uncore.coherence.msi.controller;

import java.util.ArrayList;
import java.util.List;

/**
 * Directory entry.
 *
 * @author Min Cai
 */
public class DirectoryEntry {
    private CacheController owner;
    private List<CacheController> sharers;

    /**
     * Create a directory entry.
     */
    public DirectoryEntry() {
        this.sharers = new ArrayList<>();
    }

    /**
     * Get the owner L1 cache controller.
     *
     * @return the owner L1 cache controller
     */
    public CacheController getOwner() {
        return owner;
    }

    /**
     * Set the owner L1 cache controller.
     *
     * @param owner the owner L1 cache controller
     */
    public void setOwner(CacheController owner) {
        this.owner = owner;
    }

    /**
     * Get the sharer list of L1 cache controllers.
     *
     * @return the sharer list of L1 cache controllers
     */
    public List<CacheController> getSharers() {
        return sharers;
    }
}
