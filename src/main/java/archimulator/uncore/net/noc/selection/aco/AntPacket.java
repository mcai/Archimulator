package archimulator.uncore.net.noc.selection.aco;

import archimulator.uncore.net.noc.Network;
import archimulator.uncore.net.noc.Packet;
import archimulator.util.action.Action;

/**
 * Ant packet.
 *
 * @author Min Cai
 */
public abstract class AntPacket extends Packet {
    private boolean forward;

    public AntPacket(Network network, int src, int dest, int size, boolean forward, Action onCompletedCallback) {
        super(network, src, dest, size, onCompletedCallback);

        this.forward = forward;
    }

    public boolean isForward() {
        return forward;
    }
}
