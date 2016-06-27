package archimulator.incubator.noc.routers;

import archimulator.incubator.noc.Direction;

/**
 * Input virtual channel.
 *
 * @author Min Cai
 */
public class InputVirtualChannel {
    private InputPort inputPort;
    private int id;
    private InputBuffer inputBuffer;
    private Direction route;
    private OutputVirtualChannel outputVirtualChannel;

    public InputVirtualChannel(InputPort inputPort, int id) {
        this.inputPort = inputPort;

        this.id = id;

        this.inputBuffer = new InputBuffer(this);

        this.route = null;

        this.outputVirtualChannel = null;
    }

    public InputPort getInputPort() {
        return inputPort;
    }

    public int getId() {
        return id;
    }

    public InputBuffer getInputBuffer() {
        return inputBuffer;
    }

    public Direction getRoute() {
        return route;
    }

    public void setRoute(Direction route) {
        this.route = route;
    }

    public OutputVirtualChannel getOutputVirtualChannel() {
        return outputVirtualChannel;
    }

    public void setOutputVirtualChannel(OutputVirtualChannel outputVirtualChannel) {
        this.outputVirtualChannel = outputVirtualChannel;
    }
}
