/*******************************************************************************
 * Copyright (c) 2010-2011 by Min Cai (min.cai.china@gmail.com).
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

import archimulator.util.action.Action2;

import java.util.ArrayList;
import java.util.List;

public class MultithreadedBlockingEventDispatcher<BlockingEventT extends BlockingEvent> extends BlockingEventDispatcher<BlockingEventT> {
    @SuppressWarnings({"unchecked"})
    public synchronized <BlockingEventK extends BlockingEventT> void dispatch(final Object sender, final BlockingEventK event) {
        Class<? extends BlockingEventT> eventClass = (Class<? extends BlockingEventT>) event.getClass();

        List<Thread> threads = new ArrayList<Thread>();

        if (this.listeners.containsKey(eventClass)) {
            for (final Action2<?, ? extends BlockingEventT> listener : this.listeners.get(eventClass)) {
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        ((Action2<Object, BlockingEventK>) listener).apply(sender, event);
                    }
                };

                threads.add(thread);
                thread.start();
            }

            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
