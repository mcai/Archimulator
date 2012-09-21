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
import archimulator.model.ExperimentPack;
import archimulator.model.ExperimentState;
import archimulator.service.ServiceManager;
import archimulator.util.PropertiesHelper;
import archimulator.web.ArchimulatorSession;
import archimulator.web.components.PagingNavigator;
import de.agilecoders.wicket.markup.html.bootstrap.behavior.CssClassNameAppender;
import org.apache.wicket.behavior.AttributeAppender;
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

import java.text.NumberFormat;
import java.util.Iterator;

@MountPath(value = "/", alt = "/home")
public class HomePage extends BasePage {
    public HomePage(PageParameters parameters) {
        super(PageType.HOME, parameters);

        add(new WebMarkupContainer("div_welcome") {{
            add(new Label("label_version", Model.of("Version " + PropertiesHelper.getVersion())) {{
                setEscapeModelStrings(false);
            }});

            setVisible(!ArchimulatorSession.get().isSignedIn());
        }});

        add(new WebMarkupContainer("div_social") {{
            setVisible(!ArchimulatorSession.get().isSignedIn());
        }});

        add(new WebMarkupContainer("div_experiment_packs") {{
            IDataProvider<ExperimentPack> dataProvider = new IDataProvider<ExperimentPack>() {
                @Override
                public Iterator<? extends ExperimentPack> iterator(long first, long count) {
                    return ServiceManager.getExperimentService().getAllExperimentPacks(first, count).iterator();
                }

                @Override
                public long size() {
                    return ServiceManager.getExperimentService().getAllExperimentPacks().size();
                }

                @Override
                public IModel<ExperimentPack> model(ExperimentPack object) {
                    return new Model<ExperimentPack>(object);
                }

                @Override
                public void detach() {
                }
            };

            DataView<ExperimentPack> rowExperimentPack = new DataView<ExperimentPack>("row_experiment_pack", dataProvider) {
                protected void populateItem(Item<ExperimentPack> item) {
                    final ExperimentPack experimentPack = item.getModelObject();

                    int numTotal = ServiceManager.getExperimentService().getExperimentsByExperimentPack(experimentPack).size();
                    int numRunning = 0;
                    int numStopped = 0;

                    for (Experiment experiment : ServiceManager.getExperimentService().getExperimentsByExperimentPack(experimentPack)) {
                        if (experiment.getState() == ExperimentState.RUNNING) {
                            numRunning++;
                        } else if (experiment.isStopped()) {
                            numStopped++;
                        }
                    }

                    item.add(new Label("cell_title", experimentPack.getTitle()));
                    item.add(new Label("cell_description", String.format(
                            "%s; total: %d, running: %d, stopped: %d (%s)",
                            experimentPack.getExperimentType(),
                            numTotal, numRunning, numStopped,
                            NumberFormat.getPercentInstance().format((double) numStopped / numTotal)
                    )));

                    item.add(new WebMarkupContainer("cell_operations_1") {{
                        add(new Link<Void>("button_start") {
                            {
                                if (!(ServiceManager.getExperimentService().getNumExperimentsByExperimentPackAndState(experimentPack, ExperimentState.PENDING) > 0)) {
                                    add(new CssClassNameAppender("disabled"));
                                    setEnabled(false);
                                }
                            }

                            @Override
                            public void onClick() {
                                ServiceManager.getExperimentService().startExperimentPack(experimentPack);
                            }
                        });

                        add(new Link<Void>("button_stop") {
                            {
                                if (!(ServiceManager.getExperimentService().getNumExperimentsByExperimentPackAndState(experimentPack, ExperimentState.READY_TO_RUN) > 0)) {
                                    add(new CssClassNameAppender("disabled"));
                                    setEnabled(false);
                                }
                            }

                            @Override
                            public void onClick() {
                                ServiceManager.getExperimentService().stopExperimentPack(experimentPack);
                            }
                        });

                        add(new Link<Void>("button_reset_stopped_experiments") {
                            {
                                if (!(ServiceManager.getExperimentService().getNumExperimentsByExperimentPackAndState(experimentPack, ExperimentState.COMPLETED) > 0 ||
                                        ServiceManager.getExperimentService().getNumExperimentsByExperimentPackAndState(experimentPack, ExperimentState.ABORTED) > 0)) {
                                    add(new CssClassNameAppender("disabled"));
                                    setEnabled(false);
                                }
                            }

                            @Override
                            public void onClick() {
                                ServiceManager.getExperimentService().resetExperimentsByExperimentPack(experimentPack);
                            }
                        });
                    }});

                    item.add(new WebMarkupContainer("cell_operations_2") {{
                        add(new Label("button_edit", "Edit") {{
                            add(new AttributeAppender("href", "./experiment_pack?experiment_pack_title=" + experimentPack.getTitle()));
                        }});

                        add(new Link<Void>("button_remove") {
                            @Override
                            public void onClick() {
                                ServiceManager.getExperimentService().removeExperimentPackById(experimentPack.getId());
                            }
                        });
                    }});
                }
            };
            rowExperimentPack.setItemsPerPage(12);
            add(rowExperimentPack);

            add(new PagingNavigator("navigator", rowExperimentPack));

            setVisible(ArchimulatorSession.get().isSignedIn());
        }});
    }
}
