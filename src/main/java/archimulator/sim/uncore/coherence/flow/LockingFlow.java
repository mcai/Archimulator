package archimulator.sim.uncore.coherence.flow;

import archimulator.sim.uncore.coherence.common.LockableCacheLineReplacementState;

public abstract class LockingFlow extends Flow {
    protected void endFillOrEvict(FindAndLockFlow findAndLockFlow) {
        LockableCacheLineReplacementState replacementState = findAndLockFlow.getCacheAccess().getLine().getReplacementState();
        if (replacementState == LockableCacheLineReplacementState.HITTING) {
            findAndLockFlow.getCacheAccess().getLine().endHit();
        } else if (replacementState == LockableCacheLineReplacementState.EVICTED) {
            throw new IllegalArgumentException();
        } else if (replacementState == LockableCacheLineReplacementState.FILLING) {
            findAndLockFlow.getCacheAccess().getLine().endFill();
        } else {
            throw new IllegalArgumentException();
        }
    }

    protected void abort(FindAndLockFlow findAndLockFlow) {
        findAndLockFlow.getCacheAccess().getLine().abort();
    }

    protected void afterFlowEnd(FindAndLockFlow findAndLockFlow) {
        LockableCacheLineReplacementState replacementStateAfterEndHitOrFill = findAndLockFlow.getCacheAccess().getLine().getReplacementState();
        if(replacementStateAfterEndHitOrFill != LockableCacheLineReplacementState.INVALID && replacementStateAfterEndHitOrFill != LockableCacheLineReplacementState.VALID) {
            throw new IllegalArgumentException();
        }
    }
}
