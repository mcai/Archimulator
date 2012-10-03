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
import net.pickapack.model.ModelElement;
import net.pickapack.service.AbstractService;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class UserServiceImpl extends AbstractService implements UserService {
    private Dao<User, Long> users;

    @SuppressWarnings("unchecked")
    public UserServiceImpl() {
        super(ServiceManager.DATABASE_URL, Arrays.<Class<? extends ModelElement>>asList(User.class));

        this.users = createDao(User.class);

        if(this.getFirstUser() == null) {
            this.addUser(new User("min.cai.china@gmail.com", "1026@ustc")); //TODO: should not be hardcoded
        }
    }

    @Override
    public List<User> getAllUsers() {
        return this.getAllItems(this.users);
    }

    @Override
    public User getUserById(long id) {
        return this.getItemById(this.users, id);
    }

    @Override
    public User getUserByEmail(String email) {
        try {
            PreparedQuery<User> query = this.users.queryBuilder().where().eq("email", email).prepare();
            return this.users.queryForFirst(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public User getFirstUser() {
        return this.getFirstItem(this.users);
    }

    @Override
    public void addUser(User user) {
        this.addItem(this.users, User.class, user);
    }

    @Override
    public void removeUserById(long id) {
        this.removeItemById(this.users, User.class, id);
    }

    @Override
    public void updateUser(User user) {
        this.updateItem(this.users, User.class, user);
    }
}