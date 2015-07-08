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
package archimulator.util.fsm;

import archimulator.util.Params;

import java.io.Serializable;
import java.util.Map;

/**
 * Finite state machine.
 *
 * @param <StateT>     the type of the states
 * @param <ConditionT> the type of the conditions
 * @author Min Cai
 */
public interface FiniteStateMachine<StateT, ConditionT> extends Serializable {
    /**
     * Get the number of executions by the specified transition.
     *
     * @param state     the state
     * @param condition the condition
     * @return the number of executions by the specified transition
     */
    long getNumExecutionsByTransition(StateT state, ConditionT condition);

    /**
     * Get the map of the number of executions ordered by states and conditions.
     *
     * @return the map of the number of executions ordered by states and conditions
     */
    Map<StateT, Map<ConditionT, Long>> getNumExecutions();

    /**
     * Get the current state.
     *
     * @return the current state
     */
    StateT getState();

    /**
     * Set the current state.
     *
     * @param sender the event sender
     * @param condition the condition
     * @param params the event parameters
     * @param state the new state
     */
    void setState(Object sender, ConditionT condition, Params params, StateT state);
}
