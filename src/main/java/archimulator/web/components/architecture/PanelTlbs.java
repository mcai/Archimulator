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
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

public class PanelTlbs extends Panel {
    public PanelTlbs(String id, Architecture architecture) {
        super(id);

        this.add(new RequiredTextField<String>("input_tlb_size", new PropertyModel<String>(architecture, "tlbSizeInStorageUnit")));
        this.add(new NumberTextField<Integer>("input_tlb_associativity", new PropertyModel<Integer>(architecture, "tlbAssociativity")));
        this.add(new NumberTextField<Integer>("input_tlb_line_size", new PropertyModel<Integer>(architecture, "tlbLineSize")));
        this.add(new NumberTextField<Integer>("input_tlb_hit_latency", new PropertyModel<Integer>(architecture, "tlbHitLatency")));
        this.add(new NumberTextField<Integer>("input_tlb_miss_latency", new PropertyModel<Integer>(architecture, "tlbMissLatency")));
    }
}
