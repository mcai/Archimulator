/**
 * ****************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.uncore.net.basic.routing.aco;

import archimulator.uncore.net.basic.Router;

import java.util.ArrayList;
import java.util.List;

/**
 * Ant packet.
 *
 * @author Min Cai
 */
public class AntPacket {
    private long createTime;

    private AntType antType;
    private Router source;
    private Router destination;

    private Router nextHop;

    private List<Memory> memories;

    /**
     * Create an ant packet.
     *
     * @param antType the ant type
     * @param source the source router
     * @param destination the destination router
     */
    public AntPacket(AntType antType, Router source, Router destination) {
        this.antType = antType;
        this.source = source;
        this.destination = destination;

        this.memories = new ArrayList<>();

        this.createTime = source.getNet().getCycleAccurateEventQueue().getCurrentCycle();
    }

    /**
     * Get the create time.
     *
     * @return the create time
     */
    public long getCreateTime() {
        return createTime;
    }

    /**
     * Get the ant type.
     *
     * @return the ant type
     */
    public AntType getAntType() {
        return antType;
    }

    /**
     * Set the ant type.
     *
     * @param antType the ant type
     */
    public void setAntType(AntType antType) {
        this.antType = antType;
    }

    /**
     * Get the source router.
     *
     * @return the source router
     */
    public Router getSource() {
        return source;
    }

    /**
     * Set the source router.
     *
     * @param source the source router
     */
    public void setSource(Router source) {
        this.source = source;
    }

    /**
     * Get the destination router.
     *
     * @return the destination router
     */
    public Router getDestination() {
        return destination;
    }

    /**
     * Set the destination router.
     *
     * @param destination the destination router
     */
    public void setDestination(Router destination) {
        this.destination = destination;
    }

    /**
     * Get the next hop router.
     *
     * @return the next hop router
     */
    public Router getNextHop() {
        return nextHop;
    }

    /**
     * Set the next hop router.
     *
     * @param nextHop the next hop router
     */
    public void setNextHop(Router nextHop) {
        this.nextHop = nextHop;
    }

    /**
     * Get the map of memories.
     *
     * @return the map of memories
     */
    public List<Memory> getMemories() {
        return memories;
    }
}
