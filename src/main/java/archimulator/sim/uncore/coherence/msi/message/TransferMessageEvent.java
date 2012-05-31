package archimulator.sim.uncore.coherence.msi.message;

import archimulator.sim.uncore.coherence.msi.controller.Controller;
import net.pickapack.action.Action;
import net.pickapack.event.CycleAccurateEvent;

public class TransferMessageEvent extends CycleAccurateEvent {
    private final Controller from;
    private final Controller to;
    private CoherenceMessage message;

    public TransferMessageEvent(Controller from, final Controller to, final CoherenceMessage message, long when) {
        super(from, new Action() {
            @Override
            public void apply() {
                message.onCompleted();
                to.receive(message);
            }
        }, when);
        this.from = from;
        this.to = to;
        this.message = message;
    }

    public Controller getFrom() {
        return from;
    }

    public Controller getTo() {
        return to;
    }

    public CoherenceMessage getMessage() {
        return message;
    }
}
