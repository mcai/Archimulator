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

import archimulator.model.Architecture;
import archimulator.service.ServiceManager;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyType;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.util.StorageUnitHelper;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

import java.util.Arrays;

@MountPath(value = "/", alt = "/architecture")
public class ArchitecturePage extends AuthenticatedWebPage {
    public ArchitecturePage(PageParameters parameters) {
        super(PageType.ARCHITECTURE, parameters);

        final String action = parameters.get("action").toString();

        final Architecture architecture;

        if (action.equals("add")) {
            architecture = new Architecture(false, 2, 2,
                    (int) StorageUnitHelper.displaySizeToByteCount("32 KB"),
                    4,
                    (int) StorageUnitHelper.displaySizeToByteCount("32 KB"),
                    4,
                    (int) StorageUnitHelper.displaySizeToByteCount("96 KB"),
                    8,
                    CacheReplacementPolicyType.LRU);
        } else if (action.equals("edit")) {
            long architectureId = parameters.get("architecture_id").toLong();
            architecture = ServiceManager.getArchitectureService().getArchitectureById(architectureId);
        } else {
            throw new IllegalArgumentException();
        }

        if (architecture == null) {
            setResponsePage(HomePage.class);
            return;
        }

        setTitle((action.equals("add") ? "Add" : "Edit") + " Architecture - Archimulator");

        this.add(new Label("section_header_architecture", (action.equals("add") ? "Add" : "Edit") + " Architecture"));

        add(new FeedbackPanel("span_feedback"));

        this.add(new Form("form_architecture") {{
            this.add(new TextField<String>("input_id", Model.of(architecture.getId() + "")));
            this.add(new TextField<String>("input_title", Model.of(architecture.getTitle())));

            this.add(new NumberTextField<Integer>("input_num_cores", new PropertyModel<Integer>(architecture, "numCores")));
            this.add(new NumberTextField<Integer>("input_num_threads_per_core", new PropertyModel<Integer>(architecture, "numThreadsPerCore")));

            this.add(new CheckBox(
                    "input_ht_llc_request_profiling_enabled",
                    new PropertyModel<Boolean>(architecture, "helperThreadL2CacheRequestProfilingEnabled")));

            this.add(new RequiredTextField<String>("input_l1I_size", new PropertyModel<String>(architecture, "l1ISizeInStorageUnit")));
            this.add(new NumberTextField<Integer>("input_l1I_associativity", new PropertyModel<Integer>(architecture, "l1IAssociativity")));
            this.add(new RequiredTextField<String>("input_l1D_size", new PropertyModel<String>(architecture, "l1DSizeInStorageUnit")));
            this.add(new NumberTextField<Integer>("input_l1D_associativity", new PropertyModel<Integer>(architecture, "l1DAssociativity")));
            this.add(new RequiredTextField<String>("input_l2_size", new PropertyModel<String>(architecture, "l2SizeInStorageUnit")));
            this.add(new NumberTextField<Integer>("input_l2_associativity", new PropertyModel<Integer>(architecture, "l2Associativity")));

            this.add(new DropDownChoice<CacheReplacementPolicyType>(
                    "select_l2_repl",
                    new PropertyModel<CacheReplacementPolicyType>(architecture, "l2ReplacementPolicyType"),
                    Arrays.asList(CacheReplacementPolicyType.values())));

            this.add(new TextField<String>("input_create_time", Model.of(DateHelper.toString(architecture.getCreateTime()))));

            this.add(new Button("button_save", Model.of(action.equals("add") ? "Add" : "Save")) {
                @Override
                public void onSubmit() {
                    if (action.equals("add")) {
                        architecture.updateTitle();

                        if(ServiceManager.getArchitectureService().getArchitectureByTitle(architecture.getTitle()) == null) {
                            ServiceManager.getArchitectureService().addArchitecture(architecture);
                        }
                    } else {
                        architecture.updateTitle();

                        Architecture architectureWithSameTitle = ServiceManager.getArchitectureService().getArchitectureByTitle(architecture.getTitle());
                        if(architectureWithSameTitle != null && architectureWithSameTitle.getId() != architecture.getId()) {
                            ServiceManager.getArchitectureService().removeArchitectureById(architecture.getId());
                        }
                        else {
                            ServiceManager.getArchitectureService().updateArchitecture(architecture);
                        }
                    }
                    setResponsePage(ArchitecturesPage.class);
                }
            });

            this.add(new Button("button_remove") {
                {
                    setVisible(action.equals("edit"));
                }

                @Override
                public void onSubmit() {
                    ServiceManager.getArchitectureService().removeArchitectureById(architecture.getId());
                    setResponsePage(ArchitecturesPage.class);
                }
            });
        }});
    }
}
