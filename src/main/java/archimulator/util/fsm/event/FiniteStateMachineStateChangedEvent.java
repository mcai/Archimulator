/*******************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
 *
 * This file is part of the PickaPack library.
 *
 * PickaPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PickaPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PickaPack. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package archimulator.util.fsm.event;

import archimulator.util.fsm.FiniteStateMachine;
import archimulator.util.Params;

/**
 * The event when a finite state machine has changed its state.
 *
 * @author Min Cai
 */
public class FiniteStateMachineStateChangedEvent extends FiniteStateMachineEvent {
    private FiniteStateMachine<?, ?> fsm;
    private Object sender;
    private Object condition;
    private Params params;

    /**
     * Create an event when a finite state machine has changed its state.
     *
     * @param fsm the finite state machine
     * @param sender the event sender
     * @param condition the condition
     * @param params the event parameters
     */
    public FiniteStateMachineStateChangedEvent(FiniteStateMachine<?, ?> fsm, Object sender, Object condition, Params params) {
        this.fsm = fsm;
        this.sender = sender;
        this.condition = condition;
        this.params = params;
    }

    /**
     * Get the finite state machine.
     *
     * @return the finite state machine
     */
    public FiniteStateMachine<?, ?> getFsm() {
        return fsm;
    }

    /**
     * Get the event sender.
     *
     * @return the event sender
     */
    public Object getSender() {
        return sender;
    }

    /**
     * Get the condition.
     *
     * @return the condition
     */
    public Object getCondition() {
        return condition;
    }

    /**
     * Get the event parameters.
     *
     * @return the event parameters
     */
    public Params getParams() {
        return params;
    }
}
