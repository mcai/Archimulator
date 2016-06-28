package archimulator.incubator.noc.startup;

import archimulator.incubator.noc.Experiment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Common.
 *
 * @author Min Cai
 */
public class Common {
    private static Map<String, List<Experiment>> testTrafficsAndDataPacketInjectionRates() {
        List<String> traffics = new ArrayList<>();
        traffics.add("uniform");
        traffics.add("transpose");
        traffics.add("hotspot");

        List<Double> dataPacketInjectionRates = new ArrayList<>();
        dataPacketInjectionRates.add(0.030);
        dataPacketInjectionRates.add(0.045);
        dataPacketInjectionRates.add(0.060);
        dataPacketInjectionRates.add(0.075);
        dataPacketInjectionRates.add(0.090);
        dataPacketInjectionRates.add(0.105);
        dataPacketInjectionRates.add(0.120);

        double antPacketInjectionRate = 0.001;

        double acoSelectionAlpha = 0.45;
        double reinforcementFactor = 0.001;

        Map<String, List<Experiment>> experiments = new HashMap<>();

        for(String traffic : traffics) {
            experiments.put(traffic, new ArrayList<>());

            for(double dataPacketInjectionRate : dataPacketInjectionRates) {
                experiments.get(traffic).add(new Experiment() {{
                    getConfig().setDataPacketInjectionRate(dataPacketInjectionRate);
                    getConfig().setTraffic(traffic);
                    getConfig().setRouting("xy");
                    getConfig().setSelection("random");
                }});

                experiments.get(traffic).add(new Experiment(){{
                    getConfig().setDataPacketInjectionRate(dataPacketInjectionRate);
                    getConfig().setTraffic(traffic);
                    getConfig().setRouting("oddEven");
                    getConfig().setSelection("bufferLevel");
                }});

                experiments.get(traffic).add(new Experiment(){{
                    getConfig().setDataPacketInjectionRate(dataPacketInjectionRate);
                    getConfig().setTraffic(traffic);
                    getConfig().setRouting("oddEven");
                    getConfig().setSelection("aco");
                    getConfig().setAntPacketInjectionRate(antPacketInjectionRate);
                    getConfig().setAcoSelectionAlpha(acoSelectionAlpha);
                    getConfig().setReinforcementFactor(reinforcementFactor);
                }});
            }
        }

        return experiments;
    }

    private static List<Experiment> testAntPacketInjectionRates() {
        String traffic = "transpose";
        double dataPacketInjectionRate = 0.060;

        List<Double> antPacketInjectionRates = new ArrayList<>();
        antPacketInjectionRates.add(0.0002);
        antPacketInjectionRates.add(0.001);
        antPacketInjectionRates.add(0.005);
        antPacketInjectionRates.add(0.025);

        double acoSelectionAlpha = 0.45;
        double reinforcementFactor = 0.001;

        List<Experiment> experiments = new ArrayList<>();

        experiments.add(new Experiment() {{
            getConfig().setDataPacketInjectionRate(dataPacketInjectionRate);
            getConfig().setTraffic(traffic);
            getConfig().setRouting("xy");
            getConfig().setSelection("random");
        }});

        experiments.add(new Experiment() {{
            getConfig().setDataPacketInjectionRate(dataPacketInjectionRate);
            getConfig().setTraffic(traffic);
            getConfig().setRouting("oddEven");
            getConfig().setSelection("bufferLevel");
        }});

        for(double antPacketInjectionRate : antPacketInjectionRates) {
            experiments.add(new Experiment() {{
                getConfig().setDataPacketInjectionRate(dataPacketInjectionRate);
                getConfig().setTraffic(traffic);
                getConfig().setRouting("oddEven");
                getConfig().setSelection("aco");
                getConfig().setAntPacketInjectionRate(antPacketInjectionRate);
                getConfig().setAcoSelectionAlpha(acoSelectionAlpha);
                getConfig().setReinforcementFactor(reinforcementFactor);
            }});
        }

        return experiments;
    }

    private static List<Experiment> testAcoSelectionAlphasAndReinforcementFactors() {
        String traffic = "transpose";
        double dataPacketInjectionRate  = 0.060;

        double antPacketInjectionRate = 0.001;

        List<Double> acoSelectionAlphas = new ArrayList<>();
        acoSelectionAlphas.add(0.30);
        acoSelectionAlphas.add(0.35);
        acoSelectionAlphas.add(0.40);
        acoSelectionAlphas.add(0.45);
        acoSelectionAlphas.add(0.50);
        acoSelectionAlphas.add(0.55);
        acoSelectionAlphas.add(0.60);
        acoSelectionAlphas.add(0.65);
        acoSelectionAlphas.add(0.70);

        List<Double> reinforcementFactors = new ArrayList<>();
        reinforcementFactors.add(0.0005);
        reinforcementFactors.add(0.001);
        reinforcementFactors.add(0.002);
        reinforcementFactors.add(0.004);
        reinforcementFactors.add(0.008);
        reinforcementFactors.add(0.016);
        reinforcementFactors.add(0.032);
        reinforcementFactors.add(0.064);

        List<Experiment> experiments = new ArrayList<>();

        experiments.add(new Experiment(){{
            getConfig().setDataPacketInjectionRate(dataPacketInjectionRate);
            getConfig().setTraffic(traffic);
            getConfig().setRouting("xy");
            getConfig().setSelection("random");
        }});

        experiments.add(new Experiment(){{
            getConfig().setDataPacketInjectionRate(dataPacketInjectionRate);
            getConfig().setTraffic(traffic);
            getConfig().setRouting("oddEven");
            getConfig().setSelection("bufferLevel");
        }});

        for(double acoSelectionAlpha : acoSelectionAlphas) {
            for(double reinforcementFactor : reinforcementFactors) {
                experiments.add(new Experiment(){{
                    getConfig().setDataPacketInjectionRate(dataPacketInjectionRate);
                    getConfig().setTraffic(traffic);
                    getConfig().setRouting("oddEven");
                    getConfig().setSelection("aco");
                    getConfig().setAntPacketInjectionRate(antPacketInjectionRate);
                    getConfig().setAcoSelectionAlpha(acoSelectionAlpha);
                    getConfig().setReinforcementFactor(reinforcementFactor);
                }});
            }
        }

        return experiments;
    }

    public static Map<String, List<Experiment>> trafficsAndDataPacketInjectionRates = testTrafficsAndDataPacketInjectionRates();
    public static List<Experiment> antPacketInjectionRates = testAntPacketInjectionRates();
    public static List<Experiment> acoSelectionAlphasAndReinforcementFactors = testAcoSelectionAlphasAndReinforcementFactors();
}
