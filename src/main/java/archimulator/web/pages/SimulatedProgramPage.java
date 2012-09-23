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

import archimulator.model.SimulatedProgram;
import archimulator.service.ServiceManager;
import net.pickapack.dateTime.DateHelper;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

@MountPath(value = "/", alt = "/simulated_program")
public class SimulatedProgramPage extends AuthenticatedWebPage {
    public SimulatedProgramPage(PageParameters parameters) {
        super(PageType.SIMULATED_PROGRAM, parameters);

        final String action = parameters.get("action").toString();

        final SimulatedProgram simulatedProgram;

        if (action.equals("add")) {
            simulatedProgram = new SimulatedProgram("", "", "", "");
        } else if (action.equals("edit")) {
            long simulatedProgramId = parameters.get("simulated_program_id").toLong();
            simulatedProgram = ServiceManager.getSimulatedProgramService().getSimulatedProgramById(simulatedProgramId);
        } else {
            throw new IllegalArgumentException();
        }

        if (simulatedProgram == null) {
            setResponsePage(HomePage.class);
            return;
        }

        setTitle((action.equals("add") ? "Add" : "Edit") + " Simulated Program - Archimulator");

        this.add(new Label("section_header_simulated_program", (action.equals("add") ? "Add" : "Edit") + " Simulated Program"));

        add(new FeedbackPanel("span_feedback"));

        this.add(new Form("form_simulated_program") {{
            this.add(new TextField<Long>("input_id", Model.of(simulatedProgram.getId())));
            this.add(new RequiredTextField<String>("input_title", new PropertyModel<String>(simulatedProgram, "title")));
            this.add(new RequiredTextField<String>("input_executable", new PropertyModel<String>(simulatedProgram, "executable")));
            this.add(new TextField<String>("input_default_arguments", new PropertyModel<String>(simulatedProgram, "defaultArguments")));
            this.add(new TextField<String>("input_standard_in", new PropertyModel<String>(simulatedProgram, "standardIn")));
            this.add(new CheckBox("input_helper_thread_enabled", new PropertyModel<Boolean>(simulatedProgram, "helperThreadEnabled")));
            this.add(new TextField<String>("input_create_time", Model.of(DateHelper.toString(simulatedProgram.getCreateTime()))));

            this.add(new Button("button_save", Model.of(action.equals("add") ? "Add" : "Save")) {
                @Override
                public void onSubmit() {
                    if(action.equals("add")) {
                        ServiceManager.getSimulatedProgramService().addSimulatedProgram(simulatedProgram);
                    }
                    else {
                        ServiceManager.getSimulatedProgramService().updateSimulatedProgram(simulatedProgram);
                    }
                    setResponsePage(SimulatedProgramsPage.class);
                }
            });

            this.add(new Button("button_remove") {
                {
                    setVisible(action.equals("edit"));
                }

                @Override
                public void onSubmit() {
                    ServiceManager.getSimulatedProgramService().removeSimulatedProgramById(simulatedProgram.getId());
                    setResponsePage(SimulatedProgramsPage.class);
                }
            });
        }});
    }
}
