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
package archimulator.web.data.view;

import archimulator.model.Experiment;
import archimulator.web.pages.ExperimentPage;
import net.pickapack.dateTime.DateHelper;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class ExperimentDataView extends DataView<Experiment> {
    private IRequestablePage page;

    public ExperimentDataView(IRequestablePage page, String id, IDataProvider<Experiment> dataProvider) {
        super(id, dataProvider);
        this.page = page;
    }

    protected void populateItem(Item<Experiment> item) {
        final Experiment experiment = item.getModelObject();

        item.add(new Label("cell_id", experiment.getId() + ""));
        item.add(new Label("cell_title", experiment.getTitle()));
        item.add(new Label("cell_type", experiment.getType() + ""));
        item.add(new Label("cell_state", experiment.getState() + ""));
        item.add(new Label("cell_architecture", String.format("{%d} %s", experiment.getArchitecture().getId(), experiment.getArchitecture().getTitle())));
        item.add(new Label("cell_num_max_instructions", experiment.getNumMaxInstructions() + ""));
        item.add(new Label("cell_create_time", DateHelper.toString(experiment.getCreateTime())));

        item.add(new WebMarkupContainer("cell_operations") {{
            add(new Link<Void>("button_edit") {
                @Override
                public void onClick() {
                    PageParameters pageParameters1 = new PageParameters();
                    pageParameters1.set("action", "edit");
                    pageParameters1.set("experiment_id", experiment.getId());
                    pageParameters1.set("back_page_id", page.getId());

                    setResponsePage(ExperimentPage.class, pageParameters1);
                }
            });
        }});
    }
}
