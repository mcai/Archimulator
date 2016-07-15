package archimulator.startup;

import archimulator.common.CPUExperiment;
import archimulator.common.ContextMapping;
import archimulator.common.ExperimentType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Experiments.
 *
 * @author Min Cai
 */
public class Experiments {
    private static void setupCommonConfig(CPUExperiment experiment) {
        experiment.getConfig().setType(ExperimentType.DETAILED);

        experiment.getConfig().setNumCores(16);
        experiment.getConfig().setNumThreadsPerCore(1);

        String executable = "benchmarks/Olden_Custom1/mst/ht/mst.mips";
        String arguments = "100";

        experiment.getConfig().getContextMappings().add(new ContextMapping(0, executable, arguments));
        experiment.getConfig().getContextMappings().add(new ContextMapping(4, executable, arguments));
        experiment.getConfig().getContextMappings().add(new ContextMapping(8, executable, arguments));
        experiment.getConfig().getContextMappings().add(new ContextMapping(12, executable, arguments));
    }

    private static List<CPUExperiment> testAntPacketInjectionRates() {
        List<Double> antPacketInjectionRates = Arrays.asList(
                0.0002, 0.001, 0.005, 0.025
        );

        double acoSelectionAlpha = 0.45;
        double reinforcementFactor = 0.001;

        List<CPUExperiment> experiments = new ArrayList<>();

        CPUExperiment experimentXy = new CPUExperiment();
        setupCommonConfig(experimentXy);
        experimentXy.getConfig().setOutputDirectory(String.format(
                "results/antPacketInjectionRates/r_%s/s_%s/",
                "xy", "random"
        ));
        experimentXy.getConfig().setRouting("xy");
        experimentXy.getConfig().setSelection("random");
        experiments.add(experimentXy);

        CPUExperiment experimentBufferLevel = new CPUExperiment();
        setupCommonConfig(experimentBufferLevel);
        experimentBufferLevel.getConfig().setOutputDirectory(String.format(
                "results/antPacketInjectionRates/r_%s/s_%s/",
                "oddEven", "bufferLevel"
        ));
        experimentBufferLevel.getConfig().setRouting("oddEven");
        experimentBufferLevel.getConfig().setSelection("bufferLevel");
        experiments.add(experimentBufferLevel);

        for(double antPacketInjectionRate : antPacketInjectionRates) {
            CPUExperiment experimentAco = new CPUExperiment();
            setupCommonConfig(experimentAco);
            experimentAco.getConfig().setOutputDirectory(String.format(
                    "results/antPacketInjectionRates/r_%s/s_%s/aj_%s/a_%s/rf_%s/",
                    "oddEven", "aco",
                    antPacketInjectionRate, acoSelectionAlpha, reinforcementFactor
            ));
            experimentAco.getConfig().setRouting("oddEven");
            experimentAco.getConfig().setSelection("aco");
            experimentAco.getConfig().setAntPacketInjectionRate(antPacketInjectionRate);
            experimentAco.getConfig().setAcoSelectionAlpha(acoSelectionAlpha);
            experimentAco.getConfig().setReinforcementFactor(reinforcementFactor);
            experiments.add(experimentAco);
        }

        return experiments;
    }

    private static List<CPUExperiment> testAcoSelectionAlphasAndReinforcementFactors() {
        double antPacketInjectionRate = 0.001;

        List<Double> acoSelectionAlphas = Arrays.asList(
                0.30, 0.35, 0.40, 0.45, 0.50, 0.55, 0.60, 0.65, 0.70
        );

        List<Double> reinforcementFactors = Arrays.asList(
                0.0005, 0.001, 0.002, 0.004, 0.008, 0.016, 0.032, 0.064
        );

        List<CPUExperiment> experiments = new ArrayList<>();

        CPUExperiment experimentXy = new CPUExperiment();
        setupCommonConfig(experimentXy);
        experimentXy.getConfig().setOutputDirectory(String.format(
                "results/acoSelectionAlphasAndReinforcementFactors/r_%s/s_%s/",
                "xy", "random"
        ));
        experimentXy.getConfig().setRouting("xy");
        experimentXy.getConfig().setSelection("random");
        experiments.add(experimentXy);

        CPUExperiment experimentBufferLevel = new CPUExperiment();
        setupCommonConfig(experimentBufferLevel);
        experimentBufferLevel.getConfig().setOutputDirectory(String.format(
                "results/acoSelectionAlphasAndReinforcementFactors/r_%s/s_%s/",
                "oddEven", "bufferLevel"
        ));
        experimentBufferLevel.getConfig().setRouting("oddEven");
        experimentBufferLevel.getConfig().setSelection("bufferLevel");
        experiments.add(experimentBufferLevel);

        for(double acoSelectionAlpha : acoSelectionAlphas) {
            for(double reinforcementFactor : reinforcementFactors) {
                CPUExperiment experimentAco = new CPUExperiment();
                setupCommonConfig(experimentAco);
                experimentAco.getConfig().setOutputDirectory(String.format(
                        "results/acoSelectionAlphasAndReinforcementFactors/r_%s/s_%s/aj_%s/a_%s/rf_%s/",
                        "oddEven", "aco",
                        antPacketInjectionRate, acoSelectionAlpha, reinforcementFactor
                ));
                experimentAco.getConfig().setRouting("oddEven");
                experimentAco.getConfig().setSelection("aco");
                experimentAco.getConfig().setAntPacketInjectionRate(antPacketInjectionRate);
                experimentAco.getConfig().setAcoSelectionAlpha(acoSelectionAlpha);
                experimentAco.getConfig().setReinforcementFactor(reinforcementFactor);
                experiments.add(experimentAco);
            }
        }

        return experiments;
    }

    public static List<CPUExperiment> antPacketInjectionRates = testAntPacketInjectionRates();
    public static List<CPUExperiment> acoSelectionAlphasAndReinforcementFactors = testAcoSelectionAlphasAndReinforcementFactors();
}
