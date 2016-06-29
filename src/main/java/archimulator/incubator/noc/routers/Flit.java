package archimulator.incubator.noc.routers;

import archimulator.incubator.noc.Node;
import archimulator.incubator.noc.Packet;

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

        this.state = FlitState.INPUT_BUFFER;

        this.prevStateTimestamp = this.timestamp = this.packet.getNetwork().getCycleAccurateEventQueue().getCurrentCycle();
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

    public void setNode(Node node) {
        this.node = node;
    }

    public FlitState getState() {
        return state;
    }

    public void setState(FlitState state) {
        if(state == this.state) {
            throw new IllegalArgumentException(String.format("%s", state));
        }

        this.packet.getNetwork().logFlitPerStateDelay(
                this.head,
                this.tail,
                this.state,
                (int) (this.packet.getNetwork().getCycleAccurateEventQueue().getCurrentCycle() - this.prevStateTimestamp)
        );

        this.state = state;

        this.prevStateTimestamp = this.packet.getNetwork().getCycleAccurateEventQueue().getCurrentCycle();
    }

    public long getPrevStateTimestamp() {
        return prevStateTimestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
