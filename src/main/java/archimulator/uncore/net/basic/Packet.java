package archimulator.uncore.net.basic;

import archimulator.uncore.MemoryDevice;
import archimulator.util.action.Action;

/**
 * Packet.
 *
 * @author Min Cai
 */
public class Packet {
    private BasicNet net;

    private long id;

    private MemoryDevice from;
    private MemoryDevice to;

    private int size;
    private Action onCompletedCallback;

    /**
     * Create a packet.
     *
     * @param net the net
     * @param from the source memory device
     * @param to the destination memory device
     * @param size the size of the packet
     * @param onCompletedCallback the callback performed on completion
     */
    public Packet(BasicNet net, MemoryDevice from, MemoryDevice to, int size, Action onCompletedCallback) {
        this.net = net;
        this.id = net.currentRequestId++;
        this.from = from;
        this.to = to;
        this.size = size;
        this.onCompletedCallback = onCompletedCallback;
    }

    /**
     * Get the net.
     *
     * @return the net
     */
    public BasicNet getNet() {
        return net;
    }

    /**
     * Get the ID of the packet.
     *
     * @return the ID of the packet
     */
    public long getId() {
        return id;
    }

    /**
     * Get the source memory device.
     *
     * @return the source memory device
     */
    public MemoryDevice getFrom() {
        return from;
    }

    /**
     * Get the destination memory device.
     *
     * @return the destination memory device
     */
    public MemoryDevice getTo() {
        return to;
    }

    /**
     * Get the size of the packet.
     *
     * @return the size of the packet
     */
    public int getSize() {
        return size;
    }

    /**
     * Get the callback performed on completion.
     *
     * @return the callback performed on completion
     */
    public Action getOnCompletedCallback() {
        return onCompletedCallback;
    }
}
