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

import archimulator.model.Architecture;
import archimulator.service.ServiceManager;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyType;
import archimulator.sim.uncore.dram.MemoryControllerType;
import archimulator.web.component.architecture.*;
import net.pickapack.web.util.JavascriptEventConfirmation;
import de.agilecoders.wicket.markup.html.bootstrap.common.NotificationPanel;
import de.agilecoders.wicket.markup.html.bootstrap.tabs.BootstrapTabbedPanel;
import net.pickapack.util.StorageUnitHelper;
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
 * Architecture page.
 *
 * @author Min Cai
 */
@MountPath(value = "/architecture")
public class ArchitecturePage extends AuthenticatedBasePage {
    /**
     * Create an architecture page.
     *
     * @param parameters the page parameters
     */
    public ArchitecturePage(final PageParameters parameters) {
        super(parameters);

        final String action = parameters.get("action").toString();

        final Architecture architecture;

        if (action == null) {
            setResponsePage(getApplication().getHomePage());
            return;
        } else if (action.equals("add")) {
            architecture = new Architecture(
                    false,
                    -1,
                    2,
                    2,
                    (int) StorageUnitHelper.displaySizeToByteCount("32 KB"),
                    4,
                    (int) StorageUnitHelper.displaySizeToByteCount("32 KB"),
                    4,
                    (int) StorageUnitHelper.displaySizeToByteCount("96 KB"),
                    8,
                    CacheReplacementPolicyType.LRU,
                    MemoryControllerType.FIXED_LATENCY
            );
        } else if (action.equals("edit")) {
            long architectureId = parameters.get("architecture_id").toLong(-1);
            architecture = ServiceManager.getArchitectureService().getArchitectureById(architectureId);
        } else {
            throw new IllegalArgumentException();
        }

        if (architecture == null) {
            setResponsePage(getApplication().getHomePage());
            return;
        }

        setTitle((action.equals("add") ? "Add" : "Edit") + " Architecture - Archimulator");

        this.add(new Label("sectionHeaderArchitecture", String.format("%s Architecture '{%d} %s'", action.equals("add") ? "Add" : "Edit", architecture.getId(), architecture.getTitle())));

        add(new NotificationPanel("feedback"));

        this.add(new Form("architecture") {{
            add(new BootstrapTabbedPanel<>("tabs", new ArrayList<ITab>() {{
                add(new AbstractTab(new Model<>("General")) {
                    public Panel getPanel(String panelId) {
                        return new PanelGeneral(panelId, architecture);
                    }
                });

                add(new AbstractTab(new Model<>("Pipelines")) {
                    public Panel getPanel(String panelId) {
                        return new PanelPipelines(panelId, architecture);
                    }
                });

                add(new AbstractTab(new Model<>("Branch Predictors")) {
                    public Panel getPanel(String panelId) {
                        return new PanelBranchPredictors(panelId, architecture);
                    }
                });

                add(new AbstractTab(new Model<>("TLBs")) {
                    public Panel getPanel(String panelId) {
                        return new PanelTlbs(panelId, architecture);
                    }
                });

                add(new AbstractTab(new Model<>("Cache Controllers")) {
                    public Panel getPanel(String panelId) {
                        return new PanelCacheControllers(panelId, architecture);
                    }
                });

                add(new AbstractTab(new Model<>("Memory Controller")) {
                    public Panel getPanel(String panelId) {
                        return new PanelMemoryController(panelId, architecture);
                    }
                });

                add(new AbstractTab(new Model<>("Interconnects")) {
                    public Panel getPanel(String panelId) {
                        return new PanelInterconnects(panelId, architecture);
                    }
                });
            }}));

            this.add(new Button("save", Model.of(action.equals("add") ? "Add" : "Save")) {
                {
                    add(new JavascriptEventConfirmation("onclick", "Are you sure to " + (action.equals("add") ? "add" : "save") + "?"));
                }

                @Override
                public void onSubmit() {
                    if (action.equals("add")) {
                        architecture.updateTitle();

                        if (ServiceManager.getArchitectureService().getArchitectureByTitle(architecture.getTitle()) == null) {
                            ServiceManager.getArchitectureService().addArchitecture(architecture);
                        }
                    } else {
                        architecture.updateTitle();

                        Architecture architectureWithSameTitle = ServiceManager.getArchitectureService().getArchitectureByTitle(architecture.getTitle());
                        if (architectureWithSameTitle != null && architectureWithSameTitle.getId() != architecture.getId()) {
                            ServiceManager.getArchitectureService().removeArchitectureById(architecture.getId());
                        } else {
                            ServiceManager.getArchitectureService().updateArchitecture(architecture);
                        }
                    }

                    back(parameters, ArchitecturesPage.class);
                }
            });

            this.add(new Button("cancel") {
                {
                    setDefaultFormProcessing(false);
                }

                @Override
                public void onSubmit() {
                    back(parameters, ArchitecturesPage.class);
                }
            });

            this.add(new Button("remove") {
                {
                    setDefaultFormProcessing(false);
                    setVisible(action.equals("edit"));

                    add(new JavascriptEventConfirmation("onclick", "Are you sure to remove?"));
                }

                @Override
                public void onSubmit() {
                    ServiceManager.getArchitectureService().removeArchitectureById(architecture.getId());

                    back(parameters, ArchitecturesPage.class);
                }
            });
        }});
    }
}
