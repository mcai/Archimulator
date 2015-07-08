/**
 * ****************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the PickaPack library.
 * <p>
 * PickaPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * PickaPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with PickaPack. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.util.event;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
    protected final Map<Class<? extends BlockingEventT>, List<BiConsumer<?, ? extends BlockingEventT>>> listeners;

    /**
     * Map of any event listeners.
     */
    protected final List<BiConsumer<Object, BlockingEventT>> anyListeners;

    /**
     * Create a blocking event dispatcher.
     */
    public BlockingEventDispatcher() {
        this.listeners = new LinkedHashMap<>();
        this.anyListeners = new ArrayList<>();
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
            for (BiConsumer<?, ? extends BlockingEventT> listener : this.listeners.get(eventClass)) {
                ((BiConsumer<Object, BlockingEventK>) listener).accept(sender, event);
            }
        }

        for (BiConsumer<Object, BlockingEventT> anyListener : this.anyListeners) {
            anyListener.accept(sender, event);
        }
    }

    /**
     * Add a listener for the specified event class.
     *
     * @param <BlockingEventK> the type of the event
     * @param eventClass       the event class
     * @param listener         the listener that is to be added
     */
    public synchronized <BlockingEventK extends BlockingEventT> void addListener(Class<BlockingEventK> eventClass, final Consumer<BlockingEventK> listener) {
        this.addListener(eventClass, new ProxyAction2<>(listener));
    }

    /**
     * Add a listener for the specified event class.
     *
     * @param <BlockingEventK> the type of the event
     * @param eventClass       the event class
     * @param listener         the listener that is to be added
     */
    public synchronized <BlockingEventK extends BlockingEventT> void addListener(Class<BlockingEventK> eventClass, BiConsumer<?, BlockingEventK> listener) {
        if (!this.listeners.containsKey(eventClass)) {
            this.listeners.put(eventClass, new ArrayList<>());
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
    public synchronized void addAnyListener(BiConsumer<Object, BlockingEventT> listener) {
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
    public synchronized <BlockingEventK extends BlockingEventT> void removeListener(Class<BlockingEventK> eventClass, Consumer<BlockingEventK> listener) {
        if (this.listeners.containsKey(eventClass)) {
            List<BiConsumer<?, ? extends BlockingEventT>> listenersInTheEventClass = this.listeners.get(eventClass);

            for (BiConsumer<?, ? extends BlockingEventT> listenerFound : listenersInTheEventClass) {
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
    public synchronized void removeAnyListener(BiConsumer<Object, BlockingEventT> listener) {
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
    public synchronized <BlockingEventK extends BlockingEventT> void removeListener(Class<BlockingEventK> eventClass, BiConsumer<?, BlockingEventK> listener) {
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
    protected static class ProxyAction2<BlockingEventK> implements BiConsumer<Object, BlockingEventK> {
        private Consumer<BlockingEventK> listener;

        /**
         * Create a proxy action.
         *
         * @param listener the listener
         */
        public ProxyAction2(Consumer<BlockingEventK> listener) {
            this.listener = listener;
        }

        /**
         * Apply.
         *
         * @param param1 the first parameter
         * @param param2 the second parameter
         */
        @Override
        public void accept(Object param1, BlockingEventK param2) {
            this.listener.accept(param2);
        }
    }
}