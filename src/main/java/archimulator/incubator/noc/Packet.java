package archimulator.incubator.noc;

import archimulator.util.action.Action;

import java.util.List;

/**
 * Packet.
 *
 * @author Min Cai
 */
public class Packet {
    private Network network;

    private long id;

    private long beginCycle;
    private long endCycle;

    private int src;
    private int dest;

    private int size;

    private Action onCompletedCallback;

    private List<PacketMemoryEntry> memory;

    private List<Flit> flits;

    public Packet(Network network, int src, int dest, int size) {
        this.network = network;

        this.id = this.network.currentPacketId++;

        this.beginCycle = this.network.getCycleAccurateEventQueue().getCurrentCycle();
        this.endCycle = -1;

        this.src = src;
        this.dest = dest;

        this.size = size;
    }
}
