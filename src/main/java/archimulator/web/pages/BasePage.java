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

import archimulator.web.application.ArchimulatorSession;
import de.agilecoders.wicket.markup.html.bootstrap.behavior.CssClassNameAppender;
import org.apache.wicket.PageReference;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.GenericWebPage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public abstract class BasePage<T> extends GenericWebPage<T> {
    private String title = "Archimulator";

    public BasePage(final PageType pageType, PageParameters parameters) {
        super(parameters);

        add(new Label("title", new PropertyModel<String>(this, "title")));

        add(new ListItem("li_nav_home", 0) {{
            if (pageType == PageType.HOME) {
                add(new CssClassNameAppender("active"));
            }
        }});

        add(new ListItem("li_nav_benchmarks", 1){{
            if (pageType == PageType.BENCHMARKS) {
                add(new CssClassNameAppender("active"));
            }
        }});

        add(new ListItem("li_nav_architectures", 2){{
            if (pageType == PageType.ARCHITECTURES) {
                add(new CssClassNameAppender("active"));
            }
        }});

        add(new ListItem("li_nav_experiments", 3){{
            if (pageType == PageType.EXPERIMENTS) {
                add(new CssClassNameAppender("active"));
            }
        }});

        add(new ListItem("li_nav_experiment_packs", 4){{
            if (pageType == PageType.EXPERIMENT_PACKS) {
                add(new CssClassNameAppender("active"));
            }
        }});

        add(new ListItem("li_nav_help", 5){{
            if (pageType == PageType.HELP) {
                add(new CssClassNameAppender("active"));
            }
        }});

        add(new WebMarkupContainer("div_nav_sign_in"){{
            setVisible(!ArchimulatorSession.get().isSignedIn());
        }});

        add(new WebMarkupContainer("div_nav_sign_out") {{
            add(new Label("button_nav_user", new PropertyModel<String>(BasePage.this, "userName")) {{
                setVisible(ArchimulatorSession.get().isSignedIn());
            }});

            setVisible(ArchimulatorSession.get().isSignedIn());
        }});
    }

    public String getUserName() {
        return getSession() != null ? "Logout as: " + ((ArchimulatorSession)getSession()).getUser().getEmail() : null;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    protected void back(PageParameters parameters, Class<? extends IRequestablePage> defaultPageClz) {
        int backPageId = parameters.get("back_page_id").toInt(-1);
        if(backPageId != -1 && Session.get().getPageManager().getPage(backPageId) != null) {
            setResponsePage(new PageReference(backPageId).getPage());
        }
        else {
            setResponsePage(defaultPageClz);
        }
    }
}
