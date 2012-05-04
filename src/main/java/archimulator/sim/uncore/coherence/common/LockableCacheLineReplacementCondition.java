package archimulator.sim.uncore.coherence.common;

public enum LockableCacheLineReplacementCondition {
    BEGIN_HIT,
    END_HIT,
    INVALIDATE,
    BEGIN_EVICT,
    END_EVICT,
    BEGIN_FILL,
    END_FILL
}
