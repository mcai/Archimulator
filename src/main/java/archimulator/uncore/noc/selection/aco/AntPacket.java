package archimulator.uncore.noc.selection.aco;

import archimulator.uncore.noc.Network;
import archimulator.uncore.noc.Packet;

/**
 * Ant packet.
 *
 * @author Min Cai
 */
public abstract class AntPacket extends Packet {
    private boolean forward;

    public AntPacket(Network network, int src, int dest, int size, boolean forward, Runnable onCompletedCallback) {
        super(network, src, dest, size, onCompletedCallback);

        this.forward = forward;
    }

    public boolean isForward() {
        return forward;
    }
}
