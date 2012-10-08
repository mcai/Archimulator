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

import archimulator.sim.common.BasicSimulationObject;
import archimulator.sim.common.SimulationObject;
import net.pickapack.util.ValueProvider;
import net.pickapack.util.ValueProviderFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Min Cai
 * @param <StateT>
 */
public class Cache<StateT extends Serializable> extends BasicSimulationObject implements Serializable {
    private String name;
    private CacheGeometry geometry;
    private List<CacheSet<StateT>> sets;
    private int numTagsInUse;

    /**
     *
     * @param parent
     * @param name
     * @param geometry
     * @param cacheLineStateProviderFactory
     */
    public Cache(SimulationObject parent, String name, CacheGeometry geometry, ValueProviderFactory<StateT, ValueProvider<StateT>> cacheLineStateProviderFactory) {
        super(parent);

        this.name = name;
        this.geometry = geometry;

        this.sets = new ArrayList<CacheSet<StateT>>();
        for (int i = 0; i < this.getNumSets(); i++) {
            this.sets.add(new CacheSet<StateT>(this, this.getAssociativity(), i, cacheLineStateProviderFactory));
        }
    }

    /**
     *
     * @param set
     * @return
     */
    public List<CacheLine<StateT>> getLines(int set) {
        if (set < 0 || set >= this.getNumSets()) {
            throw new IllegalArgumentException(String.format("set: %d, this.numSets: %d", set, this.getNumSets()));
        }

        return this.sets.get(set).getLines();
    }

    /**
     *
     * @param set
     * @param way
     * @return
     */
    public CacheLine<StateT> getLine(int set, int way) {
        if (way < 0 || way >= this.getAssociativity()) {
            getSimulation().dumpPendingFlowTree();
            System.out.flush();
            throw new IllegalArgumentException(String.format("set: %d, way: %d, this.associativity: %d", set, way, this.getAssociativity()));
        }

        return this.getLines(set).get(way);
    }

    /**
     *
     * @param set
     * @return
     */
    public CacheSet<StateT> get(int set) {
        if (set < 0 || set >= this.getNumSets()) {
            throw new IllegalArgumentException(String.format("set: %d, this.numSets: %d", set, this.getNumSets()));
        }

        return this.sets.get(set);
    }

    /**
     *
     * @param address
     * @return
     */
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

    /**
     *
     * @param address
     * @return
     */
    public CacheLine<StateT> findLine(int address) {
        int set = this.getSet(address);
        int way = this.findWay(address);
        return way != -1 ? this.getLine(set, way) : null;
    }

    /**
     *
     * @param address
     * @return
     */
    public int getTag(int address) {
        return CacheGeometry.getTag(address, this.geometry);
    }

    /**
     *
     * @param address
     * @return
     */
    public int getSet(int address) {
        int set = CacheGeometry.getSet(address, this.geometry);

        if (set < 0 || set >= this.getNumSets()) {
            throw new IllegalArgumentException(String.format("address: 0x%08x, set: %d, this.numSets: %d", address, set, this.getNumSets()));
        }

        return set;
    }

    /**
     *
     * @param address
     * @return
     */
    public int getLineId(int address) {
        return CacheGeometry.getLineId(address, this.geometry);
    }

    /**
     *
     * @return
     */
    public int getNumSets() {
        return this.geometry.getNumSets();
    }

    /**
     *
     * @return
     */
    public int getAssociativity() {
        return this.geometry.getAssociativity();
    }

    /**
     *
     * @return
     */
    public int getLineSize() {
        return this.geometry.getLineSize();
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return
     */
    public CacheGeometry getGeometry() {
        return geometry;
    }

    /**
     *
     * @return
     */
    public int getNumTagsInUse() {
        return numTagsInUse;
    }

    /**
     *
     * @param numTagsInUse
     */
    public void setNumTagsInUse(int numTagsInUse) {
        this.numTagsInUse = numTagsInUse;
    }

    /**
     *
     * @return
     */
    public double getOccupancyRatio() {
        return (double) numTagsInUse / this.getGeometry().getNumLines();
    }
}
