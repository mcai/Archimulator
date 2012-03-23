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
package archimulator.util;

import archimulator.util.action.Function1;
import archimulator.util.action.Predicate;

import java.util.BitSet;
import java.util.List;

public class RoundRobinScheduler<ResourceT> {
    private final List<ResourceT> resources;
    private final Predicate<ResourceT> pred;
    private final Function1<ResourceT, Boolean> consumeAction;
    private final int quant;

    private int resourceId;

    private BitSet stalled;

    public RoundRobinScheduler(List<ResourceT> resources, Predicate<ResourceT> pred, Function1<ResourceT, Boolean> consumeAction, int quant) {
        this.resources = resources;
        this.pred = pred;
        this.consumeAction = consumeAction;
        this.quant = quant;

        this.resourceId = 0;

        this.stalled = new BitSet(this.resources.size());
    }

    public void consumeNext() {
        this.resourceId = consumeNext(this.resourceId);
    }

    private int findNext(BitSet except) {
        for (int i = 0; i < this.resources.size(); i++) {
            if (this.pred.apply(this.resources.get(i)) && !except.get(i)) {
                return i;
            }
        }

        return -1;
    }

    private int consumeNext(int resourceId) {
        this.stalled.clear();

        resourceId = (resourceId + 1) % this.resources.size();

        for (int numConsumed = 0; numConsumed < this.quant; numConsumed++) {
            if (this.stalled.get(resourceId) || !this.pred.apply(this.resources.get(resourceId))) {
                resourceId = findNext(this.stalled);
            }

            if (resourceId == -1) {
                break;
            }

            if (!this.consumeAction.apply(this.resources.get(resourceId))) {
                this.stalled.set(resourceId);
            }
        }

        return resourceId;
    }
}
