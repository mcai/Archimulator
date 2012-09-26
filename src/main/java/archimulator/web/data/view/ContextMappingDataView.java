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
import archimulator.model.ContextMapping;
import archimulator.model.Experiment;
import archimulator.service.ServiceManager;
import archimulator.web.data.provider.ContextMappingDataProvider;
import net.pickapack.action.Function1;
import net.pickapack.util.CollectionHelper;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.PropertyModel;

import java.util.ArrayList;
import java.util.List;

public class ContextMappingDataView extends DataView<ContextMapping> {
    private final Experiment experiment;
    private final WebMarkupContainer tableContextMappings;

    public ContextMappingDataView(String id, Experiment experiment, WebMarkupContainer tableContextMappings) {
        super(id, new ContextMappingDataProvider(experiment));
        this.experiment = experiment;
        this.tableContextMappings = tableContextMappings;
    }

    @Override
    protected void populateItem(final Item<ContextMapping> item) {
        final ContextMapping contextMapping = item.getModelObject();

        final TextField<String> inputArguments = new TextField<String>("input_arguments", new PropertyModel<String>(contextMapping, "arguments"));
        inputArguments.setOutputMarkupId(true);
        item.add(inputArguments);

        final TextField<String> inputStandardOut = new TextField<String>("input_standard_out", new PropertyModel<String>(contextMapping, "standardOut"));
        inputStandardOut.setOutputMarkupId(true);
        item.add(inputStandardOut);

        List<Integer> threadIds = new ArrayList<Integer>();
        for (int i = 0; i < experiment.getArchitecture().getNumCores() * experiment.getArchitecture().getNumThreadsPerCore(); i++) {
            threadIds.add(i);
        }

        item.add(new DropDownChoice<Integer>("select_thread_id", new PropertyModel<Integer>(contextMapping, "threadId"), threadIds) {{
            add(new AjaxFormComponentUpdatingBehavior("onchange") {
                protected void onUpdate(AjaxRequestTarget target) {
                    contextMapping.setStandardOut(ContextMapping.getDefaultStandardOut(contextMapping.getThreadId()));
                    target.add(inputStandardOut);
                }
            });
        }});
        item.add(new DropDownChoice<Long>("select_benchmark", new PropertyModel<Long>(item.getModel(), "benchmarkId"), CollectionHelper.transform(ServiceManager.getBenchmarkService().getAllBenchmarks(), new Function1<Benchmark, Long>() {
            @Override
            public Long apply(Benchmark benchmark) {
                return benchmark.getId();
            }
        }), new IChoiceRenderer<Long>() {
            @Override
            public Object getDisplayValue(Long benchmarkId) {
                Benchmark benchmark = ServiceManager.getBenchmarkService().getBenchmarkById(benchmarkId);
                return String.format("{%d} %s", benchmark.getId(), benchmark.getTitle());
            }

            @Override
            public String getIdValue(Long benchmarkId, int index) {
                return benchmarkId + "";
            }
        }) {
            {
                add(new AjaxFormComponentUpdatingBehavior("onchange") {
                    protected void onUpdate(AjaxRequestTarget target) {
                        contextMapping.setArguments(contextMapping.getBenchmark().getDefaultArguments());

                        target.add(inputArguments);
                    }
                });
            }
        });

        item.add(new TextField<Integer>("input_helper_thread_lookahead", new PropertyModel<Integer>(contextMapping, "helperThreadLookahead")));
        item.add(new TextField<Integer>("input_helper_thread_stride", new PropertyModel<Integer>(contextMapping, "helperThreadStride")));
        item.add(new CheckBox("input_dynamic_helper_thread_params", new PropertyModel<Boolean>(contextMapping, "dynamicHelperThreadParams")));

        item.add(new WebMarkupContainer("cell_operations") {{
            add(new AjaxFallbackLink("button_remove") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    if (experiment.getContextMappings().size() > 1) {
                        experiment.getContextMappings().remove(item.getModelObject());
                        ContextMappingDataView.this.detach();

                        target.add(tableContextMappings);
                    }
                }
            });
        }});
    }
}
