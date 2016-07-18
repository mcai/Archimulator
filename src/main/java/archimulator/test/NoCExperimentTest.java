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
    private int numNodes;
    private int maxCycles;
    private int maxPackets;
    private boolean noDrain;

    private List<NoCExperiment> experiments;

    /**
     * Setup.
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        numNodes = 64;
        maxCycles = 20000;
        maxPackets = -1;
        noDrain = false;

        experiments = Arrays.asList(
                xy(),
                bufferLevel(),
                aco()
        );
    }

    /**
     * Create a NoC experiment using the XY routing algorithm.
     *
     * @return a newly created NoC experiment using the XY routing algorithm
     */
    private NoCExperiment xy() {
        NoCExperiment experimentXy = new NoCExperiment(
                "test_results/synthetic/xy",
                numNodes,
                maxCycles,
                maxPackets,
                noDrain
        );

        experimentXy.getConfig().setRouting("xy");
        experimentXy.getConfig().setSelection("random");

        experimentXy.getConfig().setDataPacketTraffic("transpose");
        experimentXy.getConfig().setDataPacketInjectionRate(0.06);

        return experimentXy;
    }

    /**
     * Create a NoC experiment using odd even routing + buffer level selection algorithms.
     *
     * @return a newly created NoC experiment using odd even routing + buffer level selection algorithms
     */
    private NoCExperiment bufferLevel() {
        NoCExperiment experimentBufferLevel = new NoCExperiment(
                "test_results/synthetic/bufferLevel",
                numNodes,
                maxCycles,
                maxPackets,
                noDrain
        );

        experimentBufferLevel.getConfig().setRouting("oddEven");
        experimentBufferLevel.getConfig().setSelection("bufferLevel");

        experimentBufferLevel.getConfig().setDataPacketTraffic("transpose");
        experimentBufferLevel.getConfig().setDataPacketInjectionRate(0.06);

        return experimentBufferLevel;
    }

    /**
     * Create a NoC experiment using odd even routing + ACO selection algorithms.
     *
     * @return a newly created NoC experiment using odd even routing + buffer level selection algorithms
     */
    private NoCExperiment aco() {
        NoCExperiment experimentAco = new NoCExperiment(
                "test_results/synthetic/aco",
                numNodes,
                maxCycles,
                maxPackets,
                noDrain
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

    /**
     * Run experiments.
     */
    @Test
    public void run() {
        Experiment.runExperiments(experiments, true);
    }

    /**
     * Analyze experiments.
     */
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
