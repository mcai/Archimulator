package archimulator.uncore.net.basic;

/**
 * Flit state.
 *
 * @author Min Cai
 */
public enum FlitState {
    /**
     * Init.
     */
    INIT,

    /**
     * Input buffer.
     */
    INPUT_BUFFER,

    /**
     * Route calculation.
     */
    ROUTE_CALCULATION,

    /**
     * Virtual channel allocation.
     */
    VIRTUAL_CHANNEL_ALLOCATION,

    /**
     * Switch allocation.
     */
    SWITCH_ALLOCATION,

    /**
     * Switch traversal.
     */
    SWITCH_TRAVERSAL,

    /**
     * Link traversal.
     */
    LINK_TRAVERSAL
}
