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
package archimulator.web.component.architecture;

import archimulator.model.Architecture;
import archimulator.sim.uncore.dram.MemoryControllerType;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;

import java.util.Arrays;

/**
 * Memory controller panel.
 *
 * @author Min Cai
 */
public class PanelMemoryController extends Panel {
    /**
     * Create a memory controllers panel.
     *
     * @param id           the markup ID of the panel that is to be created
     * @param architecture the architecture
     */
    public PanelMemoryController(String id, final Architecture architecture) {
        super(id);

        setDefaultModel(new CompoundPropertyModel<>(architecture));

        final WebMarkupContainer divFixedLatencyMemoryController = new WebMarkupContainer("fixedLatencyMemoryController") {{
            setOutputMarkupPlaceholderTag(true);
            setVisible(architecture.getMemoryControllerType() == MemoryControllerType.FIXED_LATENCY);

            add(new NumberTextField<Integer>("fixedLatencyMemoryControllerLatency"));
        }};
        add(divFixedLatencyMemoryController);

        final WebMarkupContainer divSimpleMemoryController = new WebMarkupContainer("simpleMemoryController") {{
            setOutputMarkupPlaceholderTag(true);
            setVisible(architecture.getMemoryControllerType() == MemoryControllerType.SIMPLE);

            add(new NumberTextField<Integer>("simpleMemoryControllerMemoryLatency"));
            add(new NumberTextField<Integer>("simpleMemoryControllerMemoryTrunkLatency"));
            add(new NumberTextField<Integer>("simpleMemoryControllerBusWidth"));
        }};
        add(divSimpleMemoryController);

        final WebMarkupContainer divBasicMemoryController = new WebMarkupContainer("basicMemoryController") {{
            setOutputMarkupPlaceholderTag(true);
            setVisible(architecture.getMemoryControllerType() == MemoryControllerType.BASIC);

            add(new NumberTextField<Integer>("basicMemoryControllerToDramLatency"));
            add(new NumberTextField<Integer>("basicMemoryControllerFromDramLatency"));
            add(new NumberTextField<Integer>("basicMemoryControllerPrechargeLatency"));
            add(new NumberTextField<Integer>("basicMemoryControllerClosedLatency"));
            add(new NumberTextField<Integer>("basicMemoryControllerConflictLatency"));
            add(new NumberTextField<Integer>("basicMemoryControllerBusWidth"));
            add(new NumberTextField<Integer>("basicMemoryControllerNumBanks"));
            add(new NumberTextField<Integer>("basicMemoryControllerRowSize"));
        }};
        add(divBasicMemoryController);

        add(new DropDownChoice<MemoryControllerType>("memoryControllerType", Arrays.asList(MemoryControllerType.values())) {{
            add(new AjaxFormComponentUpdatingBehavior("onchange") {
                protected void onUpdate(AjaxRequestTarget target) {
                    divFixedLatencyMemoryController.setVisible(architecture.getMemoryControllerType() == MemoryControllerType.FIXED_LATENCY);
                    divSimpleMemoryController.setVisible(architecture.getMemoryControllerType() == MemoryControllerType.SIMPLE);
                    divBasicMemoryController.setVisible(architecture.getMemoryControllerType() == MemoryControllerType.BASIC);

                    target.add(divFixedLatencyMemoryController);
                    target.add(divSimpleMemoryController);
                    target.add(divBasicMemoryController);
                }
            });
        }});
    }
}
