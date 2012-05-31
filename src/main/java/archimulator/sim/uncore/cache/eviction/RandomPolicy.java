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
package archimulator.sim.uncore.cache.eviction;

import archimulator.sim.uncore.cache.EvictableCache;

import java.io.Serializable;
import java.util.Random;

public class RandomPolicy<StateT extends Serializable> extends EvictionPolicy<StateT> {
    private Random random;

    public RandomPolicy(EvictableCache<StateT> cache) {
        super(cache);

        this.random = new Random(13);
    }

    @Override
    public int getVictim(int set) {
        return this.random.nextInt(this.getCache().getAssociativity());
    }

    @Override
    public void handlePromotionOnHit(int set, int way) {
    }

    @Override
    public void handleInsertionOnMiss(int set, int way) {
    }
}
