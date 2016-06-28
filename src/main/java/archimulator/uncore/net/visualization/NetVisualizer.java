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
package archimulator.uncore.net.visualization;

import archimulator.common.SimulationType;
import archimulator.uncore.net.simple.NetMessageBeginLinkTransferEvent;
import archimulator.uncore.net.simple.SimpleNet;
import archimulator.util.math.Counter;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Net visualizer.
 *
 * @author Min Cai
 */
public class NetVisualizer {
    static {
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
    }

    public static void run(List<SimpleNet> nets) {
        Graph graph = new SingleGraph("Net Visualizer");

        final Counter i = new Counter();

        nets.stream().flatMap(net -> net.getNodes().stream()).collect(Collectors.toList()).forEach(node -> {
            if (graph.getNode(node.getName()) == null) {
                org.graphstream.graph.Node graphNode = graph.addNode(node.getName());

                graphNode.addAttribute("xy", i.getValue() / 4, i.getValue() % 4);
                graphNode.addAttribute("label", String.format("%s", node.getName()));

                i.increment();
            }
        });

        nets.stream().flatMap(net -> net.getLinks().stream()).collect(Collectors.toList()).forEach(edge -> graph.addEdge(
                edge.getPortFrom().getNode().getName() + "-" + edge.getPortTo().getNode().getName(),
                edge.getPortFrom().getNode().getName(), edge.getPortTo().getNode().getName(), true));

        graph.addAttribute("ui.stylesheet", "url('/home/itecgo/Archimulator/src/main/java/archimulator/uncore/net/visualization/stylesheet')");

        Map<String, Long> events = new HashMap<>();

        nets.get(0).getCycleAccurateEventQueue().getPerCycleEvents().add(() -> {
            if (nets.get(0).getSimulation().getType() != SimulationType.FAST_FORWARD) {
                if (nets.get(0).getCycleAccurateEventQueue().getCurrentCycle() % 2000 == 0) {
                    for (String edgeId : events.keySet()) {
                        Edge edge = graph.getEdge(edgeId);

                        if (events.containsKey(edgeId)) {
                            long numEvents = events.get(edgeId);
                            edge.removeAttribute("ui.style");
                            edge.addAttribute("ui.style",
                                    "stroke-mode: plain;\n" +
                                            "stroke-color: blue;\n" +
                                            "stroke-width: " + Math.min(1000, numEvents) + ";\n" +
                                            "fill-color: blue;\n" +
                                            "z-index: 1;");
                            events.put(edgeId, 0L);
                        }
                    }
                }
            }
        });

        nets.get(0).getBlockingEventDispatcher().addListener(NetMessageBeginLinkTransferEvent.class, event -> {
            String edgeId = event.getNodeFrom().getName() + "-" + event.getNodeTo().getName();
            if (!events.containsKey(edgeId)) {
                events.put(edgeId, 0L);
            }
            events.put(edgeId, events.get(edgeId) + 1);
        });

        Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.enableAutoLayout();

        ViewPanel view = viewer.addDefaultView(false);

        JFrame frame = new JFrame("Net Visualizer");
        frame.add(view);

        frame.setPreferredSize(new Dimension(800, 600));

        frame.pack();
        frame.setVisible(true);
    }
}
