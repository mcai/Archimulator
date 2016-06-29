package archimulator.uncore.net.noc.routers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

        List<InputVirtualChannel> inputVirtualChannels = new ArrayList<>();

        for(InputPort inputPort : this.router.getInputPorts().values()) {
            inputVirtualChannels.addAll(inputPort.getVirtualChannels());
        }

        this.arbiters = new LinkedHashMap<>();

        for(OutputPort outputPort : this.router.getOutputPorts().values()) {
            this.arbiters.put(outputPort, new SwitchArbiter(outputPort, inputVirtualChannels));
        }
    }

    public void stageSwitchAllocation() {
        for(OutputPort outputPort : this.router.getOutputPorts().values()) {
            InputVirtualChannel winnerInputVirtualChannel = this.arbiters.get(outputPort).next();

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
