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
package archimulator.web.components.experiment;

import archimulator.model.Benchmark;
import archimulator.model.ContextMapping;
import archimulator.model.Experiment;
import archimulator.service.ServiceManager;
import archimulator.web.data.view.ContextMappingListEditor;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

public class PanelContextMappings extends Panel {
    public PanelContextMappings(String id, Experiment experiment, Form<?> form) {
        super(id);

        final WebMarkupContainer tableContextMappings = new WebMarkupContainer("table_context_mappings");
        tableContextMappings.setOutputMarkupId(true);
        add(tableContextMappings);

        final ContextMappingListEditor editorContextMappings = new ContextMappingListEditor("row_context_mapping", experiment, form, tableContextMappings);
        tableContextMappings.add(editorContextMappings);

        tableContextMappings.add(new AjaxFallbackButton("button_add_context_mapping", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                Integer threadId = 0;
                Benchmark benchmark = ServiceManager.getBenchmarkService().getFirstBenchmark();
                ContextMapping contextMapping = new ContextMapping(threadId, benchmark, benchmark.getDefaultArguments(), ContextMapping.getDefaultStandardOut(threadId));

                editorContextMappings.addItem(contextMapping);

                if (target != null) {
                    target.add(tableContextMappings);
                }
            }
        });
    }
}
