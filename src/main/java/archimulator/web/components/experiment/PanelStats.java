/*******************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.web.components.experiment;

import archimulator.model.Experiment;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PanelStats extends Panel {
    public PanelStats(String id, Experiment experiment) {
        super(id);

        List<Map.Entry<String, String>> stats = new ArrayList<Map.Entry<String, String>>(experiment.getStats().entrySet());

        final ListView<Map.Entry<String, String>> rowExperimentStat = new ListView<Map.Entry<String, String>>("row_stat", stats){
            @Override
            protected void populateItem(ListItem<Map.Entry<String, String>> item) {
                final Map.Entry<String, String> entry = item.getModelObject();

                item.add(new Label("cell_key", entry.getKey()));
                item.add(new Label("cell_value", entry.getValue()));
            }
        };

        final WebMarkupContainer tableExperimentStats = new WebMarkupContainer("table_stats");
        add(tableExperimentStats);

        tableExperimentStats.add(rowExperimentStat);
    }
}
