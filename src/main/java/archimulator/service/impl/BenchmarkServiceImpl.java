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
package archimulator.service.impl;

import archimulator.model.Benchmark;
import archimulator.service.BenchmarkService;
import archimulator.service.ServiceManager;
import com.j256.ormlite.dao.Dao;
import net.pickapack.model.WithId;
import net.pickapack.service.AbstractService;

import java.util.Arrays;
import java.util.List;

/**
 * Benchmark service implementation.
 *
 * @author Min Cai
 */
public class BenchmarkServiceImpl extends AbstractService implements BenchmarkService {
    private Dao<Benchmark, Long> benchmarks;

    /**
     * Create a benchmark service implementation.
     */
    @SuppressWarnings("unchecked")
    public BenchmarkServiceImpl() {
        super(ServiceManager.getDatabaseUrl(), Arrays.<Class<? extends WithId>>asList(Benchmark.class));

        this.benchmarks = createDao(Benchmark.class);
    }

    //TODO: to be refactored to support the bundling of simulation mode and input set!!!
    @Override
    public void initialize() {
        if (this.getBenchmarkByTitle("mst_baseline") == null) {
            this.addBenchmark(new Benchmark(
                    "mst_baseline", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/Olden_Custom1/mst/baseline",
                    "mst.mips",
                    "10000"));
        }

        if (this.getBenchmarkByTitle("mst_ht") == null) {
            this.addBenchmark(new Benchmark(
                    "mst_ht", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/Olden_Custom1/mst/ht",
                    "mst.mips",
                    "10000", "", true));
        }

        if (this.getBenchmarkByTitle("em3d_baseline") == null) {
            this.addBenchmark(new Benchmark(
                    "em3d_baseline", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/Olden_Custom1/em3d/baseline",
                    "em3d.mips",
                    "400000 128 75 1"));
        }

        if (this.getBenchmarkByTitle("em3d_ht") == null) {
            this.addBenchmark(new Benchmark(
                    "em3d_ht", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/Olden_Custom1/em3d/ht",
                    "em3d.mips",
                    "400000 128 75 1", "", true));
        }

        if (this.getBenchmarkByTitle("429_mcf_baseline") == null) {
            this.addBenchmark(new Benchmark(
                    "429_mcf_baseline", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/baseline",
                    "429.mcf.mips",
                    ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/baseline/data/ref/input/inp.in"));
        }

        if (this.getBenchmarkByTitle("429_mcf_ht") == null) {
            this.addBenchmark(new Benchmark(
                    "429_mcf_ht", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/ht",
                    "429.mcf.mips",
                    ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/ht/data/ref/input/inp.in", "", true));
        }

        if (this.getBenchmarkByTitle("462_libquantum_baseline") == null) {
            this.addBenchmark(new Benchmark(
                    "462_libquantum_baseline", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/CPU2006_Custom1/462.libquantum/baseline",
                    "462.libquantum.mips",
                    "1397 8"));
        }

        if (this.getBenchmarkByTitle("462_libquantum_ht") == null) {
            this.addBenchmark(new Benchmark(
                    "462_libquantum_ht", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/CPU2006_Custom1/462.libquantum/ht",
                    "462.libquantum.mips",
                    "1397 8", "", true));
        }

        if (this.getBenchmarkByTitle("mst_baseline_sim") == null) {
            this.addBenchmark(new Benchmark(
                    "mst_baseline_sim", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/Olden_Custom1/mst/baseline",
                    "mst.mips",
                    "2000"));
        }

        if (this.getBenchmarkByTitle("mst_ht_sim") == null) {
            this.addBenchmark(new Benchmark(
                    "mst_ht_sim", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/Olden_Custom1/mst/ht",
                    "mst.mips",
                    "2000", "", true));
        }

        if (this.getBenchmarkByTitle("em3d_baseline_sim") == null) {
            this.addBenchmark(new Benchmark(
                    "em3d_baseline_sim", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/Olden_Custom1/em3d/baseline",
                    "em3d.mips",
                    "12000 128 75 1"));
        }

        if (this.getBenchmarkByTitle("em3d_ht_sim") == null) {
            this.addBenchmark(new Benchmark(
                    "em3d_ht_sim", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/Olden_Custom1/em3d/ht",
                    "em3d.mips",
                    "12000 128 75 1", "", true));
        }

        if (this.getBenchmarkByTitle("429_mcf_baseline_sim") == null) {
            this.addBenchmark(new Benchmark(
                    "429_mcf_baseline_sim", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/baseline",
                    "429.mcf.mips",
                    ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/baseline/data/ref/input/inp.in"));
        }

        if (this.getBenchmarkByTitle("429_mcf_ht_sim") == null) {
            this.addBenchmark(new Benchmark(
                    "429_mcf_ht_sim", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/ht",
                    "429.mcf.mips",
                    ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/ht/data/ref/input/inp.in", "", true));
        }

        if (this.getBenchmarkByTitle("462_libquantum_baseline_sim") == null) {
            this.addBenchmark(new Benchmark(
                    "462_libquantum_baseline_sim", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/CPU2006_Custom1/462.libquantum/baseline",
                    "462.libquantum.mips",
                    "1397 8"));
        }

        if (this.getBenchmarkByTitle("462_libquantum_ht_sim") == null) {
            this.addBenchmark(new Benchmark(
                    "462_libquantum_ht_sim", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/CPU2006_Custom1/462.libquantum/ht",
                    "462.libquantum.mips",
                    "1397 8", "", true));
        }

        if (this.getBenchmarkByTitle("mst_baseline_micro") == null) {
            this.addBenchmark(new Benchmark(
                    "mst_baseline_micro", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/Olden_Custom1/mst/baseline",
                    "mst.mips",
                    "1000"));
        }

        if (this.getBenchmarkByTitle("mst_ht_micro") == null) {
            this.addBenchmark(new Benchmark(
                    "mst_ht_micro", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/Olden_Custom1/mst/ht",
                    "mst.mips",
                    "1000", "", true));
        }
    }

    @Override
    public List<Benchmark> getAllBenchmarks() {
        return this.getItems(this.benchmarks);
    }

    @Override
    public List<Benchmark> getAllBenchmarks(long first, long count) {
        return this.getItems(this.benchmarks, first, count);
    }

    @Override
    public long getNumAllBenchmarks() {
        return this.getNumItems(this.benchmarks);
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
        return this.addItem(this.benchmarks, benchmark);
    }

    @Override
    public void removeBenchmarkById(long id) {
        this.removeItemById(this.benchmarks, id);
    }

    @Override
    public void clearBenchmarks() {
        this.clearItems(this.benchmarks);
    }

    @Override
    public void updateBenchmark(Benchmark benchmark) {
        this.updateItem(this.benchmarks, benchmark);
    }
}
