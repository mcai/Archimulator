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
package archimulator.web.application;

import archimulator.web.page.AuthenticatedBasePage;
import archimulator.web.page.BasePage;
import archimulator.web.page.HomePage;
import archimulator.web.page.SignInPage;
import de.agilecoders.wicket.Bootstrap;
import de.agilecoders.wicket.markup.html.bootstrap.extensions.html5player.Html5PlayerCssReference;
import de.agilecoders.wicket.markup.html.bootstrap.extensions.html5player.Html5PlayerJavaScriptReference;
import de.agilecoders.wicket.markup.html.bootstrap.extensions.icon.OpenWebIconsCssReference;
import de.agilecoders.wicket.markup.html.bootstrap.extensions.jqueryui.JQueryUIJavaScriptReference;
import de.agilecoders.wicket.markup.html.references.BootstrapPrettifyJavaScriptReference;
import de.agilecoders.wicket.markup.html.references.ModernizrJavaScriptReference;
import de.agilecoders.wicket.markup.html.themes.google.GoogleTheme;
import de.agilecoders.wicket.markup.html.themes.metro.MetroTheme;
import de.agilecoders.wicket.markup.html.themes.wicket.WicketTheme;
import de.agilecoders.wicket.settings.BootstrapSettings;
import de.agilecoders.wicket.settings.BootswatchThemeProvider;
import de.agilecoders.wicket.settings.ThemeProvider;
import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.Session;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.authorization.strategies.page.SimplePageAuthorizationStrategy;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.wicketstuff.annotation.scan.AnnotatedMountScanner;

/**
 * Archimulator application.
 *
 * @author Min Cai
 */
public class ArchimulatorApplication extends WebApplication {
    /**
     * Get the Archimulator application singleton.
     *
     * @return the Archimulator application singleton
     */
    public static ArchimulatorApplication get() {
        return (ArchimulatorApplication) Application.get();
    }

    /**
     * Create an Archimulator application.
     */
    public ArchimulatorApplication() {
        setConfigurationType(RuntimeConfigurationType.DEVELOPMENT);
//        setConfigurationType(RuntimeConfigurationType.DEPLOYMENT);
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return HomePage.class;
    }

    @Override
    public Session newSession(Request request, Response response) {
        return new ArchimulatorSession(request);
    }

    @Override
    public void init() {
        super.init();

//        getMarkupSettings().setStripWicketTags(true);

        getDebugSettings().setAjaxDebugModeEnabled(false);

        configureBootstrap();
        configureResourceBundles();

        new AnnotatedMountScanner().scanPackage(BasePage.class.getPackage().getName()).mount(this);

        IAuthorizationStrategy authorizationStrategy = new SimplePageAuthorizationStrategy(
                AuthenticatedBasePage.class, SignInPage.class) {
            @Override
            protected boolean isAuthorized() {
                return (((ArchimulatorSession) Session.get()).isSignedIn());
            }
        };
        getSecuritySettings().setAuthorizationStrategy(authorizationStrategy);
    }

    private void configureBootstrap() {
        final ThemeProvider themeProvider = new BootswatchThemeProvider() {{
            add(new MetroTheme());
            add(new GoogleTheme());
            add(new WicketTheme());
//            defaultTheme("wicket");
        }};

        final BootstrapSettings settings = new BootstrapSettings();
        settings.setJsResourceFilterName("footer-container")
                .setThemeProvider(themeProvider);
        Bootstrap.install(this, settings);
    }

    private void configureResourceBundles() {
        getResourceBundles().addJavaScriptBundle(ArchimulatorApplication.class, "core.js",
                (JavaScriptResourceReference) getJavaScriptLibrarySettings().getJQueryReference(),
                (JavaScriptResourceReference) getJavaScriptLibrarySettings().getWicketEventReference(),
                (JavaScriptResourceReference) getJavaScriptLibrarySettings().getWicketAjaxReference(),
                (JavaScriptResourceReference) ModernizrJavaScriptReference.INSTANCE
        );

        getResourceBundles().addJavaScriptBundle(ArchimulatorApplication.class, "bootstrap.js",
                (JavaScriptResourceReference) Bootstrap.getSettings().getJsResourceReference(),
                (JavaScriptResourceReference) BootstrapPrettifyJavaScriptReference.INSTANCE
        );

        getResourceBundles().addJavaScriptBundle(ArchimulatorApplication.class, "bootstrap-extensions.js",
                JQueryUIJavaScriptReference.instance(),
                Html5PlayerJavaScriptReference.instance()
        );

        getResourceBundles().addCssBundle(ArchimulatorApplication.class, "bootstrap-extensions.css",
                Html5PlayerCssReference.instance(),
                OpenWebIconsCssReference.instance()
        );
    }
}
