package archimulator.uncore.net.basic;

import org.apache.commons.collections.map.MultiKeyMap;

import java.util.*;

/**
 * Router.
 *
 * @author Min Cai
 */
public class Router {
    private int numVirtualChannels;
    private int numPorts;

    private String topology;
    private RouterType type;
    private int id;
    private int numRouters;

    private int linkLatency;
    private int linkWidth;

    private boolean virtualChannelPartitionEnabled;

    private int numVirtualChannelsPerCore;

    private int arbitrationPolicy;

    private EnumMap<Direction, Router> links;

    private List<Request> injectionBuffer;
    private int injectionBufferMaxSize;
    private List<Request> requestBuffer;

    private int bufferMaxSize;

    private List<Port> ports;

    private MultiKeyMap outputPortIds;
    private MultiKeyMap outputVirtualChannelIds;

    private MultiKeyMap outputVirtualChannelAvailables;
    private MultiKeyMap credits;
    private Map<Integer, Integer> linkAvailable;

    private Map<Integer, Integer> switchAvailable;

    private List<Credit> pendingCredits;

    public Router(RouterType type, int id, int numPorts) {
        //TODO

        this.requestBuffer = new ArrayList<>();

        this.injectionBuffer = new ArrayList<>();
        this.injectionBufferMaxSize = 32;

        this.ports = new ArrayList<>();

        this.outputVirtualChannelAvailables = new MultiKeyMap();
        this.outputVirtualChannelIds = new MultiKeyMap();
        this.outputPortIds = new MultiKeyMap();
        this.credits = new MultiKeyMap();

        for(int i = 0; i < this.numPorts; i++) {
            Port port = new Port();
            this.ports.add(port);


        }

        this.bufferMaxSize = 10;

        this.switchAvailable = new HashMap<>();

        this.linkAvailable = new HashMap<>();

        this.pendingCredits = new ArrayList<>();
    }

    public void advanceOneCycle() {
        this.processPendingCredit();
        this.stageLinkTraversal();
        this.stageSwitchAllocation();
        this.stageSwitchTraversal();
        this.stageRouteCalculation();
        this.stageVirtualChannelAllocation();
        this.localPacketInjection();
    }

    private void processPendingCredit() {

    }

    private void stageLinkTraversal() {

    }

    private void stageSwitchAllocation() {

    }

    private void stageSwitchTraversal() {

    }

    private void stageRouteCalculation() {

    }

    private void stageVirtualChannelAllocation() {

    }

    private void localPacketInjection() {
        for(;;) {
            if(this.injectionBuffer.isEmpty()) {
                break;
            }

            boolean requestInserted = false;


        }

    }

    public boolean injectPacket(Request request) {
        if(this.injectionBuffer.size() < this.injectionBufferMaxSize) {
            this.injectionBuffer.add(request);
            return true;
        }

        return false;
    }

    public Request receiveRequest(Direction direction) {
        return null;
    }

    public Request popRequest(Direction direction) {
        return null;
    }

    public int getId() {
        return id;
    }
}
