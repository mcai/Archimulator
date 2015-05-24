/*******************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
 *
 * This file is part of the PickaPack library.
 *
 * PickaPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PickaPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PickaPack. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package archimulator.util.event;

import archimulator.util.action.Action1;
import archimulator.util.action.Action2;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Blocking event dispatcher.
 *
 * @param <BlockingEventT> the type of the event
 * @author Min Cai
 */
public class BlockingEventDispatcher<BlockingEventT extends BlockingEvent> {
    /**
     * Map of event listeners.
     */
    protected final Map<Class<? extends BlockingEventT>, List<Action2<?, ? extends BlockingEventT>>> listeners;

    /**
     * Map of any event listeners.
     */
    protected final List<Action2<Object, BlockingEventT>> anyListeners;

    /**
     * Create a blocking event dispatcher.
     */
    public BlockingEventDispatcher() {
        this.listeners = new LinkedHashMap<Class<? extends BlockingEventT>, List<Action2<?, ? extends BlockingEventT>>>();
        this.anyListeners = new ArrayList<Action2<Object, BlockingEventT>>();
    }

    /**
     * Dispatch the specified event.
     *
     * @param <BlockingEventK> the type of the event
     * @param event            the event
     */
    public synchronized <BlockingEventK extends BlockingEventT> void dispatch(BlockingEventK event) {
        this.dispatch(null, event);
    }

    /**
     * Dispatch the specified event from the specified sender.
     *
     * @param <BlockingEventK> the type of the event
     * @param sender           the sender
     * @param event            the event
     */
    @SuppressWarnings({"unchecked"})
    public synchronized <BlockingEventK extends BlockingEventT> void dispatch(Object sender, BlockingEventK event) {
        Class<? extends BlockingEventT> eventClass = (Class<? extends BlockingEventT>) event.getClass();

        if (this.listeners.containsKey(eventClass)) {
            for (Action2<?, ? extends BlockingEventT> listener : this.listeners.get(eventClass)) {
                ((Action2<Object, BlockingEventK>) listener).apply(sender, event);
            }
        }

        for (Action2<Object, BlockingEventT> anyListener : this.anyListeners) {
            anyListener.apply(sender, event);
        }
    }

    /**
     * Add a listener for the specified event class.
     *
     * @param <BlockingEventK> the type of the event
     * @param eventClass       the event class
     * @param listener         the listener that is to be added
     */
    public synchronized <BlockingEventK extends BlockingEventT> void addListener(Class<BlockingEventK> eventClass, final Action1<BlockingEventK> listener) {
        this.addListener(eventClass, new ProxyAction2<BlockingEventK>(listener));
    }

    /**
     * Add a listener for the specified event class.
     *
     * @param <BlockingEventK> the type of the event
     * @param eventClass       the event class
     * @param listener         the listener that is to be added
     */
    public synchronized <BlockingEventK extends BlockingEventT> void addListener(Class<BlockingEventK> eventClass, Action2<?, BlockingEventK> listener) {
        if (!this.listeners.containsKey(eventClass)) {
            this.listeners.put(eventClass, new ArrayList<Action2<?, ? extends BlockingEventT>>());
        }

        if (!this.listeners.get(eventClass).contains(listener)) {
            this.listeners.get(eventClass).add(listener);
        }
    }

    /**
     * Add an any listener.
     *
     * @param listener the listener that is to be added
     */
    public synchronized void addAnyListener(Action2<Object, BlockingEventT> listener) {
        if (!this.anyListeners.contains(listener)) {
            this.anyListeners.add(listener);
        }
    }

    /**
     * Remove the specified listener for the specified event class.
     *
     * @param <BlockingEventK> the type of the event
     * @param eventClass       the event class
     * @param listener         the listener that is to be removed
     */
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

    /**
     * Remove the specified any listener.
     *
     * @param listener         the listener that is to be removed
     */
    public synchronized void removeAnyListener(Action2<Object, BlockingEventT> listener) {
        if (this.anyListeners.contains(listener)) {
            this.anyListeners.remove(listener);
        }
    }

    /**
     * Remove the specified listener for the specified event class.
     *
     * @param <BlockingEventK> the type of the event
     * @param eventClass       the event class
     * @param listener         the listener that is to be removed
     */
    public synchronized <BlockingEventK extends BlockingEventT> void removeListener(Class<BlockingEventK> eventClass, Action2<?, BlockingEventK> listener) {
        if (this.listeners.containsKey(eventClass)) {
            this.listeners.get(eventClass).remove(listener);
        }
    }

    /**
     * Clear all the listeners.
     */
    public synchronized void clearListeners() {
        this.listeners.clear();
    }

    /**
     * Get a value indicating whether the listeners is empty or not.
     *
     * @return a value indicating whether the listeners is empty or not
     */
    public boolean isEmpty() {
        return this.listeners.isEmpty();
    }

    /**
     * Proxy action.
     *
     * @param <BlockingEventK> the type of the event
     */
    protected static class ProxyAction2<BlockingEventK> implements Action2<Object, BlockingEventK> {
        private Action1<BlockingEventK> listener;

        /**
         * Create a proxy action.
         *
         * @param listener the listener
         */
        public ProxyAction2(Action1<BlockingEventK> listener) {
            this.listener = listener;
        }

        /**
         * Apply.
         *
         * @param param1 the first parameter
         * @param param2 the second parameter
         */
        public void apply(Object param1, BlockingEventK param2) {
            this.listener.apply(param2);
        }
    }
}