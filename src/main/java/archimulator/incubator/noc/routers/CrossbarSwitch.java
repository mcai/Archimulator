package archimulator.incubator.noc.routers;

import archimulator.incubator.noc.Direction;
import archimulator.incubator.noc.Node;

/**
 * Crossbar switch.
 *
 * @author Min Cai
 */
public class CrossbarSwitch {
    private Router router;

    public CrossbarSwitch(Router router) {
        this.router = router;
    }

    public void stageSwitchTraversal() {
        for (OutputPort outputPort: this.router.getOutputPorts().values()) {
            for (InputPort inputPort : this.router.getInputPorts().values()) {
                if (outputPort.getDirection() == inputPort.getDirection()) {
                    continue;
                }

                for(InputVirtualChannel inputVirtualChannel : inputPort.getVirtualChannels()) {
                    if(inputVirtualChannel.getOutputVirtualChannel() != null
                            && inputVirtualChannel.getOutputVirtualChannel().getOutputPort() != outputPort) {
                        Flit flit = inputVirtualChannel.getInputBuffer().peek();
                        if(flit != null && flit.getState() == FlitState.SWITCH_ALLOCATION) {
                            flit.setState(FlitState.SWITCH_TRAVERSAL);

                            if(inputPort.getDirection() != Direction.LOCAL) {
                                Node parent = this.router.getNode().getNetwork().getNodes().get(
                                        this.router.getNode().getNeighbors().get(inputPort.getDirection())
                                );

                                OutputVirtualChannel outputVirtualChannelAtParent =
                                        parent.getRouter().getOutputPorts().get(inputPort.getDirection().getReflexDirection()).get()
                                                .getVirtualChannels().get(inputVirtualChannel.getId());

                                outputVirtualChannelAtParent.setCredits(outputVirtualChannelAtParent.getCredits() + 1);
                            }
                        }
                    }
                }
            }
        }
    }

    public Router getRouter() {
        return router;
    }
}
