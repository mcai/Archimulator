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
import archimulator.service.ServiceManager;
import archimulator.web.components.PagingNavigator;
import net.pickapack.dateTime.DateHelper;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

import java.util.Iterator;

@MountPath(value = "/", alt = "/benchmarks")
public class BenchmarksPage extends AuthenticatedWebPage {
    public BenchmarksPage(PageParameters parameters) {
        super(PageType.BENCHMARKS, parameters);

        setTitle("Benchmarks - Archimulator");

        IDataProvider<Benchmark> dataProvider = new IDataProvider<Benchmark>() {
            @Override
            public Iterator<? extends Benchmark> iterator(long first, long count) {
                return ServiceManager.getBenchmarkService().getAllBenchmarks(first, count).iterator();
            }

            @Override
            public long size() {
                return ServiceManager.getBenchmarkService().getNumAllBenchmarks();
            }

            @Override
            public IModel<Benchmark> model(Benchmark object) {
                return new Model<Benchmark>(object);
            }

            @Override
            public void detach() {
            }
        };

        DataView<Benchmark> rowBenchmark = new DataView<Benchmark>("row_benchmark", dataProvider) {
            protected void populateItem(Item<Benchmark> item) {
                final Benchmark benchmark = item.getModelObject();

                item.add(new Label("cell_id", benchmark.getId() + ""));
                item.add(new Label("cell_title", benchmark.getTitle()));
                item.add(new Label("cell_executable", benchmark.getExecutable()));
                item.add(new Label("cell_default_arguments", benchmark.getDefaultArguments()));
                item.add(new Label("cell_stdin", benchmark.getStandardIn()));
                item.add(new Label("cell_helper_thread_enabled", benchmark.getHelperThreadEnabled() + ""));
                item.add(new Label("cell_create_time", DateHelper.toString(benchmark.getCreateTime())));

                item.add(new WebMarkupContainer("cell_operations") {{
                    add(new Link<Void>("button_edit") {
                        @Override
                        public void onClick() {
                            PageParameters pageParameters1 = new PageParameters();
                            pageParameters1.set("action", "edit");
                            pageParameters1.set("benchmark_id", benchmark.getId());
                            pageParameters1.set("back_page_id", getPageId());

                            setResponsePage(BenchmarkPage.class, pageParameters1);
                        }
                    });
                }});
            }
        };
        rowBenchmark.setItemsPerPage(10);
        add(rowBenchmark);

        add(new PagingNavigator("navigator", rowBenchmark));
    }
}
