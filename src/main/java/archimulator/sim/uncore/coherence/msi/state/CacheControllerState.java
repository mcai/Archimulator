package archimulator.sim.uncore.coherence.msi.state;

public enum CacheControllerState {
    I,
    IS_D,
    IM_AD,
    IM_A,
    S,
    SM_AD,
    SM_A,
    M,
    MI_A,
    SI_A,
    II_A;

    public boolean isStable() {
        return this == I || this == S || this == M;
    }

    public boolean isTransient() {
        return !isStable();
    }
}
