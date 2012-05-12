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
package archimulator.util.fsm;

import archimulator.util.action.Function1X;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FiniteStateMachineFactory<StateT, ConditionT, FiniteStateMachineT extends FiniteStateMachine<StateT, ConditionT>> {
    private Map<StateT, StateTransitions> transitions;

    public FiniteStateMachineFactory() {
        this.transitions = new HashMap<StateT, StateTransitions>();
    }

    public StateTransitions inState(StateT state) {
        if (!this.transitions.containsKey(state)) {
            this.transitions.put(state, new StateTransitions(state));
        }

        return this.transitions.get(state);
    }

    public void clear() {
        this.transitions.clear();
    }

    public void fireTransition(FiniteStateMachineT fsm, ConditionT condition, Object... params) {
        if (this.transitions.containsKey(fsm.getState())) {
            this.transitions.get(fsm.getState()).fireTransition(fsm, condition, params);
        } else {
            throw new IllegalArgumentException("No handler registered for condition " + condition + " in state " + fsm.getState());
        }
    }

    private void changeState(FiniteStateMachineT from, ConditionT condition, Object[] params, StateT newState) {
        if (from != newState) {
            from.setState(newState, condition, params);
        }
    }

    public class StateTransitions {
        private Map<ConditionT, Function1X<FiniteStateMachineT, StateT>> perStateTransitions;
        private StateT state;

        private StateTransitions(StateT state) {
            this.state = state;
            this.perStateTransitions = new HashMap<ConditionT, Function1X<FiniteStateMachineT, StateT>>();
        }

        public StateTransitions onConditions(List<ConditionT> conditions, Function1X<FiniteStateMachineT, StateT> transition) {
            for (ConditionT condition : conditions) {
                this.onCondition(condition, transition);
            }

            return this;
        }

        public StateTransitions onCondition(ConditionT condition, Function1X<FiniteStateMachineT, StateT> transition) {
            if (this.perStateTransitions.containsKey(condition)) {
                throw new IllegalArgumentException("Transition of condition " + condition + " in state " + this.state + " has already been registered");
            }

            this.perStateTransitions.put(condition, transition);

            return this;
        }

        public StateTransitions ignoreCondition(ConditionT condition) {
            return this.onCondition(condition, new Function1X<FiniteStateMachineT, StateT>() {
                public StateT apply(FiniteStateMachineT from, Object... params) {
                    return from.getState();
                }
            });
        }

        public void clear() {
            this.perStateTransitions.clear();
        }

        private void fireTransition(FiniteStateMachineT fsm, ConditionT condition, Object... params) {
            if (this.perStateTransitions.containsKey(condition)) {
                changeState(fsm, condition, params, this.perStateTransitions.get(condition).apply(fsm, params));
            } else {
                throw new IllegalArgumentException("Unexpected condition " + condition + " in state " + this.state + " is not among " + this.perStateTransitions.keySet());
            }
        }
    }
}
