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
package archimulator.web.component.experimentPack;

import archimulator.model.ExperimentPack;
import archimulator.model.ExperimentType;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;

import java.util.Arrays;

/**
 * General panel.
 *
 * @author Min Cai
 */
public class PanelGeneral extends Panel {
    /**
     * Create a general panel.
     *
     * @param id             the markup ID of the panel that is to be created
     * @param experimentPack the experiment pack
     */
    public PanelGeneral(String id, ExperimentPack experimentPack) {
        super(id);

        setDefaultModel(new CompoundPropertyModel<>(experimentPack));

        add(new Label("id"));
        add(new Label("title"));
        add(new Label("tags", Model.of(StringUtils.join(experimentPack.getTags(), ","))));

        add(new DropDownChoice<>("experimentType", Arrays.asList(ExperimentType.values())));

        add(new Label("createTimeAsString"));
    }
}
