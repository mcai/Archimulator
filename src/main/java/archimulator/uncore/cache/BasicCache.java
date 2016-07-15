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

import archimulator.common.BasicSimulationObject;
import archimulator.common.CPUExperiment;
import archimulator.common.Simulation;
import archimulator.common.SimulationObject;
import archimulator.util.ValueProvider;
import archimulator.util.ValueProviderFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Basic cache.
 *
 * @param <StateT> state
 * @author Min Cai
 */
public class BasicCache<StateT extends Serializable>
        extends BasicSimulationObject<CPUExperiment, Simulation>
        implements Cache<StateT> {
    private String name;
    private CacheGeometry geometry;
    private List<CacheSet<StateT>> sets;
    private int numTagsInUse;

    /**
     * Create a basic cache.
     *
     * @param parent                        the parent simulation object
     * @param name                          the name
     * @param geometry                      the geometry
     * @param cacheLineStateProviderFactory the cache line state provider factory
     */
    public BasicCache(SimulationObject parent, String name, CacheGeometry geometry, ValueProviderFactory<StateT, ValueProvider<StateT>> cacheLineStateProviderFactory) {
        super(parent);

        this.name = name;
        this.geometry = geometry;

        this.sets = new ArrayList<>();
        for (int i = 0; i < this.getNumSets(); i++) {
            this.sets.add(new CacheSet<>(this, this.getAssociativity(), i, cacheLineStateProviderFactory));
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
}
