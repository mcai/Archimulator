package archimulator.uncore.net.basic;

/**
 * Credit.
 *
 * @author Min Cai
 */
public class Credit {
    private VirtualChannel virtualChannel;
    private boolean ready;

    /**
     * Create a credit.
     *
     * @param virtualChannel the virtual channel
     */
    public Credit(VirtualChannel virtualChannel) {
        this.virtualChannel = virtualChannel;
    }

    /**
     * Get the virtual channel.
     *
     * @return the virtual channel
     */
    public VirtualChannel getVirtualChannel() {
        return virtualChannel;
    }

    /**
     * Get a boolean value indicating whether it is ready or not.
     *
     * @return a boolean value indicating whether it is ready or not
     */
    public boolean isReady() {
        return ready;
    }

    /**
     * Set a boolean value indicating whether it is ready or not.
     *
     * @param ready a boolean value indicating whether it is ready or not
     */
    public void setReady(boolean ready) {
        this.ready = ready;
    }
}
