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
package archimulator.sim.uncore.cache;

import java.io.Serializable;

public class CacheMiss<StateT extends Serializable> extends CacheAccess<StateT> {
    public CacheMiss(EvictableCache<StateT> cache, CacheReference reference, int victimWay) {
        super(cache, reference, victimWay);
    }

    @Override
    public boolean isHitInCache() {
        return false;
    }

    public boolean isBypass() {
        return this.getWay() == -1;
    }

    @Override
    public boolean isEviction() {
        return !isBypass() && this.getLine().getState() != getLine().getInitialState();
    }

    @Override
    public CacheAccess<StateT> commit() {
        if (!this.isBypass()) {
            this.getLine().setTag(this.getReference().getTag()); //TODO: should notify cache's eviction policy
            this.getCache().getEvictionPolicy().handleInsertionOnMiss(this);
        }
        return super.commit();
    }
}
