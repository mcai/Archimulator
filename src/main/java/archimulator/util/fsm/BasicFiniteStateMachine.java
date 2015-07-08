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
import archimulator.util.event.BlockingEventDispatcher;
import archimulator.util.fsm.event.EnterStateEvent;
import archimulator.util.fsm.event.ExitStateEvent;
import archimulator.util.fsm.event.FiniteStateMachineEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Basic finite state machine.
 *
 * @param <StateT>     the type of the states
 * @param <ConditionT> the type of the conditions
 * @author Min Cai
 */
public class BasicFiniteStateMachine<StateT, ConditionT> extends Params implements FiniteStateMachine<StateT, ConditionT> {
    private String name;
    private StateT state;

    private BlockingEventDispatcher<FiniteStateMachineEvent> eventDispatcher;

    private Map<StateT, Map<ConditionT, Long>> numExecutions;

    private boolean settingStates = false;

    /**
     * Create a basic finite state machine.
     *
     * @param name  the name
     * @param state the initial state
     */
    public BasicFiniteStateMachine(String name, StateT state) {
        this.name = name;
        this.state = state;

        this.eventDispatcher = new BlockingEventDispatcher<>();

        this.numExecutions = new LinkedHashMap<>();
    }

    /**
     * Add a listener for the specified event class.
     *
     * @param <EventT>   the type of the event
     * @param eventClass the event class
     * @param listener   the listener that is to be added for the specified event class
     */
    public <EventT extends FiniteStateMachineEvent> void addListener(Class<EventT> eventClass, Consumer<EventT> listener) {
        this.eventDispatcher.addListener(eventClass, listener);
    }

    /**
     * Remove a listener for the specified event class.
     *
     * @param <EventT>   the type of the event
     * @param eventClass the event class
     * @param listener   the listener that is to removed for the specified event class
     */
    public <EventT extends FiniteStateMachineEvent> void removeListener(Class<EventT> eventClass, Consumer<EventT> listener) {
        this.eventDispatcher.removeListener(eventClass, listener);
    }

    /**
     * Dump.
     *
     * @param fsmFactory the finite state machine factory
     */
    public void dump(FiniteStateMachineFactory<StateT, ConditionT, BasicFiniteStateMachine<StateT, ConditionT>> fsmFactory) {
        for (StateT state : fsmFactory.transitions.keySet()) {
            System.out.println(state);

            StateTransitions<StateT, ConditionT, BasicFiniteStateMachine<StateT, ConditionT>> stateTransitions = fsmFactory.transitions.get(state);
            Map<ConditionT, StateTransitions<StateT, ConditionT, BasicFiniteStateMachine<StateT, ConditionT>>.StateTransition> perStateTransitions = stateTransitions.getPerStateTransitions();
            for (ConditionT condition : perStateTransitions.keySet()) {
                StateTransitions<StateT, ConditionT, BasicFiniteStateMachine<StateT, ConditionT>>.StateTransition stateTransition = perStateTransitions.get(condition);
                System.out.printf("  -> %s:  %s/%s [%d] %n", condition, stateTransition.getActions(), stateTransition.getNewState(), getNumExecutionsByTransition(state, condition));
            }

            System.out.println();
        }
    }

    /**
     * Get the name of the basic finite state machine.
     *
     * @return the name of the basic finite state machine
     */
    public String getName() {
        return name;
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

        this.eventDispatcher.dispatch(new ExitStateEvent(this, sender, condition, params));
        this.state = state;
        this.eventDispatcher.dispatch(new EnterStateEvent(this, sender, condition, params));

        this.settingStates = false;
    }
}
