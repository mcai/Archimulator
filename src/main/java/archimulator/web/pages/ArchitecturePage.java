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
import net.pickapack.StorageUnit;
import net.pickapack.dateTime.DateHelper;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

@MountPath(value = "/", alt = "/architecture")
public class ArchitecturePage extends AuthenticatedWebPage {
    public ArchitecturePage(PageParameters parameters) {
        super(PageType.ARCHITECTURE, parameters);

        long architectureId = parameters.get("architecture_id").toLong();
        Architecture architecture = ServiceManager.getArchitectureService().getArchitectureById(architectureId);

        if(architecture == null) {
            setResponsePage(HomePage.class);
            return;
        }

        this.add(new TextField<String>("input_id", Model.of(architecture.getId() + "")));
        this.add(new TextField<String>("input_title", Model.of(architecture.getTitle())));
        this.add(new TextField<String>("input_num_cores", Model.of(architecture.getNumCores() + "")));
        this.add(new TextField<String>("input_num_threads_per_core", Model.of(architecture.getNumThreadsPerCore() + "")));
        this.add(new TextField<String>("input_l1I_size", Model.of(StorageUnit.toString(architecture.getL1ISize()))));
        this.add(new TextField<String>("input_l1I_associativity", Model.of(architecture.getL1IAssociativity() + "")));
        this.add(new TextField<String>("input_l1D_size", Model.of(StorageUnit.toString(architecture.getL1DSize()))));
        this.add(new TextField<String>("input_l1D_associativity", Model.of(architecture.getL1DAssociativity() + "")));
        this.add(new TextField<String>("input_l2_size", Model.of(StorageUnit.toString(architecture.getL2Size()))));
        this.add(new TextField<String>("input_l2_associativity", Model.of(architecture.getL2Associativity() + "")));
        this.add(new TextField<String>("input_l2_repl", Model.of(architecture.getL2ReplacementPolicyType() + "")));
        this.add(new TextField<String>("input_create_time", Model.of(DateHelper.toString(architecture.getCreateTime()))));
    }
}
