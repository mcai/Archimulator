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
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Authenticated base page.
 *
 * @author Min Cai
 */
public abstract class AuthenticatedBasePage extends BasePage {
    /**
     * Create an authenticated base page.
     *
     * @param parameters the page parameters
     */
    public AuthenticatedBasePage(PageParameters parameters) {
        super(parameters);
    }

    /**
     * Get the Archimulator session.
     *
     * @return the Archimulator session
     */
    public ArchimulatorSession getArchimulatorSession() {
        return (ArchimulatorSession) getSession();
    }
}
