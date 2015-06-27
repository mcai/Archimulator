package archimulator.uncore.net.basic;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;

/**
 * Router.
 *
 * @author Min Cai
 */
public class Router {
    private BasicNet net;

    private RouterType type;
    private int id;

    private EnumMap<Direction, Router> links;

    private List<Packet> injectionBuffer;
    private int injectionBufferMaxSize;

    private int bufferMaxSize;

    private EnumMap<Direction, InputPort> inputPorts;
    private EnumMap<Direction, OutputPort> outputPorts;

    private List<Credit> pendingCredits;

    //TODO: to be removed
    private List<Flit> pendingFlits = new ArrayList<>();

    /**
     * Create a router.
     *
     * @param net the net
     * @param type the type of the router
     * @param id the ID of the router
     */
    public Router(BasicNet net, RouterType type, int id) {
        this.net = net;
        this.type = type;
        this.id = id;

        this.links = new EnumMap<>(Direction.class);

        this.injectionBuffer = new ArrayList<>();
        this.injectionBufferMaxSize = 32;

        this.inputPorts = new EnumMap<>(Direction.class);
        this.outputPorts = new EnumMap<>(Direction.class);

        this.inputPorts.put(Direction.LOCAL, new InputPort(this, Direction.LOCAL));
        this.inputPorts.put(Direction.LEFT, new InputPort(this, Direction.LEFT));
        this.inputPorts.put(Direction.RIGHT, new InputPort(this, Direction.RIGHT));
        this.inputPorts.put(Direction.UP, new InputPort(this, Direction.UP));
        this.inputPorts.put(Direction.DOWN, new InputPort(this, Direction.DOWN));

        this.outputPorts.put(Direction.LOCAL, new OutputPort(this, Direction.LOCAL));
        this.outputPorts.put(Direction.LEFT, new OutputPort(this, Direction.LEFT));
        this.outputPorts.put(Direction.RIGHT, new OutputPort(this, Direction.RIGHT));
        this.outputPorts.put(Direction.UP, new OutputPort(this, Direction.UP));
        this.outputPorts.put(Direction.DOWN, new OutputPort(this, Direction.DOWN));

        this.bufferMaxSize = 10;

        this.pendingCredits = new ArrayList<>();
    }

    /**
     * Advance one cycle.
     */
    public void advanceOneCycle() {
        this.processPendingCredits();
        this.stageLinkTraversal();
        this.stageSwitchAllocation();
        this.stageSwitchTraversal();
        this.stageRouteCalculation();
        this.stageVirtualChannelAllocation();
        this.localPacketInjection();
    }

    /**
     * The link traversal (LT) stage.
     */
    private void stageLinkTraversal() {
        for(OutputPort outputPort : this.outputPorts.values()) {
            if(!outputPort.isLinkAvailable()) {
                continue;
            }

            long oldestCycle = Long.MAX_VALUE;
            VirtualChannel outputVirtualChannelFound = null;

            for(VirtualChannel outputVirtualChannel : outputPort.getVirtualChannels()) {
                if (outputVirtualChannel.getOutputBuffer().isEmpty()) {
                    continue;
                } else if (outputVirtualChannel.getCredit() == 0) {
                    continue;
                }

                Flit flit = outputVirtualChannel.getOutputBuffer().get(0);

                if(flit.getTimestamp() < oldestCycle) {
                    oldestCycle = flit.getTimestamp();
                    outputVirtualChannelFound = outputVirtualChannel;
                }
            }

            if(outputVirtualChannelFound != null) {
                Flit flitFound = outputVirtualChannelFound.getOutputBuffer().get(0);

                if(outputPort.getDirection() != Direction.LOCAL) {
                    this.links.get(outputPort.getDirection()).insertFlit(flitFound, outputPort.getDirection().opposite(), outputVirtualChannelFound.getNum());

                    flitFound.setReady(false);
                    this.net.getCycleAccurateEventQueue().schedule(this, () -> flitFound.setReady(true), this.net.getLinkLatency() + 1);

                    outputPort.setLinkAvailable(false);
                    this.net.getCycleAccurateEventQueue().schedule(this, () -> outputPort.setLinkAvailable(true), this.net.getLinkLatency());
                }

                outputVirtualChannelFound.getOutputBuffer().remove(0);

                if(outputPort.getDirection() != Direction.LOCAL) {
                    outputVirtualChannelFound.setCredit(outputVirtualChannelFound.getCredit() - 1);
                }

                if(flitFound.isTail()) {
                    outputVirtualChannelFound.setAvailable(true);

                    if(outputPort.getDirection() == Direction.LOCAL) {
                        flitFound.getPacket().getOnCompletedCallback().apply();
                    }
                }

                this.pendingFlits.remove(flitFound);
            }
        }
    }

    /**
     * The switch allocation (SA) stage.
     */
    private void stageSwitchAllocation() {
        for(OutputPort outputPort : this.outputPorts.values()) {
            if(outputPort.isSwitchAvailable()) {
                VirtualChannel inputVirtualChannel = this.stageSwitchAllocationPickWinner(outputPort);

                if(inputVirtualChannel != null) {
                    Flit flit = inputVirtualChannel.getInputBuffer().get(0);
                    flit.setState(FlitState.SWITCH_ALLOCATION);

                    outputPort.setSwitchAvailable(false);

                    this.net.getCycleAccurateEventQueue().schedule(this, () -> outputPort.setSwitchAvailable(true), 1);
                }
            }
        }
    }

    /**
     * Pick a winner virtual channel for the specified output port in the switch allocation (SA) stage.
     *
     * @param outputPort the output port
     * @return the selected winner virtual channel
     */
    private VirtualChannel stageSwitchAllocationPickWinner(OutputPort outputPort) {
        long oldestTimestamp = Long.MAX_VALUE;
        VirtualChannel inputVirtualChannelFound = null;

        for(InputPort inputPort : this.inputPorts.values()) {
            if(inputPort.getDirection() == outputPort.getDirection()) {
                continue;
            }

            for(VirtualChannel inputVirtualChannel : inputPort.getVirtualChannels()) {
                if(inputVirtualChannel.getFixedRoute() == outputPort &&
                        inputVirtualChannel.getOutputVirtualChannel().getPort() == outputPort &&
                        !inputVirtualChannel.getInputBuffer().isEmpty()) {
                    Flit flit = inputVirtualChannel.getInputBuffer().get(0);

                    if ((flit.isHead() && flit.getState() == FlitState.VIRTUAL_CHANNEL_ALLOCATION ||
                            !flit.isHead() && flit.getState() == FlitState.INPUT_BUFFER) &&
                            flit.getTimestamp() < oldestTimestamp) {
                        oldestTimestamp = flit.getTimestamp();
                        inputVirtualChannelFound = inputVirtualChannel;
                    }
                }
            }
        }

        return inputVirtualChannelFound;
    }

    /**
     * The switch traversal (ST) stage.
     */
    private void stageSwitchTraversal() {
        for(OutputPort outputPort : this.outputPorts.values()) {
            VirtualChannel inputVirtualChannelFound = null;

            for(InputPort inputPort : this.inputPorts.values()) {
                if(inputPort.getDirection() == outputPort.getDirection()) {
                    continue;
                }

                for(VirtualChannel inputVirtualChannel : inputPort.getVirtualChannels()) {
                    if(inputVirtualChannel.getFixedRoute() == outputPort &&
                            inputVirtualChannel.getOutputVirtualChannel().getPort() == outputPort &&
                            !inputVirtualChannel.getInputBuffer().isEmpty()) {
                        Flit flit = inputVirtualChannel.getInputBuffer().get(0);
                        if(flit.getState() == FlitState.SWITCH_ALLOCATION) {
                            inputVirtualChannelFound = inputVirtualChannel;
                            break;
                        }
                    }
                }
            }

            if(inputVirtualChannelFound != null) {
                Flit flit = inputVirtualChannelFound.getInputBuffer().get(0);
                flit.setState(FlitState.SWITCH_TRAVERSAL);

                VirtualChannel outputVirtualChannel = inputVirtualChannelFound.getOutputVirtualChannel();
                outputVirtualChannel.getOutputBuffer().add(flit);

                inputVirtualChannelFound.getInputBuffer().remove(0);

                if(flit.isTail()) {
                    inputVirtualChannelFound.setFixedRoute(null);
                    inputVirtualChannelFound.setOutputVirtualChannel(null);
                }

                Direction direction = inputVirtualChannelFound.getPort().getDirection();
                if(direction != Direction.LOCAL) {
                    Credit credit = new Credit(this.inputPorts.get(direction.opposite()).getVirtualChannels().get(inputVirtualChannelFound.getPort().getVirtualChannels().indexOf(inputVirtualChannelFound)));
                    this.net.getCycleAccurateEventQueue().schedule(this, () -> credit.setReady(true), 1);
                    this.links.get(direction).insertCredit(credit);
                }
            }
        }
    }

    /**
     * The route calculation (RC) stage.
     */
    private void stageRouteCalculation() {
        for(InputPort inputPort : this.inputPorts.values()) {
            for(VirtualChannel inputVirtualChannel : inputPort.getVirtualChannels()) {
                if(inputVirtualChannel.getInputBuffer().isEmpty()) {
                    continue;
                }

                Flit flit = inputVirtualChannel.getInputBuffer().get(0);
                if(flit.isHead() && flit.getState() == FlitState.INPUT_BUFFER && flit.isReady()) {
                    inputVirtualChannel.clearRoutes();
                    if(flit.getDestination() == this) {
                        inputVirtualChannel.setRoute(0, Direction.LOCAL, true);
                        inputVirtualChannel.setRoute(1, Direction.LOCAL, true);
                    }
                    else {
                        int width = (int) Math.sqrt(this.net.getRouters().size());
                        int sourceX = this.id % width;
                        int sourceY = this.id / width;
                        int destinationX = flit.getDestination().getId() % width;
                        int destinationY = flit.getDestination().getId() / width;

                        //adaptive routing
                        if(sourceX > destinationX) {
                            inputVirtualChannel.setRoute(0, Direction.LEFT, true);
                        }
                        else if (sourceX < destinationX) {
                            inputVirtualChannel.setRoute(0, Direction.RIGHT, true);
                        }

                        if(sourceY > destinationY) {
                            inputVirtualChannel.setRoute(0, Direction.UP, true);
                        }
                        else if(sourceY < destinationY) {
                            inputVirtualChannel.setRoute(0, Direction.DOWN, true);
                        }

                        //escape routing
                        if(sourceX > destinationX) {
                            inputVirtualChannel.setRoute(1, Direction.LEFT, true);
                        }
                        else if(sourceX < destinationX) {
                            inputVirtualChannel.setRoute(1, Direction.RIGHT, true);
                        }
                        else if(sourceY > destinationY) {
                            inputVirtualChannel.setRoute(1, Direction.UP, true);
                        }
                        else if(sourceY < destinationY) {
                            inputVirtualChannel.setRoute(1, Direction.DOWN, true);
                        }
                    }
                    flit.setState(FlitState.ROUTE_CALCULATION);
                }
            }
        }
    }

    /**
     * The virtual channel allocation (VCA) stage.
     */
    private void stageVirtualChannelAllocation() {
        for(OutputPort outputPort : this.outputPorts.values()) {
            for(VirtualChannel outputVirtualChannel : outputPort.getVirtualChannels()) {
                if(outputVirtualChannel.isAvailable()) {
                    VirtualChannel inputVirtualChannel = this.stageVirtualChannelAllocationPickWinner(outputVirtualChannel);
                    if(inputVirtualChannel != null) {
                        Flit flit = inputVirtualChannel.getInputBuffer().get(0);
                        flit.setState(FlitState.VIRTUAL_CHANNEL_ALLOCATION);

                        inputVirtualChannel.setOutputVirtualChannel(outputVirtualChannel);
                        inputVirtualChannel.setFixedRoute(outputPort);

                        outputVirtualChannel.setAvailable(false);
                    }
                }
            }
        }
    }

    /**
     * Pick a winner input virtual channel for the specified output virtual channel in the virtual channel allocation (VCA) stage.
     *
     * @param outputVirtualChannel the output virtual channel
     * @return the selected winner input virtual channel
     */
    private VirtualChannel stageVirtualChannelAllocationPickWinner(VirtualChannel outputVirtualChannel) {
        int routeCalculationIndex = outputVirtualChannel.getNum() / (this.net.getNumVirtualChannels() - 1);

        long oldestTimestamp = Long.MAX_VALUE;
        VirtualChannel inputVirtualChannelFound = null;

        for(InputPort inputPort : this.inputPorts.values()) {
            if(inputPort.getDirection() == outputVirtualChannel.getPort().getDirection()) {
                continue;
            }

            for(VirtualChannel inputVirtualChannel : inputPort.getVirtualChannels()) {
                if(inputVirtualChannel.getInputBuffer().isEmpty() || !inputVirtualChannel.getRoute(routeCalculationIndex, outputVirtualChannel.getPort().getDirection())) {
                    continue;
                }

                Flit flit = inputVirtualChannel.getInputBuffer().get(0);

                if(flit.isHead() && flit.getState() == FlitState.ROUTE_CALCULATION && flit.getTimestamp() < oldestTimestamp) {
                    oldestTimestamp = flit.getTimestamp();
                    inputVirtualChannelFound = inputVirtualChannel;
                }
            }
        }

        return inputVirtualChannelFound;
    }

    /**
     * Perform local packet injection.
     */
    private void localPacketInjection() {
        for(;;) {
            if(this.injectionBuffer.isEmpty()) {
                break;
            }

            boolean requestInserted = false;

            for(VirtualChannel inputVirtualChannel : this.inputPorts.get(Direction.LOCAL).getVirtualChannels()) {
                if(this.injectionBuffer.isEmpty()) {
                    continue;
                }

                Packet packet = this.injectionBuffer.get(0);

                int numFlits = 1;

                numFlits += packet.getSize() / this.net.getLinkWidth();

                if(inputVirtualChannel.getInputBuffer().size() + numFlits <= this.bufferMaxSize) {
                    for(int i = 0; i < numFlits; i++) {
                        Flit flit = new Flit(this.net, packet, this.net.getRouter(packet.getFrom()), this.net.getRouter(packet.getTo()));

                        flit.setHead(i == 0);
                        flit.setTail(i == numFlits - 1);

                        flit.setState(FlitState.INPUT_BUFFER);
                        flit.setReady(true);
                        flit.setNum(i);

                        inputVirtualChannel.getInputBuffer().add(flit);

                        this.pendingFlits.add(flit);
                    }

                    this.injectionBuffer.remove(0);
                    requestInserted = true;
                    break;
                }
            }

            if(!requestInserted) {
                break;
            }
        }
    }

    /**
     * Inject the specified packet.
     *
     * @param packet the packet
     * @return a boolean value indicating whether the packet has been injected or not
     */
    public boolean injectRequest(Packet packet) {
        if(this.injectionBuffer.size() < this.injectionBufferMaxSize) {
            this.injectionBuffer.add(packet);
            return true;
        }

        return false;
    }

    /**
     * Insert the flit.
     *
     * @param flit the flit
     * @param direction the direction
     * @param inputVirtualChannelNum the input virtual channel number
     */
    private void insertFlit(Flit flit, Direction direction, int inputVirtualChannelNum) {
        this.inputPorts.get(direction).getVirtualChannels().get(inputVirtualChannelNum).getInputBuffer().add(flit);
        flit.setState(FlitState.INPUT_BUFFER);
    }

    /**
     * Process pending credits.
     */
    private void processPendingCredits() {
        for(Iterator<Credit> it = this.pendingCredits.iterator(); it.hasNext(); ) {
            Credit credit = it.next();

            if(credit.isReady()) {
                credit.getVirtualChannel().setCredit(credit.getVirtualChannel().getCredit() + 1);
                it.remove();
            }
        }
    }

    /**
     * Insert the credit.
     *
     * @param credit the credit
     */
    private void insertCredit(Credit credit) {
        this.pendingCredits.add(credit);
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
     * Get the type of the router.
     *
     * @return the type of the router
     */
    public RouterType getType() {
        return type;
    }

    /**
     * Get the net.
     *
     * @return the net
     */
    public BasicNet getNet() {
        return net;
    }

    /**
     * Get the map of links.
     *
     * @return the map of links
     */
    public EnumMap<Direction, Router> getLinks() {
        return links;
    }
}
