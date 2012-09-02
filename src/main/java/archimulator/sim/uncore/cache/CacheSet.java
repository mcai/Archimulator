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

public class CacheSet<StateT extends Serializable> {
    private Cache<StateT> cache;
    private List<CacheLine<StateT>> lines;
    private int num;
    private List<Integer> tagsSeen;
    private List<Integer> lruStack;

    public CacheSet(Cache<StateT> cache, int associativity, int num, ValueProviderFactory<StateT, ValueProvider<StateT>> cacheLineStateProviderFactory) {
        this.cache = cache;
        this.num = num;

        this.lines = new ArrayList<CacheLine<StateT>>();
        for (int i = 0; i < associativity; i++) {
            this.lines.add(new CacheLine<StateT>(cache, this.num, i, cacheLineStateProviderFactory.createValueProvider(this.num, i)));
        }

        this.tagsSeen = new ArrayList<Integer>();
        this.lruStack = new ArrayList<Integer>();
    }

    public Cache<StateT> getCache() {
        return cache;
    }

    public List<CacheLine<StateT>> getLines() {
        return lines;
    }

    public int getNum() {
        return num;
    }

    public List<Integer> getTagsSeen() {
        return tagsSeen;
    }

    public List<Integer> getLruStack() {
        return lruStack;
    }
}
