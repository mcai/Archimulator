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

import archimulator.model.ExperimentPackVariable;
import archimulator.service.impl.ExperimentServiceImpl;
import archimulator.service.ServiceManager;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.component.IRequestablePage;

public class ExperimentPackVariableListView extends ListView<ExperimentPackVariable> {
    private IRequestablePage page;

    public ExperimentPackVariableListView(IRequestablePage page, String id, long experimentPackId) {
        super(id, ServiceManager.getExperimentService().getExperimentPackById(experimentPackId).getVariables());
        this.page = page;
    }

    @Override
    protected void populateItem(ListItem<ExperimentPackVariable> item) {
        final ExperimentPackVariable variable = item.getModelObject();

        item.add(new Label("cell_name", ExperimentServiceImpl.getDescriptionOfVariablePropertyName(variable.getName())));
        item.add(new Label("cell_values", StringUtils.join(variable.getValues(), ",")));
    }
}
