package archimulator.sim.uncore.coherence.common.process.lineLock;

public enum CacheLineReplacementCondition {
    BEGIN_EVICT,
    END_EVICT,
    BEGIN_FILL,
    END_FILL,
    INVALIDATE
}
