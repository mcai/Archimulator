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
package archimulator.web.pages;

import archimulator.model.ExperimentPack;
import archimulator.service.ServiceManager;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

@MountPath(value = "/", alt = "/experiment_pack")
public class ExperimentPackPage extends AuthenticatedWebPage {
    public ExperimentPackPage(PageParameters parameters) {
        super(PageType.SIMULATED_PROGRAM, parameters);

        String experimentPackTitle = parameters.get("experiment_pack_title").toString();
        ExperimentPack experimentPack = ServiceManager.getExperimentService().getExperimentPackByTitle(experimentPackTitle);

        if(experimentPack == null) {
            setResponsePage(HomePage.class);
            return;
        }

        this.add(new TextField<String>("input_title", Model.of(experimentPack.getTitle())));
    }
}
