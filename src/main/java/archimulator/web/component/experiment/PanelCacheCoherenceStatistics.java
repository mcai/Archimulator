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
package archimulator.web.component.experiment;

import archimulator.model.Experiment;
import archimulator.sim.uncore.coherence.msi.event.cache.CacheControllerEventType;
import archimulator.sim.uncore.coherence.msi.event.directory.DirectoryControllerEventType;
import archimulator.sim.uncore.coherence.msi.fsm.CacheControllerFiniteStateMachineFactory;
import archimulator.sim.uncore.coherence.msi.fsm.DirectoryControllerFiniteStateMachineFactory;
import de.agilecoders.wicket.markup.html.bootstrap.tabs.BootstrapTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Cache coherence statistics panel.
 *
 * @author Min Cai
 */
public class PanelCacheCoherenceStatistics extends Panel {
    /**
     * Create a cache coherence statistics panel.
     *
     * @param id         the markup ID of the panel that is to be created
     * @param experiment the experiment
     */
    public PanelCacheCoherenceStatistics(String id, final Experiment experiment) {
        super(id);

        final List<String> cacheControllerNames = new ArrayList<>();
        for (int i = 0; i < experiment.getArchitecture().getNumCores(); i++) {
            cacheControllerNames.add("c" + i + "/icache");
            cacheControllerNames.add("c" + i + "/dcache");
        }
        cacheControllerNames.add("l2");

        add(new BootstrapTabbedPanel<>("tabs", new ArrayList<ITab>() {{
            for (final String cacheControllerName : cacheControllerNames) {
                add(new AbstractTab(new Model<>(cacheControllerName)) {
                    public Panel getPanel(String panelId) {
                        if (cacheControllerName.equals("l2")) {
                            return (new PanelCacheControllerFiniteStateMachineFactory(panelId, experiment, cacheControllerName, DirectoryControllerFiniteStateMachineFactory.getSingleton(), DirectoryControllerEventType.class));
                        } else {
                            return (new PanelCacheControllerFiniteStateMachineFactory(panelId, experiment, cacheControllerName, CacheControllerFiniteStateMachineFactory.getSingleton(), CacheControllerEventType.class));
                        }
                    }
                });
            }
        }}));
    }
}
