package archimulator.uncore.net.noc.routers;

/**
 * Output virtual channel.
 *
 * @author Min Cai
 */
public class OutputVirtualChannel {
    private OutputPort outputPort;
    private int id;
    private InputVirtualChannel inputVirtualChannel;
    private int credits;
    private VirtualChannelArbiter arbiter;

    /**
     * Create an output virtual channel.
     *
     * @param outputPort the parent output port
     * @param id the output virtual channel ID
     */
    public OutputVirtualChannel(OutputPort outputPort, int id) {
        this.outputPort = outputPort;

        this.id = id;

        this.inputVirtualChannel = null;

        this.credits = 10;

        this.arbiter = new VirtualChannelArbiter(this);
    }

    /**
     * Get the parent output port.
     *
     * @return the parent output port
     */
    public OutputPort getOutputPort() {
        return outputPort;
    }

    /**
     * Get the output virtual channel ID.
     *
     * @return the output virtual channel ID
     */
    public int getId() {
        return id;
    }

    /**
     * Get the connected input virtual channel.
     * @return the connected input virtual channel
     */
    public InputVirtualChannel getInputVirtualChannel() {
        return inputVirtualChannel;
    }

    /**
     * Set the connected input virtual channel.
     *
     * @param inputVirtualChannel the connected input virtual channel
     */
    public void setInputVirtualChannel(InputVirtualChannel inputVirtualChannel) {
        this.inputVirtualChannel = inputVirtualChannel;
    }

    /**
     * Get the number of credits.
     *
     * @return the number of credits
     */
    public int getCredits() {
        return credits;
    }

    /**
     * Set the number of credits.
     *
     * @param credits the number of credits
     */
    public void setCredits(int credits) {
        this.credits = credits;
    }

    /**
     * Get the virtual channel arbiter.
     *
     * @return the virtual channel arbiter
     */
    public VirtualChannelArbiter getArbiter() {
        return arbiter;
    }
}
