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
package archimulator.web.component.experimentPack;

import archimulator.model.Experiment;
import archimulator.model.ExperimentPack;
import archimulator.service.ServiceManager;
import archimulator.util.plot.Table;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;

import java.util.Collections;
import java.util.List;

/**
 * General statistics panel.
 *
 * @author Min Cai
 */
public class PanelGeneralStatistics extends Panel {
    /**
     * Create a general statistics panel.
     *
     * @param id             the markup ID of the panel that is to be created
     * @param experimentPack the experiment pack
     */
    public PanelGeneralStatistics(String id, ExperimentPack experimentPack) {
        super(id);

        setDefaultModel(new CompoundPropertyModel<>(experimentPack));

        List<Experiment> experiments = ServiceManager.getExperimentService().getStoppedExperimentsByParent(experimentPack);

        Collections.sort(experiments, (o1, o2) -> {
            long numCycle1 = Long.parseLong(o1.getStatValue(o1.getMeasurementTitlePrefix(), "simulation/cycleAccurateEventQueue/currentCycle"));
            long numCycle2 = Long.parseLong(o2.getStatValue(o2.getMeasurementTitlePrefix(), "simulation/cycleAccurateEventQueue/currentCycle"));
            return (int) (numCycle1 - numCycle2);
        });

        Table tableSummary = ServiceManager.getExperimentStatService().tableSummary(experimentPack, experiments);

        add(new ListView<String>("column", tableSummary.getColumns()) {
            @Override
            protected void populateItem(ListItem<String> item) {
                String entry = item.getModelObject();

                item.setDefaultModel(new CompoundPropertyModel<>(entry));

                item.add(new Label("key", Model.of(entry)));
            }
        });

        add(new ListView<List<String>>("row", tableSummary.getRows()) {
            @Override
            protected void populateItem(ListItem<List<String>> item) {
                final List<String> entry = item.getModelObject();

                item.setDefaultModel(new CompoundPropertyModel<>(entry));

                item.add(new ListView<String>("value", entry) {
                    @Override
                    protected void populateItem(ListItem<String> item) {
                        String entry = item.getModelObject();

                        item.setDefaultModel(new CompoundPropertyModel<Object>(entry));

                        item.add(new Label("key", Model.of(entry)));
                    }
                });
            }
        });
    }
}
