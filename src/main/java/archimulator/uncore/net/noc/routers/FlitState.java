package archimulator.uncore.net.noc.routers;

/**
 * Flit state.
 *
 * @author Min Cai
 */
public enum FlitState {
    /**
     * Input buffer.
     */
    INPUT_BUFFER,

    /**
     * Route computation.
     */
    ROUTE_COMPUTATION,

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
    LINK_TRAVERSAL,

    /**
     * Destination arrived.
     */
    DESTINATION_ARRIVED
}
