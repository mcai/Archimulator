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

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;

public class ArchimulatorWebServer {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            return;
        }

        Server server = new Server();

        WebAppContext app = new WebAppContext();
        app.setContextPath("/");

        app.setWar(args[0]);
        app.setExtractWAR(false);
        app.setConfigurationClasses(new String[] { WebInfConfiguration.class.getName(), WebXmlConfiguration.class.getName() });
        app.setParentLoaderPriority(true);

        Connector connector = new SocketConnector();
        connector.setPort(Integer.parseInt(args[1]));
        connector.setMaxIdleTime(60000);

        server.setConnectors(new Connector[] { connector });
        server.setHandler(app);
        server.setAttribute("org.mortbay.jetty.Request.maxFormContentSize", 0);
        server.setStopAtShutdown(true);

        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
