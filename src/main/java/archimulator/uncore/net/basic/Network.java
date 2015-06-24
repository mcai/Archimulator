package archimulator.uncore.net.basic;

import org.apache.commons.collections.map.MultiKeyMap;

import java.util.List;

public class Network {
    private String topology;
    private int numPackets;
    private int numCores;
    private int numL2s;
    private int numMCs;

    private List<Router> routers;
    private MultiKeyMap routerMap;

    public boolean send(Request request, int sourceLevel, int sourceId, int destinationLevel, int destinationId) {
        Router source = getRouter(sourceLevel, sourceId);
        Router destination = getRouter(destinationLevel, destinationId);
        //TODO

        return source.injectPacket(request);
    }

    public Router getRouter(int level, int id) {
        return (Router) this.routerMap.get(level, id);
    }

    public Request receive(int level, int id) {
        return getRouter(level, id).receiveRequest(Direction.LOCAL);
    }

    public Request receivePop(int level, int id) {
        return getRouter(level, id).popRequest(Direction.LOCAL);
    }
}
