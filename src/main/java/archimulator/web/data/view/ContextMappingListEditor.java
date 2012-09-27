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
import archimulator.web.pages.listeditor.ListEditor;
import archimulator.web.pages.listeditor.ListItem;
import archimulator.web.pages.listeditor.RemoveButton;
import net.pickapack.action.Function1;
import net.pickapack.util.CollectionHelper;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;

import java.util.ArrayList;
import java.util.List;

public class ContextMappingListEditor extends ListEditor<ContextMapping> {
    private Experiment experiment;
    private Form<?> form;
    private WebMarkupContainer container;

    public ContextMappingListEditor(String id, Experiment experiment, Form<?> form, WebMarkupContainer container) {
        super(id, new PropertyModel<List<ContextMapping>>(experiment, "contextMappings"));
        this.experiment = experiment;
        this.form = form;
        this.container = container;
    }

    @Override
    protected void populateItem(final ListItem<ContextMapping> item) {
        item.setOutputMarkupId(true);

        item.setModel(new CompoundPropertyModel<ContextMapping>(item.getModel()));

        final TextField<String> inputArguments = new TextField<String>("arguments");
        item.add(inputArguments);

        final TextField<String> inputStandardOut = new TextField<String>("standardOut");
        item.add(inputStandardOut);

        List<Integer> threadIds = new ArrayList<Integer>();
        for (int i = 0; i < experiment.getArchitecture().getNumCores() * experiment.getArchitecture().getNumThreadsPerCore(); i++) {
            threadIds.add(i);
        }

        item.add(new DropDownChoice<Integer>("threadId", threadIds) {{
            add(new AjaxFormComponentUpdatingBehavior("onchange") {
                protected void onUpdate(AjaxRequestTarget target) {
                    ContextMapping contextMapping = item.getModelObject();
                    contextMapping.setStandardOut(ContextMapping.getDefaultStandardOut(contextMapping.getThreadId()));

                    target.add(item);
                }
            });
        }});
        item.add(new DropDownChoice<Long>("benchmarkId", CollectionHelper.transform(ServiceManager.getBenchmarkService().getAllBenchmarks(), new Function1<Benchmark, Long>() {
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
                        item.getModelObject().setArguments(item.getModelObject().getBenchmark().getDefaultArguments());
                        target.add(item);
                    }
                });
            }
        });

        item.add(new TextField<Integer>("helperThreadLookahead") {
            @Override
            public boolean isEnabled() {
                return super.isEnabled() && item.getModelObject().getBenchmark().getHelperThreadEnabled();
            }
        });
        item.add(new TextField<Integer>("helperThreadStride") {
            @Override
            public boolean isEnabled() {
                return super.isEnabled() && item.getModelObject().getBenchmark().getHelperThreadEnabled();
            }
        });
        item.add(new CheckBox("dynamicHelperThreadParams") {
            @Override
            public boolean isEnabled() {
                return super.isEnabled() && item.getModelObject().getBenchmark().getHelperThreadEnabled();
            }
        });

        item.add(new RemoveButton("button_remove_context_mapping", form, container));
    }
}
