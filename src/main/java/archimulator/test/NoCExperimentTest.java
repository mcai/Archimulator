/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.test;

import archimulator.common.Experiment;
import archimulator.startup.CSVFields;
import archimulator.uncore.noc.NoCExperiment;
import archimulator.util.csv.CSVHelper;
import archimulator.util.plots.PlotHelper;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * NoC experiment test.
 *
 * @author Min Cai
 */
public class NoCExperimentTest {
    private List<NoCExperiment> experiments;

    @Before
    public void setUp() throws Exception {
        NoCExperiment experimentXy = xy();

        NoCExperiment experimentBufferLevel = bufferLevel();

        NoCExperiment experimentAco = aco();

        experiments = Arrays.asList(
                experimentXy,
                experimentBufferLevel,
                experimentAco
        );
    }

    private NoCExperiment xy() {
        NoCExperiment experimentXy = new NoCExperiment(
                "test_results/synthetic/xy",
                64,
                20000,
                -1,
                false
        );

        experimentXy.getConfig().setRouting("xy");
        experimentXy.getConfig().setSelection("random");

        experimentXy.getConfig().setDataPacketTraffic("transpose");
        experimentXy.getConfig().setDataPacketInjectionRate(0.06);

        return experimentXy;
    }

    private NoCExperiment bufferLevel() {
        NoCExperiment experimentBufferLevel = new NoCExperiment(
                "test_results/synthetic/bufferLevel",
                64,
                20000,
                -1,
                false
        );

        experimentBufferLevel.getConfig().setRouting("oddEven");
        experimentBufferLevel.getConfig().setSelection("bufferLevel");

        experimentBufferLevel.getConfig().setDataPacketTraffic("transpose");
        experimentBufferLevel.getConfig().setDataPacketInjectionRate(0.06);

        return experimentBufferLevel;
    }

    private NoCExperiment aco() {
        NoCExperiment experimentAco = new NoCExperiment(
                "test_results/synthetic/aco",
                64,
                20000,
                -1,
                false
        );

        experimentAco.getConfig().setRouting("oddEven");
        experimentAco.getConfig().setSelection("aco");

        experimentAco.getConfig().setDataPacketTraffic("transpose");
        experimentAco.getConfig().setDataPacketInjectionRate(0.06);

        experimentAco.getConfig().setAntPacketTraffic("uniform");
        experimentAco.getConfig().setAntPacketInjectionRate(0.0002);
        experimentAco.getConfig().setAcoSelectionAlpha(0.45);
        experimentAco.getConfig().setReinforcementFactor(0.001);

        return experimentAco;
    }

    @Test
    public void run() {
        Experiment.runExperiments(experiments, true);
    }

    @Test
    public void analyze() {
        experiments.forEach(Experiment::loadStats);

        CSVHelper.toCsv(
                "test_results/synthetic/result.csv",
                experiments,
                CSVFields.csvFields
        );

        PlotHelper.generatePlot(
                "test_results/synthetic/result.csv",
                "test_results/synthetic/throughput.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "NoC_Routing_Solution",
                "Throughput_(packets/cycle/node)"
        );

        PlotHelper.generatePlot(
                "test_results/synthetic/result.csv",
                "test_results/synthetic/average_packet_delay.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "NoC_Routing_Solution",
                "Avg._Packet_Delay_(cycles)"
        );

        PlotHelper.generatePlot(
                "test_results/synthetic/result.csv",
                "test_results/synthetic/average_packet_hops.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "NoC_Routing_Solution",
                "Avg._Packet_Hops"
        );

        PlotHelper.generatePlot(
                "test_results/synthetic/result.csv",
                "test_results/synthetic/payload_throughput.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "NoC_Routing_Solution",
                "Payload_Throughput_(packets/cycle/node)"
        );

        PlotHelper.generatePlot(
                "test_results/synthetic/result.csv",
                "test_results/synthetic/average_payload_packet_delay.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "NoC_Routing_Solution",
                "Avg._Payload_Packet_Delay_(cycles)"
        );

        PlotHelper.generatePlot(
                "test_results/synthetic/result.csv",
                "test_results/synthetic/average_payload_packet_hops.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "NoC_Routing_Solution",
                "Avg._Payload_Packet_Hops"
        );
    }
}
