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
package archimulator.mem.cache;

import archimulator.util.action.Function2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CacheSet<StateT extends Serializable, LineT extends CacheLine<StateT>> implements Serializable {
    private List<LineT> lines;
    private int num;

    public CacheSet(int associativity, int num, Function2<Integer, Integer, LineT> createLine) {
        this.num = num;

        this.lines = new ArrayList<LineT>();
        for (int i = 0; i < associativity; i++) {
            this.lines.add(createLine.apply(this.num, i));
        }
    }

    public List<LineT> getLines() {
        return lines;
    }

    public int getNum() {
        return num;
    }
}
