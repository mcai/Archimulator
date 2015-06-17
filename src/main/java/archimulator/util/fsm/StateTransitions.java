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
package archimulator.util.fsm;

import archimulator.util.action.Action4;
import archimulator.util.Params;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Finite state machine state transitions.
 *
 * @author Min Cai
 * @param <StateT> the type of the states
 * @param <ConditionT> the type of the conditions
 * @param <FiniteStateMachineT> the type of the finite state machines
 */
public class StateTransitions<StateT, ConditionT, FiniteStateMachineT extends FiniteStateMachine<StateT, ConditionT>> implements Serializable {
    private Map<ConditionT, StateTransition> perStateTransitions;
    private FiniteStateMachineFactory<StateT, ConditionT, FiniteStateMachineT> fsmFactory;
    private StateT state;
    private Consumer<FiniteStateMachineT> onCompletedCallback;

    /**
     * Create a finite state machine state transitions object.
     *
     * @param fsmFactory the finite state machine factory
     * @param state the state
     */
    StateTransitions(FiniteStateMachineFactory<StateT, ConditionT, FiniteStateMachineT> fsmFactory, StateT state) {
        this.fsmFactory = fsmFactory;
        this.state = state;
        this.perStateTransitions = new LinkedHashMap<ConditionT, StateTransition>();
    }

    /**
     * Set the callback action performed when a transition completes.
     *
     * @param onCompletedCallback the callback action that is performed when a transition completes
     * @return this
     */
    public StateTransitions<StateT, ConditionT, FiniteStateMachineT> setOnCompletedCallback(Consumer<FiniteStateMachineT> onCompletedCallback) {
        this.onCompletedCallback = onCompletedCallback;
        return this;
    }

    /**
     * Specifies transitions on a list of conditions.
     *
     * @param conditions the list of conditions
     * @param transition the transition
     * @param newState the new state
     * @return this
     */
    public StateTransitions<StateT, ConditionT, FiniteStateMachineT> onConditions(List<ConditionT> conditions, Action4<FiniteStateMachineT, Object, ConditionT, ? extends Params> transition, StateT newState) {
        return onConditions(conditions, transition, newState, null);
    }

    /**
     * Specifies transitions on a list of conditions.
     *
     * @param conditions the list of conditions
     * @param transition the transition
     * @param newState the new state
     * @param onCompletedCallback the callback action that is performed when the transition completes
     * @return this
     */
    public StateTransitions<StateT, ConditionT, FiniteStateMachineT> onConditions(List<ConditionT> conditions, Action4<FiniteStateMachineT, Object, ConditionT, ? extends Params> transition, StateT newState, Consumer<FiniteStateMachineT> onCompletedCallback) {
        for (ConditionT condition : conditions) {
            this.onCondition(condition, transition, newState, onCompletedCallback);
        }

        return this;
    }

    /**
     * Specifies the transition on the specified condition.
     *
     * @param condition the condition
     * @param transition the transition
     * @param newState the new state
     * @return this
     */
    public StateTransitions<StateT, ConditionT, FiniteStateMachineT> onCondition(ConditionT condition, final Action4<FiniteStateMachineT, Object, ConditionT, ? extends Params> transition, final StateT newState) {
        return onCondition(condition, transition, newState, null);
    }

    /**
     * Specifies the transition on the specified condition.
     *
     * @param condition the condition
     * @param transition the transition
     * @param newState the new state
     * @param onCompletedCallback the callback action that is performed when the transition completes
     * @return this
     */
    public StateTransitions<StateT, ConditionT, FiniteStateMachineT> onCondition(ConditionT condition, final Action4<FiniteStateMachineT, Object, ConditionT, ? extends Params> transition, final StateT newState, Consumer<FiniteStateMachineT> onCompletedCallback) {
        if (this.perStateTransitions.containsKey(condition)) {
            throw new IllegalArgumentException("Transition of condition " + condition + " in state " + this.state + " has already been registered");
        }

        List<FiniteStateMachineAction<FiniteStateMachineT, ConditionT, ? extends Params>> actions = new ArrayList<FiniteStateMachineAction<FiniteStateMachineT, ConditionT, ? extends Params>>();
        actions.add(new FiniteStateMachineAction<FiniteStateMachineT, ConditionT, Params>(state + ": " + condition + " -> unamedAction/" + newState) {
            @Override
            @SuppressWarnings("unchecked")
            public void apply(FiniteStateMachineT fsm, Object sender, ConditionT eventType, Params params) {
                ((Action4<FiniteStateMachineT, Object, ConditionT, Params>) transition).apply(fsm, sender,  eventType, params);
            }
        });

        this.perStateTransitions.put(condition, new StateTransition(state, condition, newState, actions, onCompletedCallback));

        return this;
    }

    /**
     * Specifies the transition on the specified condition.
     *
     * @param condition the condition
     * @param actions the list of actions
     * @param newState the new state
     * @param onCompletedCallback the callback action that is performed when the transition completes
     * @return this
     */
    public StateTransitions<StateT, ConditionT, FiniteStateMachineT> onCondition(ConditionT condition, final List<FiniteStateMachineAction<FiniteStateMachineT, ConditionT, ? extends Params>> actions, final StateT newState, Consumer<FiniteStateMachineT> onCompletedCallback) {
        if (this.perStateTransitions.containsKey(condition)) {
            throw new IllegalArgumentException("Transition of condition " + condition + " in state " + this.state + " has already been registered");
        }

        this.perStateTransitions.put(condition, new StateTransition(state, condition, newState, actions, onCompletedCallback));

        return this;
    }

    /**
     * Ignore the specified condition.
     *
     * @param condition the condition
     * @return the state transitions
     */
    public StateTransitions<StateT, ConditionT, FiniteStateMachineT> ignoreCondition(ConditionT condition) {
        return this.onCondition(condition, new Action4<FiniteStateMachineT, Object, ConditionT, Params>() {
            public void apply(FiniteStateMachineT from, Object sender, ConditionT condition, Params params) {
                from.getState();
            }
        }, null, null);
    }

    /**
     * Clear the map of the per state transitions.
     */
    public void clear() {
        this.perStateTransitions.clear();
    }

    /**
     * Get the map of the per state transitions.
     *
     * @return the map of the per state transitions
     */
    public Map<ConditionT, StateTransition> getPerStateTransitions() {
        return perStateTransitions;
    }

    /**
     * Fire the specified transition.
     *
     * @param fsm the finite state machine
     * @param sender the event sender
     * @param condition the condition
     * @param params the event parameters
     */
    void fireTransition(FiniteStateMachineT fsm, Object sender, ConditionT condition, Params params) {
        if (this.perStateTransitions.containsKey(condition)) {
            StateTransition stateTransition = this.perStateTransitions.get(condition);
            fsmFactory.changeState(fsm, sender, condition, params, stateTransition.apply(fsm, sender, condition, params));
            if(stateTransition.onCompletedCallback != null) {
                stateTransition.onCompletedCallback.accept(fsm);
            }

            if(this.onCompletedCallback != null) {
                this.onCompletedCallback.accept(fsm);
            }
        } else {
            throw new IllegalArgumentException("Unexpected condition " + condition + " in state " + this.state + " is not among " + this.perStateTransitions.keySet());
        }
    }

    /**
     * State transition.
     */
    public class StateTransition {
        private StateT state;
        private ConditionT condition;
        private StateT newState;
        private List<FiniteStateMachineAction<FiniteStateMachineT, ConditionT, ? extends Params>> actions;
        private Consumer<FiniteStateMachineT> onCompletedCallback;

        /**
         * Create a state transition.
         *
         * @param state the state
         * @param condition the condition
         * @param newState the new state
         * @param actions the list of actions
         * @param onCompletedCallback the callback action that is performed when the transition completes
         */
        public StateTransition(StateT state, ConditionT condition, StateT newState, List<FiniteStateMachineAction<FiniteStateMachineT, ConditionT, ? extends Params>> actions, Consumer<FiniteStateMachineT> onCompletedCallback) {
            this.state = state;
            this.condition = condition;
            this.newState = newState;
            this.actions = actions;
            this.onCompletedCallback = onCompletedCallback;
        }

        /**
         * Perform the transition.
         *
         * @param fsm the finite state machine
         * @param sender the event sender
         * @param condition the condition
         * @param params the event parameters
         * @return the new state
         */
        @SuppressWarnings("unchecked")
        public StateT apply(FiniteStateMachineT fsm, Object sender, ConditionT condition, Params params) {
            for(FiniteStateMachineAction<FiniteStateMachineT, ConditionT, ? extends Params> action : this.actions) {
                ((FiniteStateMachineAction<FiniteStateMachineT, ConditionT, Params>) action).apply(fsm, sender, condition, params);
            }

            if(!fsm.getNumExecutions().containsKey(state)) {
                fsm.getNumExecutions().put(state, new LinkedHashMap<ConditionT, Long>());
            }

            if(!fsm.getNumExecutions().get(state).containsKey(condition)) {
                fsm.getNumExecutions().get(state).put(condition, 0L);
            }

            fsm.getNumExecutions().get(state).put(condition, fsm.getNumExecutions().get(state).get(condition) + 1);

            if(this.newState == null) {
                return fsm.getState();
            }

            return this.newState;
        }

        /**
         * Set the callback action that is performed when the transition completes.
         *
         * @param onCompletedCallback the callback action that is performed when the transition completes
         * @return this
         */
        public StateTransition setOnCompletedCallback(Consumer<FiniteStateMachineT> onCompletedCallback) {
            this.onCompletedCallback = onCompletedCallback;
            return this;
        }

        /**
         * Get the list of actions.
         *
         * @return the list of actions
         */
        public List<FiniteStateMachineAction<FiniteStateMachineT, ConditionT, ? extends Params>> getActions() {
            return actions;
        }

        /**
         * Get the state.
         *
         * @return the state
         */
        public StateT getState() {
            return state;
        }

        /**
         * Get the condition.
         *
         * @return the condition
         */
        public ConditionT getCondition() {
            return condition;
        }

        /**
         * Get the new state.
         *
         * @return the new state
         */
        public StateT getNewState() {
            return newState;
        }
    }
}
