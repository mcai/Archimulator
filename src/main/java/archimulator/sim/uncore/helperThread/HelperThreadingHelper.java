/*******************************************************************************
 * Copyright (c) 2010-2013 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.sim.uncore.helperThread;

import archimulator.model.Benchmark;
import archimulator.service.ServiceManager;
import archimulator.sim.core.Thread;

/**
 * Helper threading helper.
 *
 * @author Min Cai
 */
public class HelperThreadingHelper {
    /**
     * Get a value indicating whether the specified thread is the main thread in the helper threading scheme.
     *
     * @param thread the thread
     * @return a value indicating whether the specified thread is the main thread if the helper threading scheme
     */
    public static boolean isMainThread(Thread thread) {
        return isMainThread(thread.getId());
    }

    /**
     * Get a value indicating whether the specified thread is the main thread in the helper threading scheme.
     *
     * @param threadId the thread ID
     * @return a value indicating whether the specified thread is the main thread in the helper threading scheme
     */
    public static boolean isMainThread(int threadId) {
        return threadId == getMainThreadId();
    }

    /**
     * Get the main thread ID in the helper threading scheme.
     *
     * @return the main thread ID in the helper threading scheme
     */
    public static int getMainThreadId() {
        return 0; //TODO: main thread should not be hard coded.
    }

    /**
     * Get a value indicating whether the specified thread is the helper thread in the helper threading scheme.
     *
     * @param thread the thread
     * @return a value indicating whether the specified thread is the helper thread in the helper threading scheme
     */
    public static boolean isHelperThread(Thread thread) {
        return isHelperThread(thread.getId());
    }

    /**
     * Get a value indicating whether the specified thread is the helper thread in the helper threading scheme.
     *
     * @param threadId the thread ID
     * @return a value indicating whether the specified thread is the helper thread in the helper threading scheme
     */
    public static boolean isHelperThread(int threadId) {
        return threadId == getHelperThreadId();
    }

    /**
     * Get the helper thread ID in the helper threading scheme.
     *
     * @return the helper thread ID
     */
    public static int getHelperThreadId() {
        return 2; //TODO: helper thread should not be hard coded.
    }

    /**
     * Get the baseline benchmark for the specified benchmark.
     *
     * @param benchmark the benchmark
     * @return the baseline benchmark for the specified benchmark
     */
    public static Benchmark getBaselineBenchmark(Benchmark benchmark) {
        return ServiceManager.getBenchmarkService().getBenchmarkByTitle(benchmark.getTitle().replaceAll("_ht", "_baseline"));
    }

    /**
     * Get the helper threaded benchmark for the specified benchmark.
     *
     * @param benchmark the benchmark
     * @return the helper threaded benchmark for the specified benchmark
     */
    public static Benchmark getHelperThreadedBenchmark(Benchmark benchmark) {
        return ServiceManager.getBenchmarkService().getBenchmarkByTitle(benchmark.getTitle().replaceAll("_baseline", "_ht"));
    }
}
