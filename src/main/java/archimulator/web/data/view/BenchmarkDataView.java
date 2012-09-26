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

import archimulator.model.Benchmark;
import archimulator.web.pages.BenchmarkPage;
import net.pickapack.dateTime.DateHelper;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class BenchmarkDataView extends DataView<Benchmark> {
    private IRequestablePage page;

    public BenchmarkDataView(IRequestablePage page, String id, IDataProvider<Benchmark> dataProvider) {
        super(id, dataProvider);
        this.page = page;
    }

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
            add(new BookmarkablePageLink<Object>("button_edit", BenchmarkPage.class, new PageParameters() {{
                set("action", "edit");
                set("benchmark_id", benchmark.getId());
                set("back_page_id", page.getId());
            }}));
        }});
    }
}
