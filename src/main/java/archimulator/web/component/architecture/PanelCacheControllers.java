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
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyType;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;

import java.util.Arrays;

/**
 * Cache controllers panel.
 *
 * @author Min Cai
 */
public class PanelCacheControllers extends Panel {
    /**
     * Create a cache controllers panel.
     *
     * @param id           the markup ID of the panel that is to be created
     * @param architecture the architecture
     */
    public PanelCacheControllers(String id, Architecture architecture) {
        super(id);

        setDefaultModel(new CompoundPropertyModel<>(architecture));

        add(new RequiredTextField<String>("l1ISizeInStorageUnit"));
        add(new NumberTextField<Integer>("l1IAssociativity"));
        add(new NumberTextField<Integer>("l1ILineSize"));
        add(new NumberTextField<Integer>("l1IHitLatency"));
        add(new NumberTextField<Integer>("l1INumReadPorts"));
        add(new NumberTextField<Integer>("l1INumWritePorts"));
        add(new DropDownChoice<>("l1IReplacementPolicyType", Arrays.asList(CacheReplacementPolicyType.values())));

        add(new RequiredTextField<String>("l1DSizeInStorageUnit"));
        add(new NumberTextField<Integer>("l1DAssociativity"));
        add(new NumberTextField<Integer>("l1DLineSize"));
        add(new NumberTextField<Integer>("l1DHitLatency"));
        add(new NumberTextField<Integer>("l1DNumReadPorts"));
        add(new NumberTextField<Integer>("l1DNumWritePorts"));
        add(new DropDownChoice<>("l1DReplacementPolicyType", Arrays.asList(CacheReplacementPolicyType.values())));

        add(new RequiredTextField<String>("l2SizeInStorageUnit"));
        add(new NumberTextField<Integer>("l2Associativity"));
        add(new NumberTextField<Integer>("l2LineSize"));
        add(new NumberTextField<Integer>("l2HitLatency"));
        add(new DropDownChoice<>("l2ReplacementPolicyType", Arrays.asList(CacheReplacementPolicyType.values())));
    }
}
