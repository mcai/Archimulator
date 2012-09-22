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
import archimulator.web.components.PagingNavigator;
import net.pickapack.dateTime.DateHelper;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

import java.util.Iterator;

@MountPath(value = "/", alt = "/simulated_programs")
public class SimulatedProgramsPage extends AuthenticatedWebPage {
    public SimulatedProgramsPage(PageParameters parameters) {
        super(PageType.SIMULATED_PROGRAMS, parameters);

        setTitle("Simulated Programs - Archimulator");

        IDataProvider<SimulatedProgram> dataProvider = new IDataProvider<SimulatedProgram>() {
            @Override
            public Iterator<? extends SimulatedProgram> iterator(long first, long count) {
                return ServiceManager.getSimulatedProgramService().getAllSimulatedPrograms(first, count).iterator();
            }

            @Override
            public long size() {
                return ServiceManager.getSimulatedProgramService().getNumAllSimulatedPrograms();
            }

            @Override
            public IModel<SimulatedProgram> model(SimulatedProgram object) {
                return new Model<SimulatedProgram>(object);
            }

            @Override
            public void detach() {
            }
        };

        DataView<SimulatedProgram> rowSimulatedProgram = new DataView<SimulatedProgram>("row_simulated_program", dataProvider) {
            protected void populateItem(Item<SimulatedProgram> item) {
                final SimulatedProgram simulatedProgram = item.getModelObject();

                item.add(new Label("cell_id", simulatedProgram.getId() + ""));
                item.add(new Label("cell_title", simulatedProgram.getTitle()));
                item.add(new Label("cell_executable", simulatedProgram.getExecutable()));
                item.add(new Label("cell_default_arguments", simulatedProgram.getDefaultArguments()));
                item.add(new Label("cell_stdin", simulatedProgram.getStandardIn()));
                item.add(new Label("cell_helper_thread_enabled", simulatedProgram.getHelperThreadEnabled() + ""));
                item.add(new Label("cell_create_time", DateHelper.toString(simulatedProgram.getCreateTime())));

                WebMarkupContainer cellOperations = new WebMarkupContainer("cell_operations");

                cellOperations.add(new Label("button_edit", "Edit"){{
                    add(new AttributeAppender("href", "./simulated_program?action=edit&simulated_program_id=" + simulatedProgram.getId()));
                }});

                item.add(cellOperations);
            }
        };
        rowSimulatedProgram.setItemsPerPage(10);
        add(rowSimulatedProgram);

        add(new PagingNavigator("navigator", rowSimulatedProgram));
    }
}
