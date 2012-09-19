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
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

import java.util.ArrayList;
import java.util.Iterator;

@MountPath(value = "/", alt = "/experiments")
public class ExperimentsPage extends AuthenticatedWebPage {
    private String experimentId;
    private String experimentType;
    private String experimentState;
    private String experimentArchitecture;

    public ExperimentsPage(PageParameters parameters) {
        super(PageType.EXPERIMENTS, parameters);

        experimentId = getPageParameters().get("experimentId").toString();
        experimentType = getPageParameters().get("experimentType").toString();
        experimentState = getPageParameters().get("experimentState").toString();
        experimentArchitecture = getPageParameters().get("experimentArchitecture").toString();

        IDataProvider<Experiment> dataProvider = new IDataProvider<Experiment>() {
            @Override
            public Iterator<? extends Experiment> iterator(long first, long count) {
                if (!StringUtils.isEmpty(experimentType)) {
                    return new ArrayList<Experiment>().iterator(); //TODO
                }

                return ServiceManager.getExperimentService().getAllExperiments(first, count).iterator();
            }

            @Override
            public long size() {
                if (!StringUtils.isEmpty(experimentType)) {
                    return 0; //TODO
                }

                return ServiceManager.getExperimentService().getNumAllExperiments();
            }

            @Override
            public IModel<Experiment> model(final Experiment object) {
                return new LoadableDetachableModel<Experiment>(object) {
                    @Override
                    protected Experiment load() {
                        return object;
                    }
                };
            }

            @Override
            public void detach() {
            }
        };

        final DataView<Experiment> rowExperiment = new DataView<Experiment>("row_experiment", dataProvider) {
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

                cellOperations.add(new Label("button_edit", "Edit") {{
                    add(new AttributeAppender("href", "./experiment?experiment_id=" + experiment.getId()));
                }});

                item.add(cellOperations);
            }
        };
        rowExperiment.setItemsPerPage(10);

        final WebMarkupContainer tableExperiments = new WebMarkupContainer("table_experiments");
        add(tableExperiments);

        tableExperiments.add(rowExperiment);

        add(new TextField<String>("experimentId", new PropertyModel<String>(ExperimentsPage.this, "experimentId")));
        add(new TextField<String>("experimentType", new PropertyModel<String>(ExperimentsPage.this, "experimentType")));
        add(new TextField<String>("experimentState", new PropertyModel<String>(ExperimentsPage.this, "experimentState")));
        add(new TextField<String>("experimentArchitecture", new PropertyModel<String>(ExperimentsPage.this, "experimentArchitecture")));

        add(new PagingNavigator("navigator", rowExperiment));
    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getExperimentType() {
        return experimentType;
    }

    public void setExperimentType(String experimentType) {
        this.experimentType = experimentType;
    }

    public String getExperimentState() {
        return experimentState;
    }

    public void setExperimentState(String experimentState) {
        this.experimentState = experimentState;
    }

    public String getExperimentArchitecture() {
        return experimentArchitecture;
    }

    public void setExperimentArchitecture(String experimentArchitecture) {
        this.experimentArchitecture = experimentArchitecture;
    }
}
