package archimulator.incubator.noc;

import archimulator.util.action.Action;

/**
 * Data packet.
 *
 * @author Min Cai
 */
public class DataPacket extends Packet {
    public DataPacket(Network network, int src, int dest, int size, Action onCompletedCallback) {
        super(network, src, dest, size, onCompletedCallback);
    }

    @Override
    public boolean hasPayload() {
        return true;
    }
}
