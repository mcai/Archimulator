package archimulator.incubator.noc;

/**
 * Packet memory entry.
 *
 * @author Min Cai
 */
public class PacketMemoryEntry {
    private int nodeId;
    private long timestamp;

    public PacketMemoryEntry(int nodeId, long timestamp) {
        this.nodeId = nodeId;
        this.timestamp = timestamp;
    }

    public int getNodeId() {
        return nodeId;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
