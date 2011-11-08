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

import archimulator.util.action.Action2;
import archimulator.util.action.Function1X;
import archimulator.util.event.BlockingEventDispatcher;

import java.util.HashMap;
import java.util.Map;

public class FiniteStateMachine<StateT> {
    private String name;
    private StateT state;
    private Map<StateT, StateTransitions> transitions;
    private BlockingEventDispatcher<FiniteStateMachineEvent> finitestateMachineEventDispatcher;

    public FiniteStateMachine(String name, StateT initialState) {
        this.name = name;
        this.state = initialState;
        this.transitions = new HashMap<StateT, StateTransitions>();
        this.finitestateMachineEventDispatcher = new BlockingEventDispatcher<FiniteStateMachineEvent>();
    }

    public StateTransitions in(StateT state) {
        if (!this.transitions.containsKey(state)) {
            this.transitions.put(state, new StateTransitions());
        }

        return this.transitions.get(state);
    }

    public void clear() {
        this.transitions.clear();
    }

    public void fireTransition(Object condition, Object... params) {
        if (this.transitions.containsKey(this.state)) {
            this.transitions.get(this.state).fireTransition(condition, params);
        } else {
            throw new IllegalArgumentException("No handler registered for condition " + condition + " in state " + this.state);
        }
    }

    public <EventT extends FiniteStateMachineEvent> void addListener(Class<EventT> eventClass, Action2<FiniteStateMachine<StateT>, EventT> listener) {
        this.finitestateMachineEventDispatcher.addListener(eventClass, listener);
    }

    public <EventT extends FiniteStateMachineEvent> void removeListener(Class<EventT> eventClass, Action2<FiniteStateMachine<StateT>, EventT> listener) {
        this.finitestateMachineEventDispatcher.removeListener(eventClass, listener);
    }

    public StateBinding bind(StateT myState) {
        return new StateBinding(myState);
    }

    private void changeState(FiniteStateMachine<?> from, Object condition, Object[] params, StateT newState) {
        if (state != newState) {
            finitestateMachineEventDispatcher.dispatch(FiniteStateMachine.this, new ExitStateEvent(from, condition, params));
            state = newState;
            finitestateMachineEventDispatcher.dispatch(FiniteStateMachine.this, new EnterStateEvent(from, condition, params));
        }
    }

    public String getName() {
        return name;
    }

    public StateT getState() {
        return this.state;
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", this.name, this.state);
    }

    public class StateBinding {
        private StateT myState;

        public StateBinding(StateT myState) {
            this.myState = myState;
        }

        public <StateK> StateBinding to(final FiniteStateMachine<StateK> otherFsm, final StateK otherFsmState) {
            otherFsm.addListener(EnterStateEvent.class, new Action2<FiniteStateMachine<StateK>, EnterStateEvent>() {
                public void apply(FiniteStateMachine<StateK> from, EnterStateEvent event) {
                    if (from.getState() == otherFsmState) {
                        changeState(otherFsm, event.getCondition(), event.getParams(), myState);
                    }
                }
            });

            return this;
        }
    }

    public class StateTransitions {
        private Map<Object, Function1X<StateT, StateT>> perStateTransitions;

        private StateTransitions() {
            this.perStateTransitions = new HashMap<Object, Function1X<StateT, StateT>>();
        }

        public StateTransitions on(Object condition, Function1X<StateT, StateT> transition) {
            if (this.perStateTransitions.containsKey(condition)) {
                throw new IllegalArgumentException("Transition of condition " + condition + " in state " + state + " has already been registered");
            }

            this.perStateTransitions.put(condition, transition);

            return this;
        }

        public StateTransitions ignore(Object condition) {
            return this.on(condition, new Function1X<StateT, StateT>() {
                public StateT apply(StateT state, Object... params) {
                    return state;
                }
            });
        }

        public void clear() {
            this.perStateTransitions.clear();
        }

        private void fireTransition(Object condition, Object... params) {
            if (this.perStateTransitions.containsKey(condition)) {
                StateT newState = this.perStateTransitions.get(condition).apply(state, params);

                changeState(FiniteStateMachine.this, condition, params, newState);
            } else {
                throw new IllegalArgumentException("Unexpected condition " + condition + " in state " + state);
            }
        }
    }

}
