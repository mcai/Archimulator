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

import archimulator.util.JsonSerializationHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MessageSinkImpl implements MessageSink {
    private Map<String, CloudUser> users;

    public MessageSinkImpl() {
        this.users = new HashMap<String, CloudUser>();
    }

    @Override
    public Set<String> getUserIds() {
        return this.users.keySet();
    }

    @Override
    public synchronized void send(String fromUserId, String toUserId, String message) {
        this.getUser(toUserId).getInstantMessages().add(new InstantMessage(fromUserId, message));
    }

    @Override
    public synchronized String receive(String userId) {
        CloudUser user = this.getUser(userId);
        List<InstantMessage> messages = user.getInstantMessages();

        if (!messages.isEmpty()) {
            InstantMessage message = messages.iterator().next();
            user.getInstantMessages().remove(message);

            return JsonSerializationHelper.serialize(message);
        }

        return null;
    }

    private CloudUser getUser(String userId) {
        return this.users.containsKey(userId) ? this.users.get(userId) : this.createUser(userId);
    }

    private CloudUser createUser(String userId) {
        CloudUser user = new CloudUser(userId);
        this.users.put(user.getId(), user);
        return user;
    }
}