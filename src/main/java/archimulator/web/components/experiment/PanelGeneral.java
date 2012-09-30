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

import archimulator.model.Architecture;
import archimulator.model.Experiment;
import archimulator.model.ExperimentState;
import archimulator.model.ExperimentType;
import archimulator.service.ServiceManager;
import net.pickapack.dateTime.DateHelper;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import java.util.Arrays;

public class PanelGeneral extends Panel {
    public PanelGeneral(String id, Experiment experiment) {
        super(id);

        this.add(new TextField<String>("input_id", Model.of(experiment.getId() + "")));
        this.add(new TextField<String>("input_title", Model.of(experiment.getTitle())));

        this.add(new DropDownChoice<ExperimentType>("select_type", new PropertyModel<ExperimentType>(experiment, "type"), Arrays.asList(ExperimentType.values())));
        this.add(new DropDownChoice<ExperimentState>("select_state", new PropertyModel<ExperimentState>(experiment, "state"), Arrays.asList(ExperimentState.values())));
        this.add(new TextArea<String>("textArea_failedReason", Model.of(experiment.getFailedReason())));

        this.add(new DropDownChoice<Architecture>("select_architecture", new PropertyModel<Architecture>(experiment, "architecture"), ServiceManager.getArchitectureService().getAllArchitectures(), new IChoiceRenderer<Architecture>() {
            @Override
            public Object getDisplayValue(Architecture architecture) {
                return String.format("{%d} %s", architecture.getId(), architecture.getTitle());
            }

            @Override
            public String getIdValue(Architecture architecture, int index) {
                return architecture.getId() + "";
            }
        }));

        this.add(new NumberTextField<Integer>("input_num_max_instructions", new PropertyModel<Integer>(experiment, "numMaxInstructions")));
        this.add(new TextField<String>("input_create_time", Model.of(DateHelper.toString(experiment.getCreateTime()))));
    }
}
