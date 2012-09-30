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
import de.agilecoders.wicket.markup.html.bootstrap.behavior.BootstrapResourcesBehavior;
import de.agilecoders.wicket.markup.html.bootstrap.html.ChromeFrameMetaTag;
import de.agilecoders.wicket.markup.html.bootstrap.html.HtmlTag;
import de.agilecoders.wicket.markup.html.bootstrap.html.MetaTag;
import de.agilecoders.wicket.markup.html.bootstrap.html.OptimizedMobileViewportMetaTag;
import de.agilecoders.wicket.markup.html.bootstrap.image.Icon;
import de.agilecoders.wicket.markup.html.bootstrap.image.IconType;
import de.agilecoders.wicket.markup.html.bootstrap.navbar.AffixBehavior;
import de.agilecoders.wicket.markup.html.bootstrap.navbar.Navbar;
import de.agilecoders.wicket.markup.html.bootstrap.navbar.NavbarButton;
import org.apache.wicket.Component;
import org.apache.wicket.PageReference;
import org.apache.wicket.Session;
import org.apache.wicket.devutils.stateless.StatelessComponent;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.GenericWebPage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

@StatelessComponent
public abstract class BasePage<T> extends GenericWebPage<T> {
    private String title = "Archimulator";

    public BasePage(PageParameters parameters) {
        super(parameters);

        add(new HtmlTag("html"));

        add(new Label("title", new PropertyModel<String>(this, "title")));

        add(new OptimizedMobileViewportMetaTag("viewport"));
        add(new ChromeFrameMetaTag("chrome-frame"));
        add(new MetaTag("description", Model.of("description"), Model.of("Apache Wicket & Twitter Bootstrap Demo")));
        add(new MetaTag("author", Model.of("author"), Model.of("Michael Haitz <michael.haitz@agile-coders.de>")));

        add(newNavbar("navbar"));
        add(newNavigation("navigation"));

        add(new WebMarkupContainer("div_nav_sign_in") {{
            setVisible(!ArchimulatorSession.get().isSignedIn());
        }});

        add(new WebMarkupContainer("div_nav_sign_out") {{
            add(new Label("button_nav_user", new PropertyModel<String>(BasePage.this, "userName")) {{
                setVisible(ArchimulatorSession.get().isSignedIn());
            }});

            setVisible(ArchimulatorSession.get().isSignedIn());
        }});

        add(new BootstrapResourcesBehavior());
    }

    public String getUserName() {
        return getSession() != null ? "Logout as: " + ((ArchimulatorSession) getSession()).getUser().getEmail() : null;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    protected void back(PageParameters parameters, Class<? extends IRequestablePage> defaultPageClz) {
        int backPageId = parameters.get("back_page_id").toInt(-1);
        if (backPageId != -1 && Session.get().getPageManager().getPage(backPageId) != null) {
            setResponsePage(new PageReference(backPageId).getPage());
        } else {
            setResponsePage(defaultPageClz);
        }
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
//        new StatelessChecker().onBeforeRender(this);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
//        response.render(CssHeaderItem.forReference(FixBootstrapStylesCssResourceReference.INSTANCE));
    }

    protected Navbar newNavbar(String markupId) {
        Navbar navbar = new Navbar(markupId);

        navbar.setPosition(Navbar.Position.TOP);
        navbar.brandName(Model.of("Archimulator"));

        navbar.invert(false);

        navbar.addButton(Navbar.ButtonPosition.LEFT,
                new NavbarButton<HomePage>(HomePage.class, Model.of("Home")).setIcon(new Icon(IconType.Home)),
                new NavbarButton<BenchmarksPage>(BenchmarksPage.class, Model.of("Benchmarks")),
                new NavbarButton<ArchitecturesPage>(ArchitecturesPage.class, Model.of("Architectures")),
                new NavbarButton<ExperimentsPage>(ExperimentsPage.class, Model.of("Experiments")),
                new NavbarButton<ExperimentPacksPage>(ExperimentPacksPage.class, Model.of("Experiment Packs")),
                new NavbarButton<HelpPage>(HelpPage.class, Model.of("Help"))
        );

        return navbar;
    }

    protected boolean hasNavigation() {
        return false;
    }

    private Component newNavigation(String markupId) {
        WebMarkupContainer navigation = new WebMarkupContainer(markupId);
        navigation.add(new AffixBehavior("200"));
        navigation.setVisible(hasNavigation());

        return navigation;
    }
}
