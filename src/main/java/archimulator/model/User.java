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
package archimulator.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.model.WithCreateTime;
import net.pickapack.model.WithId;

import java.util.Date;

/**
 * User.
 *
 * @author Min Cai
 */
@DatabaseTable(tableName = "User")
public class User implements WithId, WithCreateTime {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField
    private String email;

    @DatabaseField
    private long createTime;

    @DatabaseField
    private String password;

    /**
     * Create a user. Reserved for ORM only.
     */
    public User() {
    }

    /**
     * Create a user.
     *
     * @param email    the email
     * @param password the password
     */
    public User(String email, String password) {
        this.email = email;
        this.password = password;
        this.createTime = DateHelper.toTick(new Date());
    }

    /**
     * Get the user's ID.
     *
     * @return the user's ID
     */
    @Override
    public long getId() {
        return id;
    }

    /**
     * Get the email.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Get the time in ticks when the user is created.
     *
     * @return the time in ticks when the user is created
     */
    @Override
    public long getCreateTime() {
        return createTime;
    }

    /**
     * Get the password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the password.
     *
     * @param password the password
     */
    public void setPassword(String password) {
        this.password = password;
    }
}