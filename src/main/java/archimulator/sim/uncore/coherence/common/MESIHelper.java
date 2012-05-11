package archimulator.sim.uncore.coherence.common;

import archimulator.util.action.Function1X;
import archimulator.util.fsm.FiniteStateMachine;
import archimulator.util.fsm.FiniteStateMachineFactory;

public class MESIHelper {
    private static FiniteStateMachineFactory<MESIState, MESICondition> fsmFactory;

    static {
        //TODO...
        fsmFactory.inState(MESIState.MODIFIED)
                .onCondition(MESICondition.LOCAL_READ_HIT, new Function1X<FiniteStateMachine<MESIState, MESICondition>, MESIState>() {
                    @Override
                    public MESIState apply(FiniteStateMachine<MESIState, MESICondition> param1, Object... otherParams) {
                        return MESIState.MODIFIED;
                    }
                })
                .onCondition(MESICondition.LOCAL_READ_MISS, new Function1X<FiniteStateMachine<MESIState, MESICondition>, MESIState>() {
                    @Override
                    public MESIState apply(FiniteStateMachine<MESIState, MESICondition> param1, Object... otherParams) {
                        return MESIState.SHARED;
                    }
                })
                .onCondition(MESICondition.LOCAL_WRITE_HIT, new Function1X<FiniteStateMachine<MESIState, MESICondition>, MESIState>() {
                    @Override
                    public MESIState apply(FiniteStateMachine<MESIState, MESICondition> param1, Object... otherParams) {
                        return MESIState.MODIFIED;
                    }
                })
                .onCondition(MESICondition.LOCAL_WRITE_MISS, new Function1X<FiniteStateMachine<MESIState, MESICondition>, MESIState>() {
                    @Override
                    public MESIState apply(FiniteStateMachine<MESIState, MESICondition> param1, Object... otherParams) {
                        return MESIState.EXCLUSIVE;
                    }
                });

        fsmFactory.inState(MESIState.EXCLUSIVE)
                .onCondition(MESICondition.LOCAL_READ_HIT, new Function1X<FiniteStateMachine<MESIState, MESICondition>, MESIState>() {
                    @Override
                    public MESIState apply(FiniteStateMachine<MESIState, MESICondition> param1, Object... otherParams) {
                        return MESIState.EXCLUSIVE;
                    }
                });

        fsmFactory.inState(MESIState.SHARED);

        fsmFactory.inState(MESIState.INVALID);
    }

    public static FiniteStateMachineFactory<MESIState, MESICondition> getFsmFactory() {
        return fsmFactory;
    }
}
