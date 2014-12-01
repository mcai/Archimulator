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
package archimulator.web.page;

import archimulator.model.Experiment;
import archimulator.model.ExperimentPack;
import archimulator.service.ServiceManager;
import archimulator.web.component.experiment.*;
import de.agilecoders.wicket.markup.html.bootstrap.common.NotificationPanel;
import de.agilecoders.wicket.markup.html.bootstrap.tabs.BootstrapTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

import java.util.ArrayList;

/**
 * Experiment page.
 *
 * @author Min Cai
 */
@MountPath(value = "/experiment")
public class ExperimentPage extends AuthenticatedBasePage {
    private Form formExperiment;

    /**
     * Create an experiment page.
     *
     * @param parameters the page parameters
     */
    public ExperimentPage(final PageParameters parameters) {
        super(parameters);

        final String action = parameters.get("action").toString();

        final long experimentPackId = parameters.get("experiment_pack_id").toLong(-1);

        ExperimentPack experimentPack = ServiceManager.getExperimentService().getExperimentPackById(experimentPackId);

        if (experimentPack == null) {
            setResponsePage(getApplication().getHomePage());
            return;
        }

        final Experiment experiment;

        if (action == null) {
            setResponsePage(getApplication().getHomePage());
            return;
        } else if (action.equals("view")) {
            long experimentId = parameters.get("experiment_id").toLong(-1);
            experiment = ServiceManager.getExperimentService().getExperimentById(experimentId);
        } else {
            throw new IllegalArgumentException();
        }

        if (experiment == null) {
            setResponsePage(getApplication().getHomePage());
            return;
        }

        setTitle("View Experiment - Archimulator");

        add(new Label("sectionHeaderExperiment", String.format("%s Experiment#%d", action.equals("add") ? "Add" : "Edit", experiment.getId())));

        add(new NotificationPanel("feedback"));

        formExperiment = new Form("experiment") {{
            add(new BootstrapTabbedPanel<>("tabs", new ArrayList<ITab>() {{
                add(new AbstractTab(new Model<>("General")) {
                    public Panel getPanel(String panelId) {
                        return new PanelGeneral(panelId, experiment);
                    }
                });

                add(new AbstractTab(new Model<>("Summary")) {
                    public Panel getPanel(String panelId) {
                        return new PanelSummary(panelId, experiment);
                    }
                });

                add(new AbstractTab(new Model<>("Context Mappings")) {
                    public Panel getPanel(String panelId) {
                        return new PanelContextMappings(panelId, experiment, formExperiment);
                    }
                });

                add(new AbstractTab(new Model<>("Statistics:General")) {
                    public Panel getPanel(String panelId) {
                        return new PanelGeneralStatistics(panelId, experiment);
                    }
                });

                add(new AbstractTab(new Model<>("Statistics:Cache Coherence")) {
                    public Panel getPanel(String panelId) {
                        return new PanelCacheCoherenceStatistics(panelId, experiment);
                    }
                });
            }}));

            add(new Button("ok") {
                {
                    setDefaultFormProcessing(false);
                }

                @Override
                public void onSubmit() {
                    back(parameters, ExperimentPackPage.class);
                }
            });
        }};

        add(formExperiment);
    }
}
