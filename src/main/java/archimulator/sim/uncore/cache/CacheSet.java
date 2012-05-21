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

import net.pickapack.action.Function3;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CacheSet<StateT extends Serializable, LineT extends CacheLine<StateT>> {
    private Cache<StateT, LineT> cache;
    private List<LineT> lines;
    private int num;

    public CacheSet(Cache<StateT, LineT> cache, int associativity, int num, Function3<Cache<?, ?>, Integer, Integer, LineT> createLine) {
        this.cache = cache;
        this.num = num;

        this.lines = new ArrayList<LineT>();
        for (int i = 0; i < associativity; i++) {
            this.lines.add(createLine.apply(cache, this.num, i));
        }
    }

    public Cache<StateT, LineT> getCache() {
        return cache;
    }

    public List<LineT> getLines() {
        return lines;
    }

    public int getNum() {
        return num;
    }
}
