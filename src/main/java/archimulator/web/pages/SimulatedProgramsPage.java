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
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

@MountPath(value = "/", alt = "/simulated_programs")
public class SimulatedProgramsPage extends AuthenticatedWebPage {
    public SimulatedProgramsPage(PageParameters parameters) {
        super(PageType.SIMULATED_PROGRAMS, parameters);

        ListView<SimulatedProgram> rowSimulatedProgram = new ListView<SimulatedProgram>("row_simulated_program", ServiceManager.getSimulatedProgramService().getAllSimulatedPrograms()) {
            protected void populateItem(ListItem item) {
                final SimulatedProgram simulatedProgram = (SimulatedProgram) item.getModelObject();

                item.add(new Label("cell_id", simulatedProgram.getId() + ""));
                item.add(new Label("cell_title", simulatedProgram.getTitle()));
                item.add(new Label("cell_executable", simulatedProgram.getExecutable()));
                item.add(new Label("cell_default_arguments", simulatedProgram.getDefaultArguments()));
                item.add(new Label("cell_stdin", simulatedProgram.getStandardIn()));
                item.add(new Label("cell_helper_thread_enabled", simulatedProgram.getHelperThreadEnabled() + ""));
                item.add(new Label("cell_create_time", DateHelper.toString(simulatedProgram.getCreateTime())));

                WebMarkupContainer cellOperations = new WebMarkupContainer("cell_operations");

                cellOperations.add(new Link<Void>("button_edit") {
                    @Override
                    public void onClick() {
                        PageParameters params = new PageParameters();
                        params.set("simulated_program_id", simulatedProgram.getId());
                        setResponsePage(SimulatedProgramPage.class, params);
                    }
                });

                item.add(cellOperations);
            }
        };
        add(rowSimulatedProgram);
    }
}
