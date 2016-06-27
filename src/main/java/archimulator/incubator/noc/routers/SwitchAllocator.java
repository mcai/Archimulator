package archimulator.incubator.noc.routers;

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

    public Router getRouter() {
        return router;
    }

    public void stageSwitchAllocation() {
        //TODO
    }
}
