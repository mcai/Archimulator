package archimulator.incubator.noc.routers;

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

    public OutputVirtualChannel(OutputPort outputPort, int id) {
        this.outputPort = outputPort;

        this.id = id;

        this.inputVirtualChannel = null;

        this.credits = 10;
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
}
