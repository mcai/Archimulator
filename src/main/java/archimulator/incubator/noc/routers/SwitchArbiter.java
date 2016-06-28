package archimulator.incubator.noc.routers;

import archimulator.incubator.noc.util.RoundRobinArbiter;

import java.util.List;

/**
 * Switch arbiter.
 *
 * @author Min Cai
 */
public class SwitchArbiter extends RoundRobinArbiter<OutputPort, InputVirtualChannel> {
    public SwitchArbiter(OutputPort outputPort, List<InputVirtualChannel> inputVirtualChannels) {
        super(outputPort, inputVirtualChannels);
    }

    @Override
    protected boolean resourceAvailable(OutputPort resource) {
        return true;
    }

    @Override
    protected boolean requesterHasRequests(InputVirtualChannel requester) {
        if(requester.getOutputVirtualChannel() != null && requester.getOutputVirtualChannel().getOutputPort() == this.getResource()) {
            Flit flit = requester.getInputBuffer().peek();
            return flit != null
                    && (flit.isHead() && flit.getState() == FlitState.VIRTUAL_CHANNEL_ALLOCATION
                    || !flit.isHead() && flit.getState() == FlitState.INPUT_BUFFER);
        }

        return false;
    }
}
