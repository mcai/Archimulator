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
package archimulator.model.simulation;

import archimulator.util.DateHelper;
import archimulator.util.io.cmd.CommandLineHelper;
import archimulator.util.io.cmd.SedHelper;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.Date;

@DatabaseTable
public class SimulatedProgram implements Serializable {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField(index = true)
    private String title;

    @DatabaseField
    private String cwd;

    @DatabaseField
    private String exe;

    @DatabaseField
    private String args;

    @DatabaseField
    private String stdin;

    @DatabaseField
    private long createdTime;

    @DatabaseField
    private boolean helperThreadedProgram;

    @DatabaseField
    private int htLookahead;

    @DatabaseField
    private int htStride;
    
    public SimulatedProgram() {
    }

    public SimulatedProgram(String title, String cwd, String exe, String args) {
        this(title, cwd, exe, args, "");
    }

    public SimulatedProgram(String title, String cwd, String exe, String args, String stdin) {
        this.title = title;
        this.cwd = cwd;
        this.exe = exe;
        this.args = args;
        this.stdin = stdin;
        this.createdTime = DateHelper.toTick(new Date());
    }
    
    public void build() {
        if(this.helperThreadedProgram) {
            this.pushMacroDefineArg("push_params.h", "LOOKAHEAD", this.htLookahead + "");
            this.pushMacroDefineArg("push_params.h", "STRIDE", this.htStride + "");
            this.buildWithMakefile();
        }
    }

    private void pushMacroDefineArg(String fileName, String key, String value) {
        SedHelper.sedInPlace(this.cwd + "/" + fileName, "#define " + key, "#define " + key + " " + value);
    }

    private void buildWithMakefile() {
        CommandLineHelper.invokeShellCommand("sh -c 'cd " + this.cwd + ";make -f Makefile.mips -B'");
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
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

    public long getCreatedTime() {
        return createdTime;
    }

    public String getCreatedTimeAsString() {
        return DateHelper.toString(DateHelper.fromTick(this.createdTime));
    }

    public boolean isHelperThreadedProgram() {
        return helperThreadedProgram;
    }

    public int getHtLookahead() {
        return htLookahead;
    }

    public int getHtStride() {
        return htStride;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCwd(String cwd) {
        this.cwd = cwd;
    }

    public void setExe(String exe) {
        this.exe = exe;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public void setStdin(String stdin) {
        this.stdin = stdin;
    }

    public void setHelperThreadedProgram(boolean helperThreadedProgram) {
        this.helperThreadedProgram = helperThreadedProgram;
    }

    public void setHtLookahead(int htLookahead) {
        this.htLookahead = htLookahead;
    }

    public void setHtStride(int htStride) {
        this.htStride = htStride;
    }

    @Override
    public String toString() {
        return String.format("SimulatedProgram{id=%d, title='%s', cwd='%s', exe='%s', args='%s', stdin='%s', createdTime=%s, helperThreadedProgram=%s, htLookahead=%d, htStride=%d}", id, title, cwd, exe, args, stdin, DateHelper.toString(createdTime), helperThreadedProgram, htLookahead, htStride);
    }
}
