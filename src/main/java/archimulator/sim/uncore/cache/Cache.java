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

import archimulator.sim.base.simulation.BasicSimulationObject;
import archimulator.sim.base.simulation.SimulationObject;
import archimulator.util.action.Action1;
import archimulator.util.action.Function3;
import archimulator.util.action.Predicate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Cache<StateT extends Serializable, LineT extends CacheLine<StateT>> extends BasicSimulationObject implements SimulationObject, Serializable {
    protected String name;
    protected CacheGeometry geometry;
    protected List<CacheSet<StateT, LineT>> sets;

    public Cache(SimulationObject parent, String name, CacheGeometry geometry, Function3<Cache<?, ?>, Integer, Integer, LineT> createLine) {
        super(parent);

        this.name = name;
        this.geometry = geometry;

        this.sets = new ArrayList<CacheSet<StateT, LineT>>();
        for (int i = 0; i < this.getNumSets(); i++) {
            this.sets.add(new CacheSet<StateT, LineT>(this, this.getAssociativity(), i, createLine));
        }
    }

    public void forAll(int set, Predicate<LineT> predicate, Action1<LineT> action) {
        for (int way = 0; way < this.getAssociativity(); way++) {
            LineT line = this.getLine(set, way);
            if (predicate.apply(line)) {
                action.apply(line);
            }
        }
    }

    public void forAny(int set, Predicate<LineT> predicate, Action1<LineT> action) {
        for (int way = 0; way < this.getAssociativity(); way++) {
            LineT line = this.getLine(set, way);
            if (predicate.apply(line)) {
                action.apply(line);
                return;
            }
        }
    }

    public void forExact(int set, Predicate<LineT> predicate, Action1<LineT> action) {
        for (int way = 0; way < this.getAssociativity(); way++) {
            LineT line = this.getLine(set, way);
            if (predicate.apply(line)) {
                action.apply(line);
                return;
            }
        }

        throw new IllegalArgumentException();
    }

    public int count(int set, Predicate<LineT> predicate) {
        int count = 0;

        for (int way = 0; way < this.getAssociativity(); way++) {
            if (predicate.apply(this.getLine(set, way))) {
                count++;
            }
        }

        return count;
    }

    public boolean containsAny(int set, Predicate<LineT> predicate) {
        for (int way = 0; way < this.getAssociativity(); way++) {
            if (predicate.apply(this.getLine(set, way))) {
                return true;
            }
        }

        return false;
    }

    public boolean containsAll(int set, Predicate<LineT> predicate) {
        for (int way = 0; way < this.getAssociativity(); way++) {
            if (!predicate.apply(this.getLine(set, way))) {
                return false;
            }
        }

        return true;
    }

    public List<LineT> getLines(int set) {
        if (set < 0 || set >= this.getNumSets()) {
            throw new IllegalArgumentException(String.format("set: %d, this.numSets: %d", set, this.getNumSets()));
        }

        return this.sets.get(set).getLines();
    }

    public LineT getLine(int set, int way) {
        if (way < 0 || way >= this.getAssociativity()) {
            throw new IllegalArgumentException(String.format("way: %d, this.associativity: %d", way, this.getAssociativity()));
        }

        return this.getLines(set).get(way);
    }

    public FindCacheLineResult<LineT> findLine(int address) {
        int tag = this.getTag(address);
        int set = this.getSet(address);

        for (LineT line : this.getLines(set)) {
            if (line.getTag() == tag && line.getState() != line.getInitialState()) {
                return new FindCacheLineResult<LineT>(FindCacheLineResultType.CACHE_HIT, line);
            }
        }

        return new FindCacheLineResult<LineT>(FindCacheLineResultType.CACHE_MISS, null);
    }

    public int getTag(int addr) {
        return CacheGeometry.getTag(addr, this.geometry);
    }

    public int getSet(int addr) {
        int set = CacheGeometry.getSet(addr, this.geometry);

        if (set < 0 || set >= this.getNumSets()) {
            throw new IllegalArgumentException(String.format("addr: 0x%08x, set: %d, this.numSets: %d", addr, set, this.getNumSets()));
        }

        return set;
    }

    public int getLineId(int addr) {
        return CacheGeometry.getLineId(addr, this.geometry);
    }

    public int getNumSets() {
        return this.geometry.getNumSets();
    }

    public int getAssociativity() {
        return this.geometry.getAssociativity();
    }

    public int getLineSize() {
        return this.geometry.getLineSize();
    }

    public String getName() {
        return name;
    }

    public CacheGeometry getGeometry() {
        return geometry;
    }
}
