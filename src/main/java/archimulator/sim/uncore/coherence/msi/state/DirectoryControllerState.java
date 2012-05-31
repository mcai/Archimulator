package archimulator.sim.uncore.coherence.msi.state;

public enum DirectoryControllerState {
    I,
    S,
    M,
    S_D,
    MI_A,
    SI_A;

    public boolean isStable() {
        return this == I || this == S || this == M;
    }
}
