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
package archimulator.sim.uncore.tlb;

import archimulator.sim.uncore.cache.CacheGeometry;

import java.io.Serializable;

public class TranslationLookasideBufferConfig implements Serializable {
    private CacheGeometry geometry;
    private int hitLatency;
    private int missLatency;

    public TranslationLookasideBufferConfig(int size, int associativity) {
        this(new CacheGeometry(size, associativity, 64), 2, 30);
    }

    public TranslationLookasideBufferConfig(CacheGeometry geometry, int hitLatency, int missLatency) {
        this.geometry = geometry;
        this.hitLatency = hitLatency;
        this.missLatency = missLatency;
    }

    public CacheGeometry getGeometry() {
        return geometry;
    }

    public int getHitLatency() {
        return hitLatency;
    }

    public int getMissLatency() {
        return missLatency;
    }
}
