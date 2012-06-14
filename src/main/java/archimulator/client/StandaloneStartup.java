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
package archimulator.client;

import archimulator.sim.base.experiment.Experiment;
import archimulator.sim.base.experiment.profile.ExperimentProfile;
import archimulator.sim.base.experiment.profile.ProcessorProfile;
import archimulator.sim.base.simulation.SimulatedProgram;
import archimulator.sim.uncore.cache.eviction.LRUPolicy;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.IOException;

public class StandaloneStartup {
    @Option(name = "-c", usage = "number of processor cores (default: 2)", metaVar = "<numCores>", required = false)
    private int numCores = 2;

    @Option(name = "-t", usage = "number of threads per core (default: 2)", metaVar = "<numThreadsPerCore>", required = false)
    private int numThreadsPerCore = 2;

    @Option(name = "-is", usage = "L1I cache size in KBytes (default: 32)", metaVar = "<l1ISizeInKByte>", required = false)
    private int l1ISizeInKByte = 32;

    @Option(name = "-ia", usage = "L1I cache associativity (default: 4)", metaVar = "<l1IAssociativity>", required = false)
    private int l1IAssociativity = 4;

    @Option(name = "-ds", usage = "L1D cache size in KBytes (default: 32)", metaVar = "<l1DSizeInKByte>", required = false)
    private int l1DSizeInKByte = 32;

    @Option(name = "-da", usage = "L1D cache associativity (default: 4)", metaVar = "<l1DAssociativity>", required = false)
    private int l1DAssociativity = 4;

    @Option(name = "-2s", usage = "L2 cache size in KBytes (default: 96)", metaVar = "<l2SizeInKByte>", required = false)
    private int l2SizeInKByte = 96;

    @Option(name = "-2a", usage = "L2 cache associativity (default: 4)", metaVar = "<l2Associativity>", required = false)
    private int l2Associativity = 4;

    @Option(name = "-a", usage = "Arguments passed to mst (default: 4000)", metaVar = "<args>", required = false)
    private String args = "4000";

    @Option(name = "-l", usage = "HT lookahead parameter (default: 20)", metaVar = "<lookahead>", required = false)
    private int lookahead = 20;

    @Option(name = "-s", usage = "HT stride parameter (default: 10)", metaVar = "<stride>", required = false)
    private int stride = 10;

    public void parseArgs(String[] args) throws IOException {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println("java -cp <path/to/achimulator.jar> archimulator.client.StandaloneStartup [options]");
            parser.printUsage(System.err);
            System.err.println();
            throw new IOException();
        }
    }

    public static void main(String[] args) {
        StandaloneStartup standaloneStartup = new StandaloneStartup();
        try {
            standaloneStartup.parseArgs(args);
            standaloneStartup.run(
                    standaloneStartup.numCores,
                    standaloneStartup.numThreadsPerCore,
                    standaloneStartup.l1ISizeInKByte,
                    standaloneStartup.l1IAssociativity,
                    standaloneStartup.l1DSizeInKByte,
                    standaloneStartup.l1DAssociativity,
                    standaloneStartup.l2SizeInKByte,
                    standaloneStartup.l2Associativity,
                    standaloneStartup.args,
                    standaloneStartup.lookahead,
                    standaloneStartup.stride
            );
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private void run(int numCores, int numThreadsPerCore, int l1ISizeInKByte, int l1IAssociativity, int l1DSizeInKByte, int l1DAssociativity, int l2SizeInKByte, int l2Associativity, String args, int lookahead, int stride) {
        SimulatedProgram simulatedProgram = Presets.SIMULATED_PROGRAM_MST_HT(args, lookahead, stride, false);
        ProcessorProfile processorProfile = Presets.processor(numCores, numThreadsPerCore, l1ISizeInKByte, l1IAssociativity, l1DSizeInKByte, l1DAssociativity, l2SizeInKByte, l2Associativity, "LRU", LRUPolicy.class);
        ExperimentProfile experimentProfile = Presets.ht_lru(3720, 200000000, processorProfile, simulatedProgram);
        Experiment experiment = experimentProfile.createExperiment();
        experiment.start();
        experiment.join();
    }
}
