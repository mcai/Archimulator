package archimulator.uncore.net.noc.selection.aco;

import archimulator.uncore.net.noc.Network;

public class ForwardAntPacket extends AntPacket {
    public ForwardAntPacket(Network network, int src, int dest, int size, Runnable onCompletedCallback) {
        super(network, src, dest, size, true, onCompletedCallback);
    }

    @Override
    public String toString() {
        return String.format("ForwardAntPacket{id=%d, src=%d, dest=%d, beginCycle=%d, delay=%d, size=%d, memory=%s}",
                getId(), getSrc(), getDest(), getBeginCycle(), delay(), getSize(), getMemory());
    }

    @Override
    public boolean hasPayload() {
        return false;
    }
}
