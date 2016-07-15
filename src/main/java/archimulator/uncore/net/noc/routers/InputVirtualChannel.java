package archimulator.uncore.net.noc.routers;

import archimulator.uncore.net.noc.Direction;

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

    /**
     * Create an input virtual channel.
     *
     * @param inputPort the parent input port
     * @param id the input virtual channel ID
     */
    public InputVirtualChannel(InputPort inputPort, int id) {
        this.inputPort = inputPort;

        this.id = id;

        this.inputBuffer = new InputBuffer(this);

        this.route = null;

        this.outputVirtualChannel = null;
    }

    /**
     * Get the input port.
     *
     * @return the input port
     */
    public InputPort getInputPort() {
        return inputPort;
    }

    /**
     * Get the ID.
     *
     * @return the ID
     */
    public int getId() {
        return id;
    }

    /**
     * Get the input buffer.
     *
     * @return the input buffer
     */
    public InputBuffer getInputBuffer() {
        return inputBuffer;
    }

    /**
     * Get the route.
     *
     * @return the router
     */
    public Direction getRoute() {
        return route;
    }

    /**
     * Set the route.
     *
     * @param route the route
     */
    public void setRoute(Direction route) {
        this.route = route;
    }

    /**
     * Get the output virtual channel.
     *
     * @return the output virtual channel
     */
    public OutputVirtualChannel getOutputVirtualChannel() {
        return outputVirtualChannel;
    }

    /**
     * Set the output virtual channel.
     *
     * @param outputVirtualChannel the output virtual channel
     */
    public void setOutputVirtualChannel(OutputVirtualChannel outputVirtualChannel) {
        this.outputVirtualChannel = outputVirtualChannel;
    }
}
