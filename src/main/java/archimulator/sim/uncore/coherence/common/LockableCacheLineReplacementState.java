package archimulator.sim.uncore.coherence.common;

public enum LockableCacheLineReplacementState {
    INVALID,
    VALID,
    HITTING,
    EVICTING,
    EVICTED,
    FILLING;

    public boolean isLocked() {
        return this == HITTING || this == EVICTING || this == EVICTED || this == FILLING;
    }
}
