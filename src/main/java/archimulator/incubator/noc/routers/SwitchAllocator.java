package archimulator.incubator.noc.routers;

import javaslang.collection.LinkedHashMap;
import javaslang.collection.List;
import javaslang.collection.Map;

/**
 * Switch allocator.
 *
 * @author Min Cai
 */
public class SwitchAllocator {
    private Router router;
    private Map<OutputPort, SwitchArbiter> arbiters;

    public SwitchAllocator(Router router) {
        this.router = router;

        List<InputVirtualChannel> inputVirtualChannels =
                this.router.getInputPorts().values().map(InputPort::getVirtualChannels).reduce(List::appendAll);

        this.arbiters = LinkedHashMap.empty();

        for(OutputPort outputPort : this.router.getOutputPorts().values()) {
            this.arbiters.put(outputPort, new SwitchArbiter(outputPort, inputVirtualChannels));
        }
    }

    public void stageSwitchAllocation() {
        for(OutputPort outputPort : this.router.getOutputPorts().values()) {
            InputVirtualChannel winnerInputVirtualChannel = this.arbiters.get(outputPort).get().next();

            if(winnerInputVirtualChannel != null) {
                Flit flit = winnerInputVirtualChannel.getInputBuffer().peek();
                flit.setState(FlitState.SWITCH_ALLOCATION);
            }
        }
    }

    public Router getRouter() {
        return router;
    }

    public Map<OutputPort, SwitchArbiter> getArbiters() {
        return arbiters;
    }
}
