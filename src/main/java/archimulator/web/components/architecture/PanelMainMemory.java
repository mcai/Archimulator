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
import archimulator.sim.uncore.dram.MainMemoryType;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import java.util.Arrays;

public class PanelMainMemory extends Panel {
    public PanelMainMemory(String id, final Architecture architecture) {
        super(id);

        final WebMarkupContainer divFixedLatencyMainMemory = new WebMarkupContainer("div_fixedLatencyMainMemory") {{
            setOutputMarkupPlaceholderTag(true);
            setVisible(architecture.getMainMemoryType() == MainMemoryType.FIXED_LATENCY);

            this.add(new NumberTextField<Integer>("input_fixedLatencyMainMemoryLatency", new PropertyModel<Integer>(architecture, "fixedLatencyMainMemoryLatency")));
        }};
        this.add(divFixedLatencyMainMemory);

        final WebMarkupContainer divSimpleMainMemory = new WebMarkupContainer("div_simpleMainMemory") {{
            setOutputMarkupPlaceholderTag(true);
            setVisible(architecture.getMainMemoryType() == MainMemoryType.SIMPLE);

            this.add(new NumberTextField<Integer>("input_simpleMainMemoryMemoryLatency", new PropertyModel<Integer>(architecture, "simpleMainMemoryMemoryLatency")));
            this.add(new NumberTextField<Integer>("input_simpleMainMemoryMemoryTrunkLatency", new PropertyModel<Integer>(architecture, "simpleMainMemoryMemoryTrunkLatency")));
            this.add(new NumberTextField<Integer>("input_simpleMainMemoryBusWidth", new PropertyModel<Integer>(architecture, "simpleMainMemoryBusWidth")));
        }};
        this.add(divSimpleMainMemory);

        final WebMarkupContainer divBasicMainMemory = new WebMarkupContainer("div_basicMainMemory") {{
            setOutputMarkupPlaceholderTag(true);
            setVisible(architecture.getMainMemoryType() == MainMemoryType.BASIC);

            this.add(new NumberTextField<Integer>("input_basicMainMemoryToDramLatency", new PropertyModel<Integer>(architecture, "basicMainMemoryToDramLatency")));
            this.add(new NumberTextField<Integer>("input_basicMainMemoryFromDramLatency", new PropertyModel<Integer>(architecture, "basicMainMemoryFromDramLatency")));
            this.add(new NumberTextField<Integer>("input_basicMainMemoryPrechargeLatency", new PropertyModel<Integer>(architecture, "basicMainMemoryPrechargeLatency")));
            this.add(new NumberTextField<Integer>("input_basicMainMemoryClosedLatency", new PropertyModel<Integer>(architecture, "basicMainMemoryClosedLatency")));
            this.add(new NumberTextField<Integer>("input_basicMainMemoryConflictLatency", new PropertyModel<Integer>(architecture, "basicMainMemoryConflictLatency")));
            this.add(new NumberTextField<Integer>("input_basicMainMemoryBusWidth", new PropertyModel<Integer>(architecture, "basicMainMemoryBusWidth")));
            this.add(new NumberTextField<Integer>("input_basicMainMemoryNumBanks", new PropertyModel<Integer>(architecture, "basicMainMemoryNumBanks")));
            this.add(new NumberTextField<Integer>("input_basicMainMemoryRowSize", new PropertyModel<Integer>(architecture, "basicMainMemoryRowSize")));
        }};
        this.add(divBasicMainMemory);

        this.add(new DropDownChoice<MainMemoryType>("select_mainMemoryType", new PropertyModel<MainMemoryType>(architecture, "mainMemoryType"), Arrays.asList(MainMemoryType.values())) {{
            add(new AjaxFormComponentUpdatingBehavior("onchange") {
                protected void onUpdate(AjaxRequestTarget target) {
                    divFixedLatencyMainMemory.setVisible(architecture.getMainMemoryType() == MainMemoryType.FIXED_LATENCY);
                    divSimpleMainMemory.setVisible(architecture.getMainMemoryType() == MainMemoryType.SIMPLE);
                    divBasicMainMemory.setVisible(architecture.getMainMemoryType() == MainMemoryType.BASIC);

                    target.add(divFixedLatencyMainMemory);
                    target.add(divSimpleMainMemory);
                    target.add(divBasicMainMemory);
                }
            });
        }});
    }
}
