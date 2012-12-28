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
package archimulator.service;

import archimulator.model.Benchmark;
import net.pickapack.service.Service;

import java.util.List;

/**
 *
 * Service for managing benchmarks.
 *
 * @author Min Cai
 */
public interface BenchmarkService extends Service {
    /**
     * Get all the benchmarks.
     *
     * @return the benchmarks
     */
    List<Benchmark> getAllBenchmarks();

    /**
     * Get all the benchmarks by offset and count.
     *
     * @param first offset
     * @param count count
     * @return the benchmarks
     */
    List<Benchmark> getAllBenchmarks(long first, long count);

    /**
     * Get the number of the benchmarks.
     *
     * @return the number of the benchmarks
     */
    long getNumAllBenchmarks();

    /**
     * Get a benchmark by ID.
     *
     * @param id the benchmark's ID
     * @return the benchmark matching the ID, if any exists; otherwise null
     */
    Benchmark getBenchmarkById(long id);

    /**
     * Get a benchmark by title.
     *
     * @param title the benchmark's title
     * @return the benchmark matching the title, if any exists; otherwise null
     */
    Benchmark getBenchmarkByTitle(String title);

    /**
     * Get the first benchmark.
     *
     * @return the first benchmark
     */
    Benchmark getFirstBenchmark();

    /**
     * Add a benchmark.
     *
     * @param benchmark the benchmark that is to be added
     * @return the ID of the benchmark that is added
     */
    long addBenchmark(Benchmark benchmark);

    /**
     * Remove a benchmark by ID.
     *
     * @param id the benchmark's ID
     */
    void removeBenchmarkById(long id);

    /**
     * Clear (remove all) the benchmarks.
     *
     */
    void clearBenchmarks();

    /**
     * Update the benchmark.
     *
     * @param benchmark the benchmark that is to be updated
     */
    void updateBenchmark(Benchmark benchmark);

    /**
     * Initialize the service.
     */
    void initialize();
}
