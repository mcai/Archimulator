package archimulator.startup;

import archimulator.common.ContextMapping;
import archimulator.common.Experiment;
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
    private static void setupCommonConfig(Experiment experiment) {
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

    private static List<Experiment> testAntPacketInjectionRates() {
        List<Double> antPacketInjectionRates = Arrays.asList(
                0.0002, 0.001, 0.005, 0.025
        );

        double acoSelectionAlpha = 0.45;
        double reinforcementFactor = 0.001;

        List<Experiment> experiments = new ArrayList<>();

        Experiment experimentXy = new Experiment();
        setupCommonConfig(experimentXy);
        experimentXy.getConfig().setOutputDirectory(String.format(
                "results/antPacketInjectionRates/r_%s/s_%s/",
                "xy", "random"
        ));
        experimentXy.getConfig().setRouting("xy");
        experimentXy.getConfig().setSelection("random");
        experiments.add(experimentXy);

        Experiment experimentBufferLevel = new Experiment();
        setupCommonConfig(experimentBufferLevel);
        experimentBufferLevel.getConfig().setOutputDirectory(String.format(
                "results/antPacketInjectionRates/r_%s/s_%s/",
                "oddEven", "bufferLevel"
        ));
        experimentBufferLevel.getConfig().setRouting("oddEven");
        experimentBufferLevel.getConfig().setSelection("bufferLevel");
        experiments.add(experimentBufferLevel);

        for(double antPacketInjectionRate : antPacketInjectionRates) {
            Experiment experimentAco = new Experiment();
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

    private static List<Experiment> testAcoSelectionAlphasAndReinforcementFactors() {
        double antPacketInjectionRate = 0.001;

        List<Double> acoSelectionAlphas = Arrays.asList(
                0.30, 0.35, 0.40, 0.45, 0.50, 0.55, 0.60, 0.65, 0.70
        );

        List<Double> reinforcementFactors = Arrays.asList(
                0.0005, 0.001, 0.002, 0.004, 0.008, 0.016, 0.032, 0.064
        );

        List<Experiment> experiments = new ArrayList<>();

        Experiment experimentXy = new Experiment();
        setupCommonConfig(experimentXy);
        experimentXy.getConfig().setOutputDirectory(String.format(
                "results/acoSelectionAlphasAndReinforcementFactors/r_%s/s_%s/",
                "xy", "random"
        ));
        experimentXy.getConfig().setRouting("xy");
        experimentXy.getConfig().setSelection("random");
        experiments.add(experimentXy);

        Experiment experimentBufferLevel = new Experiment();
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
                Experiment experimentAco = new Experiment();
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

    public static List<Experiment> antPacketInjectionRates = testAntPacketInjectionRates();
    public static List<Experiment> acoSelectionAlphasAndReinforcementFactors = testAcoSelectionAlphasAndReinforcementFactors();
}
