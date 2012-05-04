package archimulator.sim.uncore.coherence.common.process.lineLock;

import archimulator.util.action.Function1X;
import archimulator.util.fsm.FiniteStateMachine;
import archimulator.util.fsm.FiniteStateMachineFactory;

public class CacheLineReplacement {
    private static FiniteStateMachineFactory<CacheLineReplacementState, CacheLineReplacementCondition> fsmFactory;

    static {
        fsmFactory = new FiniteStateMachineFactory<CacheLineReplacementState, CacheLineReplacementCondition>();

        fsmFactory.inState(CacheLineReplacementState.INVALID)
                .onCondition(CacheLineReplacementCondition.BEGIN_FILL, new Function1X<FiniteStateMachine<CacheLineReplacementState, CacheLineReplacementCondition>, CacheLineReplacementState>() {
                    @Override
                    public CacheLineReplacementState apply(FiniteStateMachine<CacheLineReplacementState, CacheLineReplacementCondition> param1, Object... otherParams) {
                        return CacheLineReplacementState.FILLING;
                    }
                });

        fsmFactory.inState(CacheLineReplacementState.VALID)
                .onCondition(CacheLineReplacementCondition.BEGIN_EVICT, new Function1X<FiniteStateMachine<CacheLineReplacementState, CacheLineReplacementCondition>, CacheLineReplacementState>() {
                    @Override
                    public CacheLineReplacementState apply(FiniteStateMachine<CacheLineReplacementState, CacheLineReplacementCondition> param1, Object... otherParams) {
                        return CacheLineReplacementState.EVICTING;
                    }
                });

        fsmFactory.inState(CacheLineReplacementState.EVICTING)
                .onCondition(CacheLineReplacementCondition.END_EVICT, new Function1X<FiniteStateMachine<CacheLineReplacementState, CacheLineReplacementCondition>, CacheLineReplacementState>() {
                    @Override
                    public CacheLineReplacementState apply(FiniteStateMachine<CacheLineReplacementState, CacheLineReplacementCondition> param1, Object... otherParams) {
                        return CacheLineReplacementState.EVICTED;
                    }
                });

        fsmFactory.inState(CacheLineReplacementState.EVICTED)
                .onCondition(CacheLineReplacementCondition.BEGIN_FILL, new Function1X<FiniteStateMachine<CacheLineReplacementState, CacheLineReplacementCondition>, CacheLineReplacementState>() {
                    @Override
                    public CacheLineReplacementState apply(FiniteStateMachine<CacheLineReplacementState, CacheLineReplacementCondition> param1, Object... otherParams) {
                        return CacheLineReplacementState.FILLING;
                    }
                });

        fsmFactory.inState(CacheLineReplacementState.FILLING)
                .onCondition(CacheLineReplacementCondition.END_FILL, new Function1X<FiniteStateMachine<CacheLineReplacementState, CacheLineReplacementCondition>, CacheLineReplacementState>() {
                    @Override
                    public CacheLineReplacementState apply(FiniteStateMachine<CacheLineReplacementState, CacheLineReplacementCondition> param1, Object... otherParams) {
                        return CacheLineReplacementState.VALID;
                    }
                });

        fsmFactory.inState(CacheLineReplacementState.VALID)
                .onCondition(CacheLineReplacementCondition.INVALIDATE, new Function1X<FiniteStateMachine<CacheLineReplacementState, CacheLineReplacementCondition>, CacheLineReplacementState>() {
                    @Override
                    public CacheLineReplacementState apply(FiniteStateMachine<CacheLineReplacementState, CacheLineReplacementCondition> param1, Object... otherParams) {
                        return CacheLineReplacementState.INVALID;
                    }
                });
    }

    public static void main(String[] args) {
        FiniteStateMachine<CacheLineReplacementState, CacheLineReplacementCondition> fsm =
                new FiniteStateMachine<CacheLineReplacementState, CacheLineReplacementCondition>(fsmFactory, "fsm", CacheLineReplacementState.INVALID);

        fsm.fireTransition(CacheLineReplacementCondition.BEGIN_FILL);
        fsm.fireTransition(CacheLineReplacementCondition.END_FILL);

        fsm.fireTransition(CacheLineReplacementCondition.BEGIN_EVICT);
        fsm.fireTransition(CacheLineReplacementCondition.END_EVICT);
        fsm.fireTransition(CacheLineReplacementCondition.BEGIN_FILL);
        fsm.fireTransition(CacheLineReplacementCondition.END_FILL);
    }
}
