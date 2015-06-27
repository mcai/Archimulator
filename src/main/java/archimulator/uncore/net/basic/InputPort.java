package archimulator.uncore.net.basic;

/**
 * Input port.
 *
 * @author Min Cai
 */
public class InputPort extends Port {
    /**
     * Create an input port.
     *
     * @param router the router
     * @param direction the direction
     */
    public InputPort(Router router, Direction direction) {
        super(router, direction);
    }
}
