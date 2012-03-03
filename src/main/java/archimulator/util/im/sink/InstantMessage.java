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

import java.io.Serializable;

public class InstantMessage implements Serializable {
    private Long id;
    private String fromUserId;
    private String body;

    public InstantMessage(String fromUserId, String body) {
        this.id = currentId++;
        this.fromUserId = fromUserId;
        this.body = body;
    }

    public Long getId() {
        return id;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public String getBody() {
        return body;
    }

    private static Long currentId = 0L;
}