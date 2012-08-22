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
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyType;
import net.pickapack.JsonSerializationHelper;
import org.parboiled.common.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Startup {
    public static void main(String[] args) throws IOException {
        for (String arg : args) {
            ExperimentPackSpec experimentPackSpec = JsonSerializationHelper.deserialize(ExperimentPackSpec.class, FileUtils.readAllText(arg));

            if (ServiceManager.getExperimentService().getExperimentPackByTitle(experimentPackSpec.getTitle()) == null) {
                ExperimentPack experimentPack = new ExperimentPack(experimentPackSpec.getTitle());
                ServiceManager.getExperimentService().addExperimentPack(experimentPack);

                for (ExperimentSpec experimentSpec : experimentPackSpec.getExperiments()) {
                    ServiceManager.getExperimentService().addExperiment(createExperiment(
                            experimentPack,
                            experimentSpec.getProgramTitle(),
                            experimentSpec.getHelperThreadLookahead(), experimentSpec.getHelperThreadStride(),
                            experimentSpec.getNumCores(), experimentSpec.getNumThreadsPerCore(),
                            experimentSpec.getL1ISize(), experimentSpec.getL1IAssociativity(),
                            experimentSpec.getL1DSize(), experimentSpec.getL1DAssociativity(),
                            experimentSpec.getL2Size(), experimentSpec.getL2Associativity(), experimentSpec.getL2ReplacementPolicyType()
                    ));
                }
            }
        }

        ServiceManager.getExperimentService().runExperiments();
    }

    private static Experiment createExperiment(ExperimentPack parent, String programTitle, int helperThreadLookahead, int helperThreadStride, int numCores, int numThreadsPerCore, int l1ISize, int l1IAssoc, int l1DSize, int l1DAssoc, int l2Size, int l2Assoc, CacheReplacementPolicyType l2ReplacementPolicyType) {
        SimulatedProgram simulatedProgram = ServiceManager.getSimulatedProgramService().getSimulatedProgramByTitle(programTitle);

        Architecture architecture = ServiceManager.getArchitectureService().getOrAddArchitecture(true, numCores, numThreadsPerCore, l1ISize, l1IAssoc, l1DSize, l1DAssoc, l2Size, l2Assoc, l2ReplacementPolicyType);

        List<ContextMapping> contextMappings = new ArrayList<ContextMapping>();

        ContextMapping contextMapping = new ContextMapping(0, simulatedProgram);
        contextMapping.setHelperThreadLookahead(helperThreadLookahead);
        contextMapping.setHelperThreadStride(helperThreadStride);
        contextMapping.setDynamicHelperThreadParams(false);
        contextMappings.add(contextMapping);

        return new Experiment(parent, simulatedProgram.getTitle() + "_" + simulatedProgram.getArguments() + "-lookahead_" + helperThreadLookahead + "-stride_" + helperThreadStride + "-" + architecture.getTitle(), ExperimentType.DETAILED, architecture, -1, contextMappings);
    }
}
