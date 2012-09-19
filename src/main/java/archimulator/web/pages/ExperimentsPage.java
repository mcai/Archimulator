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
import archimulator.web.components.PagingNavigator;
import net.pickapack.dateTime.DateHelper;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

import java.util.Iterator;

@MountPath(value = "/", alt = "/experiments")
public class ExperimentsPage extends AuthenticatedWebPage {
    public ExperimentsPage(PageParameters parameters) {
        super(PageType.EXPERIMENTS, parameters);

        IDataProvider<Experiment> dataProvider = new IDataProvider<Experiment>() {
            @Override
            public Iterator<? extends Experiment> iterator(long first, long count) {
                return ServiceManager.getExperimentService().getAllExperiments(first, count).iterator();
            }

            @Override
            public long size() {
                return ServiceManager.getExperimentService().getNumAllExperiments();
            }

            @Override
            public IModel<Experiment> model(Experiment object) {
                return new Model<Experiment>(object);
            }

            @Override
            public void detach() {
            }
        };

        DataView<Experiment> rowExperiment = new DataView<Experiment>("row_experiment", dataProvider) {
            protected void populateItem(Item<Experiment> item) {
                final Experiment experiment = item.getModelObject();

                item.add(new Label("cell_id", experiment.getId() + ""));
                item.add(new Label("cell_title", experiment.getTitle()));
                item.add(new Label("cell_type", experiment.getType() + ""));
                item.add(new Label("cell_state", experiment.getState() + ""));
                item.add(new Label("cell_architecture", experiment.getArchitecture().getTitle()));
                item.add(new Label("cell_num_max_instructions", experiment.getNumMaxInstructions() + ""));
                item.add(new Label("cell_create_time", DateHelper.toString(experiment.getCreateTime())));

                WebMarkupContainer cellOperations = new WebMarkupContainer("cell_operations");

                cellOperations.add(new Link<Void>("button_edit") {
                    @Override
                    public void onClick() {
                        PageParameters params = new PageParameters();
                        params.set("experiment_id", experiment.getId());
                        setResponsePage(ExperimentPage.class, params);
                    }
                });

                item.add(cellOperations);
            }
        };
        rowExperiment.setItemsPerPage(10);
        add(rowExperiment);

        add(new PagingNavigator("navigator", rowExperiment));
    }
}
