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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

@MountPath(value = "/", alt = "/architectures")
public class ArchitecturesPage extends AuthenticatedWebPage {
    public ArchitecturesPage(PageParameters parameters) {
        super(PageType.ARCHITECTURES, parameters);

        ListView<Architecture> rowArchitecture = new ListView<Architecture>("row_architecture", ServiceManager.getArchitectureService().getAllArchitectures()) {
            protected void populateItem(ListItem item) {
                Architecture architecture = (Architecture) item.getModelObject();

                item.add(new Label("cell_id", architecture.getId() + ""));
                item.add(new Label("cell_title", architecture.getTitle()));
                item.add(new Label("cell_num_cores", architecture.getNumCores() + ""));
                item.add(new Label("cell_num_threads_per_core", architecture.getNumThreadsPerCore() + ""));
                item.add(new Label("cell_l1I_size", StorageUnit.toString(architecture.getL1ISize())));
                item.add(new Label("cell_l1I_associativity", architecture.getL1IAssociativity() + ""));
                item.add(new Label("cell_l1D_size", StorageUnit.toString(architecture.getL1DSize())));
                item.add(new Label("cell_l1D_associativity", architecture.getL1DAssociativity() + ""));
                item.add(new Label("cell_l2_size", StorageUnit.toString(architecture.getL2Size())));
                item.add(new Label("cell_l2_associativity", architecture.getL2Associativity() + ""));
                item.add(new Label("cell_l2_repl", architecture.getL2ReplacementPolicyType() + ""));
                item.add(new Label("cell_create_time", DateHelper.toString(architecture.getCreateTime())));
            }
        };
        add(rowArchitecture);
    }
}
