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
    private long id;
    private long createTime;

    private ACORouting routing;
    private AntPacketType type;
    private Router from;
    private Router to;

    private List<Router> memory;

    /**
     * Create an ant packet.
     *
     * @param routing the ACO routing
     * @param type    the type of the ant packet
     * @param from    the source router
     * @param to      the destination router
     */
    public AntPacket(ACORouting routing, AntPacketType type, Router from, Router to) {
        this.id = routing.currentAntId++;

        this.routing = routing;
        this.type = type;
        this.from = from;
        this.to = to;

        this.memory = new ArrayList<>();

        this.createTime = from.getNet().getCycleAccurateEventQueue().getCurrentCycle();
    }

    /**
     * Get the ID of the ant packet.
     *
     * @return the ID of the ant packet
     */
    public long getId() {
        return id;
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
     * Get the ACO routing.
     *
     * @return the ACO routing
     */
    public ACORouting getRouting() {
        return routing;
    }

    /**
     * Get the type of the ant packet.
     *
     * @return the type of the ant packet
     */
    public AntPacketType getType() {
        return type;
    }

    /**
     * Set the type of the ant packet.
     *
     * @param type the type of the ant packet
     */
    public void setType(AntPacketType type) {
        this.type = type;
    }

    /**
     * Get the source router.
     *
     * @return the source router
     */
    public Router getFrom() {
        return from;
    }

    /**
     * Set the source router.
     *
     * @param from the source router
     */
    public void setFrom(Router from) {
        this.from = from;
    }

    /**
     * Get the destination router.
     *
     * @return the destination router
     */
    public Router getTo() {
        return to;
    }

    /**
     * Set the destination router.
     *
     * @param to the destination router
     */
    public void setTo(Router to) {
        this.to = to;
    }

    /**
     * Get the memory.
     *
     * @return the memory
     */
    public List<Router> getMemory() {
        return memory;
    }

    @Override
    public String toString() {
        return String.format("AntPacket{id=%d, type=%s, from=%s, to=%s}", id, type, from, to);
    }
}
