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
package archimulator.util.im.sink;

import java.util.HashSet;
import java.util.Set;

public class GracefulMessageSink implements MessageSink {
    private MessageSink proxy;

    public GracefulMessageSink(MessageSink proxy) {
        this.proxy = proxy;
    }

    @Override
    public Set<String> getUserIds() {
        try {
            return this.proxy.getUserIds();
        } catch (Exception e) {
            System.err.println(e);
            return new HashSet<String>();
        }
    }

    @Override
    public void send(String fromUserId, String toUserId, String message) {
        try {
            this.proxy.send(fromUserId, toUserId, message);
        } catch (Exception e) {
            System.err.println(e); //TODO: schedule resend
        }
    }

    @Override
    public String receive(String userId) {
        try {
            return this.proxy.receive(userId);
        } catch (Exception e) {
            System.err.println(e);
            return null;
        }
    }
}