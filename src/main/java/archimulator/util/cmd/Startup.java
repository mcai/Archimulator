/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.util.cmd;

import archimulator.common.*;

/**
 * Startup.
 *
 * @author Min Cai
 */
public class Startup {
    /**
     * Entry point.
     *
     * @param args arguments
     */
    public static void main(String[] args) {
        ExperimentConfig config = new ExperimentConfig();

        config.setType(ExperimentType.DETAILED);
        config.setOutputDirectory("results/mst_ht_100x4");

        config.setNumCores(16);
        config.setNumThreadsPerCore(1);

        config.getContextMappings().add(new ContextMapping(0, "benchmarks/Olden_Custom1/mst/ht/mst.mips", "100"));
        config.getContextMappings().add(new ContextMapping(4, "benchmarks/Olden_Custom1/mst/ht/mst.mips", "100"));
        config.getContextMappings().add(new ContextMapping(8, "benchmarks/Olden_Custom1/mst/ht/mst.mips", "100"));
        config.getContextMappings().add(new ContextMapping(12, "benchmarks/Olden_Custom1/mst/ht/mst.mips", "100"));

        ExperimentFactory.run(config);
    }
}
