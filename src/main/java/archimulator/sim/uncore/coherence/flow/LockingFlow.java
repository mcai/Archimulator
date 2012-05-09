package archimulator.sim.uncore.coherence.flow;

public abstract class LockingFlow extends Flow {
    public LockingFlow(Flow producerFlow) {
        super(producerFlow);
    }
}
