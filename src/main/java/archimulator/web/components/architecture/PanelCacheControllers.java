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
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyType;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import java.util.Arrays;

public class PanelCacheControllers extends Panel {
    public PanelCacheControllers(String id, Architecture architecture) {
        super(id);

        this.add(new RequiredTextField<String>("input_l1I_size", new PropertyModel<String>(architecture, "l1ISizeInStorageUnit")));
        this.add(new NumberTextField<Integer>("input_l1I_associativity", new PropertyModel<Integer>(architecture, "l1IAssociativity")));
        this.add(new NumberTextField<Integer>("input_l1I_line_size", new PropertyModel<Integer>(architecture, "l1ILineSize")));
        this.add(new NumberTextField<Integer>("input_l1I_hit_latency", new PropertyModel<Integer>(architecture, "l1IHitLatency")));
        this.add(new NumberTextField<Integer>("input_l1INumReadPorts", new PropertyModel<Integer>(architecture, "l1INumReadPorts")));
        this.add(new NumberTextField<Integer>("input_l1INumWritePorts", new PropertyModel<Integer>(architecture, "l1INumWritePorts")));
        this.add(new DropDownChoice<CacheReplacementPolicyType>(
                "select_l1I_repl",
                new PropertyModel<CacheReplacementPolicyType>(architecture, "l1IReplacementPolicyType"),
                Arrays.asList(CacheReplacementPolicyType.values())));

        this.add(new RequiredTextField<String>("input_l1D_size", new PropertyModel<String>(architecture, "l1DSizeInStorageUnit")));
        this.add(new NumberTextField<Integer>("input_l1D_associativity", new PropertyModel<Integer>(architecture, "l1DAssociativity")));
        this.add(new NumberTextField<Integer>("input_l1D_line_size", new PropertyModel<Integer>(architecture, "l1DLineSize")));
        this.add(new NumberTextField<Integer>("input_l1D_hit_latency", new PropertyModel<Integer>(architecture, "l1DHitLatency")));
        this.add(new NumberTextField<Integer>("input_l1DNumReadPorts", new PropertyModel<Integer>(architecture, "l1DNumReadPorts")));
        this.add(new NumberTextField<Integer>("input_l1DNumWritePorts", new PropertyModel<Integer>(architecture, "l1DNumWritePorts")));
        this.add(new DropDownChoice<CacheReplacementPolicyType>(
                "select_l1D_repl",
                new PropertyModel<CacheReplacementPolicyType>(architecture, "l1DReplacementPolicyType"),
                Arrays.asList(CacheReplacementPolicyType.values())));

        this.add(new RequiredTextField<String>("input_l2_size", new PropertyModel<String>(architecture, "l2SizeInStorageUnit")));
        this.add(new NumberTextField<Integer>("input_l2_associativity", new PropertyModel<Integer>(architecture, "l2Associativity")));
        this.add(new NumberTextField<Integer>("input_l2_line_size", new PropertyModel<Integer>(architecture, "l2LineSize")));
        this.add(new NumberTextField<Integer>("input_l2_hit_latency", new PropertyModel<Integer>(architecture, "l2HitLatency")));
        this.add(new DropDownChoice<CacheReplacementPolicyType>(
                "select_l2_repl",
                new PropertyModel<CacheReplacementPolicyType>(architecture, "l2ReplacementPolicyType"),
                Arrays.asList(CacheReplacementPolicyType.values())));
    }
}
