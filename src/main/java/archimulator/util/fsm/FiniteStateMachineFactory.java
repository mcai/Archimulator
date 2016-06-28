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
import java.util.List;
import java.util.Map;

/**
 * Finite state machine factory.
 *
 * @author Min Cai
 * @param <StateT> the type of the states
 * @param <ConditionT> the type of the conditions
 * @param <FiniteStateMachineT> the type of the finite state machines
 */
public class FiniteStateMachineFactory<StateT, ConditionT, FiniteStateMachineT extends FiniteStateMachine<StateT, ConditionT>> {
    Map<StateT, StateTransitions<StateT, ConditionT, FiniteStateMachineT>> transitions;

    /**
     * Create a finite state machine factory.
     */
    public FiniteStateMachineFactory() {
        this.transitions = new LinkedHashMap<>();
    }

    /**
     * Get or create the state transitions object for the specified state.
     *
     * @param state the state
     * @return the state transitions object for the specified state
     */
    public StateTransitions<StateT, ConditionT, FiniteStateMachineT> inState(StateT state) {
        if (!this.transitions.containsKey(state)) {
            this.transitions.put(state, new StateTransitions<>(this, state));
        }

        return this.transitions.get(state);
    }

    /**
     * Clear the transitions.
     */
    public void clear() {
        this.transitions.clear();
    }

    /**
     * Fire the specified transition.
     *
     * @param fsm the involved finite state machine
     * @param sender the event sender
     * @param condition the condition
     * @param params the event parameters
     */
    public void fireTransition(FiniteStateMachineT fsm, Object sender, ConditionT condition, Params params) {
        if (this.transitions.containsKey(fsm.getState())) {
            this.transitions.get(fsm.getState()).fireTransition(fsm, sender, condition, params);
        } else {
            throw new IllegalArgumentException("No handler registered for condition " + condition + " in state " + fsm.getState());
        }
    }

    /**
     * Change the state of the specified finite state machine.
     *
     * @param fsm the finite state machine
     * @param sender the event sender
     * @param condition the condition
     * @param params the event parameters
     * @param newState the new state
     */
    void changeState(FiniteStateMachineT fsm, Object sender, ConditionT condition, Params params, StateT newState) {
        fsm.setState(sender, condition, params, newState);
    }

    /**
     * Dump.
     */
    public void dump() {
        for (StateT state : this.getTransitions().keySet()) {
            System.out.println(state);

            StateTransitions<StateT, ConditionT, FiniteStateMachineT> stateTransitions = this.getTransitions().get(state);
            Map<ConditionT, StateTransitions<StateT, ConditionT, FiniteStateMachineT>.StateTransition> perStateTransitions = stateTransitions.getPerStateTransitions();
            for (ConditionT condition : perStateTransitions.keySet()) {
                StateTransitions<StateT, ConditionT, FiniteStateMachineT>.StateTransition stateTransition = perStateTransitions.get(condition);
                System.out.printf(" :%s ->%s/%s %n", condition, stateTransition.getActions(), stateTransition.getNewState());
            }

            System.out.println();
        }
    }

    /**
     * Dump.
     *
     * @param name the name of the finite state machine factory.
     * @param fsms the list of the finite state machines
     * @param stats the map of statistics
     */
    public void dump(String name, List<? extends FiniteStateMachine<StateT, ConditionT>> fsms, Map<String, String> stats) {
        for (StateT state : this.transitions.keySet()) {
            StateTransitions<StateT, ConditionT, FiniteStateMachineT> stateTransitions = this.transitions.get(state);
            Map<ConditionT, StateTransitions<StateT, ConditionT, FiniteStateMachineT>.StateTransition> perStateTransitions = stateTransitions.getPerStateTransitions();
            for (ConditionT condition : perStateTransitions.keySet()) {
                long numExecutions = 0;
                for (FiniteStateMachine<StateT, ConditionT> fsm : fsms) {
                    numExecutions += fsm.getNumExecutionsByTransition(state, condition);
                }

                stats.put(String.format("%s.%s.%s", name, state, condition), "" + numExecutions);
            }
        }
    }

    /**
     * Get the transitions specified in the finite state machine factory.
     *
     * @return the transitions specified in the finite state machine factory
     */
    public Map<StateT, StateTransitions<StateT, ConditionT, FiniteStateMachineT>> getTransitions() {
        return transitions;
    }
}
