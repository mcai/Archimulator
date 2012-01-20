/*******************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
 *
 * This file is part of the Archimulator multicore architectural simulator.
 *
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package archimulator.util.event;

import archimulator.util.action.Action1;
import archimulator.util.action.Action2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BlockingEventDispatcher<BlockingEventT extends BlockingEvent> implements Serializable {
    protected final Map<Class<? extends BlockingEventT>, List<Action2<?, ? extends BlockingEventT>>> listeners;

    public BlockingEventDispatcher() {
        this.listeners = new LinkedHashMap<Class<? extends BlockingEventT>, List<Action2<?, ? extends BlockingEventT>>>();
    }

    public synchronized <BlockingEventK extends BlockingEventT> void dispatch(BlockingEventK event) {
        this.dispatch(null, event);
    }

    @SuppressWarnings({"unchecked"})
    public synchronized <BlockingEventK extends BlockingEventT> void dispatch(Object sender, BlockingEventK event) {
        Class<? extends BlockingEventT> eventClass = (Class<? extends BlockingEventT>) event.getClass();

        if (this.listeners.containsKey(eventClass)) {
            for (Action2<?, ? extends BlockingEventT> listener : this.listeners.get(eventClass)) {
                ((Action2<Object, BlockingEventK>) listener).apply(sender, event);
            }
        }
    }

    public synchronized <BlockingEventK extends BlockingEventT> void addListener(Class<BlockingEventK> eventClass, final Action1<BlockingEventK> listener) {
        this.addListener(eventClass, new ProxyAction2<BlockingEventK>(listener));
    }

    public synchronized <BlockingEventK extends BlockingEventT> void addListener(Class<BlockingEventK> eventClass, Action2<?, BlockingEventK> listener) {
        if (!this.listeners.containsKey(eventClass)) {
            this.listeners.put(eventClass, new ArrayList<Action2<?, ? extends BlockingEventT>>());
        }

        if (!this.listeners.get(eventClass).contains(listener)) {
            this.listeners.get(eventClass).add(listener);
        }
    }

    public synchronized <BlockingEventK extends BlockingEventT> void removeListener(Class<BlockingEventK> eventClass, Action1<BlockingEventK> listener) {
        if (this.listeners.containsKey(eventClass)) {
            List<Action2<?, ? extends BlockingEventT>> listenersInTheEventClass = this.listeners.get(eventClass);

            for (Action2<?, ? extends BlockingEventT> listenerFound : listenersInTheEventClass) {
                if (listenerFound instanceof ProxyAction2) {
                    if (((ProxyAction2) listenerFound).listener == listener) {
                        this.listeners.get(eventClass).remove(listenerFound);
                        break;
                    }
                }
            }
        }
    }

    public synchronized <BlockingEventK extends BlockingEventT> void removeListener(Class<BlockingEventK> eventClass, Action2<?, BlockingEventK> listener) {
        if (this.listeners.containsKey(eventClass)) {
            this.listeners.get(eventClass).remove(listener);
        }
    }

    public synchronized void clearListeners() {
        this.listeners.clear();
    }

    private class ProxyAction2<BlockingEventK extends BlockingEventT> implements Action2<Object, BlockingEventK> {
        private Action1<BlockingEventK> listener;

        public ProxyAction2(Action1<BlockingEventK> listener) {
            this.listener = listener;
        }

        public void apply(Object param1, BlockingEventK param2) {
            this.listener.apply(param2);
        }
    }
}