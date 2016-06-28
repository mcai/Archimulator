package archimulator.incubator.noc.routers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Virtual channel allocator.
 *
 * @author Min Cai
 */
public class VirtualChannelAllocator {
    private Router router;
    private Map<OutputVirtualChannel, VirtualChannelArbiter> arbiters;

    public VirtualChannelAllocator(Router router) {
        this.router = router;

        List<InputVirtualChannel> inputVirtualChannels = new ArrayList<>();

        for(InputPort inputPort : this.router.getInputPorts().values()) {
            inputVirtualChannels.addAll(inputPort.getVirtualChannels());
        }

        this.arbiters = new LinkedHashMap<>();

        for(OutputPort outputPort : this.router.getOutputPorts().values()) {
            for(OutputVirtualChannel outputVirtualChannel : outputPort.getVirtualChannels()) {
                this.arbiters.put(outputVirtualChannel, new VirtualChannelArbiter(outputVirtualChannel, inputVirtualChannels));
            }
        }
    }

    public void stageVirtualChannelAllocation() {
        for(OutputPort outputPort : this.router.getOutputPorts().values()) {
            for(OutputVirtualChannel outputVirtualChannel : outputPort.getVirtualChannels()) {
                if(outputVirtualChannel.getInputVirtualChannel() == null) {
                    InputVirtualChannel winnerInputVirtualChannel = this.arbiters.get(outputVirtualChannel).next();

                    if(winnerInputVirtualChannel != null) {
                        Flit flit = winnerInputVirtualChannel.getInputBuffer().peek();
                        flit.setState(FlitState.VIRTUAL_CHANNEL_ALLOCATION);

                        winnerInputVirtualChannel.setOutputVirtualChannel(outputVirtualChannel);
                        outputVirtualChannel.setInputVirtualChannel(winnerInputVirtualChannel);
                    }
                }
            }
        }
    }

    public Router getRouter() {
        return router;
    }

    public Map<OutputVirtualChannel, VirtualChannelArbiter> getArbiters() {
        return arbiters;
    }
}
