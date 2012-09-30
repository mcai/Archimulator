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
package archimulator.web.components.experimentPack;

import archimulator.model.ExperimentPack;
import archimulator.model.ExperimentPackVariable;
import archimulator.web.data.view.ExperimentPackVariableListView;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.component.IRequestablePage;

public class PanelVariables extends Panel {
    public PanelVariables(String id, IRequestablePage page, ExperimentPack experimentPack) {
        super(id);

        ListView<ExperimentPackVariable> rowExperiment = new ExperimentPackVariableListView(page, "row_variable", experimentPack.getId());

        WebMarkupContainer tableExperiments = new WebMarkupContainer("table_variables");
        add(tableExperiments);

        tableExperiments.add(rowExperiment);
    }
}
