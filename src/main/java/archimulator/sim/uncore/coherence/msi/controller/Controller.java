package archimulator.sim.uncore.coherence.msi.controller;

import archimulator.sim.uncore.CacheHierarchy;
import archimulator.sim.uncore.MemoryDevice;
import archimulator.sim.uncore.coherence.config.CoherentCacheConfig;
import archimulator.sim.uncore.coherence.msi.message.CoherenceMessage;
import net.pickapack.action.Action;

public abstract class Controller extends MemoryDevice {
    private MemoryDevice next;
    private CoherentCacheConfig config;

    public Controller(CacheHierarchy cacheHierarchy, String name, CoherentCacheConfig config) {
        super(cacheHierarchy, name);
        this.config = config;
    }

    public abstract void receive(CoherenceMessage message);

    public void transfer(final Controller to, int size, final CoherenceMessage message) {
        if (to == null || this == to) {
            throw new IllegalArgumentException();
        }

        this.getNet(to).transfer(this, to, size, new Action() {
            @Override
            public void apply() {
                getCycleAccurateEventQueue().schedule(Controller.this, to, message, 0);
            }
        });
    }

    //TODO: to be called from controller
    protected int getHitLatency() {
        return config.getHitLatency();
    }

    public CoherentCacheConfig getConfig() {
        return config;
    }

    public MemoryDevice getNext() {
        return next;
    }

    public void setNext(MemoryDevice next) {
        this.next = next;
    }
}
