package archimulator.uncore.net.noc.routers;

/**
 * Flit state.
 *
 * @author Min Cai
 */
public enum  FlitState {
    INPUT_BUFFER,

    ROUTE_CALCULATION,

    VIRTUAL_CHANNEL_ALLOCATION,

    SWITCH_ALLOCATION,

    SWITCH_TRAVERSAL,

    LINK_TRAVERSAL,

    DESTINATION_ARRIVED
}
