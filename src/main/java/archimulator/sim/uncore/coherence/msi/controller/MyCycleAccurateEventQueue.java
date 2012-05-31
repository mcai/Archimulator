package archimulator.sim.uncore.coherence.msi.controller;

import archimulator.sim.uncore.coherence.msi.message.CoherenceMessage;
import archimulator.sim.uncore.coherence.msi.message.TransferMessageEvent;
import net.pickapack.event.CycleAccurateEvent;
import net.pickapack.event.CycleAccurateEventQueue;

public class MyCycleAccurateEventQueue extends CycleAccurateEventQueue {
    public void schedule(Controller from, Controller to, CoherenceMessage message, int delay) {
        long when = this.getCurrentCycle() + delay;

        for (CycleAccurateEvent event : this.events) {
            if (event instanceof TransferMessageEvent) {
                TransferMessageEvent transferMessageEvent = (TransferMessageEvent) event;
                if (transferMessageEvent.getFrom() == from && transferMessageEvent.getTo() == to && transferMessageEvent.getMessage().getId() < message.getId() && transferMessageEvent.getWhen() >= when) {
                    when = transferMessageEvent.getWhen() + 1;
                }
            }
        }

        this.schedule(new TransferMessageEvent(from, to, message, when));
    }
}
