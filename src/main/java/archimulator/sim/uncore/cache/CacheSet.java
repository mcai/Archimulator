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

import net.pickapack.util.ValueProvider;
import net.pickapack.util.ValueProviderFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 *
 * @author Min Cai
 * @param <StateT>
 */
public class CacheSet<StateT extends Serializable> {
    private Cache<StateT> cache;
    private List<CacheLine<StateT>> lines;
    private int num;
    private Stack<Integer> lruStack;

    /**
     *
     * @param cache
     * @param associativity
     * @param num
     * @param cacheLineStateProviderFactory
     */
    public CacheSet(Cache<StateT> cache, int associativity, int num, ValueProviderFactory<StateT, ValueProvider<StateT>> cacheLineStateProviderFactory) {
        this.cache = cache;
        this.num = num;

        this.lines = new ArrayList<CacheLine<StateT>>();
        for (int i = 0; i < associativity; i++) {
            this.lines.add(new CacheLine<StateT>(cache, this.num, i, cacheLineStateProviderFactory.createValueProvider(this.num, i)));
        }

        this.lruStack = new Stack<Integer>();
    }

    /**
     *
     * @return
     */
    public Cache<StateT> getCache() {
        return cache;
    }

    /**
     *
     * @return
     */
    public List<CacheLine<StateT>> getLines() {
        return lines;
    }

    /**
     *
     * @return
     */
    public int getNum() {
        return num;
    }

    /**
     *
     * @return
     */
    public Stack<Integer> getLruStack() {
        return lruStack;
    }
}
