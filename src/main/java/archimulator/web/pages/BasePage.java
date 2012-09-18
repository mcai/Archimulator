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

import archimulator.model.User;
import archimulator.web.ArchimulatorSession;
import archimulator.web.assets.base.FixBootstrapStylesCssResourceReference;
import archimulator.web.components.Footer;
import de.agilecoders.wicket.markup.html.bootstrap.behavior.BootstrapBaseBehavior;
import de.agilecoders.wicket.markup.html.bootstrap.behavior.CssClassNameAppender;
import de.agilecoders.wicket.markup.html.bootstrap.html.ChromeFrameMetaTag;
import de.agilecoders.wicket.markup.html.bootstrap.html.HtmlTag;
import de.agilecoders.wicket.markup.html.bootstrap.html.MetaTag;
import de.agilecoders.wicket.markup.html.bootstrap.html.OptimizedMobileViewportMetaTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.GenericWebPage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public abstract class BasePage<T> extends GenericWebPage<T> {
    private PageType pageType;

    public BasePage(PageType pageType, PageParameters parameters) {
        super(parameters);

        this.pageType = pageType;

        add(new HtmlTag("html"));

        add(new OptimizedMobileViewportMetaTag("viewport"));
        add(new ChromeFrameMetaTag("chrome-frame"));
        add(new MetaTag("description", Model.of("description"), Model.of("Archimulator")));
        add(new MetaTag("author", Model.of("author"), Model.of("Min Cai <min.cai.china@gmail.com>")));

        ListItem listItemNavHome = new ListItem("li_nav_home", 0);
        ListItem listItemNavSimulatedPrograms = new ListItem("li_nav_simulated_programs", 1);
        ListItem listItemNavArchitectures = new ListItem("li_nav_architectures", 2);
        ListItem listItemNavExperiments = new ListItem("li_nav_experiments", 3);
        ListItem listItemNavFaq = new ListItem("li_nav_faq", 4);

        if (this.pageType == PageType.HOME) {
            listItemNavHome.add(new CssClassNameAppender("active"));
        }

        if (this.pageType == PageType.SIMULATED_PROGRAMS) {
            listItemNavSimulatedPrograms.add(new CssClassNameAppender("active"));
        }

        if (this.pageType == PageType.ARCHITECTURES) {
            listItemNavArchitectures.add(new CssClassNameAppender("active"));
        }

        if (this.pageType == PageType.EXPERIMENTS) {
            listItemNavExperiments.add(new CssClassNameAppender("active"));
        }

        if (this.pageType == PageType.FAQ) {
            listItemNavFaq.add(new CssClassNameAppender("active"));
        }

        add(listItemNavHome);
        add(listItemNavSimulatedPrograms);
        add(listItemNavArchitectures);
        add(listItemNavExperiments);
        add(listItemNavFaq);

        WebMarkupContainer divNavSignIn = new WebMarkupContainer("div_nav_sign_in");
        add(divNavSignIn);
        divNavSignIn.setVisible(!ArchimulatorSession.get().isSignedIn());

        WebMarkupContainer divNavSignUp = new WebMarkupContainer("div_nav_sign_up");
        add(divNavSignUp);
        divNavSignUp.setVisible(!ArchimulatorSession.get().isSignedIn());

        WebMarkupContainer divNavUser = new WebMarkupContainer("div_nav_user");
        add(divNavUser);
        divNavUser.setVisible(ArchimulatorSession.get().isSignedIn());

        Label buttonNavUser = new Label("button_nav_user", new PropertyModel<User>(this, "session.user.name"));
        divNavUser.add(buttonNavUser);
        buttonNavUser.setVisible(ArchimulatorSession.get().isSignedIn());

        WebMarkupContainer divNavSignOut = new WebMarkupContainer("div_nav_sign_out");
        add(divNavSignOut);
        divNavSignOut.setVisible(ArchimulatorSession.get().isSignedIn());

        add(new Footer("footer"));

        add(new BootstrapBaseBehavior());
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(FixBootstrapStylesCssResourceReference.INSTANCE));
    }

    public PageType getPageType() {
        return pageType;
    }
}
