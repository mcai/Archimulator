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

import archimulator.model.Benchmark;
import archimulator.model.ContextMapping;
import archimulator.model.Experiment;
import archimulator.model.ExperimentType;
import archimulator.service.ServiceManager;
import archimulator.web.components.experiment.PanelContextMappings;
import archimulator.web.components.experiment.PanelGeneral;
import archimulator.web.components.experiment.PanelStats;
import de.agilecoders.wicket.markup.html.bootstrap.tabs.BootstrapTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

import java.util.ArrayList;
import java.util.List;

@MountPath(value = "/experiment")
public class ExperimentPage extends AuthenticatedBasePage {
    public ExperimentPage(final PageParameters parameters) {
        super(parameters);

        final String action = parameters.get("action").toString();

        final Experiment experiment;

        if (action == null) {
            setResponsePage(getApplication().getHomePage());
            return;
        } else if (action.equals("add")) {
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

        if (experiment == null) {
            setResponsePage(getApplication().getHomePage());
            return;
        }

        setTitle((action.equals("add") ? "Add" : "Edit") + " Experiment - Archimulator");

        this.add(new Label("section_header_experiment", String.format("%s Experiment '{%d} %s'", action.equals("add") ? "Add" : "Edit", experiment.getId(), experiment.getTitle())));

        add(new FeedbackPanel("span_feedback"));

        this.add(new FormExperiment(experiment, action, parameters));
    }

    private class FormExperiment extends Form {
        public FormExperiment(final Experiment experiment, final String action, final PageParameters parameters) {
            super("form_experiment");

            add(new BootstrapTabbedPanel<ITab>("tabs", new ArrayList<ITab>() {{
                add(new AbstractTab(new Model<String>("General")) {
                    public Panel getPanel(String panelId) {
                        return new PanelGeneral(panelId, experiment);
                    }
                });

                add(new AbstractTab(new Model<String>("Context Mappings")) {
                    public Panel getPanel(String panelId) {
                        return new PanelContextMappings(panelId, experiment, FormExperiment.this);
                    }
                });

                add(new AbstractTab(new Model<String>("Statistics")) {
                    public Panel getPanel(String panelId) {
                        return new PanelStats(panelId, experiment);
                    }
                });
            }}));

            this.add(new Button("button_save", Model.of(action.equals("add") ? "Add" : "Save")) {
                @Override
                public void onSubmit() {
                    if (action.equals("add")) {
                        experiment.updateTitle();

                        if (ServiceManager.getExperimentService().getLatestExperimentByTitle(experiment.getTitle()) == null) {
                            ServiceManager.getExperimentService().addExperiment(experiment);
                        }
                    } else {
                        experiment.updateTitle();

                        Experiment experimentWithSameTitle = ServiceManager.getExperimentService().getLatestExperimentByTitle(experiment.getTitle());
                        if (experimentWithSameTitle != null && experimentWithSameTitle.getId() != experiment.getId()) {
                            ServiceManager.getExperimentService().removeExperimentById(experiment.getId());
                        } else {
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
        }
    }
}
