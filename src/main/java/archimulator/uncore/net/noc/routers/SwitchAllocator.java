package archimulator.uncore.net.noc.routers;

/**
 * Switch allocator.
 *
 * @author Min Cai
 */
public class SwitchAllocator {
    private Router router;

    public SwitchAllocator(Router router) {
        this.router = router;
    }

    public void stageSwitchAllocation() {
        for(OutputPort outputPort : this.router.getOutputPorts().values()) {
            InputVirtualChannel winnerInputVirtualChannel = outputPort.getArbiter().next();

            if(winnerInputVirtualChannel != null) {
                Flit flit = winnerInputVirtualChannel.getInputBuffer().peek();
                flit.setState(FlitState.SWITCH_ALLOCATION);
            }
        }
    }

    public Router getRouter() {
        return router;
    }
}
