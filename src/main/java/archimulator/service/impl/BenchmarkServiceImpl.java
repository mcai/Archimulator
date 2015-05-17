/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
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
import archimulator.util.serialization.XMLSerializationHelper;
import org.apache.commons.io.FileUtils;

import java.io.File;

/**
 * Benchmark service implementation.
 *
 * @author Min Cai
 */
public class BenchmarkServiceImpl implements BenchmarkService {
    @Override
    public Benchmark getBenchmarkByTitle(String title) {
        try {
            File file = new File("configs/benchmarks/", title + ".xml");

            if (file.exists()) {
                String text = FileUtils.readFileToString(file);

                Benchmark benchmark = XMLSerializationHelper.deserialize(Benchmark.class, text);

                if (benchmark != null) {
                    return benchmark;
                }
            }

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
