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
package archimulator.util;

import archimulator.util.io.CommandLineHelper;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

public class ThreadPool {
    private BlockingQueue queue = new BlockingQueue();
    private boolean closed = true;
    private int poolSize;

    public ThreadPool(int poolSize) {
        this.poolSize = poolSize;
    }

    public synchronized void open() {
        if (!closed) {
            throw new IllegalStateException("Pool already started.");
        }
        closed = false;
        for (int i = 0; i < poolSize; ++i) {
            new PooledThread().start();
        }
    }

    public synchronized void execute(Runnable job) {
        if (closed) {
            throw new PoolClosedException();
        }
        queue.enqueue(job);
    }

    private class PooledThread extends Thread {
        public void run() {
            while (true) {
                Runnable job = (Runnable) queue.dequeue();
                if (job == null) {
                    break;
                }
                try {
                    job.run();
                } catch (Throwable t) {
                    // ignore
                }
            }
        }
    }

    public void close() {
        closed = true;
        queue.close();
    }

    public int getPoolSize() {
        return poolSize;
    }

    private static class PoolClosedException extends RuntimeException {
        PoolClosedException() {
            super("Pool closed.");
        }
    }

    class BlockingQueue {
        private LinkedList<Object> list = new LinkedList<Object>();
        private boolean closed = false;
        private boolean wait = false;

        public synchronized void enqueue(Object o) {
            if (closed) {
                throw new ClosedException();
            }
            list.add(o);
            notify();
        }

        public synchronized Object dequeue() {
            while (!closed && list.size() == 0) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    // ignore
                }
            }
            if (list.size() == 0) {
                return null;
            }
            return list.removeFirst();
        }

        public synchronized int size() {
            return list.size();
        }

        public synchronized void close() {
            closed = true;
            notifyAll();
        }

        public synchronized void open() {
            closed = false;
        }
    }

    public class ClosedException extends RuntimeException {
        ClosedException() {
            super("Queue closed.");
        }
    }

    public static void main(String[] args) {
        System.out.println("Total memory: " + CommandLineHelper.invokeShellCommandAndGetResult("cat /proc/meminfo | grep MemTotal").get(0).split("\\s+")[1] + "KB");
        System.out.println("Free memory: " + CommandLineHelper.invokeShellCommandAndGetResult("cat /proc/meminfo | grep MemFree").get(0).split("\\s+")[1] + "KB");

        int numProcessors = Integer.parseInt(CommandLineHelper.invokeShellCommandAndGetResult("cat /proc/cpuinfo | grep -c processor").get(0));

        System.out.println("Using " + numProcessors + " threads to execute jobs...");

        ThreadPool threadPool = new ThreadPool(numProcessors);
        threadPool.open();

        final Random random = new Random();

        for (int i = 0; i < 100; i++) {
            final int finalI = i;
            threadPool.execute(new Runnable() {
                public void run() {
                    System.out.println("Executing job #" + finalI + "...");

                    try {
                        Thread.sleep(random.nextInt(5) * 1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    System.out.println("Job #" + finalI + " done.");
                }
            });
        }
        
        threadPool.close();
    }
}