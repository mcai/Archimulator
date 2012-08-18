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

@DatabaseTable(tableName = "SimulatedProgram")
public class SimulatedProgram implements ModelElement {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField(index = true)
    private String title;

    @DatabaseField
    private long createTime;

    @DatabaseField
    private String cwd;

    @DatabaseField
    private String exe;

    @DatabaseField
    private String args;

    @DatabaseField
    private String stdin;

    @DatabaseField
    private boolean ht;

    public SimulatedProgram() {
    }

    public SimulatedProgram(String title, String cwd, String exe, String args) {
        this(title, cwd, exe, args, "", false);
    }

    public SimulatedProgram(String title, String cwd, String exe, String args, String stdin, boolean ht) {
        this.title = title;
        this.cwd = cwd;
        this.exe = exe;
        this.args = args;
        this.stdin = stdin;

        this.ht = ht;

        this.createTime = DateHelper.toTick(new Date());
    }

    public long getId() {
        return id;
    }

    @Override
    public long getParentId() {
        return -1;
    }

    public String getTitle() {
        return title;
    }

    public long getCreateTime() {
        return createTime;
    }

    public String getCwd() {
        return cwd;
    }

    public String getExe() {
        return exe;
    }

    public String getArgs() {
        return args;
    }

    public String getStdin() {
        return stdin;
    }

    public boolean isHt() {
        return ht;
    }

    @Override
    public String toString() {
        return String.format("SimulatedProgram{id=%d, title='%s', cwd='%s', exe='%s', args='%s', stdin='%s', ht=%s}", id, title, cwd, exe, args, stdin, ht);
    }
}
