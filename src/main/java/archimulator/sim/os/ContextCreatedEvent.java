package archimulator.sim.os;

import archimulator.sim.common.SimulationEvent;

/**
 * Event when a context is created.
 */
public class ContextCreatedEvent extends SimulationEvent {
    private Context context;

    /**
     * Create an event when the specified context is created.
     *
     * @param context the context
     */
    public ContextCreatedEvent(Context context) {
        super(context);

        this.context = context;
    }

    /**
     * Get the context.
     *
     * @return the context
     */
    public Context getContext() {
        return context;
    }
}
