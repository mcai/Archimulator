package archimulator.sim.uncore.coherence.flow;

import archimulator.util.action.Action;

public abstract class AbstractEvictFlow extends Flow {
    public abstract void start(Action onSuccessCallback, Action onFailureCallback);
}
