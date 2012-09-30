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
package archimulator.web.components.experimentPack;

import archimulator.model.ExperimentPack;
import archimulator.model.ExperimentType;
import net.pickapack.dateTime.DateHelper;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import java.util.Arrays;

public class PanelGeneral extends Panel {
    public PanelGeneral(String id, ExperimentPack experimentPack) {
        super(id);

        add(new TextField<String>("input_id", Model.of(experimentPack.getId() + "")));
        add(new TextField<String>("input_title", Model.of(experimentPack.getTitle())));

        add(new DropDownChoice<ExperimentType>("select_type", new PropertyModel<ExperimentType>(experimentPack, "experimentType"), Arrays.asList(ExperimentType.values())));

        add(new TextField<String>("input_create_time", Model.of(DateHelper.toString(experimentPack.getCreateTime()))));
    }
}
