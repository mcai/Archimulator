package archimulator.sim.uncore.coherence.common.process.lineLock;

public enum CacheLineReplacementState {
    INVALID,
    VALID,
    EVICTING,
    EVICTED,
    FILLING;

    public boolean isLocked() {
        return this == EVICTING && this == EVICTED && this == FILLING;
    }
}
