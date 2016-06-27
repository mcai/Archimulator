package archimulator.incubator.noc.routers;

import archimulator.incubator.noc.Direction;
import archimulator.incubator.noc.Node;
import archimulator.incubator.noc.Packet;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

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

        this.injectionBuffer = new ArrayList<>();

        this.inputPorts = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            this.inputPorts.put(direction, new InputPort(this, direction));
        }

        this.outputPorts = new EnumMap<>(Direction.class);
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
        //TODO
    }

    private void linkTransfer(Flit flit, int nextHop, Direction ip, int ivc) {
        //TODO
    }

    private void localPacketInjection() {
        //TODO
    }

    public boolean injectPacket(Packet packet) {
        return false; //TODO
    }

    public void insertFlit(Flit flit, Direction ip, int ivc) {
        //TODO
    }

    public int freeSlots(Direction ip, int ivc) {
        return -1; //TODO
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
}
