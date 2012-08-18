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

import archimulator.model.*;
import archimulator.service.ServiceManager;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Startup {
    @Option(name = "-p", usage = "Program title (default: mst_ht)", metaVar = "<programTitle>", required = false)
    private String programTitle = "mst_ht";

    @Option(name = "-a", usage = "Architecture title (default: default)", metaVar = "<architectureTitle>", required = false)
    private String architectureTitle = "default";

    @Option(name = "-l", usage = "Helper threading lookahead parameter (default: 20)", metaVar = "<htLookahead>", required = false)
    private int htLookahead = 20;

    @Option(name = "-s", usage = "Helper threading stride parameter (default: 10)", metaVar = "<htStride>", required = false)
    private int htStride = 10;

    public void parseArgs(String[] args) throws IOException {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println("java -cp <path/to/achimulator.jar> archimulator.client.Startup [options]");
            parser.printUsage(System.err);
            System.err.println();
            throw new IOException();
        }
    }

    public static void main(String[] args) throws SQLException, InterruptedException {
        Startup startup = new Startup();
        try {
            startup.parseArgs(args);
            startup.run(startup.programTitle, startup.architectureTitle, startup.htLookahead, startup.htStride);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public void run(String programTitle, String architectureTitle, int htLookahead, int htStride) throws SQLException {
        SimulatedProgram simulatedProgram = ServiceManager.getSimulatedProgramService().getSimulatedProgramByTitle(programTitle);

        Architecture architecture = ServiceManager.getArchitectureService().getArchitectureByTitle(architectureTitle);

        String title = simulatedProgram.getTitle() + "_" + simulatedProgram.getArgs() + "-" + architecture.getProcessorPropertiesTitle();

        List<ContextMapping> contextMappings = new ArrayList<ContextMapping>();

        ContextMapping contextMapping = new ContextMapping(0, simulatedProgram);
        contextMapping.setHtLookahead(htLookahead);
        contextMapping.setHtStride(htStride);
        contextMapping.setDynamicHtParams(false);
        contextMappings.add(contextMapping);

        Experiment experiment = new Experiment(title, ExperimentType.DETAILED, architecture, -1, contextMappings);
        ServiceManager.getExperimentService().addExperiment(experiment);
        ServiceManager.getExperimentService().waitForExperimentStopped(experiment);
    }
}
