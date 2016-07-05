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
package archimulator.uncore.coherence.msi.flow;

import archimulator.isa.Memory;
import archimulator.uncore.MemoryHierarchyAccess;
import archimulator.uncore.coherence.msi.controller.Controller;
import archimulator.util.Params;
import archimulator.util.collection.tree.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Cache coherence flow.
 *
 * @author Min Cai
 */
public abstract class CacheCoherenceFlow extends Params implements Node {
    private long id;
    private Controller generator;
    private CacheCoherenceFlow producerFlow;
    private CacheCoherenceFlow ancestorFlow;
    private List<CacheCoherenceFlow> childFlows;
    private int numPendingDescendantFlows;
    private long beginCycle;
    private long endCycle;
    private boolean completed;
    private MemoryHierarchyAccess access;
    private int tag;

    /**
     * Create a cache coherence flow.
     *
     * @param generator    the generator controller
     * @param producerFlow the producer cache coherence flow
     * @param access       the memory hierarchy access
     * @param tag          the tag
     */
    public CacheCoherenceFlow(Controller generator, CacheCoherenceFlow producerFlow, MemoryHierarchyAccess access, int tag) {
        this.id = generator.getSimulation().currentCacheCoherenceFlowId++;
        this.generator = generator;
        this.producerFlow = producerFlow;
        this.ancestorFlow = producerFlow == null ? this : producerFlow.ancestorFlow;
        this.childFlows = new ArrayList<>();
        this.access = access;
        this.tag = tag;

        this.onCreate();
    }

    /**
     * Act on when the cache coherence flow begins.
     */
    private void onCreate() {
        this.beginCycle = this.generator.getCycleAccurateEventQueue().getCurrentCycle();
        if (this.producerFlow == null) {
            this.generator.getSimulation().pendingFlows.add(this);
        } else {
            this.producerFlow.childFlows.add(this);
        }
        this.ancestorFlow.numPendingDescendantFlows++;
    }

    /**
     * Act on when the cache coherence flow is completed.
     */
    public void onCompleted() {
        this.completed = true;
        this.endCycle = this.generator.getCycleAccurateEventQueue().getCurrentCycle();
        this.ancestorFlow.numPendingDescendantFlows--;

        if (this.ancestorFlow.numPendingDescendantFlows == 0) {
            this.generator.getSimulation().pendingFlows.remove(this.ancestorFlow);
        }
    }

    /**
     * Get the ID of the cache coherence flow.
     *
     * @return the ID of the cache coherence flow
     */
    public long getId() {
        return id;
    }

    /**
     * Get the ancestor cache coherence flow.
     *
     * @return the ancestor cache coherence flow
     */
    public CacheCoherenceFlow getAncestorFlow() {
        return ancestorFlow;
    }

    /**
     * Get the producer cache coherence flow.
     *
     * @return the producer cache coherence flow
     */
    public CacheCoherenceFlow getProducerFlow() {
        return producerFlow;
    }

    /**
     * Get the list of child flows.
     *
     * @return the list of child flows
     */
    public List<CacheCoherenceFlow> getChildFlows() {
        return childFlows;
    }

    /**
     * Get the cycle when the cache coherence flow begins.
     *
     * @return the cycle when the cache coherence flow begins
     */
    public long getBeginCycle() {
        return beginCycle;
    }

    /**
     * Get the cycle when the cache coherence flow is completed (ended).
     *
     * @return the cycle when the cache coherence flow is completed (ended)
     */
    public long getEndCycle() {
        return endCycle;
    }

    /**
     * Get the generator controller.
     *
     * @return the generator controller
     */
    public Controller getGenerator() {
        return generator;
    }

    /**
     * Get a value indicating whether the cache coherence flow is completed or not.
     *
     * @return a value indicating whether the cache coherence flow is completed or not
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Get the memory hierarchy access.
     *
     * @return the memory hierarchy access
     */
    public MemoryHierarchyAccess getAccess() {
        return access;
    }

    /**
     * Get the node value.
     *
     * @return the node value
     */
    @Override
    public Object getValue() {
        return String.format("%s%s", this, this.completed ? " -> completed at " + endCycle : "");
    }

    /**
     * Get the list of child flows.
     *
     * @return the list of child flows
     */
    @Override
    public List<CacheCoherenceFlow> getChildren() {
        return this.childFlows;
    }

    /**
     * Get the tag.
     *
     * @return the tag
     */
    public int getTag() {
        return tag;
    }

    /**
     * Get the data if meaningful.
     *
     * @return the data if meaningful
     */
    public byte[] getData() {
        Memory memory = this.getAccess().getThread().getContext().getProcess().getMemory();
        int lineSize = this.getAccess().getThread().getExperiment().getConfig().getL1DLineSize();
        return memory.readBlock(this.getTag(), lineSize / 4);
    }
}
