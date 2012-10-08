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
package archimulator.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.model.ModelElement;

import java.util.Date;

/**
 *
 * @author Min Cai
 */
@DatabaseTable(tableName = "User")
public class User implements ModelElement {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField
    private String email;

    @DatabaseField
    private long createTime;

    @DatabaseField
    private String password;

    /**
     *
     */
    public User() {
    }

    /**
     *
     * @param email email
     * @param password password
     */
    public User(String email, String password) {
        this.email = email;
        this.password = password;
        this.createTime = DateHelper.toTick(new Date());
    }

    /**
     *
     * @return id
     */
    @Override
    public long getId() {
        return id;
    }

    /**
     *
     * @return parent id
     */
    @Override
    public long getParentId() {
        return -1;
    }

    /**
     *
     * @return title
     */
    @Override
    public String getTitle() {
        return email;
    }

    /**
     *
     * @return
     */
    public String getEmail() {
        return email;
    }

    /**
     *
     * @return
     */
    @Override
    public long getCreateTime() {
        return createTime;
    }

    /**
     *
     * @return
     */
    public String getPassword() {
        return password;
    }

    /**
     *
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }
}