package archimulator.incubator.noc.routers;

import javaslang.collection.List;

/**
 * Input buffer.
 *
 * @author Min Cai
 */
public class InputBuffer {
    private InputVirtualChannel inputVirtualChannel;

    private List<Flit> flits;

    public InputBuffer(InputVirtualChannel inputVirtualChannel) {
        this.inputVirtualChannel = inputVirtualChannel;
        this.flits = List.empty();
    }

    public void append(Flit flit) {
        if(this.flits.size() + 1 >
                this.inputVirtualChannel.getInputPort().getRouter().getNode().getNetwork().getExperiment().getConfig().getMaxInputBufferSize()) {
            throw new IllegalArgumentException();
        }

        this.flits.append(flit);
    }

    public Flit peek() {
        return !this.flits.isEmpty() ? this.flits.get(0) : null;
    }

    public void pop() {
        if(!this.flits.isEmpty()) {
            this.flits.removeAt(0);
        }
    }

    public boolean full() {
        return this.flits.size() >=
                this.inputVirtualChannel.getInputPort().getRouter().getNode().getNetwork().getExperiment().getConfig().getMaxInputBufferSize();
    }

    public int size() {
        return this.flits.size();
    }

    public InputVirtualChannel getInputVirtualChannel() {
        return inputVirtualChannel;
    }
}
