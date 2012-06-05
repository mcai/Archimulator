package archimulator.sim.base.event;

import net.pickapack.action.Action1;
import net.pickapack.action.Action2;
import net.pickapack.event.BlockingEvent;
import net.pickapack.event.BlockingEventDispatcher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MyBlockingEventDispatcher<BlockingEventT extends BlockingEvent> extends BlockingEventDispatcher<BlockingEventT> {
    private List<Action2<?, ? extends BlockingEventT>> simulationWideListeners = new ArrayList<Action2<?, ? extends BlockingEventT>>();

    public static enum ListenerType {
        EXPERIMENT_WIDE,
        SIMULATION_WIDE
    }

    public synchronized <BlockingEventK extends BlockingEventT> void addListener2(Class<BlockingEventK> eventClass, ListenerType listenerType, final Action1<BlockingEventK> listener) {
        ProxyAction2<BlockingEventK> listener2 = new ProxyAction2<BlockingEventK>(listener);
        super.addListener(eventClass, listener2);
        if(listenerType == ListenerType.SIMULATION_WIDE) {
            this.simulationWideListeners.add(listener2);
        }
    }

    public synchronized <BlockingEventK extends BlockingEventT> void addListener(Class<BlockingEventK> eventClass, final Action1<BlockingEventK> listener) {
        throw new UnsupportedOperationException();
    }

    public void clearSimulationWideListeners() {
        for(Class<? extends BlockingEventT> eventClass : this.listeners.keySet()) {
            List<Action2<?, ? extends BlockingEventT>> listenersPerEventClass = this.listeners.get(eventClass);
            for (Iterator<Action2<?, ? extends BlockingEventT>> iterator = listenersPerEventClass.iterator(); iterator.hasNext(); ) {
                Action2<?, ? extends BlockingEventT> listener = iterator.next();
                if (this.simulationWideListeners.contains(listener)) {
                    iterator.remove();
                }
            }
        }

        this.simulationWideListeners.clear();
    }
}
