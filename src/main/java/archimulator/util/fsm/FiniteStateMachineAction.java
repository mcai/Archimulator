package archimulator.util.fsm;

import archimulator.util.action.Action4;
import archimulator.util.Params;

/**
 * Finite state machine action.
 *
 * @author Min Cai
 * @param <FiniteStateMachineT> the type of the finite state machine
 * @param <ConditionT> the type of the conditions
 * @param <ParamsT> the type of the event parameters
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
