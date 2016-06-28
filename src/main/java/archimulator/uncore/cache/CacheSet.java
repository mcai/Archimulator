/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.uncore.cache;

import archimulator.util.ValueProvider;
import archimulator.util.ValueProviderFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Cache set.
 *
 * @param <StateT> state
 * @author Min Cai
 */
public class CacheSet<StateT extends Serializable> implements Serializable {
    private Cache<StateT> cache;
    private List<CacheLine<StateT>> lines;
    private int num;

    /**
     * Create a cache set.
     *
     * @param cache                         the parent cache
     * @param associativity                 the associativity
     * @param num                           the cache set number
     * @param cacheLineStateProviderFactory the cache line state provider factory
     */
    public CacheSet(Cache<StateT> cache, int associativity, int num, ValueProviderFactory<StateT, ValueProvider<StateT>> cacheLineStateProviderFactory) {
        this.cache = cache;
        this.num = num;
        this.lines = new ArrayList<>();
        for (int i = 0; i < associativity; i++) {
            this.lines.add(new CacheLine<>(cache, this.num, i, cacheLineStateProviderFactory.createValueProvider(this.num, i)));
        }

    }

    /**
     * Get the parent cache.
     *
     * @return the parent cache
     */
    public Cache<StateT> getCache() {
        return cache;
    }

    /**
     * Get the constituent list of lines.
     *
     * @return the constituent list of lines
     */
    public List<CacheLine<StateT>> getLines() {
        return lines;
    }

    /**
     * Get the cache set number.
     *
     * @return the cache set number
     */
    public int getNum() {
        return num;
    }
}
