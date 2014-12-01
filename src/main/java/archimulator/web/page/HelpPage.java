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
package archimulator.web.page;

import archimulator.web.component.help.PanelFeatures;
import archimulator.web.component.help.PanelResources;
import archimulator.web.component.help.PanelSystemStatus;
import archimulator.web.component.help.PanelUsages;
import de.agilecoders.wicket.markup.html.bootstrap.tabs.BootstrapTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

import java.util.ArrayList;

/**
 * Help page.
 *
 * @author Min Cai
 */
@MountPath(value = "/help")
public class HelpPage extends BasePage {
    /**
     * Create a help page.
     *
     * @param parameters the page parameters
     */
    public HelpPage(PageParameters parameters) {
        super(parameters);

        setTitle("Help - Archimulator");

        add(new BootstrapTabbedPanel<>("tabs", new ArrayList<ITab>() {{
            add(new AbstractTab(new Model<>("System Status")) {
                public Panel getPanel(String panelId) {
                    return new PanelSystemStatus(panelId);
                }
            });

            add(new AbstractTab(new Model<>("Features")) {
                public Panel getPanel(String panelId) {
                    return new PanelFeatures(panelId);
                }
            });

            add(new AbstractTab(new Model<>("Usages")) {
                public Panel getPanel(String panelId) {
                    return new PanelUsages(panelId);
                }
            });

            add(new AbstractTab(new Model<>("Resources")) {
                public Panel getPanel(String panelId) {
                    return new PanelResources(panelId);
                }
            });
        }}));
    }
}
