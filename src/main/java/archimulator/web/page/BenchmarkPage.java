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

import archimulator.model.Benchmark;
import archimulator.service.ServiceManager;
import de.agilecoders.wicket.markup.html.bootstrap.common.NotificationPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

/**
 * Benchmark page.
 *
 * @author Min Cai
 */
@MountPath(value = "/benchmark")
public class BenchmarkPage extends AuthenticatedBasePage {
    /**
     * Create a benchmark page.
     *
     * @param parameters the page parameters
     */
    public BenchmarkPage(final PageParameters parameters) {
        super(parameters);

        final String action = parameters.get("action").toString();

        final Benchmark benchmark;

        if (action == null) {
            setResponsePage(getApplication().getHomePage());
            return;
        } else if (action.equals("view")) {
            String benchmarkTitle = parameters.get("benchmark_title").toString();
            benchmark = ServiceManager.getBenchmarkService().getBenchmarkByTitle(benchmarkTitle);
        } else {
            throw new IllegalArgumentException();
        }

        if (benchmark == null) {
            setResponsePage(getApplication().getHomePage());
            return;
        }

        setTitle(String.format("%s Benchmark - Archimulator", action.equals("add") ? "Add" : "Edit"));

        add(new Label("section_header_benchmark", String.format("View Benchmark '%s'", benchmark.getTitle())));

        add(new NotificationPanel("feedback"));

        add(new Form("benchmark") {{
            setDefaultModel(new CompoundPropertyModel<>(benchmark));

            add(new RequiredTextField<String>("title"));
            add(new RequiredTextField<String>("workingDirectory"));
            add(new RequiredTextField<String>("executable"));
            add(new TextField<String>("defaultArguments"));
            add(new TextField<String>("standardIn"));
            add(new CheckBox("helperThreadEnabled"));

            add(new Button("ok") {
                {
                    setDefaultFormProcessing(false);
                }

                @Override
                public void onSubmit() {
                    back(parameters, BenchmarksPage.class);
                }
            });
        }});
    }
}
