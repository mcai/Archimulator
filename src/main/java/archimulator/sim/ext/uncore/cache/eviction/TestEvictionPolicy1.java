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
package archimulator.sim.ext.uncore.cache.eviction;

import archimulator.model.event.ProcessorInitializedEvent;
import archimulator.sim.core.BasicThread;
import archimulator.sim.core.Processor;
import archimulator.sim.ext.uncore.delinquentLoad.DelinquentLoadIdentificationCapability;
import archimulator.sim.uncore.cache.CacheHit;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.cache.CacheMiss;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.eviction.LeastRecentlyUsedEvictionPolicy;
import archimulator.util.action.Action1;

import java.io.Serializable;

public class TestEvictionPolicy1<StateT extends Serializable, LineT extends CacheLine<StateT>> extends LeastRecentlyUsedEvictionPolicy<StateT, LineT> {
    private Processor processor;

    public TestEvictionPolicy1(EvictableCache<StateT, LineT> cache) {
        super(cache);

        cache.getBlockingEventDispatcher().addListener(ProcessorInitializedEvent.class, new Action1<ProcessorInitializedEvent>() {
            public void apply(ProcessorInitializedEvent event) {
                TestEvictionPolicy1.this.processor = event.getProcessor();
            }
        });
    }

//    @Override
//    public CacheMiss<StateT, LineT> handleReplacement(CacheReference reference) {
//        if (BasicThread.isHelperThread(reference.getThreadId()) && !isDelinquentLoad(reference.getPc())) {
//            return new CacheMiss<>(reference, -1, true);
//        } else {
//            return super.handleReplacement(reference);
//        }
//    }

    private boolean isDelinquentPc(int pc) {
        return this.processor.getCapability(DelinquentLoadIdentificationCapability.class).isDelinquentPc(BasicThread.getMainThreadId(), pc);
    }

    @Override
    public void handlePromotionOnHit(CacheHit<StateT, LineT> hit) {
        if (this.isDelinquentPc(hit.getReference().getAccess().getVirtualPc())) {
            this.setMRU(hit.getReference().getSet(), hit.getWay());
        } else {
            this.setStackPosition(hit.getReference().getSet(), hit.getWay(), Math.max(this.getStackPosition(hit.getReference().getSet(), hit.getWay()) - 1, 0));
        }
    }

    @Override
    public void handleInsertionOnMiss(CacheMiss<StateT, LineT> miss) {
        if (this.isDelinquentPc(miss.getReference().getAccess().getVirtualPc())) {
            this.setMRU(miss.getReference().getSet(), miss.getWay());
        } else {
//            this.setLRU(miss.getReference().getSet(), miss.getWay());
            this.setStackPosition(miss.getReference().getSet(), miss.getWay(), 4);
        }
    }
}
