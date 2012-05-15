package archimulator.sim.uncore.coherence.common;

import archimulator.sim.uncore.cache.Cache;

public class FirstLevelCacheLine extends LockableCacheLine<MESIState> {
    private MESIFiniteStateMachine mesiFsm;
    private MESIActionProvider mesiActionProvider;

    public FirstLevelCacheLine(Cache<?, ?> cache, int set, int way, MESIState initialState) {
        super(cache, set, way, initialState);
        this.mesiFsm = new MESIFiniteStateMachine(this);
        this.mesiActionProvider = new MESIActionProviderImpl();
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

    public MESIActionProvider getMesiActionProvider() {
        return mesiActionProvider;
    }

    private class MESIActionProviderImpl implements MESIActionProvider {
        public MESIActionProviderImpl() {
        }

        @Override
        public void notifyDirectory() {
        }

        @Override
        public void ackToDirectory() {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void copyBackToDirectory() {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void writeBackToDirectory() {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void peerTransfer(String peer) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
