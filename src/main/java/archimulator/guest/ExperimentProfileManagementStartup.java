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
package archimulator.guest;

import archimulator.model.experiment.profile.ExperimentProfile;
import archimulator.model.experiment.profile.ProcessorProfile;
import archimulator.model.simulation.SimulatedProgram;
import archimulator.service.ArchimulatorService;
import archimulator.sim.Startup;
import archimulator.sim.ext.uncore.newHt2.LastLevelCacheHtRequestCachePollutionProfilingCapability;
import archimulator.util.DateHelper;
import com.caucho.hessian.client.HessianProxyFactory;

import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExperimentProfileManagementStartup {
    private ArchimulatorService archimulatorService;

    public ExperimentProfileManagementStartup() {
        try {
            HessianProxyFactory factory = new HessianProxyFactory();
            factory.setReadTimeout(30000);
            factory.setConnectTimeout(20000);
            factory.setOverloadEnabled(true);

            this.archimulatorService = (ArchimulatorService) factory.create(ArchimulatorService.class, GuestStartup.SERVICE_URL);
        } catch (MalformedURLException e) {
            recordException(e);
            throw new RuntimeException(e);
        }
    }

    public static void recordException(Exception e) {
        System.out.print(String.format("[%s Exception] %s\r\n", DateHelper.toString(new Date()), e));
        e.printStackTrace();
    }

    private void submitExperimentProfiles() throws SQLException {
        List<SimulatedProgram> simulatedPrograms = new ArrayList<SimulatedProgram>();
        simulatedPrograms.add(Startup.SIMULATED_PROGRAM_MST_HT);
        simulatedPrograms.add(Startup.SIMULATED_PROGRAM_EM3D_HT);
        simulatedPrograms.add(Startup.SIMULATED_PROGRAM_429_MCF_HT);

        List<Integer> l2SizeInKBytes = new ArrayList<Integer>();
        l2SizeInKBytes.add(512);
        l2SizeInKBytes.add(512 * 2);
        l2SizeInKBytes.add(512 * 4);
        l2SizeInKBytes.add(512 * 8);

        List<ExperimentProfile> experimentProfiles = new ArrayList<ExperimentProfile>();

        for (SimulatedProgram simulatedProgram : simulatedPrograms) {
            ProcessorProfile processorProfile = new ProcessorProfile(2, 2, 1024 * 1024 * 4, 8);

            ExperimentProfile experimentProfile = new ExperimentProfile(processorProfile);
            experimentProfile.addWorkload(simulatedProgram);
            experimentProfile.functionallyToEnd();
            experimentProfiles.add(experimentProfile);

            for (int l2SizeInKByte : l2SizeInKBytes) {
                ProcessorProfile processorProfile1 = new ProcessorProfile(2, 2, 1024 * l2SizeInKByte, 8);

                ExperimentProfile experimentProfile1 = new ExperimentProfile(processorProfile1);
                experimentProfile1.addWorkload(simulatedProgram);
                experimentProfile1.functionallyToPseudoCallAndInDetailForMaxInsts(3720, 2000000000).addSimulationCapabilityClass(LastLevelCacheHtRequestCachePollutionProfilingCapability.class);
                experimentProfiles.add(experimentProfile1);

                ExperimentProfile experimentProfile2 = new ExperimentProfile(processorProfile1);
                experimentProfile2.addWorkload(simulatedProgram);
                experimentProfile2.inDetailToEnd().addSimulationCapabilityClass(LastLevelCacheHtRequestCachePollutionProfilingCapability.class);
                experimentProfiles.add(experimentProfile2);
            }
        }

        for(ExperimentProfile experimentProfile : experimentProfiles) {
            this.archimulatorService.addExperimentProfile(experimentProfile);
        }
        
        System.out.println(this.archimulatorService.getExperimentProfilesAsList().size());
        
        for(ExperimentProfile experimentProfile : this.archimulatorService.getExperimentProfilesAsList()) {
            System.out.println(experimentProfile.getState());
        }
        
        this.archimulatorService.setRunningExperimentEnabled(true);
    }

    public static void main(String[] args) throws SQLException {
        ExperimentProfileManagementStartup startup = new ExperimentProfileManagementStartup();
        startup.submitExperimentProfiles();
    }
}
