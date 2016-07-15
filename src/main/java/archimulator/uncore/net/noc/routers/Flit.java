package archimulator.uncore.net.noc.routers;

import archimulator.uncore.net.noc.Node;
import archimulator.uncore.net.noc.Packet;

import java.util.Map;

/**
 * Flit.
 *
 * @author Min Cai
 */
public class Flit {
    private Packet packet;

    private int num;

    private boolean head;

    private boolean tail;

    private Node node;

    private FlitState state;

    private long prevStateTimestamp;

    private long timestamp;

    /**
     * Create a flit.
     *
     * @param packet the packet
     * @param num the num
     * @param head whether the flit is head flit or not
     * @param tail whether the flit is tail flit or not
     */
    public Flit(Packet packet, int num, boolean head, boolean tail) {
        this.packet = packet;
        this.packet.getFlits().add(this);

        this.num = num;

        this.head = head;

        this.tail = tail;

        this.node = null;

        this.state = null;

        this.prevStateTimestamp = this.timestamp = this.packet.getNetwork().getCycleAccurateEventQueue().getCurrentCycle();
    }

    @Override
    public String toString() {
        return String.format(
                "Flit{packet.id=%d, num=%d, head=%s, tail=%s, node.id=%s, state=%s, timestamp=%d}",
                packet.getId(), num, head, tail, node.getId(), state, timestamp
        );
    }

    /**
     * Get the packet.
     *
     * @return the packet
     */
    public Packet getPacket() {
        return packet;
    }

    /**
     * Get the num.
     *
     * @return the num
     */
    public int getNum() {
        return num;
    }

    /**
     * Get a boolean value indicating whether the flit is head flit or not.
     *
     * @return a boolean value indicating whether the flit is head flit or not
     */
    public boolean isHead() {
        return head;
    }

    /**
     * Get the boolean value indicating whether the flit is tail flit or not.
     *
     * @return a boolean value indicating whether the flit is tail flit or not
     */
    public boolean isTail() {
        return tail;
    }

    /**
     * Get the node.
     *
     * @return the node
     */
    public Node getNode() {
        return node;
    }

    /**
     * Get the state.
     *
     * @return the state
     */
    public FlitState getState() {
        return state;
    }

    /**
     * Set the node and state.
     *
     * @param node the node
     * @param state the state
     */
    public void setNodeAndState(Node node, FlitState state) {
        if(state == this.state) {
            throw new IllegalArgumentException(String.format("Flit is already in the %s state", state));
        }

        if(this.state != null) {
            this.packet.getNetwork().logFlitPerStateDelay(
                    this.head,
                    this.tail,
                    this.state,
                    (int) (this.packet.getNetwork().getCycleAccurateEventQueue().getCurrentCycle() - this.prevStateTimestamp)
            );

            if(this.getNumInflightFlits().get(this.state) == 0) {
                throw new IllegalArgumentException();
            }

            this.getNumInflightFlits().put(
                    this.state,
                    this.getNumInflightFlits().get(this.state) - 1
            );
        }

        this.state = state;
        this.node = node;

        if(this.state != FlitState.DESTINATION_ARRIVED) {
            this.getNumInflightFlits().put(
                    this.state,
                    this.getNumInflightFlits().get(this.state) + 1
            );
        }

        this.prevStateTimestamp = this.packet.getNetwork().getCycleAccurateEventQueue().getCurrentCycle();
    }

    /**
     * Get the number of inflight flits.
     *
     * @return the number of inflight flits
     */
    private Map<FlitState, Integer> getNumInflightFlits() {
        return this.head ? this.node.getRouter().getNumInflightHeadFlits() : this.node.getRouter().getNumInflightNonHeadFlits();
    }

    /**
     * Get the timestamp.
     *
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }
}
