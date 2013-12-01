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

import archimulator.model.*;
import archimulator.service.BenchmarkService;
import archimulator.service.ServiceManager;
import archimulator.util.serialization.XMLSerializationHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import net.pickapack.model.WithId;
import net.pickapack.service.AbstractService;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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

    @Override
    public void initialize() {
        try {
            TransactionManager.callInTransaction(getConnectionSource(),
                    () -> {
                        try {
                            File fileBenchmarks = new File("configs/benchmarks");

                            if (fileBenchmarks.exists()) {
                                List<File> files = new ArrayList<>(FileUtils.listFiles(fileBenchmarks, new String[]{"xml"}, true));

                                files.sort(Comparator.comparing(File::getAbsolutePath));

                                for (File file : files) {
                                    String text = FileUtils.readFileToString(file);

                                    Benchmark benchmark = XMLSerializationHelper.deserialize(Benchmark.class, text);

                                    if (benchmark != null) {
                                        if (getBenchmarkByTitle(benchmark.getTitle()) == null) {
                                            addBenchmark(benchmark);
                                            System.out.println("Benchmark " + benchmark.getTitle() + " added.");
                                        }
                                    }
                                }
                            }

                            return null;
                        } catch (Exception e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
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

    private void addBenchmarkIfNotExists(Benchmark benchmark) {
        if (this.getBenchmarkByTitle(benchmark.getTitle()) == null) {
            this.addBenchmark(benchmark);
        }
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
