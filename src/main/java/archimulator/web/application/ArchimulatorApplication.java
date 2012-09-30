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
package archimulator.web.application;

import archimulator.web.pages.AuthenticatedBasePage;
import archimulator.web.pages.HomePage;
import archimulator.web.pages.SignInPage;
import de.agilecoders.wicket.Bootstrap;
import de.agilecoders.wicket.settings.BootstrapSettings;
import de.agilecoders.wicket.settings.BootswatchThemeProvider;
import de.agilecoders.wicket.settings.ThemeProvider;
import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.Session;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.authorization.strategies.page.SimplePageAuthorizationStrategy;
import org.apache.wicket.markup.html.IPackageResourceGuard;
import org.apache.wicket.markup.html.SecurePackageResourceGuard;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.wicketstuff.annotation.scan.AnnotatedMountScanner;

public class ArchimulatorApplication extends WebApplication {
    public static ArchimulatorApplication get() {
        return (ArchimulatorApplication) Application.get();
    }

    public ArchimulatorApplication() {
//        setConfigurationType(RuntimeConfigurationType.DEVELOPMENT);
        setConfigurationType(RuntimeConfigurationType.DEPLOYMENT);
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

        getMarkupSettings().setStripWicketTags(true);

        IPackageResourceGuard packageResourceGuard = getResourceSettings().getPackageResourceGuard();
        if (packageResourceGuard instanceof SecurePackageResourceGuard) {
            SecurePackageResourceGuard guard = (SecurePackageResourceGuard) packageResourceGuard;
            guard.addPattern("+*.woff");
            guard.addPattern("+*.ttf");
            guard.addPattern("+*.svg");
        }

        configureBootstrap();

        new AnnotatedMountScanner().scanPackage("archimulator.web.pages").mount(this);

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
        BootstrapSettings settings = new BootstrapSettings();
        settings.minify(true)
                .useJqueryPP(true)
                .useModernizr(true)
                .useResponsiveCss(true)
                .getBootstrapLessCompilerSettings().setUseLessCompiler(true);

        ThemeProvider themeProvider = new BootswatchThemeProvider() {{
        }};
        settings.setThemeProvider(themeProvider);

        Bootstrap.install(this, settings);
    }
}
