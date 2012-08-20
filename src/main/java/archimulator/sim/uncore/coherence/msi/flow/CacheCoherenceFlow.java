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
package archimulator.sim.uncore.coherence.msi.flow;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.msi.controller.Controller;
import net.pickapack.Params;
import net.pickapack.tree.Node;
import net.pickapack.tree.NodeHelper;

import java.util.ArrayList;
import java.util.List;

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

    public CacheCoherenceFlow(Controller generator, CacheCoherenceFlow producerFlow, MemoryHierarchyAccess access, int tag) {
        this.id = generator.getSimulation().currentCacheCoherenceFlowId++;
        this.generator = generator;
        this.producerFlow = producerFlow;
        this.ancestorFlow = producerFlow == null ? this : producerFlow.ancestorFlow;
        this.childFlows = new ArrayList<CacheCoherenceFlow>();
        this.onCreate(this.generator.getCycleAccurateEventQueue().getCurrentCycle());
        this.access = access;
        this.tag = tag;
    }

    public static void dumpTree() {
        for (CacheCoherenceFlow pendingFlow : pendingFlows) {
            NodeHelper.print(pendingFlow);
            System.out.println();
        }

        System.out.println();
    }

    public void onCreate(long beginCycle) {
        this.beginCycle = beginCycle;
        if (this.producerFlow == null) {
            pendingFlows.add(this);
        } else {
            this.producerFlow.childFlows.add(this);
        }
        this.ancestorFlow.numPendingDescendantFlows++;
    }

    public void onCompleted() {
        this.completed = true;
        this.endCycle = this.generator.getCycleAccurateEventQueue().getCurrentCycle();
        this.ancestorFlow.numPendingDescendantFlows--;

        if (this.ancestorFlow.numPendingDescendantFlows == 0) {
            pendingFlows.remove(this.ancestorFlow);
        }
    }

    public long getId() {
        return id;
    }

    public CacheCoherenceFlow getAncestorFlow() {
        return ancestorFlow;
    }

    public CacheCoherenceFlow getProducerFlow() {
        return producerFlow;
    }

    public List<CacheCoherenceFlow> getChildFlows() {
        return childFlows;
    }

    public long getBeginCycle() {
        return beginCycle;
    }

    public long getEndCycle() {
        return endCycle;
    }

    public Object getGenerator() {
        return generator;
    }

    public boolean isCompleted() {
        return completed;
    }

    public MemoryHierarchyAccess getAccess() {
        return access;
    }

    @Override
    public Object getValue() {
        return String.format("%s%s", this, this.completed ? " -> completed at " + endCycle : "");
    }

    @Override
    public List<CacheCoherenceFlow> getChildren() {
        return this.childFlows;
    }

    private static List<CacheCoherenceFlow> pendingFlows = new ArrayList<CacheCoherenceFlow>();

    public static List<CacheCoherenceFlow> getPendingFlows() {
        return pendingFlows;
    }

    public int getTag() {
        return tag;
    }
}
