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
package archimulator.service;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ArchimulatorServletContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ArchimulatorService archimulatorService = new ArchimulatorServiceImpl();
        sce.getServletContext().setAttribute("archimulatorService", archimulatorService);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        getArchimulatorService(sce.getServletContext()).stop();
        sce.getServletContext().removeAttribute("archimulatorService");
    }

    @SuppressWarnings("unchecked")
    public static ArchimulatorService getArchimulatorService(ServletContext context) {
        return (ArchimulatorService) context.getAttribute("archimulatorService");
    }
}
