/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.util.ai.aco;

/**
 * Euclidean node.
 *
 * @author Min Cai
 */
public class EuclideanNode extends Node {
    private double x;
    private double y;

    /**
     * Create a node.
     *
     * @param acoHelper the ACO helper
     * @param name      the name
     * @param x         the x coordinate
     * @param y         the y coordinate
     */
    public EuclideanNode(ACOHelper acoHelper, String name, double x, double y) {
        super(acoHelper, name);
        this.x = x;
        this.y = y;
    }

    /**
     * Get the x coordinate.
     *
     * @return the x coordinate
     */
    public double getX() {
        return x;
    }

    /**
     * Get the y coordinate.
     *
     * @return the y coordinate
     */
    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return String.format("'%s'{x=%s, y=%s}", this.getName(), x, y);
    }
}
