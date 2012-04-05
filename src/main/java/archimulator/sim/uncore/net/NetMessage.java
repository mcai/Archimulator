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
package archimulator.sim.uncore.net;

import archimulator.sim.base.simulation.Simulation;
import archimulator.util.action.Action;

public class NetMessage {
    private long id;
    private NetNode srcNode;
    private NetNode destNode;
    private int size;
    private Action onCompletedCallback;

    private long beginCycle;

    public NetMessage(NetNode srcNode, NetNode destNode, int size, Action onCompletedCallback, long beginCycle) {
        this.id = Simulation.currentNetMessageId++;

        this.srcNode = srcNode;
        this.destNode = destNode;
        this.size = size;
        this.onCompletedCallback = onCompletedCallback;

        this.beginCycle = beginCycle;
    }

    public void complete(long endCycle) {
        this.onCompletedCallback.apply();

//        System.out.printf("%s -> %s: size: %d, latency: %d%n", srcNode.getName(), destNode.getName(), size, (endCycle - beginCycle));
    }

    public long getId() {
        return id;
    }

    public NetNode getSrcNode() {
        return srcNode;
    }

    public NetNode getDestNode() {
        return destNode;
    }

    public int getSize() {
        return size;
    }

    public long getBeginCycle() {
        return beginCycle;
    }
}
