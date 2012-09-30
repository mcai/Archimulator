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
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

public class PanelPipelines extends Panel {
    public PanelPipelines(String id, Architecture architecture) {
        super(id);

        this.add(new NumberTextField<Integer>("input_physical_register_file_capacity", new PropertyModel<Integer>(architecture, "physicalRegisterFileCapacity")));
        this.add(new NumberTextField<Integer>("input_decode_width", new PropertyModel<Integer>(architecture, "decodeWidth")));
        this.add(new NumberTextField<Integer>("input_issue_width", new PropertyModel<Integer>(architecture, "issueWidth")));
        this.add(new NumberTextField<Integer>("input_commit_width", new PropertyModel<Integer>(architecture, "commitWidth")));
        this.add(new NumberTextField<Integer>("input_decode_buffer_capacity", new PropertyModel<Integer>(architecture, "decodeBufferCapacity")));
        this.add(new NumberTextField<Integer>("input_reorder_buffer_capacity", new PropertyModel<Integer>(architecture, "reorderBufferCapacity")));
        this.add(new NumberTextField<Integer>("input_load_store_queue_capacity", new PropertyModel<Integer>(architecture, "loadStoreQueueCapacity")));
    }
}
