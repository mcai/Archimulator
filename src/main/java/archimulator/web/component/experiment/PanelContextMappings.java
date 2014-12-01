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
package archimulator.web.component.experiment;

import archimulator.model.Benchmark;
import archimulator.model.ContextMapping;
import archimulator.model.Experiment;
import archimulator.service.ServiceManager;
import archimulator.web.component.listeditor.ListEditor;
import archimulator.web.component.listeditor.ListItem;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Context mappings panel.
 *
 * @author Min Cai
 */
public class PanelContextMappings extends Panel {
    /**
     * Create a context mappings panel.
     *
     * @param id         the markup ID of the panel that is to be created
     * @param experiment the experiment
     * @param form       the form
     */
    public PanelContextMappings(String id, final Experiment experiment, final Form<?> form) {
        super(id);

        setOutputMarkupId(true);

        final ListEditor<ContextMapping> editorContextMappings = new ListEditor<ContextMapping>("contextMapping", new PropertyModel<List<ContextMapping>>(experiment, "contextMappings")) {
            @Override
            protected void populateItem(final ListItem<ContextMapping> item) {
                item.setOutputMarkupId(true);

                item.setDefaultModel(new CompoundPropertyModel<>(item.getModel()));

                item.add(new TextField<String>("arguments"));

                item.add(new TextField<String>("standardOut"));

                List<Integer> threadIds = new ArrayList<>();
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
                item.add(new DropDownChoice<String>("benchmarkTitle", ServiceManager.getBenchmarkService().getAllBenchmarks().stream().map(Benchmark::getTitle).collect(Collectors.<String>toList()), new IChoiceRenderer<String>() {
                    @Override
                    public Object getDisplayValue(String benchmarkTitle) {
                        return benchmarkTitle;
                    }

                    @Override
                    public String getIdValue(String benchmarkTitle, int index) {
                        return benchmarkTitle + "";
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
                        return super.isEnabled() && item.getModelObject().getBenchmark() != null && item.getModelObject().getBenchmark().getHelperThreadEnabled();
                    }
                });
                item.add(new TextField<Integer>("helperThreadStride") {
                    @Override
                    public boolean isEnabled() {
                        return super.isEnabled() && item.getModelObject().getBenchmark() != null && item.getModelObject().getBenchmark().getHelperThreadEnabled();
                    }
                });
            }
        };
        add(editorContextMappings);
    }
}
