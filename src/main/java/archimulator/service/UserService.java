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

import archimulator.model.User;
import net.pickapack.service.Service;

import java.util.List;

/**
 * Service for managing users.
 *
 * @author Min Cai
 */
public interface UserService extends Service {
    /**
     * Get all the users.
     *
     * @return all the users
     */
    List<User> getAllUsers();

    /**
     * Get a user by ID.
     *
     * @param id the user's ID
     * @return the user matching the ID if any exists; otherwise null
     */
    User getUserById(long id);

    /**
     * Get a user by email.
     *
     * @param email the user's email
     * @return the user matching the email if any exists; otherwise null
     */
    User getUserByEmail(String email);

    /**
     * Get the first user.
     *
     * @return the first user if any exists; otherwise null
     */
    User getFirstUser();

    /**
     * Add a user.
     *
     * @param user the user that is to be added
     */
    void addUser(User user);

    /**
     * Remove a user by ID.
     *
     * @param id the ID of the user that is to be removed
     */
    void removeUserById(long id);

    /**
     * Update a user.
     *
     * @param user the user that is to be updated
     */
    void updateUser(User user);

    /**
     * Initialize the service.
     */
    void initialize();
}
