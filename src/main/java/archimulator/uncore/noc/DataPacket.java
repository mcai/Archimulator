package archimulator.uncore.noc;

/**
 * Data packet.
 *
 * @author Min Cai
 */
public class DataPacket extends Packet {
    public DataPacket(Network network, int src, int dest, int size, Runnable onCompletedCallback) {
        super(network, src, dest, size, onCompletedCallback);
    }

    @Override
    public boolean hasPayload() {
        return true;
    }
}
