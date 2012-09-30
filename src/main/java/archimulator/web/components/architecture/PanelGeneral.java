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
import net.pickapack.dateTime.DateHelper;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

public class PanelGeneral extends Panel {
    public PanelGeneral(String id, Architecture architecture) {
        super(id);

        this.add(new TextField<String>("input_id", Model.of(architecture.getId() + "")));
        this.add(new TextField<String>("input_title", Model.of(architecture.getTitle())));

        this.add(new NumberTextField<Integer>("input_num_cores", new PropertyModel<Integer>(architecture, "numCores")));
        this.add(new NumberTextField<Integer>("input_num_threads_per_core", new PropertyModel<Integer>(architecture, "numThreadsPerCore")));

        this.add(new NumberTextField<Integer>("input_helperThreadPthreadSpawnIndex", new PropertyModel<Integer>(architecture, "helperThreadPthreadSpawnIndex")));
        this.add(new CheckBox(
                "input_ht_llc_request_profiling_enabled",
                new PropertyModel<Boolean>(architecture, "helperThreadL2CacheRequestProfilingEnabled")));

        this.add(new TextField<String>("input_create_time", Model.of(DateHelper.toString(architecture.getCreateTime()))));
    }
}
