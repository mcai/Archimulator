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
package archimulator.web.data.provider;

import archimulator.model.Benchmark;
import archimulator.service.ServiceManager;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import java.util.Iterator;

public class BenchmarkDataProvider implements IDataProvider<Benchmark> {
    @Override
    public Iterator<? extends Benchmark> iterator(long first, long count) {
        return ServiceManager.getBenchmarkService().getAllBenchmarks(first, count).iterator();
    }

    @Override
    public long size() {
        return ServiceManager.getBenchmarkService().getNumAllBenchmarks();
    }

    @Override
    public IModel<Benchmark> model(final Benchmark object) {
        return new LoadableDetachableModel<Benchmark>(object) {
            @Override
            protected Benchmark load() {
                return object;
            }
        };
    }

    @Override
    public void detach() {
    }
}
