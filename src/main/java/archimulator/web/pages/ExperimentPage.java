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

import archimulator.model.*;
import archimulator.service.ServiceManager;
import net.pickapack.dateTime.DateHelper;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@MountPath(value = "/", alt = "/experiment")
public class ExperimentPage extends AuthenticatedWebPage {
    public ExperimentPage(final PageParameters parameters) {
        super(PageType.EXPERIMENT, parameters);

        final String action = parameters.get("action").toString();

        final Experiment experiment;

        if(action == null) {
            setResponsePage(getApplication().getHomePage());
            return;
        }
        else if (action.equals("add")) {
            List<ContextMapping> contextMappings = new ArrayList<ContextMapping>();
            Benchmark benchmark = ServiceManager.getBenchmarkService().getFirstBenchmark();
            contextMappings.add(new ContextMapping(0, benchmark, benchmark.getDefaultArguments()));

            experiment = new Experiment(ExperimentType.DETAILED, ServiceManager.getArchitectureService().getFirstArchitecture(), -1, contextMappings);
        } else if (action.equals("edit")) {
            long experimentId = parameters.get("experiment_id").toLong(-1);
            experiment = ServiceManager.getExperimentService().getExperimentById(experimentId);
        } else {
            throw new IllegalArgumentException();
        }

        if(experiment == null) {
            setResponsePage(getApplication().getHomePage());
            return;
        }

        setTitle((action.equals("add") ? "Add" : "Edit") + " Experiment - Archimulator");

        this.add(new Label("section_header_experiment", (action.equals("add") ? "Add" : "Edit") + " Experiment"));

        add(new FeedbackPanel("span_feedback"));

        this.add(new Form("form_experiment") {{
            this.add(new TextField<String>("input_id", Model.of(experiment.getId() + "")));
            this.add(new TextField<String>("input_title", Model.of(experiment.getTitle())));

            this.add(new DropDownChoice<ExperimentType>("select_type", new PropertyModel<ExperimentType>(experiment, "type"), Arrays.asList(ExperimentType.values())));
            this.add(new DropDownChoice<ExperimentState>("select_state", new PropertyModel<ExperimentState>(experiment, "state"), Arrays.asList(ExperimentState.values())));

            this.add(new DropDownChoice<Architecture>("select_architecture", new PropertyModel<Architecture>(experiment, "architecture"), ServiceManager.getArchitectureService().getAllArchitectures(), new IChoiceRenderer<Architecture>() {
                @Override
                public Object getDisplayValue(Architecture architecture) {
                    return String.format("{%d} %s", architecture.getId(), architecture.getTitle());
                }

                @Override
                public String getIdValue(Architecture architecture, int index) {
                    return architecture.getTitle();
                }
            }));

            this.add(new NumberTextField<Integer>("input_num_max_instructions", new PropertyModel<Integer>(experiment, "numMaxInstructions")));
            this.add(new TextField<String>("input_create_time", Model.of(DateHelper.toString(experiment.getCreateTime()))));

            this.add(new Button("button_save", Model.of(action.equals("add") ? "Add" : "Save")) {
                @Override
                public void onSubmit() {
                    if (action.equals("add")) {
                        experiment.updateTitle();

                        if(ServiceManager.getExperimentService().getLatestExperimentByTitle(experiment.getTitle()) == null) {
                            ServiceManager.getExperimentService().addExperiment(experiment);
                        }
                    } else {
                        experiment.updateTitle();

                        Experiment experimentWithSameTitle = ServiceManager.getExperimentService().getLatestExperimentByTitle(experiment.getTitle());
                        if(experimentWithSameTitle != null && experimentWithSameTitle.getId() != experiment.getId()) {
                            ServiceManager.getExperimentService().removeExperimentById(experiment.getId());
                        }
                        else {
                            ServiceManager.getExperimentService().updateExperiment(experiment);
                        }
                    }

                    back(parameters, ExperimentsPage.class);
                }
            });

            this.add(new Button("button_cancel") {
                {
                    setDefaultFormProcessing(false);
                }

                @Override
                public void onSubmit() {
                    back(parameters, ExperimentsPage.class);
                }
            });

            this.add(new Button("button_remove") {
                {
                    setDefaultFormProcessing(false);
                    setVisible(action.equals("edit"));
                }

                @Override
                public void onSubmit() {
                    ServiceManager.getExperimentService().removeExperimentById(experiment.getId());

                    back(parameters, ExperimentsPage.class);
                }
            });
        }});
    }
}
