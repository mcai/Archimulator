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
import archimulator.web.data.provider.ExperimentDataProvider;
import archimulator.web.data.view.ExperimentDataView;
import de.agilecoders.wicket.markup.html.bootstrap.navigation.BootstrapPagingNavigator;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

@MountPath(value = "/experiments")
public class ExperimentsPage extends AuthenticatedBasePage {
    public ExperimentsPage(PageParameters parameters) {
        super(parameters);

        setTitle("Experiments - Archimulator");

        final long experimentPackId = parameters.get("experiment_pack_id").toLong(-1);

        IDataProvider<Experiment> dataProvider = new ExperimentDataProvider(experimentPackId);

        final DataView<Experiment> rowExperiment = new ExperimentDataView(this, "row_experiment", dataProvider);
        rowExperiment.setItemsPerPage(10);

        final WebMarkupContainer tableExperiments = new WebMarkupContainer("table_experiments");
        add(tableExperiments);

        tableExperiments.add(rowExperiment);

        add(new BootstrapPagingNavigator("navigator", rowExperiment));
    }
}
