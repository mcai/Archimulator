/*******************************************************************************
 * Copyright (c) 2010-2011 by Min Cai (min.cai.china@gmail.com).
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

public class FiniteStateMachineStateChangedEvent extends FiniteStateMachineEvent {
    private FiniteStateMachine<?> from;
    private Object condition;
    private Object[] params;

    public FiniteStateMachineStateChangedEvent(FiniteStateMachine<?> from, Object condition, Object... params) {
        this.from = from;
        this.condition = condition;
        this.params = params;
    }

    public FiniteStateMachine<?> getFrom() {
        return from;
    }

    public Object getCondition() {
        return condition;
    }

    public Object[] getParams() {
        return params;
    }
}
