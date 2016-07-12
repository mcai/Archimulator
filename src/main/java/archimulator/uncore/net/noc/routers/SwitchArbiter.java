package archimulator.uncore.net.noc.routers;

import archimulator.uncore.net.noc.util.RoundRobinArbiter;

import java.util.ArrayList;
import java.util.List;

/**
 * Switch arbiter.
 *
 * @author Min Cai
 */
public class SwitchArbiter extends RoundRobinArbiter<OutputPort, InputVirtualChannel> {
    private List<InputVirtualChannel> inputVirtualChannels;

    public SwitchArbiter(OutputPort outputPort) {
        super(outputPort);
    }

    @Override
    protected List<InputVirtualChannel> getRequesters() {
        if(inputVirtualChannels == null) {
            inputVirtualChannels = new ArrayList<>();

            for(InputPort inputPort : this.getResource().getRouter().getInputPorts().values()) {
                inputVirtualChannels.addAll(inputPort.getVirtualChannels());
            }
        }

        return inputVirtualChannels;
    }

    @Override
    protected boolean resourceAvailable(OutputPort outputPort) {
        return true;
    }

    @Override
    protected boolean requesterHasRequests(InputVirtualChannel inputVirtualChannel) {
        if(inputVirtualChannel.getOutputVirtualChannel() != null
                && inputVirtualChannel.getOutputVirtualChannel().getOutputPort() == this.getResource()) {
            Flit flit = inputVirtualChannel.getInputBuffer().peek();
            return flit != null
                    && (flit.isHead() && flit.getState() == FlitState.VIRTUAL_CHANNEL_ALLOCATION
                    || !flit.isHead() && flit.getState() == FlitState.INPUT_BUFFER);
        }

        return false;
    }
}
