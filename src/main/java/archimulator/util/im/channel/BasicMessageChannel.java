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
package archimulator.util.im.channel;

import archimulator.util.im.sink.MessageSink;

public class BasicMessageChannel extends AbstractMessageChannel {
    private boolean open;
    private String userId;

    public BasicMessageChannel(String userId, MessageSink sink, long checkReceivedMessagePeriod) {
        super(sink, checkReceivedMessagePeriod);
        this.userId = userId;
    }

    @Override
    public void open() {
        super.open();

        this.open = true;

        Thread threadReceive = new Thread() {
            @Override
            public void run() {
                receive();
            }
        };
        threadReceive.setDaemon(true);
        threadReceive.start();
    }

    @Override
    public void send(String to, Object obj) {
        if (this.open) {
            sendObj(this.userId, to, obj);
        }
    }

    private void receive() {
        while (this.open) {
            this.receiveBatch(this.userId);

            try {
                Thread.sleep(this.checkReceivedMessagePeriod);
            } catch (InterruptedException e) {
                //ignore
            }
        }
    }

    @Override
    public void close() {
        super.close();
    }

    public boolean isOpen() {
        return open;
    }

    public String getUserId() {
        return userId;
    }
}