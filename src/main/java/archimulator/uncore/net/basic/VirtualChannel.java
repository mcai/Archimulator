package archimulator.uncore.net.basic;

import org.apache.commons.collections.map.MultiKeyMap;

import java.util.ArrayList;
import java.util.List;

public class VirtualChannel {
    private int fixedRoute;
    private MultiKeyMap routes;

    private List<Flit> inputBuffer;
    private List<Flit> outputBuffer;

    public VirtualChannel() {
        this.routes = new MultiKeyMap();

        this.inputBuffer = new ArrayList<>();
        this.outputBuffer = new ArrayList<>();
    }

    public void setRoute(int i, Direction direction, boolean route) {
        this.routes.put(i, direction, route);
    }

    public boolean getRoute(int i, Direction direction) {
        return (boolean) this.routes.get(i, direction);
    }
}
