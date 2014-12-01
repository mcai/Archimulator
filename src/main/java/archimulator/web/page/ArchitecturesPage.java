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
import archimulator.web.data.ArchitectureDataProvider;
import de.agilecoders.wicket.markup.html.bootstrap.navigation.BootstrapPagingNavigator;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

/**
 * Architectures page.
 *
 * @author Min Cai
 */
@MountPath(value = "/architectures")
public class ArchitecturesPage extends AuthenticatedBasePage {
    /**
     * Create an architectures page.
     *
     * @param parameters the page parameters
     */
    public ArchitecturesPage(PageParameters parameters) {
        super(parameters);

        setTitle("Architectures - Archimulator");

        final IDataProvider<Architecture> dataProviderArchitecture = new ArchitectureDataProvider();

        DataView<Architecture> rowArchitecture = new DataView<Architecture>("architecture", dataProviderArchitecture) {
            {
                setItemsPerPage(10);
            }

            @Override
            protected void populateItem(Item<Architecture> item) {
                final Architecture architecture = item.getModelObject();

                item.setDefaultModel(new CompoundPropertyModel<>(architecture));

                item.add(new Label("id"));
                item.add(new Label("title"));
                item.add(new Label("numCores"));
                item.add(new Label("numThreadsPerCore"));
                item.add(new Label("l1ISizeInStorageUnit"));
                item.add(new Label("l1IAssociativity"));
                item.add(new Label("l1DSizeInStorageUnit"));
                item.add(new Label("l1DAssociativity"));
                item.add(new Label("l2SizeInStorageUnit"));
                item.add(new Label("l2Associativity"));
                item.add(new Label("l2ReplacementPolicyType"));
                item.add(new Label("createTimeAsString"));

                item.add(new WebMarkupContainer("operations") {{
                    add(new BookmarkablePageLink<>("edit", ArchitecturePage.class, new PageParameters() {{
                        set("action", "edit");
                        set("architecture_id", architecture.getId());
                        set("back_page_id", ArchitecturesPage.this.getId());
                    }}));
                }});
            }
        };

        add(rowArchitecture);

        add(new BootstrapPagingNavigator("navigator", rowArchitecture));
    }
}
