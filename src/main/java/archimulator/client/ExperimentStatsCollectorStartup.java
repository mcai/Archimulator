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

import archimulator.service.ArchimulatorService;
import archimulator.sim.base.experiment.profile.ExperimentProfile;
import archimulator.sim.base.experiment.profile.ExperimentProfileState;
import archimulator.util.DateHelper;
import archimulator.util.action.Predicate;
import com.caucho.hessian.client.HessianProxyFactory;

import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class ExperimentStatsCollectorStartup {
    private static void recordException(Exception e) {
        System.out.print(String.format("[%s Exception] %s\r\n", DateHelper.toString(new Date()), e));
        e.printStackTrace();
    }

    public static void main(String[] args) throws SQLException {
        collectExperimentStats(new Predicate<ExperimentProfile>() {
            @Override
            public boolean apply(ExperimentProfile experimentProfile) {
                return experimentProfile.getState() == ExperimentProfileState.STOPPED && experimentProfile.getTitle().contains("-L2_1024KB_Assoc4");
            }
        },
//                "checkpointedSimulation/phase0.duration",
//                "checkpointedSimulation/phase1.duration",
                "checkpointedSimulation/phase1.totalCycles",

                "checkpointedSimulation/phase1.llc.hitRatio",
                "checkpointedSimulation/phase1.llc.downwardAccesses",
                "checkpointedSimulation/phase1.llc.downwardHits",
                "checkpointedSimulation/phase1.llc.downwardMisses",
                "checkpointedSimulation/phase1.llc.downwardReadHits",
                "checkpointedSimulation/phase1.llc.downwardReadMisses",
                "checkpointedSimulation/phase1.llc.downwardReadBypasses",
                "checkpointedSimulation/phase1.llc.downwardWriteHits",
                "checkpointedSimulation/phase1.llc.downwardWriteMisses",
                "checkpointedSimulation/phase1.llc.downwardWriteBypasses",
                "checkpointedSimulation/phase1.llc.evictions",

                "checkpointedSimulation/phase1.llcHTRequestProfilingCapability.llc.numTotalHTLLCRequests",
                "checkpointedSimulation/phase1.llcHTRequestProfilingCapability.llc.numGoodHTLLCRequests",
                "checkpointedSimulation/phase1.llcHTRequestProfilingCapability.llc.numBadHTLLCRequests",
                "checkpointedSimulation/phase1.llcHTRequestProfilingCapability.llc.numUglyHTLLCRequests",
                "checkpointedSimulation/phase1.llcHTRequestProfilingCapability.llc.numLateHTLLCRequests",
                "checkpointedSimulation/phase1.llcReuseDistanceProfilingCapability.llc.ht_mt_inter-thread_reuseDistances[1]",
                "checkpointedSimulation/phase1.llcReuseDistanceProfilingCapability.llc.ht_mt_inter-thread_reuseDistances[2]",
                "checkpointedSimulation/phase1.llcReuseDistanceProfilingCapability.llc.ht_mt_inter-thread_reuseDistances[3]",
                "checkpointedSimulation/phase1.llcReuseDistanceProfilingCapability.llc.ht_mt_inter-thread_reuseDistances[4]",
                "checkpointedSimulation/phase1.llcReuseDistanceProfilingCapability.llc.ht_mt_inter-thread_reuseDistances[5]",
                "checkpointedSimulation/phase1.llcReuseDistanceProfilingCapability.llc.ht_mt_inter-thread_reuseDistances[6]",
                "checkpointedSimulation/phase1.llcReuseDistanceProfilingCapability.llc.ht_mt_inter-thread_reuseDistances[7]",
                "checkpointedSimulation/phase1.llcReuseDistanceProfilingCapability.llc.ht_mt_inter-thread_reuseDistances[8]",
                "checkpointedSimulation/phase1.llcReuseDistanceProfilingCapability.llc.ht_mt_inter-thread_reuseDistances[9]",
                "checkpointedSimulation/phase1.llcReuseDistanceProfilingCapability.llc.ht_mt_inter-thread_reuseDistances[10]",
                "checkpointedSimulation/phase1.llcReuseDistanceProfilingCapability.llc.ht_mt_inter-thread_reuseDistances[11]",
                "checkpointedSimulation/phase1.llcReuseDistanceProfilingCapability.llc.ht_mt_inter-thread_reuseDistances[12]",
                "checkpointedSimulation/phase1.llcReuseDistanceProfilingCapability.llc.ht_mt_inter-thread_reuseDistances[13]",
                "checkpointedSimulation/phase1.llcReuseDistanceProfilingCapability.llc.ht_mt_inter-thread_reuseDistances[14]",
                "checkpointedSimulation/phase1.llcReuseDistanceProfilingCapability.llc.ht_mt_inter-thread_reuseDistances[15]",
                "checkpointedSimulation/phase1.llcReuseDistanceProfilingCapability.llc.ht_mt_inter-thread_reuseDistances[16]",
                "checkpointedSimulation/phase1.llcReuseDistanceProfilingCapability.llc.ht_mt_inter-thread_reuseDistances[17]",
                "checkpointedSimulation/phase1.llcReuseDistanceProfilingCapability.llc.ht_mt_inter-thread_reuseDistances[18]",
                "checkpointedSimulation/phase1.llcReuseDistanceProfilingCapability.llc.ht_mt_inter-thread_reuseDistances[19]",
                "checkpointedSimulation/phase1.llcReuseDistanceProfilingCapability.llc.ht_mt_inter-thread_reuseDistances[20]",
                "checkpointedSimulation/phase1.llcReuseDistanceProfilingCapability.llc.ht_mt_inter-thread_reuseDistances[21]",
                "checkpointedSimulation/phase1.llcReuseDistanceProfilingCapability.llc.ht_mt_inter-thread_reuseDistances[22]",
                "checkpointedSimulation/phase1.llcReuseDistanceProfilingCapability.llc.ht_mt_inter-thread_reuseDistances[23]",
                "checkpointedSimulation/phase1.llcReuseDistanceProfilingCapability.llc.ht_mt_inter-thread_reuseDistances[24]",
                "checkpointedSimulation/phase1.llcReuseDistanceProfilingCapability.llc.ht_mt_inter-thread_reuseDistances[25]",
                "checkpointedSimulation/phase1.llcReuseDistanceProfilingCapability.llc.ht_mt_inter-thread_reuseDistances[26]",
                "checkpointedSimulation/phase1.llcReuseDistanceProfilingCapability.llc.ht_mt_inter-thread_reuseDistances[27]",
                "checkpointedSimulation/phase1.llcReuseDistanceProfilingCapability.llc.ht_mt_inter-thread_reuseDistances[28]",
                "checkpointedSimulation/phase1.llcReuseDistanceProfilingCapability.llc.ht_mt_inter-thread_reuseDistances[29]",
                "checkpointedSimulation/phase1.llcReuseDistanceProfilingCapability.llc.ht_mt_inter-thread_reuseDistances[30]",
                "checkpointedSimulation/phase1.llcReuseDistanceProfilingCapability.llc.ht_mt_inter-thread_reuseDistances[31]",
                "checkpointedSimulation/phase1.llcReuseDistanceProfilingCapability.llc.ht_mt_inter-thread_reuseDistances[32]"
        );
    }

    private static void collectExperimentStats(Predicate<ExperimentProfile> experimentProfilePred, String... keys) throws SQLException {
        ArchimulatorService archimulatorService;
        try {
            HessianProxyFactory factory = new HessianProxyFactory();
            factory.setReadTimeout(30000);
            factory.setConnectTimeout(20000);
            factory.setOverloadEnabled(true);

            archimulatorService = (ArchimulatorService) factory.create(ArchimulatorService.class, ManagementStartup.SERVICE_URL);
        } catch (MalformedURLException e) {
            recordException(e);
            throw new RuntimeException(e);
        }

        List<ExperimentProfile> experimentProfiles = archimulatorService.getExperimentProfilesAsList();

        for (ExperimentProfile experimentProfile : experimentProfiles) {
            if (experimentProfilePred.apply(experimentProfile)) {
                System.out.printf("%s:\n", experimentProfile.getTitle());
                for (String key : keys) {
                    String value = archimulatorService.getExperimentStatById(experimentProfile.getId(), key);

                    if (value != null) {
                        System.out.printf("  %s: %s\n", key, value);
                    }
                }
                System.out.println();
            }
        }
    }
}
