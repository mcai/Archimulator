package archimulator.startup;

import archimulator.uncore.noc.NoCExperiment;

import java.util.*;

/**
 * Experiments.
 *
 * @author Min Cai
 */
public class Experiments {
    private static int numNodes = 8 * 8;
    private static int maxCycles = 20000;
    private static int maxPackets = -1;

    private static Map<String, List<NoCExperiment>> testTrafficsAndDataPacketInjectionRates() {
        List<String> traffics = new ArrayList<>();
        traffics.add("uniform");
        traffics.add("transpose");
        traffics.add("hotspot");

        List<Double> dataPacketInjectionRates = Arrays.asList(
                0.030, 0.045, 0.060, 0.075, 0.090, 0.105, 0.120
        );

        double antPacketInjectionRate = 0.001;

        double acoSelectionAlpha = 0.45;
        double reinforcementFactor = 0.001;

        Map<String, List<NoCExperiment>> experiments = new HashMap<>();

        for(String traffic : traffics) {
            experiments.put(traffic, new ArrayList<>());

            for(double dataPacketInjectionRate : dataPacketInjectionRates) {
                NoCExperiment experimentXy = new NoCExperiment(
                        String.format(
                                "results/trafficsAndDataPacketInjectionRates/t_%s/j_%s/r_%s/s_%s/",
                                traffic, dataPacketInjectionRate, "xy", "random"
                        ),
                        numNodes,
                        maxCycles,
                        maxPackets,
                        false
                );
                experimentXy.getConfig().setDataPacketTraffic(traffic);
                experimentXy.getConfig().setDataPacketInjectionRate(dataPacketInjectionRate);
                experimentXy.getConfig().setRouting("xy");
                experimentXy.getConfig().setSelection("random");
                experiments.get(traffic).add(experimentXy);

                NoCExperiment experimentBufferLevel = new NoCExperiment(
                        String.format(
                                "results/trafficsAndDataPacketInjectionRates/t_%s/j_%s/r_%s/s_%s/",
                                traffic, dataPacketInjectionRate, "oddEven", "bufferLevel"
                        ),
                        numNodes,
                        maxCycles,
                        maxPackets,
                        false
                );
                experimentBufferLevel.getConfig().setDataPacketTraffic(traffic);
                experimentBufferLevel.getConfig().setDataPacketInjectionRate(dataPacketInjectionRate);
                experimentBufferLevel.getConfig().setRouting("oddEven");
                experimentBufferLevel.getConfig().setSelection("bufferLevel");
                experiments.get(traffic).add(experimentBufferLevel);

                NoCExperiment experimentAco = new NoCExperiment(
                        String.format(
                                "results/trafficsAndDataPacketInjectionRates/t_%s/j_%s/r_%s/s_%s/aj_%s/a_%s/rf_%s/",
                                traffic, dataPacketInjectionRate, "oddEven", "aco",
                                antPacketInjectionRate, acoSelectionAlpha, reinforcementFactor
                        ),
                        numNodes,
                        maxCycles,
                        maxPackets,
                        false
                );
                experimentAco.getConfig().setDataPacketTraffic(traffic);
                experimentAco.getConfig().setDataPacketInjectionRate(dataPacketInjectionRate);
                experimentAco.getConfig().setRouting("oddEven");
                experimentAco.getConfig().setSelection("aco");
                experimentAco.getConfig().setAntPacketInjectionRate(antPacketInjectionRate);
                experimentAco.getConfig().setAcoSelectionAlpha(acoSelectionAlpha);
                experimentAco.getConfig().setReinforcementFactor(reinforcementFactor);
                experiments.get(traffic).add(experimentAco);
            }
        }

        return experiments;
    }

    private static List<NoCExperiment> testAntPacketInjectionRates() {
        String traffic = "transpose";
        double dataPacketInjectionRate = 0.060;

        List<Double> antPacketInjectionRates = Arrays.asList(
                0.0002, 0.001, 0.005, 0.025
        );

        double acoSelectionAlpha = 0.45;
        double reinforcementFactor = 0.001;

        List<NoCExperiment> experiments = new ArrayList<>();

        NoCExperiment experimentXy = new NoCExperiment(
                String.format(
                        "results/antPacketInjectionRates/t_%s/j_%s/r_%s/s_%s/",
                        traffic, dataPacketInjectionRate, "xy", "random"
                ),
                numNodes,
                maxCycles,
                maxPackets,
                false
        );
        experimentXy.getConfig().setDataPacketTraffic(traffic);
        experimentXy.getConfig().setDataPacketInjectionRate(dataPacketInjectionRate);
        experimentXy.getConfig().setRouting("xy");
        experimentXy.getConfig().setSelection("random");
        experiments.add(experimentXy);

        NoCExperiment experimentBufferLevel = new NoCExperiment(
                String.format(
                        "results/antPacketInjectionRates/t_%s/j_%s/r_%s/s_%s/",
                        traffic, dataPacketInjectionRate, "oddEven", "bufferLevel"
                ),
                numNodes,
                maxCycles,
                maxPackets,
                false
        );
        experimentBufferLevel.getConfig().setDataPacketTraffic(traffic);
        experimentBufferLevel.getConfig().setDataPacketInjectionRate(dataPacketInjectionRate);
        experimentBufferLevel.getConfig().setRouting("oddEven");
        experimentBufferLevel.getConfig().setSelection("bufferLevel");
        experiments.add(experimentBufferLevel);

        for(double antPacketInjectionRate : antPacketInjectionRates) {
            NoCExperiment experimentAco = new NoCExperiment(
                    String.format(
                            "results/antPacketInjectionRates/t_%s/j_%s/r_%s/s_%s/aj_%s/a_%s/rf_%s/",
                            traffic, dataPacketInjectionRate, "oddEven", "aco",
                            antPacketInjectionRate, acoSelectionAlpha, reinforcementFactor
                    ),
                    numNodes,
                    maxCycles,
                    maxPackets,
                    false
            );
            experimentAco.getConfig().setDataPacketTraffic(traffic);
            experimentAco.getConfig().setDataPacketInjectionRate(dataPacketInjectionRate);
            experimentAco.getConfig().setRouting("oddEven");
            experimentAco.getConfig().setSelection("aco");
            experimentAco.getConfig().setAntPacketInjectionRate(antPacketInjectionRate);
            experimentAco.getConfig().setAcoSelectionAlpha(acoSelectionAlpha);
            experimentAco.getConfig().setReinforcementFactor(reinforcementFactor);
            experiments.add(experimentAco);
        }

        return experiments;
    }

    private static List<NoCExperiment> testAcoSelectionAlphasAndReinforcementFactors() {
        String traffic = "transpose";
        double dataPacketInjectionRate  = 0.060;

        double antPacketInjectionRate = 0.0002;

        List<Double> acoSelectionAlphas = Arrays.asList(
                0.30, 0.35, 0.40, 0.45, 0.50, 0.55, 0.60, 0.65, 0.70
        );

        List<Double> reinforcementFactors = Arrays.asList(
                0.0005, 0.001, 0.002, 0.004, 0.008, 0.016, 0.032, 0.064
        );

        List<NoCExperiment> experiments = new ArrayList<>();

        NoCExperiment experimentXy = new NoCExperiment(
                String.format(
                        "results/acoSelectionAlphasAndReinforcementFactors/t_%s/j_%s/r_%s/s_%s/",
                        traffic, dataPacketInjectionRate, "xy", "random"
                ),
                numNodes,
                maxCycles,
                maxPackets,
                false
        );
        experimentXy.getConfig().setDataPacketTraffic(traffic);
        experimentXy.getConfig().setDataPacketInjectionRate(dataPacketInjectionRate);
        experimentXy.getConfig().setRouting("xy");
        experimentXy.getConfig().setSelection("random");
        experiments.add(experimentXy);

        NoCExperiment experimentBufferLevel = new NoCExperiment(
                String.format(
                        "results/acoSelectionAlphasAndReinforcementFactors/t_%s/j_%s/r_%s/s_%s/",
                        traffic, dataPacketInjectionRate, "oddEven", "bufferLevel"
                ),
                numNodes,
                maxCycles,
                maxPackets,
                false
        );
        experimentBufferLevel.getConfig().setDataPacketTraffic(traffic);
        experimentBufferLevel.getConfig().setDataPacketInjectionRate(dataPacketInjectionRate);
        experimentBufferLevel.getConfig().setRouting("oddEven");
        experimentBufferLevel.getConfig().setSelection("bufferLevel");
        experiments.add(experimentBufferLevel);

        for(double acoSelectionAlpha : acoSelectionAlphas) {
            for(double reinforcementFactor : reinforcementFactors) {
                NoCExperiment experimentAco = new NoCExperiment(
                        String.format(
                                "results/acoSelectionAlphasAndReinforcementFactors/t_%s/j_%s/r_%s/s_%s/aj_%s/a_%s/rf_%s/",
                                traffic, dataPacketInjectionRate, "oddEven", "aco",
                                antPacketInjectionRate, acoSelectionAlpha, reinforcementFactor
                        ),
                        numNodes,
                        maxCycles,
                        maxPackets,
                        false
                );
                experimentAco.getConfig().setDataPacketTraffic(traffic);
                experimentAco.getConfig().setDataPacketInjectionRate(dataPacketInjectionRate);
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

    public static Map<String, List<NoCExperiment>> trafficsAndDataPacketInjectionRates = testTrafficsAndDataPacketInjectionRates();
    public static List<NoCExperiment> antPacketInjectionRates = testAntPacketInjectionRates();
    public static List<NoCExperiment> acoSelectionAlphasAndReinforcementFactors = testAcoSelectionAlphasAndReinforcementFactors();
}
