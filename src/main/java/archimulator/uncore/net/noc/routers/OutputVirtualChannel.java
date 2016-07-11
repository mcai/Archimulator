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

    public OutputVirtualChannel(OutputPort outputPort, int id) {
        this.outputPort = outputPort;

        this.id = id;

        this.inputVirtualChannel = null;

        this.credits = 10;

        this.arbiter = new VirtualChannelArbiter(this);
    }

    public OutputPort getOutputPort() {
        return outputPort;
    }

    public int getId() {
        return id;
    }

    public InputVirtualChannel getInputVirtualChannel() {
        return inputVirtualChannel;
    }

    public void setInputVirtualChannel(InputVirtualChannel inputVirtualChannel) {
        this.inputVirtualChannel = inputVirtualChannel;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public VirtualChannelArbiter getArbiter() {
        return arbiter;
    }
}
