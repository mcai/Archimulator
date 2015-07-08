/**
 * ****************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the PickaPack library.
 * <p>
 * PickaPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * PickaPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with PickaPack. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.util.fsm.event;

import archimulator.util.Params;
import archimulator.util.fsm.FiniteStateMachine;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;

/**
 * The event when a finite state machine exits a state.
 *
 * @author Min Cai
 */
public class ExitStateEvent extends FiniteStateMachineStateChangedEvent {
    /**
     * Create an event when a finite state machine has changed its state.
     *
     * @param fsm the finite state machine
     * @param sender the event sender
     * @param condition the condition
     * @param params the event parameters
     */
    public ExitStateEvent(FiniteStateMachine<?, ?> fsm, Object sender, Object condition, Params params) {
        super(fsm, sender, condition, params);
    }

    @Override
    public String toString() {
        return String.format("Before %s: %s.%s%s", getFsm(), getSender(), getCondition(), (getParams() == null || getParams().isEmpty() ? "" : "(" + StringUtils.join(Arrays.asList(getParams()), ", ") + ")"));
    }
}
