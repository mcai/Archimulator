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

import archimulator.util.DateHelper;
import archimulator.util.JsonSerializationHelper;
import archimulator.util.im.sink.InstantMessage;
import archimulator.util.im.sink.MessageSink;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class AbstractMessageChannel implements MessageChannel {
    protected MessageSink sink;
    protected long checkReceivedMessagePeriod;
    private final List<MessagingListener> listeners;

    public AbstractMessageChannel(MessageSink sink, long checkReceivedMessagePeriod) {
        this.checkReceivedMessagePeriod = checkReceivedMessagePeriod;
        this.sink = sink;

        this.listeners = new ArrayList<MessagingListener>();
    }

    @Override
    public void addMessagingListener(MessagingListener listener) {
        synchronized (this.listeners) {
            this.listeners.add(listener);
        }
    }

    @Override
    public void removeMessagingListener(MessagingListener listener) {
        synchronized (this.listeners) {
            this.listeners.remove(listener);
        }
    }

    protected void sendObj(String fromUserId, String toUserId, Object obj) {
        this.sink.send(fromUserId, toUserId, JsonSerializationHelper.serialize(new JsonSerializationHelper.ObjectWrapper(obj.getClass().getName(), obj)));
    }

    public void send(Object message) {
        this.send(MessageSink.USER_ID_SERVER, message);
    }

    protected void fireMessageReceived(InstantMessage instantMessage) {
        JsonSerializationHelper.ObjectWrapper objectWrapper = JsonSerializationHelper.deserialize(JsonSerializationHelper.ObjectWrapper.class, instantMessage.getBody());

        for (MessagingListener listener : listeners) {
            listener.messageReceived(instantMessage.getFromUserId(), objectWrapper.getObj());
        }
    }

    protected boolean receiveOne(String userId) {
        String str;
        if ((str = this.sink.receive(userId)) != null) {
            this.fireMessageReceived(JsonSerializationHelper.deserialize(InstantMessage.class, str));
            return true;
        }

        System.out.printf("[%s Message Channel] No new message for user %s\n", DateHelper.toString(new Date()), userId);

        return false;
    }

    protected void receiveBatch(String userId) {
        for (; receiveOne(userId); ) ;
    }

    @Override
    public void open() {
    }

    @Override
    public void close() {
        this.listeners.clear();
    }
}