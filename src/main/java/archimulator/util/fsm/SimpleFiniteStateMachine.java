/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple finite state machine.
 *
 * @author Min Cai
 * @param <StateT> the type of the states
 * @param <ConditionT> the type of the conditions
 */
public class SimpleFiniteStateMachine<StateT, ConditionT> implements FiniteStateMachine<StateT, ConditionT> {
    private StateT state;

    private boolean settingStates = false;

    private Map<StateT, Map<ConditionT, Long>> numExecutions;

    /**
     * Create a simple finite state machine.
     *
     * @param state the initial state
     */
    public SimpleFiniteStateMachine(StateT state) {
        this.state = state;

        this.numExecutions = new LinkedHashMap<>();
    }

    @Override
    public long getNumExecutionsByTransition(StateT state, ConditionT condition) {
        return this.numExecutions.containsKey(state) && this.numExecutions.get(state).containsKey(condition) ? this.numExecutions.get(state).get(condition) : 0L;
    }

    @Override
    public Map<StateT, Map<ConditionT, Long>> getNumExecutions() {
        return numExecutions;
    }

    @Override
    public StateT getState() {
        return state;
    }

    @Override
    public void setState(Object sender, ConditionT condition, Params params, StateT state) {
        if (this.settingStates) {
            throw new IllegalArgumentException();
        }

        this.settingStates = true;

        this.state = state;

        this.settingStates = false;
    }
}
