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
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

@MountPath(value = "/", alt = "/experiment")
public class ExperimentPage extends AuthenticatedWebPage {
    public ExperimentPage(PageParameters parameters) {
        super(PageType.EXPERIMENT, parameters);

        long experimentId = parameters.get("experiment_id").toLong();
        Experiment experiment = ServiceManager.getExperimentService().getExperimentById(experimentId);

        if(experiment == null) {
            setResponsePage(HomePage.class);
            return;
        }

        setTitle((experimentId == -1 ? "Add" : "Edit") + " Experiment - Archimulator");

        this.add(new TextField<String>("input_id", Model.of(experiment.getId() + "")));
        this.add(new TextField<String>("input_title", Model.of(experiment.getTitle())));
        this.add(new TextField<String>("input_type", Model.of(experiment.getType() + "")));
        this.add(new TextField<String>("input_state", Model.of(experiment.getState() + "")));
        this.add(new TextField<String>("input_architecture", Model.of(experiment.getArchitecture().getTitle())));
        this.add(new TextField<String>("input_num_max_instructions", Model.of(experiment.getNumMaxInstructions() + "")));
        this.add(new TextField<String>("input_create_time", Model.of(DateHelper.toString(experiment.getCreateTime()))));
    }
}
