package archimulator.uncore.net.basic;

/**
 * Output port.
 *
 * @author Min Cai
 */
public class OutputPort extends Port {
    private boolean switchAvailable;
    private boolean linkAvailable;

    /**
     * Create an output port.
     *
     * @param router the router
     * @param direction the direction
     */
    public OutputPort(Router router, Direction direction) {
        super(router, direction);

        this.switchAvailable = true;
        this.linkAvailable = true;
    }

    /**
     * Get a boolean value indicating whether the switch is available or not.
     *
     * @return a boolean value indicating whether the switch is available or not
     */
    public boolean isSwitchAvailable() {
        return switchAvailable;
    }

    /**
     * Set a boolean value indicating whether the switch is available or not.
     *
     * @param switchAvailable a boolean value indicating whether the switch is available or not
     */
    public void setSwitchAvailable(boolean switchAvailable) {
        this.switchAvailable = switchAvailable;
    }

    /**
     * Get a boolean value indicating whether the link is available or not.
     *
     * @return a boolean value indicating whether the link is available or not
     */
    public boolean isLinkAvailable() {
        return linkAvailable;
    }

    /**
     * Set a boolean value indicating whether the link is available or not.
     *
     * @param linkAvailable a boolean value indicating whether the link is available or not
     */
    public void setLinkAvailable(boolean linkAvailable) {
        this.linkAvailable = linkAvailable;
    }
}
