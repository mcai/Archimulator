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
package archimulator.sim.uncore;

import archimulator.sim.uncore.coherence.msi.controller.Controller;
import archimulator.sim.uncore.coherence.msi.message.CoherenceMessage;

import java.util.ArrayList;
import java.util.List;

public class PointToPointReorderBuffer {
    private List<CoherenceMessage> messages;
    private final Controller from;
    private final Controller to;

    public PointToPointReorderBuffer(Controller from, Controller to) {
        this.from = from;
        this.to = to;
        this.messages = new ArrayList<CoherenceMessage>();
    }

    public void transfer(CoherenceMessage message) {
        this.messages.add(message);
    }

    public void onDestinationArrived(CoherenceMessage message) {
        message.onDestinationArrived();
        this.commit();
    }

    public void commit() {
        while(!this.messages.isEmpty()) {
            CoherenceMessage message = this.messages.get(0);
            if(!message.isDestinationArrived()) {
                break;
            }

            message.onCompleted();
            this.messages.remove(message);
            to.receive(message);
        }
    }

    public List<CoherenceMessage> getMessages() {
        return messages;
    }

    public Controller getFrom() {
        return from;
    }

    public Controller getTo() {
        return to;
    }
}
