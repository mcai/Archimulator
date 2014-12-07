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

import java.util.List;
import java.util.stream.Collectors;

/**
 * Node.
 *
 * @author Min Cai
 */
public class Node {
    private ACOHelper<?> acoHelper;
    private String name;

    /**
     * Create a node.
     *
     * @param acoHelper the ACO helper
     * @param name      the name
     */
    public Node(ACOHelper<?> acoHelper, String name) {
        this.acoHelper = acoHelper;
        this.name = name;
    }

    /**
     * Get the ACO helper.
     *
     * @return the ACO helper
     */
    public ACOHelper<?> getAcoHelper() {
        return acoHelper;
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
     * Get the list of edges.
     *
     * @return the list of edges
     */
    public List<Edge> getEdges() {
        return this.getAcoHelper().getEdges().stream().filter(edge -> edge.getNodeFrom() == this).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return String.format("'%s'", name);
    }
}
