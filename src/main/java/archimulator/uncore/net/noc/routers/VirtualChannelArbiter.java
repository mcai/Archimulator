package archimulator.uncore.net.noc.routers;

import archimulator.uncore.net.noc.util.RoundRobinArbiter;

import java.util.ArrayList;
import java.util.List;

/**
 * Virtual channel arbiter.
 *
 * @author Min Cai
 */
public class VirtualChannelArbiter extends RoundRobinArbiter<OutputVirtualChannel, InputVirtualChannel> {
    private List<InputVirtualChannel> inputVirtualChannels;

    public VirtualChannelArbiter(OutputVirtualChannel outputVirtualChannel) {
        super(outputVirtualChannel);
    }

    @Override
    protected List<InputVirtualChannel> getRequesters() {
        if(inputVirtualChannels == null) {
            inputVirtualChannels = new ArrayList<>();

            for(InputPort inputPort : this.getResource().getOutputPort().getRouter().getInputPorts().values()) {
                inputVirtualChannels.addAll(inputPort.getVirtualChannels());
            }
        }

        return inputVirtualChannels;
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
