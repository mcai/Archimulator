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

    public RoundRobinScheduler(List<ResourceT> resources, Predicate<ResourceT> pred, Function1<ResourceT, Boolean> consumeAction, int quant) {
        this.resources = resources;
        this.pred = pred;
        this.consumeAction = consumeAction;
        this.quant = quant;

        this.resourceId = 0;
    }

    public void consumeNext() {
        this.resourceId = consumeNext(this.resources, this.resourceId, this.quant, this.pred, this.consumeAction);
    }

    private static <T> int findNext(List<T> resources, Predicate<T> pred, BitSet except) {
        for (int i = 0; i < resources.size(); i++) {
            if (pred.apply(resources.get(i)) && !except.get(i)) {
                return i;
            }
        }

        return -1;
    }

    private static <T> int consumeNext(List<T> resources, int resourceId, int quant, Predicate<T> pred, Function1<T, Boolean> consumeAction) {
        BitSet stalled = new BitSet(resources.size());

        resourceId = (resourceId + 1) % resources.size();

        for (int numConsumed = 0; numConsumed < quant; numConsumed++) {
            if (stalled.get(resourceId) || !pred.apply(resources.get(resourceId))) {
                resourceId = findNext(resources, pred, stalled);
            }

            if (resourceId == -1) {
                break;
            }

            if (!consumeAction.apply(resources.get(resourceId))) {
                stalled.set(resourceId);
            }
        }

        return resourceId;
    }
}
