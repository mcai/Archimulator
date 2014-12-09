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

import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

/**
 * Graph stream viewer.
 *
 * @author Min Cai
 */
public class GraphStreamViewer {
    private ACOHelper acoHelper;
    private Graph graph;

    static {
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
    }

    /**
     * Create a graph stream viewer.
     *
     * @param acoHelper the ACO helper
     */
    public GraphStreamViewer(ACOHelper acoHelper) {
        this.acoHelper = acoHelper;

        this.graph = new SingleGraph("ACO for TSP");

        acoHelper.getNodes().forEach(node -> {
            Node graphNode = graph.addNode(node.getName());
            graphNode.addAttribute("xy", node.getX(), node.getY());
            graphNode.addAttribute("label", String.format("%s", node.getName()));
        });

        acoHelper.getEdges().forEach(edge -> {
            Edge graphEdge = graph.addEdge(
                    edge.getNodeFrom().getName() + "-" + edge.getNodeTo().getName(),
                    edge.getNodeFrom().getName(),
                    edge.getNodeTo().getName());
//            graphEdge.addAttribute("label", String.format("%.4f", edge.getPheromone()));
//            graphEdge.addAttribute("ui.style", "shape: angle;");
//            graphEdge.addAttribute("ui.style", "stroke-width: 10;");
//            graphEdge.addAttribute("ui.style", "fill-color: white;");
        });

        graph.addAttribute("ui.stylesheet", "url('/home/itecgo/Archimulator/src/main/java/archimulator/util/ai/aco/stylesheet')");

        this.graph.display(false);
    }

    /**
     * Get the ACO helper.
     *
     * @return the ACO helper
     */
    public ACOHelper getAcoHelper() {
        return acoHelper;
    }

    /**
     * Get the graph.
     *
     * @return the graph
     */
    public Graph getGraph() {
        return graph;
    }
}