package archimulator.guest;

import archimulator.service.ArchimulatorService;
import archimulator.util.DateHelper;
import com.caucho.hessian.client.HessianProxyFactory;

import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.Date;

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
//        List<SimulatedProgram> simulatedPrograms = new ArrayList<SimulatedProgram>();
//        simulatedPrograms.add(Startup.SIMULATED_PROGRAM_MST_HT);
//        simulatedPrograms.add(Startup.SIMULATED_PROGRAM_EM3D_HT);
//        simulatedPrograms.add(Startup.SIMULATED_PROGRAM_429_MCF_HT);
//
//        List<Integer> l2SizeInKBytes = new ArrayList<Integer>();
//        l2SizeInKBytes.add(512);
//        l2SizeInKBytes.add(512 * 2);
//        l2SizeInKBytes.add(512 * 4);
//        l2SizeInKBytes.add(512 * 8);
//
//        List<ExperimentBuilder.ExperimentProfile> experimentProfiles = new ArrayList<ExperimentBuilder.ExperimentProfile>();
//
//        for (SimulatedProgram simulatedProgram : simulatedPrograms) {
//            experimentProfiles.add(on().cores(2).threadsPerCore(2)
//                    .with().workload(simulatedProgram)
//                    .simulate().functionallyToEnd());
//
//            for (int l2SizeInKByte : l2SizeInKBytes) {
//                experimentProfiles.add(on().cores(2).threadsPerCore(2).l2Size(1024 * l2SizeInKByte).l2Associativity(8)
//                        .with().workload(simulatedProgram)
//                        .simulate().functionallyToPseudoCallAndInDetailForMaxInsts(3720, 2000000000)
//                        .addSimulationCapabilityFactory(LastLevelCacheHtRequestCachePollutionProfilingCapability.class, LastLevelCacheHtRequestCachePollutionProfilingCapability.FACTORY));
//
//                experimentProfiles.add(on().cores(2).threadsPerCore(2).l2Size(1024 * l2SizeInKByte).l2Associativity(8)
//                        .with().workload(simulatedProgram)
//                        .simulate().inDetailToEnd()
//                        .addSimulationCapabilityFactory(LastLevelCacheHtRequestCachePollutionProfilingCapability.class, LastLevelCacheHtRequestCachePollutionProfilingCapability.FACTORY));
//            }
//        }
//
//        for(ExperimentBuilder.ExperimentProfile experimentProfile : experimentProfiles) {
//            this.archimulatorService.addExperimentProfile(experimentProfile);
//        }

        this.archimulatorService.setRunningExperimentEnabled(true);
    }

    public static void main(String[] args) throws SQLException {
        ExperimentProfileManagementStartup startup = new ExperimentProfileManagementStartup();
        startup.submitExperimentProfiles();
    }
}
