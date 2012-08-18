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
package archimulator.sim.uncore.coherence.msi.flow;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import net.pickapack.action.Action;

public class StoreFlow extends CacheCoherenceFlow {
    private Action onCompletedCallback;
    private Action onCompletedCallback2;

    public StoreFlow(final CacheController generator, int tag, final Action onCompletedCallback, MemoryHierarchyAccess access) {
        super(generator, null, access, tag);
        this.onCompletedCallback = onCompletedCallback;

        this.onCompletedCallback2 = new Action() {
            @Override
            public void apply() {
                onCompletedCallback.apply();
                onCompleted();
            }
        };
    }

    public Action getOnCompletedCallback() {
        return onCompletedCallback;
    }

    public Action getOnCompletedCallback2() {
        return onCompletedCallback2;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s: StoreFlow{id=%d, tag=0x%08x}", getBeginCycle(), getGenerator(), getId(), getTag());
    }
}
