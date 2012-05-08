package archimulator.sim.uncore.coherence.flow;

import archimulator.util.action.Action;

public interface AbstractEvictFlow {
    void start(Action onSuccessCallback, Action onFailureCallback);
}
