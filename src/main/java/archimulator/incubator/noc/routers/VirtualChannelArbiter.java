package archimulator.incubator.noc.routers;

import archimulator.incubator.noc.util.RoundRobinArbiter;

import java.util.List;

/**
 * Virtual channel arbiter.
 *
 * @author Min Cai
 */
public class VirtualChannelArbiter extends RoundRobinArbiter<OutputVirtualChannel, InputVirtualChannel> {
    public VirtualChannelArbiter(OutputVirtualChannel outputVirtualChannel, List<InputVirtualChannel> inputVirtualChannels) {
        super(outputVirtualChannel, inputVirtualChannels);
    }

    @Override
    protected boolean resourceAvailable(OutputVirtualChannel resource) {
        return resource.getInputVirtualChannel() == null;
    }

    @Override
    protected boolean requesterHasRequests(InputVirtualChannel requester) {
        if(requester.getRoute() == this.getResource().getOutputPort().getDirection()) {
            Flit flit = requester.getInputBuffer().peek();
            return flit != null && flit.isHead() && flit.getState() == FlitState.ROUTE_CALCULATION;
        }

        return false;
    }
}
