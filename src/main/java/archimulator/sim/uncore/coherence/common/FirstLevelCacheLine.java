package archimulator.sim.uncore.coherence.common;

import archimulator.sim.uncore.cache.Cache;

public class FirstLevelCacheLine extends LockableCacheLine<MESIState> {
    private MESIFiniteStateMachine mesiFsm;

    public FirstLevelCacheLine(Cache<?, ?> cache, int set, int way, MESIState initialState) {
        super(cache, set, way, initialState);
        this.mesiFsm = new MESIFiniteStateMachine(this);
    }

    @Override
    public void setNonInitialState(MESIState state) {
        throw new UnsupportedOperationException();
    }

    public void _setNonInitialState(MESIState state) {
        super.setNonInitialState(state);
    }

    @Override
    public void invalidate() {
        throw new UnsupportedOperationException();
    }

    public void _invalidate() {
        super.invalidate();
    }

    public MESIFiniteStateMachine getMesiFsm() {
        return mesiFsm;
    }
}
