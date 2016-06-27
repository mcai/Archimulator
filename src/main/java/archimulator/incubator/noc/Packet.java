package archimulator.incubator.noc;

import archimulator.util.action.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Packet.
 *
 * @author Min Cai
 */
public abstract class Packet {
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

    public Packet(Network network, int src, int dest, int size, Action onCompletedCallback) {
        this.network = network;

        this.id = this.network.currentPacketId++;

        this.beginCycle = this.network.getCycleAccurateEventQueue().getCurrentCycle();
        this.endCycle = -1;

        this.src = src;
        this.dest = dest;

        this.size = size;

        int numFlits = (int) Math.ceil((double)(this.size) / this.network.getExperiment().getConfig().getLinkWidth());
        if(numFlits > this.network.getExperiment().getConfig().getMaxInputBufferSize()) {
            throw new IllegalArgumentException(
                    String.format("Number of flits (%d) in a packet cannot be greater than max input buffer size: %d",
                            numFlits, this.network.getExperiment().getConfig().getMaxInputBufferSize()
                    )
            );
        }

        this.onCompletedCallback = onCompletedCallback;

        this.memory = new ArrayList<>();

        this.flits = new ArrayList<>();
    }

    @Override
    public String toString() {
        return String.format("Packet{id=%d, src=%d, dest=%d, beginCycle=%d, delay=%d, size=%d, memory=%s}",
                id, src, dest, beginCycle, delay(), size, memory);
    }

    public int delay() {
        return this.endCycle != -1 ? (int)(this.endCycle - this.beginCycle) : -1;
    }

    public void memorize(int currentNodeId) {
        if(this.memory.stream().map(PacketMemoryEntry::getNodeId).collect(Collectors.toList()).contains(currentNodeId)) {
            throw new IllegalArgumentException(String.format("%d", currentNodeId));
        }

        this.memory.add(new PacketMemoryEntry(currentNodeId, this.network.getCycleAccurateEventQueue().getCurrentCycle()));
    }

    public int hops() {
        return this.memory.size();
    }

    public abstract boolean hasPayload();

    public Network getNetwork() {
        return network;
    }

    public long getId() {
        return id;
    }

    public long getBeginCycle() {
        return beginCycle;
    }

    public long getEndCycle() {
        return endCycle;
    }

    public void setEndCycle(long endCycle) {
        this.endCycle = endCycle;
    }

    public int getSrc() {
        return src;
    }

    public int getDest() {
        return dest;
    }

    public int getSize() {
        return size;
    }

    public Action getOnCompletedCallback() {
        return onCompletedCallback;
    }

    public List<PacketMemoryEntry> getMemory() {
        return memory;
    }

    public List<Flit> getFlits() {
        return flits;
    }
}
