/**
 * ****************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
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
import archimulator.util.action.Action4;

/**
 * Finite state machine action.
 *
 * @param <FiniteStateMachineT> the type of the finite state machine
 * @param <ConditionT>          the type of the conditions
 * @param <ParamsT>             the type of the event parameters
 * @author Min Cai
 */
public abstract class FiniteStateMachineAction<FiniteStateMachineT, ConditionT, ParamsT extends Params> implements Action4<FiniteStateMachineT, Object, ConditionT, ParamsT> {
    private String name;

    /**
     * Create a finite state machine action.
     *
     * @param name the name of finite state machine action
     */
    public FiniteStateMachineAction(String name) {
        this.name = name;
    }

    /**
     * Get the name of the finite state machine action.
     *
     * @return the name of the finite state machine action
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
