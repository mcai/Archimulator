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
package archimulator.web.components.architecture;

import archimulator.model.Architecture;
import archimulator.sim.uncore.dram.MemoryControllerType;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import java.util.Arrays;

public class PanelMemoryController extends Panel {
    public PanelMemoryController(String id, final Architecture architecture) {
        super(id);

        final WebMarkupContainer divFixedLatencyMemoryController = new WebMarkupContainer("div_fixedLatencyMemoryController") {{
            setOutputMarkupPlaceholderTag(true);
            setVisible(architecture.getMemoryControllerType() == MemoryControllerType.FIXED_LATENCY);

            this.add(new NumberTextField<Integer>("input_fixedLatencyMemoryControllerLatency", new PropertyModel<Integer>(architecture, "fixedLatencyMemoryControllerLatency")));
        }};
        this.add(divFixedLatencyMemoryController);

        final WebMarkupContainer divSimpleMemoryController = new WebMarkupContainer("div_simpleMemoryController") {{
            setOutputMarkupPlaceholderTag(true);
            setVisible(architecture.getMemoryControllerType() == MemoryControllerType.SIMPLE);

            this.add(new NumberTextField<Integer>("input_simpleMemoryControllerMemoryLatency", new PropertyModel<Integer>(architecture, "simpleMemoryControllerMemoryLatency")));
            this.add(new NumberTextField<Integer>("input_simpleMemoryControllerMemoryTrunkLatency", new PropertyModel<Integer>(architecture, "simpleMemoryControllerMemoryTrunkLatency")));
            this.add(new NumberTextField<Integer>("input_simpleMemoryControllerBusWidth", new PropertyModel<Integer>(architecture, "simpleMemoryControllerBusWidth")));
        }};
        this.add(divSimpleMemoryController);

        final WebMarkupContainer divBasicMemoryController = new WebMarkupContainer("div_basicMemoryController") {{
            setOutputMarkupPlaceholderTag(true);
            setVisible(architecture.getMemoryControllerType() == MemoryControllerType.BASIC);

            this.add(new NumberTextField<Integer>("input_basicMemoryControllerToDramLatency", new PropertyModel<Integer>(architecture, "basicMemoryControllerToDramLatency")));
            this.add(new NumberTextField<Integer>("input_basicMemoryControllerFromDramLatency", new PropertyModel<Integer>(architecture, "basicMemoryControllerFromDramLatency")));
            this.add(new NumberTextField<Integer>("input_basicMemoryControllerPrechargeLatency", new PropertyModel<Integer>(architecture, "basicMemoryControllerPrechargeLatency")));
            this.add(new NumberTextField<Integer>("input_basicMemoryControllerClosedLatency", new PropertyModel<Integer>(architecture, "basicMemoryControllerClosedLatency")));
            this.add(new NumberTextField<Integer>("input_basicMemoryControllerConflictLatency", new PropertyModel<Integer>(architecture, "basicMemoryControllerConflictLatency")));
            this.add(new NumberTextField<Integer>("input_basicMemoryControllerBusWidth", new PropertyModel<Integer>(architecture, "basicMemoryControllerBusWidth")));
            this.add(new NumberTextField<Integer>("input_basicMemoryControllerNumBanks", new PropertyModel<Integer>(architecture, "basicMemoryControllerNumBanks")));
            this.add(new NumberTextField<Integer>("input_basicMemoryControllerRowSize", new PropertyModel<Integer>(architecture, "basicMemoryControllerRowSize")));
        }};
        this.add(divBasicMemoryController);

        this.add(new DropDownChoice<MemoryControllerType>("select_memoryControllerType", new PropertyModel<MemoryControllerType>(architecture, "memoryControllerType"), Arrays.asList(MemoryControllerType.values())) {{
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
