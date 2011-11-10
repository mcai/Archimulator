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

public class FiniteStateMachine<StateT, ConditionT> {
    private FiniteStateMachineFactory<StateT, ConditionT> factory;
    private String name;
    private StateT state;

    public FiniteStateMachine(FiniteStateMachineFactory<StateT, ConditionT> factory, String name, StateT state) {
        this.factory = factory;
        this.name = name;
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public StateT getState() {
        return state;
    }

    public void setState(StateT state) {
        this.state = state;
    }

    public void fireTransition(ConditionT condition, Object... params) {
        this.factory.fireTransition(this, condition, params);
    }
}
