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

import archimulator.model.Benchmark;
import archimulator.web.data.provider.BenchmarkDataProvider;
import archimulator.web.data.view.BenchmarkDataView;
import de.agilecoders.wicket.markup.html.bootstrap.navigation.BootstrapPagingNavigator;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

@MountPath(value = "/", alt = "/benchmarks")
public class BenchmarksPage extends AuthenticatedBasePage {
    public BenchmarksPage(PageParameters parameters) {
        super(parameters);

        setTitle("Benchmarks - Archimulator");

        IDataProvider<Benchmark> dataProvider = new BenchmarkDataProvider();

        DataView<Benchmark> rowBenchmark = new BenchmarkDataView(this, "row_benchmark", dataProvider);
        rowBenchmark.setItemsPerPage(10);
        add(rowBenchmark);

        add(new BootstrapPagingNavigator("navigator", rowBenchmark));
    }
}
