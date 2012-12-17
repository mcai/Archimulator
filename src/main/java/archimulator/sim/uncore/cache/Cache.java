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
 * Cache.
 *
 * @param <StateT> state
 * @author Min Cai
 */
public class Cache<StateT extends Serializable> extends BasicSimulationObject implements Serializable {
    private String name;
    private CacheGeometry geometry;
    private List<CacheSet<StateT>> sets;
    private int numTagsInUse;

    /**
     * Create a cache.
     *
     * @param parent                        the parent simulation object
     * @param name                          the name
     * @param geometry                      the geometry
     * @param cacheLineStateProviderFactory the cache line state provider factory
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
     * Get lines in the specified set.
     *
     * @param set the set index
     * @return the lines in the specified set
     */
    public List<CacheLine<StateT>> getLines(int set) {
        if (set < 0 || set >= this.getNumSets()) {
            throw new IllegalArgumentException(String.format("set: %d, this.numSets: %d", set, this.getNumSets()));
        }

        return this.sets.get(set).getLines();
    }

    /**
     * Get the line at the specified set and way.
     *
     * @param set the set index
     * @param way the way
     * @return the line at the specified set and way
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
     * Get the set at the specified index.
     *
     * @param set the set index
     * @return the set at the specified index
     */
    public CacheSet<StateT> get(int set) {
        if (set < 0 || set >= this.getNumSets()) {
            throw new IllegalArgumentException(String.format("set: %d, this.numSets: %d", set, this.getNumSets()));
        }

        return this.sets.get(set);
    }

    /**
     * Find the way of the line that matches the specified address.
     *
     * @param address the address
     * @return the way of the line that matches the specified address if any exists; otherwise -1
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
     * Find the line that matches the specified address.
     *
     * @param address the address
     * @return the line that matches the specified address if any exists; otherwise null
     */
    public CacheLine<StateT> findLine(int address) {
        int set = this.getSet(address);
        int way = this.findWay(address);
        return way != -1 ? this.getLine(set, way) : null;
    }

    /**
     * Get the tag of the specified address.
     *
     * @param address the address
     * @return the tag of the specified address
     */
    public int getTag(int address) {
        return CacheGeometry.getTag(address, this.geometry);
    }

    /**
     * Get the set index of the specified address.
     *
     * @param address the address
     * @return the set index of the specified address
     */
    public int getSet(int address) {
        int set = CacheGeometry.getSet(address, this.geometry);

        if (set < 0 || set >= this.getNumSets()) {
            throw new IllegalArgumentException(String.format("address: 0x%08x, set: %d, this.numSets: %d", address, set, this.getNumSets()));
        }

        return set;
    }

    /**
     * Get the line ID of the specified address.
     *
     * @param address the address
     * @return the line ID of the specified address
     */
    public int getLineId(int address) {
        return CacheGeometry.getLineId(address, this.geometry);
    }

    /**
     * Get the number of sets.
     *
     * @return the number of sets
     */
    public int getNumSets() {
        return this.geometry.getNumSets();
    }

    /**
     * Get the associativity.
     *
     * @return the associativity
     */
    public int getAssociativity() {
        return this.geometry.getAssociativity();
    }

    /**
     * Get the line size.
     *
     * @return the line size
     */
    public int getLineSize() {
        return this.geometry.getLineSize();
    }

    /**
     * Get the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the geometry.
     *
     * @return the geometry
     */
    public CacheGeometry getGeometry() {
        return geometry;
    }

    /**
     * Get the number of tags in use.
     *
     * @return the number of tags in use
     */
    public int getNumTagsInUse() {
        return numTagsInUse;
    }

    /**
     * Set the number of tags in use.
     *
     * @param numTagsInUse the number of tags in use
     */
    public void setNumTagsInUse(int numTagsInUse) {
        this.numTagsInUse = numTagsInUse;
    }

    /**
     * Get the occupancy ratio.
     *
     * @return the occupancy ratio
     */
    public double getOccupancyRatio() {
        return (double) numTagsInUse / this.getGeometry().getNumLines();
    }
}
