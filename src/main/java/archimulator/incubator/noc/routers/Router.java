package archimulator.incubator.noc.routers;

import archimulator.incubator.noc.Direction;
import archimulator.incubator.noc.Node;
import archimulator.incubator.noc.Packet;
import javaslang.collection.LinkedHashMap;
import javaslang.collection.List;
import javaslang.collection.Map;

/**
 * Router.
 *
 * @author Min Cai
 */
public class Router {
    private Node node;

    private List<Packet> injectionBuffer;

    private Map<Direction, InputPort> inputPorts;

    private Map<Direction, OutputPort> outputPorts;

    private RouteComputation routeComputation;

    private VirtualChannelAllocator virtualChannelAllocator;

    private SwitchAllocator switchAllocator;

    private CrossbarSwitch crossbarSwitch;

    public Router(Node node) {
        this.node = node;

        this.injectionBuffer = List.empty();

        this.inputPorts = LinkedHashMap.empty();
        for (Direction direction : Direction.values()) {
            this.inputPorts.put(direction, new InputPort(this, direction));
        }

        this.outputPorts = LinkedHashMap.empty();
        for (Direction direction : Direction.values()) {
            this.outputPorts.put(direction, new OutputPort(this, direction));
        }

        this.routeComputation = new RouteComputation(this);
        this.virtualChannelAllocator = new VirtualChannelAllocator(this);
        this.switchAllocator = new SwitchAllocator(this);
        this.crossbarSwitch = new CrossbarSwitch(this);

        this.node.getNetwork().getCycleAccurateEventQueue().getPerCycleEvents().add(this::advanceOneCycle);
    }

    private void advanceOneCycle() {
        this.stageLinkTraversal();

        this.crossbarSwitch.stageSwitchTraversal();

        this.switchAllocator.stageSwitchAllocation();

        this.virtualChannelAllocator.stageVirtualChannelAllocation();

        this.routeComputation.stageRouteComputation();

        this.localPacketInjection();
    }

    private void stageLinkTraversal() {
        for(OutputPort outputPort : this.outputPorts.values()) {
            for(OutputVirtualChannel outputVirtualChannel : outputPort.getVirtualChannels()) {
                InputVirtualChannel inputVirtualChannel = outputVirtualChannel.getInputVirtualChannel();
                if(inputVirtualChannel != null && outputVirtualChannel.getCredits() > 0) {
                    Flit flit = inputVirtualChannel.getInputBuffer().peek();
                    if(flit != null && flit.getState() == FlitState.SWITCH_TRAVERSAL) {
                        if(outputPort.getDirection() != Direction.LOCAL) {
                            flit.setState(FlitState.LINK_TRAVERSAL);

                            int nextHop = this.node.getNeighbors().get(outputPort.getDirection()).get();
                            Direction ip = outputPort.getDirection().getReflexDirection();
                            int ivc = outputVirtualChannel.getId();

                            this.node.getNetwork().getCycleAccurateEventQueue().schedule(this, () -> {
                                nextHopArrived(flit, nextHop, ip, ivc);
                            }, this.node.getNetwork().getExperiment().getConfig().getLinkDelay());
                        }

                        inputVirtualChannel.getInputBuffer().pop();

                        if(outputPort.getDirection() != Direction.LOCAL) {
                            outputVirtualChannel.setCredits(outputVirtualChannel.getCredits() - 1);
                        } else {
                            flit.setState(FlitState.DESTINATION_ARRIVED);
                        }

                        if(flit.isTail()) {
                            inputVirtualChannel.setOutputVirtualChannel(null);
                            outputVirtualChannel.setInputVirtualChannel(null);

                            if(outputPort.getDirection() == Direction.LOCAL) {
                                this.node.handleDestArrived(flit.getPacket(), inputVirtualChannel);
                            }
                        }
                    }
                }
            }
        }
    }

    private void nextHopArrived(Flit flit, int nextHop, Direction ip, int ivc) {
        InputBuffer inputBuffer =
                this.node.getNetwork().getNodes().get(nextHop).getRouter().getInputPorts().get(ip).get().getVirtualChannels().get(ivc).getInputBuffer();

        if(inputBuffer.size() + 1 <= this.node.getNetwork().getExperiment().getConfig().getMaxInputBufferSize()) {
            flit.setState(FlitState.INPUT_BUFFER);
            this.node.getNetwork().getNodes().get(nextHop).getRouter().insertFlit(flit, ip, ivc);
        }
        else {
            this.node.getNetwork().getCycleAccurateEventQueue().schedule(this, () -> this.nextHopArrived(flit, nextHop, ip, ivc), 1);
        }
    }

    private void localPacketInjection() {
        while (true) {
            boolean requestInserted = false;

            for(int ivc = 0; ivc < this.node.getNetwork().getExperiment().getConfig().getNumVirtualChannels(); ivc++) {
                if(this.injectionBuffer.isEmpty()) {
                    continue;
                }

                Packet packet = this.injectionBuffer.get(0);

                int numFlits = (int) Math.ceil((double)(packet.getSize()) / this.node.getNetwork().getExperiment().getConfig().getLinkWidth());

                InputBuffer inputBuffer = this.inputPorts.get(Direction.LOCAL).get().getVirtualChannels().get(ivc).getInputBuffer();

                if(inputBuffer.size() + numFlits <= this.node.getNetwork().getExperiment().getConfig().getMaxInputBufferSize()) {
                    for(int i = 0; i < numFlits; i++) {
                        Flit flit = new Flit(packet, i, i == 0, i == numFlits - 1);
                        this.insertFlit(flit, Direction.LOCAL, ivc);
                    }

                    this.injectionBuffer.remove(packet);
                    requestInserted = true;
                    break;
                }
            }

            if(!requestInserted) {
                break;
            }
        }
    }

    public boolean injectPacket(Packet packet) {
        if(this.injectionBuffer.size() < this.node.getNetwork().getExperiment().getConfig().getMaxInjectionBufferSize()) {
            this.injectionBuffer.append(packet);
            return true;
        }

        return false;
    }

    public void insertFlit(Flit flit, Direction ip, int ivc) {
        this.inputPorts.get(ip).get().getVirtualChannels().get(ivc).getInputBuffer().append(flit);

        flit.setNode(this.node);
    }

    public int freeSlots(Direction ip, int ivc) {
        return this.node.getNetwork().getExperiment().getConfig().getMaxInputBufferSize()
                - this.inputPorts.get(ip).get().getVirtualChannels().get(ivc).getInputBuffer().size();
    }

    @Override
    public String toString() {
        return String.format("Router{node=%s}", node);
    }

    public Node getNode() {
        return node;
    }

    public List<Packet> getInjectionBuffer() {
        return injectionBuffer;
    }

    public Map<Direction, InputPort> getInputPorts() {
        return inputPorts;
    }

    public Map<Direction, OutputPort> getOutputPorts() {
        return outputPorts;
    }

    public RouteComputation getRouteComputation() {
        return routeComputation;
    }

    public VirtualChannelAllocator getVirtualChannelAllocator() {
        return virtualChannelAllocator;
    }

    public SwitchAllocator getSwitchAllocator() {
        return switchAllocator;
    }

    public CrossbarSwitch getCrossbarSwitch() {
        return crossbarSwitch;
    }
}
