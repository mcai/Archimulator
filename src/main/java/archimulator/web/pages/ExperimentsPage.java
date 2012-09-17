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
package archimulator.web.pages;

import archimulator.model.Experiment;
import archimulator.service.ServiceManager;
import net.pickapack.dateTime.DateHelper;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

@MountPath(value = "/", alt = "/experiments")
public class ExperimentsPage extends BasePage {
    public ExperimentsPage(PageParameters parameters) {
        super(PageType.EXPERIMENTS, parameters);

        ListView<Experiment> rowExperiment = new ListView<Experiment>("row_experiment", ServiceManager.getExperimentService().getAllExperiments()) {
            protected void populateItem(ListItem item) {
                Experiment experiment = (Experiment) item.getModelObject();

                item.add(new Label("cell_id", experiment.getId() + ""));
                item.add(new Label("cell_title", experiment.getTitle()));

                item.add(new Label("cell_type", experiment.getType() + ""));
                item.add(new Label("cell_state", experiment.getState() + ""));
                item.add(new Label("cell_architecture", experiment.getArchitecture().getTitle()));
                item.add(new Label("cell_num_max_instructions", experiment.getNumMaxInstructions() + ""));

                item.add(new Label("cell_create_time", DateHelper.toString(experiment.getCreateTime())));
            }
        };
        add(rowExperiment);
    }
}
