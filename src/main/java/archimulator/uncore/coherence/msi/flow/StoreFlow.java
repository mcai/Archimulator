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
package archimulator.uncore.coherence.msi.flow;

import archimulator.uncore.MemoryHierarchyAccess;
import archimulator.uncore.coherence.msi.controller.CacheController;
import archimulator.util.action.Action;

/**
 * Store flow.
 *
 * @author Min Cai
 */
public class StoreFlow extends CacheCoherenceFlow {
    private Action onCompletedCallback;

    /**
     * Create a store flow.
     *
     * @param generator           the generator L1 cache controller
     * @param tag                 the tag
     * @param onCompletedCallback the callback action performed when the store flow is completed
     * @param access              the memory hierarchy access
     */
    public StoreFlow(final CacheController generator, int tag, final Action onCompletedCallback, MemoryHierarchyAccess access) {
        super(generator, null, access, tag);

        this.onCompletedCallback = () -> {
            onCompletedCallback.apply();
            onCompleted();
        };
    }

    /**
     * Get the callback action performed when the store flow is completed.
     *
     * @return the callback action performed when the store flow is completed
     */
    public Action getOnCompletedCallback() {
        return onCompletedCallback;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s: StoreFlow{id=%d, tag=0x%08x}", getBeginCycle(), getGenerator(), getId(), getTag());
    }
}
