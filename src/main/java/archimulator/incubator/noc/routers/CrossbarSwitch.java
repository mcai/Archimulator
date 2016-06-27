package archimulator.incubator.noc.routers;

/**
 * Crossbar switch.
 *
 * @author Min Cai
 */
public class CrossbarSwitch {
    private Router router;

    public CrossbarSwitch(Router router) {
        this.router = router;
    }

    public void stageSwitchTraversal() {
        //TODO
    }

    public Router getRouter() {
        return router;
    }
}
