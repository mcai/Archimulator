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

import org.jgraph.JGraph;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.ext.JGraphModelAdapter;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

/**
 * A JGraph adapter frame.
 *
 * @author Min Cai
 */
public class JGraphAdapterFrame extends JFrame {
    private JGraphModelAdapter<Vertex, Edge> jGraphModelAdapter;
    private JGraph jgraph;
    private ACOHelper acoHelper;

    public JGraphAdapterFrame(ACOHelper acoHelper) {
        this.acoHelper = acoHelper;

        this.jGraphModelAdapter = new JGraphModelAdapter<>(this.acoHelper.getGraph());

        this.jgraph = new JGraph(this.jGraphModelAdapter);
        this.jgraph.setBackground(Color.decode("#FAFBFF"));
        this.getContentPane().add(jgraph);

        this.setSize(new Dimension(800, 600));

        this.acoHelper.getVertexes().forEach(this::setVertexAttributes);
        this.acoHelper.getEdges().forEach(this::setEdgeAttributes);
    }

    private void setVertexAttributes(Vertex vertex) {
        DefaultGraphCell cell = this.jGraphModelAdapter.getVertexCell(vertex);
        Map attributes = cell.getAttributes();
        Rectangle2D bounds = GraphConstants.getBounds(attributes);

        GraphConstants.setBounds(attributes, new Rectangle((int) vertex.getX() / 2, (int) vertex.getY() / 2, (int) bounds.getWidth(), (int) bounds.getHeight()));

        Map<Object, Object> cellAttr = new HashMap<>();
        cellAttr.put(cell, attributes);
        this.jGraphModelAdapter.edit(cellAttr, null, null, null);
    }

    private void setEdgeAttributes(Edge edge) {
        DefaultEdge cell = this.jGraphModelAdapter.getEdgeCell(edge);
        Map attributes = cell.getAttributes();

        GraphConstants.setLineWidth(attributes, (float)edge.getPheromone() * 10);

        Map<Object, Object> cellAttr = new HashMap<>();
        cellAttr.put(cell, attributes);
        this.jGraphModelAdapter.edit(cellAttr, null, null, null);
    }

    public void refresh() {
//        this.acoHelper.getEdges().forEach(this::setEdgeAttributes);
        this.jgraph.refresh();
    }
}