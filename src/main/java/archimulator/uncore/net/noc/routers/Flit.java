package archimulator.uncore.net.noc.routers;

import archimulator.uncore.net.noc.Node;
import archimulator.uncore.net.noc.Packet;

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

    public Packet getPacket() {
        return packet;
    }

    public int getNum() {
        return num;
    }

    public boolean isHead() {
        return head;
    }

    public boolean isTail() {
        return tail;
    }

    public Node getNode() {
        return node;
    }

    public FlitState getState() {
        return state;
    }

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

            if(this.node.getRouter().getNumInflightFlits().get(this.state) == 0) {
                throw new IllegalArgumentException();
            }

            this.node.getRouter().getNumInflightFlits().put(
                    this.state,
                    this.node.getRouter().getNumInflightFlits().get(this.state) - 1
            );
        }

        this.state = state;
        this.node = node;

        if(this.state != FlitState.DESTINATION_ARRIVED) {
            this.node.getRouter().getNumInflightFlits().put(
                    this.state,
                    this.node.getRouter().getNumInflightFlits().get(this.state) + 1
            );
        }

        this.prevStateTimestamp = this.packet.getNetwork().getCycleAccurateEventQueue().getCurrentCycle();
    }

    public long getTimestamp() {
        return timestamp;
    }
}
