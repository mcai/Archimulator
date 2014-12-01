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

import archimulator.model.Architecture;
import archimulator.model.Experiment;
import archimulator.model.ExperimentState;
import archimulator.model.ExperimentType;
import archimulator.service.ServiceManager;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;

import java.util.Arrays;

/**
 * General panel.
 *
 * @author Min Cai
 */
public class PanelGeneral extends Panel {
    /**
     * Create a general panel.
     *
     * @param id         the markup ID of the panel that is to be created
     * @param experiment the experiment
     */
    public PanelGeneral(String id, Experiment experiment) {
        super(id);

        setDefaultModel(new CompoundPropertyModel<>(experiment));

        this.add(new Label("id"));
        this.add(new Label("title"));

        this.add(new DropDownChoice<>("type", Arrays.asList(ExperimentType.values())));
        this.add(new DropDownChoice<>("state", Arrays.asList(ExperimentState.values())));
        this.add(new TextArea<String>("failedReason"));

        this.add(new DropDownChoice<>("architecture", ServiceManager.getArchitectureService().getAllArchitectures(), new IChoiceRenderer<Architecture>() {
            @Override
            public Object getDisplayValue(Architecture architecture) {
                return String.format("{%d} %s", architecture.getId(), architecture.getTitle());
            }

            @Override
            public String getIdValue(Architecture architecture, int index) {
                return architecture.getId() + "";
            }
        }));

        this.add(new NumberTextField<Integer>("numMaxInstructions"));
        this.add(new Label("createTimeAsString"));
    }
}
