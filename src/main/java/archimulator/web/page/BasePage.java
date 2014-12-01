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

import archimulator.web.application.ArchimulatorSession;
import net.pickapack.web.resource.D3;
import net.pickapack.web.resource.NVD3;
import de.agilecoders.wicket.markup.html.bootstrap.behavior.BootstrapResourcesBehavior;
import de.agilecoders.wicket.markup.html.bootstrap.html.ChromeFrameMetaTag;
import de.agilecoders.wicket.markup.html.bootstrap.html.MetaTag;
import de.agilecoders.wicket.markup.html.bootstrap.html.OptimizedMobileViewportMetaTag;
import de.agilecoders.wicket.markup.html.bootstrap.image.IconType;
import de.agilecoders.wicket.markup.html.bootstrap.navbar.INavbarComponent;
import de.agilecoders.wicket.markup.html.bootstrap.navbar.ImmutableNavbarComponent;
import de.agilecoders.wicket.markup.html.bootstrap.navbar.Navbar;
import de.agilecoders.wicket.markup.html.bootstrap.navbar.NavbarButton;
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

import java.util.ArrayList;

/**
 * Base page.
 *
 * @param <T> the type of the page's model object
 * @author Min Cai
 */
@StatelessComponent
public abstract class BasePage<T> extends GenericWebPage<T> {
    private String title = "Archimulator";

    /**
     * Create a base page.
     *
     * @param parameters the page parameters
     */
    public BasePage(PageParameters parameters) {
        super(parameters);

        add(new Label("title", new PropertyModel<String>(this, "title")));

        add(new OptimizedMobileViewportMetaTag("viewport"));
        add(new ChromeFrameMetaTag("chrome-frame"));
        add(new MetaTag("description", Model.of("description"), Model.of("Archimulator")));
        add(new MetaTag("author", Model.of("author"), Model.of("Min Cai <min.cai.china@gmail.com>")));

        add(new Navbar("navbar") {{
            setPosition(Position.TOP);
            brandName(Model.of("Archimulator"));
            setInverted(true);
            fluid();

            addComponents(new ArrayList<INavbarComponent>() {{
                add(new ImmutableNavbarComponent(new NavbarButton<HomePage>(HomePage.class, Model.of("Home")).setIconType(IconType.home)));
                add(new ImmutableNavbarComponent(new NavbarButton<BenchmarksPage>(BenchmarksPage.class, Model.of("Benchmarks"))));
                add(new ImmutableNavbarComponent(new NavbarButton<ArchitecturesPage>(ArchitecturesPage.class, Model.of("Architectures"))));
                add(new ImmutableNavbarComponent(new NavbarButton<ExperimentPacksPage>(ExperimentPacksPage.class, Model.of("Experiments"))));
                add(new ImmutableNavbarComponent(new NavbarButton<HelpPage>(HelpPage.class, Model.of("Help"))));
            }});
        }});

        add(new WebMarkupContainer("navSignIn") {{
            setVisible(!ArchimulatorSession.get().isSignedIn());
        }});

        add(new WebMarkupContainer("navSignOut") {{
            add(new Label("userName", new PropertyModel<String>(BasePage.this, "userName")) {{
                setVisible(ArchimulatorSession.get().isSignedIn());
            }});

            setVisible(ArchimulatorSession.get().isSignedIn());
        }});

        add(new BootstrapResourcesBehavior());
    }

    /**
     * Get the user name.
     *
     * @return the user name
     */
    public String getUserName() {
        return getSession() != null ? "Logout as: " + ((ArchimulatorSession) getSession()).getUser().getEmail() : null;
    }

    /**
     * Get the title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the title.
     *
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Navigate back.
     *
     * @param parameters     the page parameters
     * @param defaultPageClz the default page class
     */
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

        D3.renderHead(response);
        NVD3.renderHead(response);

//        response.render(CssHeaderItem.forReference(FixBootstrapStylesCssResourceReference.INSTANCE));
    }
}
