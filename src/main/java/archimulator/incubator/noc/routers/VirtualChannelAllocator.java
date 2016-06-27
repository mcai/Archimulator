package archimulator.incubator.noc.routers;

/**
 * Virtual channel allocator.
 *
 * @author Min Cai
 */
public class VirtualChannelAllocator {
    private Router router;

    public VirtualChannelAllocator(Router router) {
        this.router = router;
    }

    public void stageVirtualChannelAllocation() {
        //TODO
    }

    public Router getRouter() {
        return router;
    }
}
