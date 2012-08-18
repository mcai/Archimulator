package archimulator.sim.uncore.cache; /*******************************************************************************
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

import archimulator.sim.common.BasicSimulationObject;
import archimulator.sim.common.SimulationObject;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;
import archimulator.util.ValueProvider;
import archimulator.util.ValueProviderFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Cache<StateT extends Serializable> extends BasicSimulationObject implements Serializable {
    protected String name;
    protected CacheGeometry geometry;
    protected List<CacheSet<StateT>> sets;
    private int numTagsInUse;

    public Cache(SimulationObject parent, String name, CacheGeometry geometry, ValueProviderFactory<StateT, ValueProvider<StateT>> cacheLineStateProviderFactory) {
        super(parent);

        this.name = name;
        this.geometry = geometry;

        this.sets = new ArrayList<CacheSet<StateT>>();
        for (int i = 0; i < this.getNumSets(); i++) {
            this.sets.add(new CacheSet<StateT>(this, this.getAssociativity(), i, cacheLineStateProviderFactory));
        }
    }

    public List<CacheLine<StateT>> getLines(int set) {
        if (set < 0 || set >= this.getNumSets()) {
            throw new IllegalArgumentException(String.format("set: %d, this.numSets: %d", set, this.getNumSets()));
        }

        return this.sets.get(set).getLines();
    }

    public CacheLine<StateT> getLine(int set, int way) {
        if (way < 0 || way >= this.getAssociativity()) {
            CacheCoherenceFlow.dumpTree();
            System.out.flush();
            throw new IllegalArgumentException(String.format("set: %d, way: %d, this.associativity: %d", set, way, this.getAssociativity()));
        }

        return this.getLines(set).get(way);
    }

    public int findWay(int address) {
        int tag = this.getTag(address);
        int set = this.getSet(address);

        for (CacheLine<StateT> line : this.getLines(set)) {
            if (line.getTag() == tag && line.getState() != line.getInitialState()) {
                return line.getWay();
            }
        }

        return -1;
    }

    public CacheLine<StateT> findLine(int address) {
        int set = this.getSet(address);
        int way = this.findWay(address);
        return way != -1 ? this.getLine(set, way) : null;
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

    public int getNumTagsInUse() {
        return numTagsInUse;
    }

    public void setNumTagsInUse(int numTagsInUse) {
        this.numTagsInUse = numTagsInUse;
    }

    public double getOccupancyRatio() {
        return (double) numTagsInUse / this.getGeometry().getNumLines();
    }
}
