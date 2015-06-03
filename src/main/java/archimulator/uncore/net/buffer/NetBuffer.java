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
package archimulator.uncore.net.buffer;

import archimulator.uncore.net.common.NetMessage;
import archimulator.uncore.net.port.NetPort;
import archimulator.util.action.Action;

import java.util.ArrayList;
import java.util.List;

/**
 * Net buffer.
 *
 * @author Min Cai
 */
public abstract class NetBuffer {
    private int size;
    private int count;

    /**
     * A value indicating whether the net buffer is busy reading or not.
     */
    protected boolean readBusy;

    /**
     * A value indicating whether the net buffer is busy writing or not.
     */
    protected boolean writeBusy;

    private List<Action> pendingReadActions;
    private List<Action> pendingWriteActions;
    private List<Action> pendingFullActions;

    /**
     * Create a net buffer of the specified size.
     *
     * @param size the size of the net buffer
     */
    public NetBuffer(int size) {
        this.size = size;

        this.pendingReadActions = new ArrayList<>();
        this.pendingWriteActions = new ArrayList<>();
        this.pendingFullActions = new ArrayList<>();
    }

    /**
     * Begin the reading.
     */
    public void beginRead() {
        this.readBusy = true;
    }

    /**
     * End the reading of the specified message.
     *
     * @param message the message
     */
    public void endRead(NetMessage message) {
        this.readBusy = false;
        this.count -= message.getSize();
        this.doPendingReadActions();
        this.doPendingFullActions();
    }

    /**
     * Begin the writing.
     */
    public void beginWrite() {
        this.writeBusy = true;
    }

    /**
     * End the writing of the specified message.
     *
     * @param message the message
     */
    public void endWrite(NetMessage message) {
        this.writeBusy = false;
        this.count += message.getSize();
        this.doPendingWriteActions();
    }

    /**
     * Add the specified action to the pending read action list.
     *
     * @param action the action
     */
    public void addPendingReadAction(Action action) {
        this.pendingReadActions.add(action);
    }

    /**
     * Add the specified action to the pending write action list.
     *
     * @param action the action
     */
    public void addPendingWriteAction(Action action) {
        this.pendingWriteActions.add(action);
    }

    /**
     * Add the specified action to the pending full action list.
     *
     * @param action the action
     */
    public void addPendingFullAction(Action action) {
        this.pendingFullActions.add(action);
    }

    /**
     * Do pending read actions.
     */
    private void doPendingReadActions() {
        if (!this.pendingReadActions.isEmpty()) {
            this.pendingReadActions.remove(0).apply();
        }
    }

    /**
     * Do pending write actions.
     */
    private void doPendingWriteActions() {
        if (!this.pendingWriteActions.isEmpty()) {
            this.pendingWriteActions.remove(0).apply();
        }
    }

    /**
     * Do pending full actions.
     */
    private void doPendingFullActions() {
        if (!this.pendingFullActions.isEmpty()) {
            this.pendingFullActions.remove(0).apply();
        }
    }

    /**
     * Get the port.
     *
     * @return the port
     */
    public abstract NetPort getPort();

    /**
     * Get the size of the buffer.
     *
     * @return the size of the buffer
     */
    public int getSize() {
        return size;
    }

    /**
     * Get the count of messages in the buffer.
     *
     * @return the count of messages in the buffer
     */
    public int getCount() {
        return count;
    }

    /**
     * Get a value indicating whether the buffer is busy reading or not.
     *
     * @return a value indicating whether the buffer is busy reading or not
     */
    public boolean isReadBusy() {
        return readBusy;
    }

    /**
     * Get a value indicating whether the buffer is busy writing or not.
     *
     * @return a value indicating whether the buffer is busy writing or not
     */
    public boolean isWriteBusy() {
        return writeBusy;
    }

    /**
     * Get a value indicating whether the buffer becomes full after accommodating the message of the specified size.
     *
     * @param message the message that is to be accommodated
     * @return a value indicating whether the buffer becomes full after accommodating the message of the specified size
     */
    public boolean becomesFull(NetMessage message) {
        return this.getCount() + message.getSize() > this.getSize();
    }
}
