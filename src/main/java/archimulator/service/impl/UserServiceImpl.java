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
package archimulator.service.impl;

import archimulator.model.User;
import archimulator.service.ServiceManager;
import archimulator.service.UserService;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import net.pickapack.model.WithId;
import net.pickapack.service.AbstractService;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Min Cai
 */
public class UserServiceImpl extends AbstractService implements UserService {
    private Dao<User, Long> users;

    /**
     *
     */
    @SuppressWarnings("unchecked")
    public UserServiceImpl() {
        super(ServiceManager.getDatabaseUrl(), Arrays.<Class<? extends WithId>>asList(User.class));

        this.users = createDao(User.class);
    }

    @Override
    public void initialize() {
        if(this.getFirstUser() == null) {
            this.addUser(new User("test@archimulator.com", "archimulator")); //TODO: should not be hardcoded
        }
    }

    /**
     *
     * @return
     */
    @Override
    public List<User> getAllUsers() {
        return this.getItems(this.users);
    }

    /**
     *
     * @param id
     * @return
     */
    @Override
    public User getUserById(long id) {
        return this.getItemById(this.users, id);
    }

    /**
     *
     * @param email
     * @return
     */
    @Override
    public User getUserByEmail(String email) {
        try {
            PreparedQuery<User> query = this.users.queryBuilder().where().eq("email", email).prepare();
            return this.users.queryForFirst(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @return
     */
    @Override
    public User getFirstUser() {
        return this.getFirstItem(this.users);
    }

    /**
     *
     * @param user
     */
    @Override
    public void addUser(User user) {
        this.addItem(this.users, user);
    }

    /**
     *
     * @param id
     */
    @Override
    public void removeUserById(long id) {
        this.removeItemById(this.users, id);
    }

    /**
     *
     * @param user
     */
    @Override
    public void updateUser(User user) {
        this.updateItem(this.users, user);
    }
}
