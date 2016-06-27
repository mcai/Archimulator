package archimulator.incubator.noc.routers;

import javaslang.collection.LinkedHashMap;
import javaslang.collection.List;
import javaslang.collection.Map;

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

        List<InputVirtualChannel> inputVirtualChannels =
                this.router.getInputPorts().values().map(InputPort::getVirtualChannels).reduce(List::appendAll);

        this.arbiters = LinkedHashMap.empty();

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
                    InputVirtualChannel winnerInputVirtualChannel = this.arbiters.get(outputVirtualChannel).get().next();

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
