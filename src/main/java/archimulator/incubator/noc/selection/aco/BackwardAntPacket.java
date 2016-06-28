package archimulator.incubator.noc.selection.aco;

import archimulator.incubator.noc.Network;
import archimulator.util.action.Action;

public class BackwardAntPacket extends AntPacket {
    public BackwardAntPacket(Network network, int src, int dest, int size, Action onCompletedCallback) {
        super(network, src, dest, size, false, onCompletedCallback);
    }

    @Override
    public String toString() {
        return String.format("BackwardAntPacket{id=%d, src=%d, dest=%d, beginCycle=%d, delay=%d, size=%d, memory=%s}",
                getId(), getSrc(), getDest(), getBeginCycle(), delay(), getSize(), getMemory());
    }

    @Override
    public boolean hasPayload() {
        return false;
    }
}