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
package archimulator.web.component.experimentPack;

import archimulator.model.Experiment;
import archimulator.model.ExperimentPack;
import archimulator.model.ExperimentState;
import archimulator.service.ServiceManager;
import archimulator.web.data.ExperimentDataProvider;
import archimulator.web.page.ExperimentPage;
import de.agilecoders.wicket.markup.html.bootstrap.behavior.CssClassNameAppender;
import de.agilecoders.wicket.markup.html.bootstrap.navigation.BootstrapPagingNavigator;
import net.pickapack.web.util.JavascriptEventConfirmation;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Experiments panel.
 *
 * @author Min Cai
 */
public class PanelExperiments extends Panel {
    /**
     * Create an experiments panel.
     *
     * @param id             the markup ID of the panel that is to be created
     * @param page           the page
     * @param experimentPack the experiment pack
     */
    public PanelExperiments(String id, final IRequestablePage page, final ExperimentPack experimentPack) {
        super(id);

        IDataProvider<Experiment> dataProvider = new ExperimentDataProvider(experimentPack.getId());

        final DataView<Experiment> rowExperiment = new DataView<Experiment>("experiment", dataProvider) {
            {
                setItemsPerPage(10);
            }

            protected void populateItem(Item<Experiment> item) {
                final Experiment experiment = item.getModelObject();

                item.setDefaultModel(new CompoundPropertyModel<>(experiment));

                item.add(new Label("id"));
                item.add(new Label("title"));
                item.add(new Label("type"));
                item.add(new Label("state"));
                item.add(new Label("architecture.id"));
                item.add(new Label("numMaxInstructions"));
                item.add(new Label("createTimeAsString"));

                item.add(new WebMarkupContainer("operations") {{
                    add(new Link<Void>("start") {
                        {
                            if(experiment.getState() == ExperimentState.PENDING) {
                                add(new JavascriptEventConfirmation("onclick", "Are you sure to start?"));
                            }
                            else {
                                add(new CssClassNameAppender("disabled"));
                                setEnabled(false);
                            }
                        }

                        @Override
                        public void onClick() {
                            experiment.setState(ExperimentState.READY_TO_RUN);
                            ServiceManager.getExperimentService().updateExperiment(experiment);
                        }
                    });

                    add(new Link<Void>("stop") {
                        {
                            if(experiment.getState() == ExperimentState.RUNNING) {
                                add(new JavascriptEventConfirmation("onclick", "Are you sure to stop?"));
                            }
                            else {
                                add(new CssClassNameAppender("disabled"));
                                setEnabled(false);
                            }
                        }

                        @Override
                        public void onClick() {
                            experiment.setState(ExperimentState.PENDING); //TODO: stop the thread running the experiment.
                            ServiceManager.getExperimentService().updateExperiment(experiment);
                        }
                    });

                    add(new Link<Void>("resetCompleted") {
                        {
                            if(experiment.getState() == ExperimentState.COMPLETED) {
                                add(new JavascriptEventConfirmation("onclick", "Are you sure to reset if completed?"));
                            }
                            else {
                                add(new CssClassNameAppender("disabled"));
                                setEnabled(false);
                            }
                        }

                        @Override
                        public void onClick() {
                            experiment.setState(ExperimentState.PENDING);
                            ServiceManager.getExperimentService().updateExperiment(experiment);
                        }
                    });

                    add(new Link<Void>("resetAborted") {
                        {
                            if(experiment.getState() == ExperimentState.ABORTED) {
                                add(new JavascriptEventConfirmation("onclick", "Are you sure to reset if aborted?"));
                            }
                            else {
                                add(new CssClassNameAppender("disabled"));
                                setEnabled(false);
                            }
                        }

                        @Override
                        public void onClick() {
                            experiment.setState(ExperimentState.PENDING);
                            ServiceManager.getExperimentService().updateExperiment(experiment);
                        }
                    });

                    add(new BookmarkablePageLink<>("view", ExperimentPage.class, new PageParameters() {{
                        set("action", "view");
                        set("experiment_pack_id", experimentPack.getId());
                        set("experiment_id", experiment.getId());
                        set("back_page_id", page.getId());
                    }}));
                }});
            }
        };

        add(rowExperiment);

        add(new BootstrapPagingNavigator("navigator", rowExperiment));
    }
}
