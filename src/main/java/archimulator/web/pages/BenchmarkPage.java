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
import net.pickapack.dateTime.DateHelper;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

@MountPath(value = "/", alt = "/benchmark")
public class BenchmarkPage extends AuthenticatedWebPage {
    public BenchmarkPage(final PageParameters parameters) {
        super(PageType.BENCHMARK, parameters);

        final String action = parameters.get("action").toString();

        final Benchmark benchmark;

        if (action == null) {
            setResponsePage(getApplication().getHomePage());
            return;
        } else if (action.equals("add")) {
            benchmark = new Benchmark("", "", "", "");
        } else if (action.equals("edit")) {
            long benchmarkId = parameters.get("benchmark_id").toLong(-1);
            benchmark = ServiceManager.getBenchmarkService().getBenchmarkById(benchmarkId);
        } else {
            throw new IllegalArgumentException();
        }

        if (benchmark == null) {
            setResponsePage(getApplication().getHomePage());
            return;
        }

        setTitle((action.equals("add") ? "Add" : "Edit") + " Benchmark - Archimulator");

        this.add(new Label("section_header_benchmark", (action.equals("add") ? "Add" : "Edit") + " Benchmark"));

        add(new FeedbackPanel("span_feedback"));

        this.add(new Form("form_benchmark") {{
            this.add(new TextField<Long>("input_id", Model.of(benchmark.getId())));
            this.add(new RequiredTextField<String>("input_title", new PropertyModel<String>(benchmark, "title")));
            this.add(new RequiredTextField<String>("input_working_directory", new PropertyModel<String>(benchmark, "workingDirectory")));
            this.add(new RequiredTextField<String>("input_executable", new PropertyModel<String>(benchmark, "executable")));
            this.add(new TextField<String>("input_default_arguments", new PropertyModel<String>(benchmark, "defaultArguments")));
            this.add(new TextField<String>("input_standard_in", new PropertyModel<String>(benchmark, "standardIn")));
            this.add(new CheckBox("input_helper_thread_enabled", new PropertyModel<Boolean>(benchmark, "helperThreadEnabled")));
            this.add(new CheckBox("input_locked_for_building", Model.of(benchmark.getLocked())));
            this.add(new TextField<String>("input_create_time", Model.of(DateHelper.toString(benchmark.getCreateTime()))));

            this.add(new Button("button_save", Model.of(action.equals("add") ? "Add" : "Save")) {
                @Override
                public void onSubmit() {
                    if (action.equals("add")) {
                        ServiceManager.getBenchmarkService().addBenchmark(benchmark);
                    } else {
                        ServiceManager.getBenchmarkService().updateBenchmark(benchmark);
                    }

                    back(parameters, BenchmarksPage.class);
                }
            });

            this.add(new Button("button_cancel") {
                {
                    setDefaultFormProcessing(false);
                }

                @Override
                public void onSubmit() {
                    back(parameters, BenchmarksPage.class);
                }
            });

            this.add(new Button("button_remove") {
                {
                    setDefaultFormProcessing(false);
                    setVisible(action.equals("edit"));
                }

                @Override
                public void onSubmit() {
                    ServiceManager.getBenchmarkService().removeBenchmarkById(benchmark.getId());

                    back(parameters, BenchmarksPage.class);
                }
            });
        }});
    }
}
