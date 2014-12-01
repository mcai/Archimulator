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

import archimulator.model.ExperimentPack;
import archimulator.service.ServiceManager;
import archimulator.web.component.experimentPack.*;
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
 * Experiment pack page.
 *
 * @author Min Cai
 */
@MountPath(value = "/experiment_pack")
public class ExperimentPackPage extends AuthenticatedBasePage {
    /**
     * Create an experiment pack page.
     *
     * @param parameters the page parameters
     */
    public ExperimentPackPage(final PageParameters parameters) {
        super(parameters);

        final long experimentPackId = parameters.get("experiment_pack_id").toLong(-1);

        final ExperimentPack experimentPack = ServiceManager.getExperimentService().getExperimentPackById(experimentPackId);

        if (experimentPack == null) {
            setResponsePage(getApplication().getHomePage());
            return;
        }

        setTitle((experimentPackId == -1 ? "Add" : "Edit") + " Experiment Pack - Archimulator");

        add(new Label("sectionHeaderExperimentPack", String.format("%s Experiment Pack '{%d} %s'", "Edit", experimentPack.getId(), experimentPack.getTitle())));

        add(new Form("experimentPack") {{
            add(new BootstrapTabbedPanel<>("tabs", new ArrayList<ITab>() {{
                add(new AbstractTab(new Model<>("General")) {
                    public Panel getPanel(String panelId) {
                        return new PanelGeneral(panelId, experimentPack);
                    }
                });

                add(new AbstractTab(new Model<>("Inputs")) {
                    public Panel getPanel(String panelId) {
                        return new PanelInput(panelId, experimentPack);
                    }
                });

                add(new AbstractTab(new Model<>("Experiments")) {
                    public Panel getPanel(String panelId) {
                        return new PanelExperiments(panelId, ExperimentPackPage.this, experimentPack);
                    }
                });

                add(new AbstractTab(new Model<>("Statistics:General")) {
                    public Panel getPanel(String panelId) {
                        return new PanelGeneralStatistics(panelId, experimentPack);
                    }
                });
            }}));

            add(new Button("ok") {
                {
                    setDefaultFormProcessing(false);
                }

                @Override
                public void onSubmit() {
                    back(parameters, ExperimentPacksPage.class);
                }
            });
        }});
    }
}
