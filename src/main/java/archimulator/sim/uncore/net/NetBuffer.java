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
package archimulator.sim.uncore.net;

import net.pickapack.action.Action;

import java.util.ArrayList;
import java.util.List;

public abstract class NetBuffer {
    private int size;
    private int count;

    protected boolean readBusy;
    protected boolean writeBusy;

    private List<Action> pendingReadActions;
    private List<Action> pendingWriteActions;
    private List<Action> pendingFullActions;

    public NetBuffer(int size) {
        this.size = size;

        this.pendingReadActions = new ArrayList<Action>();
        this.pendingWriteActions = new ArrayList<Action>();
        this.pendingFullActions = new ArrayList<Action>();
    }

    public void beginRead() {
        this.readBusy = true;
    }

    public void endRead(NetMessage message) {
        this.readBusy = false;
        this.count -= message.getSize();
        this.doPendingReadActions();
        this.doPendingFullActions();
    }

    public void beginWrite() {
        this.writeBusy = true;
    }

    public void endWrite(NetMessage message) {
        this.writeBusy = false;
        this.count += message.getSize();
        this.doPendingWriteActions();
    }

    public void addPendingReadAction(Action action) {
        this.pendingReadActions.add(action);
    }

    public void addPendingWriteAction(Action action) {
        this.pendingWriteActions.add(action);
    }

    public void addPendingFullAction(Action action) {
        this.pendingFullActions.add(action);
    }

    private void doPendingReadActions() {
        if (!this.pendingReadActions.isEmpty()) {
            Action action = this.pendingReadActions.get(0);
            action.apply();
            this.pendingReadActions.remove(action);
        }
    }

    private void doPendingWriteActions() {
        if (!this.pendingWriteActions.isEmpty()) {
            Action action = this.pendingWriteActions.get(0);
            action.apply();
            this.pendingWriteActions.remove(action);
        }
    }

    private void doPendingFullActions() {
        if (!this.pendingFullActions.isEmpty()) {
            Action action = this.pendingFullActions.get(0);
            action.apply();
            this.pendingFullActions.remove(action);
        }
    }

    public abstract NetPort getPort();

    public int getSize() {
        return size;
    }

    public int getCount() {
        return count;
    }

    public boolean isReadBusy() {
        return readBusy;
    }

    public boolean isWriteBusy() {
        return writeBusy;
    }
}
