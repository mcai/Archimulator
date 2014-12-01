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

import archimulator.model.User;
import archimulator.service.ServiceManager;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.Request;

/**
 * Archimulator session.
 *
 * @author Min Cai
 */
public class ArchimulatorSession extends AuthenticatedWebSession {
    private User user;

    /**
     * Create an Archimulator session.
     *
     * @param request the request
     */
    public ArchimulatorSession(Request request) {
        super(request);
    }

    @Override
    public Roles getRoles() {
        return null;
    }

    @Override
    public boolean authenticate(String username, String password) {
        User userFound = ServiceManager.getUserService().getUserByEmail(username);

        if (userFound != null && userFound.getPassword().equals(password)) {
            user = userFound;
            return true;
        }

        return false;
    }

    @Override
    public void signOut() {
        super.signOut();
        user = null;
    }

    /**
     * Get the user.
     *
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * Set the user.
     *
     * @param user the user
     */
    public void setUser(User user) {
        this.user = user;
    }
}
