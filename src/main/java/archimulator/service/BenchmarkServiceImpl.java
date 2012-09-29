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
package archimulator.service;

import archimulator.model.Benchmark;
import com.j256.ormlite.dao.Dao;
import net.pickapack.model.ModelElement;
import net.pickapack.service.AbstractService;

import java.util.Arrays;
import java.util.List;

public class BenchmarkServiceImpl extends AbstractService implements BenchmarkService {
    private Dao<Benchmark, Long> benchmarks;

    @SuppressWarnings("unchecked")
    public BenchmarkServiceImpl() {
        super(ServiceManager.DATABASE_URL, Arrays.<Class<? extends ModelElement>>asList(Benchmark.class));

        this.benchmarks = createDao(Benchmark.class);

        if (this.getFirstBenchmark() == null) {
            this.addBenchmark(new Benchmark(
                    "mst_baseline", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/Olden_Custom1/mst/baseline",
                    "mst.mips",
                    "4000"));

            this.addBenchmark(new Benchmark(
                    "mst_ht", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/Olden_Custom1/mst/ht",
                    "mst.mips",
                    "4000", "", true));

            this.addBenchmark(new Benchmark(
                    "em3d_baseline", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/Olden_Custom1/em3d/baseline",
                    "em3d.mips",
                    "400000 128 75 1"));

            this.addBenchmark(new Benchmark(
                    "em3d_ht", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/Olden_Custom1/em3d/ht",
                    "em3d.mips",
                    "400000 128 75 1", "", true));

            this.addBenchmark(new Benchmark(
                    "429_mcf_baseline", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/baseline",
                    "429.mcf.mips",
                    ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/baseline/data/ref/input/inp.in"));

            this.addBenchmark(new Benchmark(
                    "429_mcf_ht", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/ht",
                    "429.mcf.mips",
                    ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/ht/data/ref/input/inp.in", "", true));
        }
    }

    @Override
    public List<Benchmark> getAllBenchmarks() {
        return this.getAllItems(this.benchmarks);
    }

    @Override
    public List<Benchmark> getAllBenchmarks(long first, long count) {
        return this.getAllItems(this.benchmarks, first, count);
    }

    @Override
    public long getNumAllBenchmarks() {
        return this.getNumAllItems(this.benchmarks);
    }

    @Override
    public Benchmark getBenchmarkById(long id) {
        return this.getItemById(this.benchmarks, id);
    }

    @Override
    public Benchmark getBenchmarkByTitle(String title) {
        return this.getFirstItemByTitle(this.benchmarks, title);
    }

    @Override
    public Benchmark getFirstBenchmark() {
        return this.getFirstItem(this.benchmarks);
    }

    @Override
    public long addBenchmark(Benchmark benchmark) {
        return this.addItem(this.benchmarks, Benchmark.class, benchmark);
    }

    @Override
    public void removeBenchmarkById(long id) {
        this.removeItemById(this.benchmarks, Benchmark.class, id);
    }

    @Override
    public void clearBenchmarks() {
        this.clearItems(this.benchmarks, Benchmark.class);
    }

    @Override
    public void updateBenchmark(Benchmark benchmark) {
        this.updateItem(this.benchmarks, Benchmark.class, benchmark);
    }
}
