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
import archimulator.model.ExperimentStat;
import archimulator.service.ServiceManager;
import archimulator.sim.uncore.BasicMemoryHierarchy;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;

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
     * @param id         the markup ID of the panel that is to be created
     * @param experiment the experiment
     */
    public PanelGeneralStatistics(String id, final Experiment experiment) {
        super(id);

        add(new ListView<String>("prefix", ServiceManager.getExperimentStatService().getStatPrefixesByParent(experiment)) {
            @Override
            protected void populateItem(ListItem<String> item) {
                final String prefix = item.getModelObject();

                item.add(new Label("prefixLabel", prefix));

                List<ExperimentStat> stats = ServiceManager.getExperimentStatService().getStatsByParentAndPrefix(experiment, prefix);

                item.add(new ListView<ExperimentStat>("stat", stats) {
                    @Override
                    protected void populateItem(ListItem<ExperimentStat> item) {
                        ExperimentStat stat = item.getModelObject();

                        item.setDefaultModel(new CompoundPropertyModel<>(stat));

                        if (stat.getKey().startsWith(BasicMemoryHierarchy.PREFIX_CC_FSM)) {
                            item.setVisible(false);
                        }

                        item.add(new Label("key"));
                        item.add(new Label("value"));
                    }
                });
            }
        });
    }
}
