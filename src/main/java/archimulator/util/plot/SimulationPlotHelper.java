/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.util.plot;

import archimulator.model.Experiment;
import archimulator.model.ExperimentStat;
import archimulator.model.ExperimentType;
import archimulator.service.ServiceManager;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyType;
import archimulator.sim.uncore.helperThread.BasicHelperThreadL2RequestBreakdown;
import archimulator.sim.uncore.helperThread.HelperThreadL2RequestBreakdown;
import archimulator.util.ExperimentTableHelper;
import de.erichseifert.gral.data.DataSeries;
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.io.plots.DrawableWriter;
import de.erichseifert.gral.io.plots.DrawableWriterFactory;
import de.erichseifert.gral.plots.Plot;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.axes.AxisRenderer;
import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
import de.erichseifert.gral.plots.lines.LineRenderer;
import de.erichseifert.gral.plots.points.PointRenderer;
import de.erichseifert.gral.util.Insets2D;
import net.pickapack.action.Action1;
import net.pickapack.util.Pair;
import net.pickapack.util.Triple;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.parboiled.common.FileUtils;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simulation plot helper.
 *
 * @author Min Cai
 */
public class SimulationPlotHelper {
    private static final List<Color> COLORS = Arrays.asList(
            Color.BLACK, Color.GREEN, Color.YELLOW, Color.RED, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.ORANGE, Color.PINK
    );

    /**
     * Generate the CSV file.
     *
     * @param currentExperimentPackTitle the current experiment pack title
     * @param experimentPackTitles the array of experiment pack titles
     */
    public static Table table(String currentExperimentPackTitle, String... experimentPackTitles) {
        Table table = ExperimentTableHelper.table(new TableFilterCriteria(
        ) {{
            setPreserveColumns(true);

            getConditions().add(new Pair<>("State", Arrays.asList("COMPLETED")));

        }}, currentExperimentPackTitle, experimentPackTitles
        );

        ExperimentTableHelper.normalize(table);

        return table;
    }

    /**
     * Generate the CSV file by the specified L2 size.
     *
     * @param currentExperimentPackTitle the current experiment pack title
     * @param l2Size               the L2 size
     * @param experimentPackTitles the array of experiment pack titles
     */
    public static Table tableByL2Size(String currentExperimentPackTitle, final String l2Size, String... experimentPackTitles) {
        Table table = ExperimentTableHelper.table(new TableFilterCriteria(
        ) {{
            setPreserveColumns(true);

            getConditions().add(new Pair<>("State", Arrays.asList("COMPLETED")));

            getConditions().add(new Pair<>("L2_Size", Arrays.asList(l2Size)));

        }}, currentExperimentPackTitle, experimentPackTitles
        );

        ExperimentTableHelper.normalize(table);

        return table;
    }

    /**
     * Generate the CSV file by the specified L2 replacement policy type.
     *
     * @param l2Replacement        the L2 replacement policy type
     * @param experimentPackTitles the array of experiment pack titles
     */
    public static Table tableByL2Replacement(String currentExperimentPackTitle, final String l2Replacement, String... experimentPackTitles) {
        Table table = ExperimentTableHelper.table(new TableFilterCriteria(
        ) {{
            setPreserveColumns(true);

            getConditions().add(new Pair<>("State", Arrays.asList("COMPLETED")));

            getConditions().add(new Pair<>("L2_Replacement", Arrays.asList(l2Replacement)));

        }}, currentExperimentPackTitle, experimentPackTitles
        );

        ExperimentTableHelper.normalize(table);

        return table;
    }

    /**
     * Generate the helper threaded CSV file.
     *
     * @param experimentPackTitles the array of experiment pack titles
     */
    public static Table tableHelperThreadedByStrideAndLookahead(String currentExperimentPackTitle, final String stride, final String lookahead, String... experimentPackTitles) {
        Table table = ExperimentTableHelper.table(new TableFilterCriteria(
        ) {{
            setPreserveColumns(true);

            getConditions().add(new Pair<>("State", Arrays.asList("COMPLETED")));

            getConditions().add(new Pair<>("Stride", Arrays.asList(stride)));
            getConditions().add(new Pair<>("Lookahead", Arrays.asList(lookahead)));

        }}, currentExperimentPackTitle, experimentPackTitles
        );

        ExperimentTableHelper.normalize(table);

        return table;
    }

    /**
     * Generate the helper threaded CSV file.
     *
     * @param experimentPackTitles the array of experiment pack titles
     */
    public static Table tableHelperThreadedByLookahead(String currentExperimentPackTitle, final String lookahead, String... experimentPackTitles) {
        Table table = ExperimentTableHelper.table(new TableFilterCriteria(
        ) {{
            setPreserveColumns(true);

            getConditions().add(new Pair<>("State", Arrays.asList("COMPLETED")));

            getConditions().add(new Pair<>("Lookahead", Arrays.asList(lookahead)));

        }}, currentExperimentPackTitle, experimentPackTitles
        );

        ExperimentTableHelper.normalize(table);

        return table;
    }

    /**
     * Generate the helper threaded CSV file.
     *
     * @param experimentPackTitles the array of experiment pack titles
     */
    public static Table tableHelperThreadedByStride(String currentExperimentPackTitle, final String stride, String... experimentPackTitles) {
        Table table = ExperimentTableHelper.table(new TableFilterCriteria(
        ) {{
            setPreserveColumns(true);

            getConditions().add(new Pair<>("State", Arrays.asList("COMPLETED")));

            getConditions().add(new Pair<>("Stride", Arrays.asList(stride)));

        }}, currentExperimentPackTitle, experimentPackTitles
        );

        ExperimentTableHelper.normalize(table);

        return table;
    }

    /**
     * Generate the "main thread ways in the partitioned L2 cache" CSV file.
     *
     * @param mtWaysInPartitionedL2 the main thread ways in the partitioned L2 cache
     * @param experimentPackTitles  the array of experiment pack titles
     */
    public static Table tableStaticPartitionedL2(String currentExperimentPackTitle, final String mtWaysInPartitionedL2, String... experimentPackTitles) {
        Table table = ExperimentTableHelper.table(new TableFilterCriteria(
        ) {{
            setPreserveColumns(true);

            getConditions().add(new Pair<>("State", Arrays.asList("COMPLETED")));

            getConditions().add(new Pair<>("MT_Ways_In_Partitioned_L2", Arrays.asList(mtWaysInPartitionedL2)));

        }}, currentExperimentPackTitle, experimentPackTitles
        );

        ExperimentTableHelper.normalize(table);

        return table;
    }

    /**
     * Generate the "main thread ways in the partitioned L2 cache" CSV file.
     *
     * @param l2ReplacementPolicyType the L2 replacement policy type
     * @param experimentPackTitles    the array of experiment pack titles
     */
    public static Table tableDynamicPartitionedL2(String currentExperimentPackTitle, final String l2ReplacementPolicyType, String... experimentPackTitles) {
        Table table = ExperimentTableHelper.table(new TableFilterCriteria(
        ) {{
            setPreserveColumns(true);

            getConditions().add(new Pair<>("State", Arrays.asList("COMPLETED")));

            getConditions().add(new Pair<>("L2_Replacement", Arrays.asList(l2ReplacementPolicyType)));

        }}, currentExperimentPackTitle, experimentPackTitles
        );

        ExperimentTableHelper.normalize(table);

        return table;
    }

    /**
     * Create a bar graph illustrating the number of cycles.
     *
     * @param labeledRows the list of labeled rows
     * @return the newly created bar graph illustrating the number of cycles
     */
    public static BarGraph numCycles(
            final List<Triple<String, Table, List<Pair<String, String>>>> labeledRows
    ) {
        return new SimpleBarGraph("", "# Cycles") {
            {
                setxScale(0.67);
                setyScale(0.67);

                setyFormat(YFormat.LONG_SCIENTIFIC);

                for (Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
                    String label = labeledRow.getFirst();
                    Table table = labeledRow.getSecond();
                    List<Pair<String, String>> conditions = labeledRow.getThird();

                    List<String> row = Table.findRow(table, conditions);

                    getBars().add(new Pair<>(label, Long.parseLong(Table.getValue(table, row, "Num_Cycles"))));
                }
            }
        };
    }

    /**
     * Create a bar graph illustrating the speedup.
     *
     * @param labeledRows the list of labeled rows
     * @return the newly created bar graph illustrating the helper thread prefetch accuracy
     */
    public static BarGraph speedup(
            final List<Triple<String, Table, List<Pair<String, String>>>> labeledRows
    ) {
        return new SimpleBarGraph("", "Speedup") {
            {
                setxScale(0.67);
                setyScale(0.67);

                if(labeledRows.size() > 15) {
                    setxScale(2.0);
                    setNoRotate(true);
                }

                setyFormat(YFormat.DOUBLE);

                for (Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
                    String label = labeledRow.getFirst();
                    Table table = labeledRow.getSecond();
                    List<Pair<String, String>> conditions = labeledRow.getThird();

                    List<String> row = Table.findRow(table, conditions);

                    getBars().add(new Pair<>(label, Double.parseDouble(Table.getValue(table, row, "Speedup"))));
                }
            }
        };
    }

    /**
     * Create a bar graph illustrating the IPC.
     *
     * @param labeledRows the list of labeled rows
     * @return the newly created bar graph illustrating the IPC
     */
    public static BarGraph ipc(
            final List<Triple<String, Table, List<Pair<String, String>>>> labeledRows
    ) {
        return new SimpleBarGraph("", "IPC") {
            {
                setxScale(0.67);
                setyScale(0.67);

                setyFormat(YFormat.DOUBLE);

                for (Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
                    String label = labeledRow.getFirst();
                    Table table = labeledRow.getSecond();
                    List<Pair<String, String>> conditions = labeledRow.getThird();

                    List<String> row = Table.findRow(table, conditions);

                    getBars().add(new Pair<>(label, Double.parseDouble(Table.getValue(table, row, "IPC"))));
                }
            }
        };
    }

    /**
     * Create a bar graph illustrating the IPC for the thread C0T0.
     *
     * @param labeledRows the list of labeled rows
     * @return the newly created bar graph illustrating the IPC for the thread C0T0
     */
    public static BarGraph c0t0Ipc(
            final List<Triple<String, Table, List<Pair<String, String>>>> labeledRows
    ) {
        return new SimpleBarGraph("", "MT.IPC") {
            {
                setxScale(0.67);
                setyScale(0.67);

                setyFormat(YFormat.DOUBLE);

                for (Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
                    String label = labeledRow.getFirst();
                    Table table = labeledRow.getSecond();
                    List<Pair<String, String>> conditions = labeledRow.getThird();

                    List<String> row = Table.findRow(table, conditions);

                    getBars().add(new Pair<>(label, Double.parseDouble(Table.getValue(table, row, "C0T0.IPC"))));
                }
            }
        };
    }

    /**
     * Create a bar graph illustrating the IPC for the thread C1T0.
     *
     * @param labeledRows the list of labeled rows
     * @return the newly created bar graph illustrating the IPC for the thread C1T0
     */
    public static BarGraph c1t0Ipc(
            final List<Triple<String, Table, List<Pair<String, String>>>> labeledRows
    ) {
        return new SimpleBarGraph("", "HT.IPC") {
            {
                setxScale(0.67);
                setyScale(0.67);

                setyFormat(YFormat.DOUBLE);

                for (Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
                    String label = labeledRow.getFirst();
                    Table table = labeledRow.getSecond();
                    List<Pair<String, String>> conditions = labeledRow.getThird();

                    List<String> row = Table.findRow(table, conditions);

                    getBars().add(new Pair<>(label, Double.parseDouble(Table.getValue(table, row, "C1T0.IPC"))));
                }
            }
        };
    }

    /**
     * Create a bar graph illustrating the CPI.
     *
     * @param labeledRows the list of labeled rows
     * @return the newly created bar graph illustrating the CPI
     */
    public static BarGraph cpi(
            final List<Triple<String, Table, List<Pair<String, String>>>> labeledRows
    ) {
        return new SimpleBarGraph("", "CPI") {
            {
                setxScale(0.67);
                setyScale(0.67);

                setyFormat(YFormat.DOUBLE);

                for (Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
                    String label = labeledRow.getFirst();
                    Table table = labeledRow.getSecond();
                    List<Pair<String, String>> conditions = labeledRow.getThird();

                    List<String> row = Table.findRow(table, conditions);

                    getBars().add(new Pair<>(label, Double.parseDouble(Table.getValue(table, row, "CPI"))));
                }
            }
        };
    }

    /**
     * Create a bar graph illustrating the CPI for the thread C0T0.
     *
     * @param labeledRows the list of labeled rows
     * @return the newly created bar graph illustrating the CPI for the thread C0T0
     */
    public static BarGraph c0t0Cpi(
            final List<Triple<String, Table, List<Pair<String, String>>>> labeledRows
    ) {
        return new SimpleBarGraph("", "MT.CPI") {
            {
                setxScale(0.67);
                setyScale(0.67);

                setyFormat(YFormat.DOUBLE);

                for (Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
                    String label = labeledRow.getFirst();
                    Table table = labeledRow.getSecond();
                    List<Pair<String, String>> conditions = labeledRow.getThird();

                    List<String> row = Table.findRow(table, conditions);

                    getBars().add(new Pair<>(label, Double.parseDouble(Table.getValue(table, row, "C0T0.CPI"))));
                }
            }
        };
    }

    /**
     * Create a bar graph illustrating the CPI for the thread C1T0.
     *
     * @param labeledRows the list of labeled rows
     * @return the newly created bar graph illustrating the CPI for the thread C1T0
     */
    public static BarGraph c1t0Cpi(
            final List<Triple<String, Table, List<Pair<String, String>>>> labeledRows
    ) {
        return new SimpleBarGraph("", "HT.CPI") {
            {
                setxScale(0.67);
                setyScale(0.67);

                setyFormat(YFormat.DOUBLE);

                for (Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
                    String label = labeledRow.getFirst();
                    Table table = labeledRow.getSecond();
                    List<Pair<String, String>> conditions = labeledRow.getThird();

                    List<String> row = Table.findRow(table, conditions);

                    getBars().add(new Pair<>(label, Double.parseDouble(Table.getValue(table, row, "C1T0.CPI"))));
                }
            }
        };
    }

    /**
     * Create a bar graph illustrating the L2 MPKI.
     *
     * @param labeledRows the list of labeled rows
     * @return the newly created bar graph illustrating the L2 MPKI
     */
    public static BarGraph l2Mpki(
            final List<Triple<String, Table, List<Pair<String, String>>>> labeledRows
    ) {
        return new SimpleBarGraph("", "L2_MPKI") {
            {
                setxScale(0.67);
                setyScale(0.67);

                setyFormat(YFormat.DOUBLE);

                for (Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
                    String label = labeledRow.getFirst();
                    Table table = labeledRow.getSecond();
                    List<Pair<String, String>> conditions = labeledRow.getThird();

                    List<String> row = Table.findRow(table, conditions);

                    getBars().add(new Pair<>(label, Double.parseDouble(Table.getValue(table, row, "L2_MPKI"))));
                }
            }
        };
    }

    /**
     * Create a bar graph illustrating the L2 MPKI for the thread C0T0.
     *
     * @param labeledRows the list of labeled rows
     * @return the newly created bar graph illustrating the L2 MPKI for the thread C0T0
     */
    public static BarGraph c0t0L2Mpki(
            final List<Triple<String, Table, List<Pair<String, String>>>> labeledRows
    ) {
        return new SimpleBarGraph("", "MT.L2_MPKI") {
            {
                setxScale(0.67);
                setyScale(0.67);

                setyFormat(YFormat.DOUBLE);

                for (Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
                    String label = labeledRow.getFirst();
                    Table table = labeledRow.getSecond();
                    List<Pair<String, String>> conditions = labeledRow.getThird();

                    List<String> row = Table.findRow(table, conditions);

                    getBars().add(new Pair<>(label, Double.parseDouble(Table.getValue(table, row, "C0T0.L2_MPKI"))));
                }
            }
        };
    }

    /**
     * Create a bar graph illustrating the L2 MPKI for the thread C1T0.
     *
     * @param labeledRows the list of labeled rows
     * @return the newly created bar graph illustrating the L2 MPKI for the thread C1T0
     */
    public static BarGraph c1t0L2Mpki(
            final List<Triple<String, Table, List<Pair<String, String>>>> labeledRows
    ) {
        return new SimpleBarGraph("", "HT.L2_MPKI") {
            {
                setxScale(0.67);
                setyScale(0.67);

                setyFormat(YFormat.DOUBLE);

                for (Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
                    String label = labeledRow.getFirst();
                    Table table = labeledRow.getSecond();
                    List<Pair<String, String>> conditions = labeledRow.getThird();

                    List<String> row = Table.findRow(table, conditions);

                    getBars().add(new Pair<>(label, Double.parseDouble(Table.getValue(table, row, "C1T0.L2_MPKI"))));
                }
            }
        };
    }

    /**
     * Create a bar graph illustrating the number of dynamic instructions committed.
     *
     * @param labeledRows the list of labeled rows
     * @return the newly created bar graph illustrating the number of dynamic instructions committed
     */
    public static BarGraph numDynamicInstructionsCommitted(
            final List<Triple<String, Table, List<Pair<String, String>>>> labeledRows
    ) {
        return new StackedBarGraph("", "# Dynamic Instructions Committed") {
            {
                setxScale(0.67);
                setyScale(0.67);

                setyFormat(YFormat.LONG_SCIENTIFIC);

                for (final Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
                    getStackedBars().add(new StackedBarGraph.StackedBar(labeledRow.getFirst()) {{
                        Table table = labeledRow.getSecond();
                        List<Pair<String, String>> conditions = labeledRow.getThird();

                        List<String> row = Table.findRow(table, conditions);

                        getStack().add(new Pair<>("MT.Insts", Long.parseLong(Table.getValue(table, row, "C0T0.Num_Instructions"))));
                        getStack().add(new Pair<>("HT.Insts", Long.parseLong(Table.getValue(table, row, "C1T0.Num_Instructions"))));
                    }});
                }
            }
        };
    }

    /**
     * Create a bar graph illustrating the number of L2 cache requests.
     *
     * @param labeledRows the list of labeled rows
     * @return the newly created bar graph illustrating the number of L2 cache requests
     */
    public static BarGraph numL2Requests(
            final List<Triple<String, Table, List<Pair<String, String>>>> labeledRows
    ) {
        return new StackedBarGraph("", "# L2 Requests") {
            {
                setxScale(0.67);
                setyScale(0.67);

                setyFormat(YFormat.LONG_SCIENTIFIC);

                for (final Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
                    getStackedBars().add(new StackedBarGraph.StackedBar(labeledRow.getFirst()) {{
                        Table table = labeledRow.getSecond();
                        List<Pair<String, String>> conditions = labeledRow.getThird();

                        List<String> row = Table.findRow(table, conditions);

                        getStack().add(new Pair<>("MT.Hits", Long.parseLong(Table.getValue(table, row, "MT.Hits"))));
                        getStack().add(new Pair<>("MT.Misses", Long.parseLong(Table.getValue(table, row, "MT.Misses"))));
                        getStack().add(new Pair<>("HT.Hits", Long.parseLong(Table.getValue(table, row, "HT.Hits"))));
                        getStack().add(new Pair<>("HT.Misses", Long.parseLong(Table.getValue(table, row, "HT.Misses"))));
                    }});
                }
            }
        };
    }

    /**
     * Create a bar graph illustrating the number of helper thread L2 cache requests.
     *
     * @param labeledRows the list of labeled rows
     * @return the newly created bar graph illustrating the number of helper thread L2 cache requests
     */
    public static BarGraph numHelperThreadL2Requests(
            final List<Triple<String, Table, List<Pair<String, String>>>> labeledRows
    ) {
        return new StackedBarGraph("", "# HT L2 Requests") {
            {
                setxScale(0.67);
                setyScale(0.67);

                setyFormat(YFormat.LONG_SCIENTIFIC);

                for (final Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
                    getStackedBars().add(new StackedBarGraph.StackedBar(labeledRow.getFirst()) {{
                        Table table = labeledRow.getSecond();
                        List<Pair<String, String>> conditions = labeledRow.getThird();

                        List<String> row = Table.findRow(table, conditions);

                        long numLateHelperThreadL2Requests = Long.parseLong(Table.getValue(table, row, "Late"));
                        long numTimelyHelperThreadL2Requests = Long.parseLong(Table.getValue(table, row, "Timely"));
                        long numBadHelperThreadL2Requests = Long.parseLong(Table.getValue(table, row, "Bad"));
                        long numEarlyHelperThreadL2Requests = Long.parseLong(Table.getValue(table, row, "Early"));
                        long numUglyHelperThreadL2Requests = Long.parseLong(Table.getValue(table, row, "Ugly"));
                        long numRedundantMshrHelperThreadL2Requests = Long.parseLong(Table.getValue(table, row, "Redundant_MSHR"));
                        long numRedundantCacheHelperThreadL2Requests = Long.parseLong(Table.getValue(table, row, "Redundant_Cache"));

                        long numUsefulHelperThreadL2Requests = numLateHelperThreadL2Requests + numTimelyHelperThreadL2Requests;
                        long numUselessHelperThreadL2Requests = numBadHelperThreadL2Requests + numEarlyHelperThreadL2Requests + numUglyHelperThreadL2Requests + numRedundantMshrHelperThreadL2Requests + numRedundantCacheHelperThreadL2Requests;

                        getStack().add(new Pair<>("Useful", numUsefulHelperThreadL2Requests));
                        getStack().add(new Pair<>("Useless", numUselessHelperThreadL2Requests));
                    }});
                }
            }
        };
    }

    /**
     * Create a bar graph illustrating the number of useful helper thread L2 cache requests.
     *
     * @param labeledRows the list of labeled rows
     * @return the newly created bar graph illustrating the number of useful helper thread L2 cache requests
     */
    public static BarGraph numUsefulHelperThreadL2Requests(
            final List<Triple<String, Table, List<Pair<String, String>>>> labeledRows
    ) {
        return new StackedBarGraph("", "# Useful") {
            {
                setxScale(0.67);
                setyScale(0.67);

                if(labeledRows.size() > 15) {
                    setxScale(2.0);
                    setNoRotate(true);
                }

                setyFormat(YFormat.LONG_SCIENTIFIC);

                for (final Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
                    getStackedBars().add(new StackedBarGraph.StackedBar(labeledRow.getFirst()) {{
                        Table table = labeledRow.getSecond();
                        List<Pair<String, String>> conditions = labeledRow.getThird();

                        List<String> row = Table.findRow(table, conditions);

                        getStack().add(new Pair<>("Late", Long.parseLong(Table.getValue(table, row, "Late"))));
                        getStack().add(new Pair<>("Timely", Long.parseLong(Table.getValue(table, row, "Timely"))));
                    }});
                }
            }
        };
    }

    /**
     * Create a bar graph illustrating the number of useless helper thread L2 cache requests.
     *
     * @param labeledRows the list of labeled rows
     * @return the newly created bar graph illustrating the number of useless helper thread L2 cache requests
     */
    public static BarGraph numUselessHelperThreadL2Requests(
            final List<Triple<String, Table, List<Pair<String, String>>>> labeledRows
    ) {
        return new StackedBarGraph("", "# Useless") {
            {
                setxScale(0.67);
                setyScale(0.67);

                if(labeledRows.size() > 15) {
                    setxScale(2.0);
                    setNoRotate(true);
                }

                setyFormat(YFormat.LONG_SCIENTIFIC);

                for (final Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
                    getStackedBars().add(new StackedBarGraph.StackedBar(labeledRow.getFirst()) {{
                        Table table = labeledRow.getSecond();
                        List<Pair<String, String>> conditions = labeledRow.getThird();

                        List<String> row = Table.findRow(table, conditions);

                        getStack().add(new Pair<>("Bad", Long.parseLong(Table.getValue(table, row, "Bad"))));
                        getStack().add(new Pair<>("Early", Long.parseLong(Table.getValue(table, row, "Early"))));
                        getStack().add(new Pair<>("Ugly", Long.parseLong(Table.getValue(table, row, "Ugly"))));
                        getStack().add(new Pair<>("Redundant_MSHR", Long.parseLong(Table.getValue(table, row, "Redundant_MSHR"))));
                        getStack().add(new Pair<>("Redundant_Cache", Long.parseLong(Table.getValue(table, row, "Redundant_Cache"))));
                    }});
                }
            }
        };
    }

    /**
     * Create a bar graph illustrating the helper thread prefetch coverage.
     *
     * @param labeledRows the list of labeled rows
     * @return the newly created bar graph illustrating the helper thread prefetch coverage
     */
    public static BarGraph helperThreadPrefetchCoverage(
            final List<Triple<String, Table, List<Pair<String, String>>>> labeledRows
    ) {
        return new SimpleBarGraph("", "Coverage (%)") {
            {
                setxScale(0.67);
                setyScale(0.67);

                if(labeledRows.size() > 15) {
                    setxScale(2.0);
                    setNoRotate(true);
                }

                setyFormat(YFormat.DOUBLE);

                for (Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
                    String label = labeledRow.getFirst();
                    Table table = labeledRow.getSecond();
                    List<Pair<String, String>> conditions = labeledRow.getThird();

                    List<String> row = Table.findRow(table, conditions);

                    getBars().add(new Pair<>(label, Double.parseDouble(Table.getValue(table, row, "HT.Coverage")) * 100));
                }
            }
        };
    }

    /**
     * Create a bar graph illustrating the helper thread prefetch accuracy.
     *
     * @param labeledRows the list of labeled rows
     * @return the newly created bar graph illustrating the helper thread prefetch accuracy
     */
    public static BarGraph helperThreadPrefetchAccuracy(
            final List<Triple<String, Table, List<Pair<String, String>>>> labeledRows
    ) {
        return new SimpleBarGraph("", "Accuracy (%)") {
            {
                setxScale(0.67);
                setyScale(0.67);

                if(labeledRows.size() > 15) {
                    setxScale(2.0);
                    setNoRotate(true);
                }

                setyFormat(YFormat.DOUBLE);

                for (Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
                    String label = labeledRow.getFirst();
                    Table table = labeledRow.getSecond();
                    List<Pair<String, String>> conditions = labeledRow.getThird();

                    List<String> row = Table.findRow(table, conditions);

                    getBars().add(new Pair<>(label, Double.parseDouble(Table.getValue(table, row, "HT.Accuracy")) * 100));
                }
            }
        };
    }

    /**
     * Create a bar graph illustrating the helper thread prefetch lateness.
     *
     * @param labeledRows the list of labeled rows
     * @return the newly created bar graph illustrating the helper thread prefetch lateness
     */
    public static BarGraph helperThreadPrefetchLateness(
            final List<Triple<String, Table, List<Pair<String, String>>>> labeledRows
    ) {
        return new SimpleBarGraph("", "Lateness (%)") {
            {
                setxScale(0.67);
                setyScale(0.67);

                if(labeledRows.size() > 15) {
                    setxScale(2.0);
                    setNoRotate(true);
                }

                setyFormat(YFormat.DOUBLE);

                for (Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
                    String label = labeledRow.getFirst();
                    Table table = labeledRow.getSecond();
                    List<Pair<String, String>> conditions = labeledRow.getThird();

                    List<String> row = Table.findRow(table, conditions);

                    getBars().add(new Pair<>(label, Double.parseDouble(Table.getValue(table, row, "HT.Lateness")) * 100));
                }
            }
        };
    }

    /**
     * Create a bar graph illustrating the helper thread prefetch pollution.
     *
     * @param labeledRows the list of labeled rows
     * @return the newly created bar graph illustrating the helper thread prefetch pollution
     */
    public static BarGraph helperThreadPrefetchPollution(
            final List<Triple<String, Table, List<Pair<String, String>>>> labeledRows
    ) {
        double maxValue = labeledRows.stream().map(labeledRow -> Double.parseDouble(Table.getValue(labeledRow.getSecond(), Table.findRow(labeledRow.getSecond(), labeledRow.getThird()), "HT.Pollution")))
                .max(Comparator.<Double>naturalOrder()).get();

        double minValue = labeledRows.stream().map(labeledRow -> Double.parseDouble(Table.getValue(labeledRow.getSecond(), Table.findRow(labeledRow.getSecond(), labeledRow.getThird()), "HT.Pollution")))
                .min(Comparator.<Double>naturalOrder()).get();

        return new SimpleBarGraph("", "Pollution (%)") {
            {
                setxScale(0.67);
                setyScale(0.67);

                if(labeledRows.size() > 15) {
                    setxScale(2.0);
                    setNoRotate(true);
                }

                setMax(maxValue * 100);
                setMin(minValue * 100);

                setyFormat(YFormat.DOUBLE);

                for (Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
                    String label = labeledRow.getFirst();
                    Table table = labeledRow.getSecond();
                    List<Pair<String, String>> conditions = labeledRow.getThird();

                    List<String> row = Table.findRow(table, conditions);
                    getBars().add(new Pair<>(label, Double.parseDouble(Table.getValue(table, row, "HT.Pollution")) * 100));
                }
            }
        };
    }

    /**
     * Set the line graph plot properties for the specified plot.
     *
     * @param plot the plot
     * @param labelX the label X
     * @param labelY the label Y
     */
    private static void setLineGraphPlotProperties(Plot plot, String labelX, String labelY) {
        plot.setInsets(new Insets2D.Double(10.0, 230, 170, 10.0));
//        plot.setSetting(Plot.LEGEND, true);
        plot.setSetting(Plot.LEGEND, false);

        plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.LABEL, labelX);
        plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.LABEL, labelY);

        plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.LABEL_DISTANCE, 2.85);

        plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.TICKS_FONT, new Font("Courier New", 0, 34));
        plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.TICKS_FONT, new Font("Courier New", 0, 34));

        plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.LABEL_FONT, new Font("Courier New", 0, 38));
        plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.LABEL_FONT, new Font("Courier New", 0, 38));
    }

    /**
     * Set the line graph point renderer properties for the specified plot, data series and color.
     *
     * @param plot       the plot
     * @param dataSeries the data series
     * @param color      the color
     */
    private static void setLineGraphPointRendererProperties(XYPlot plot, DataSeries dataSeries, Color color) {
        PointRenderer pointRenderer = plot.getPointRenderer(dataSeries);
        pointRenderer.setSetting(PointRenderer.COLOR, color);
        pointRenderer.setSetting(PointRenderer.SHAPE, new Ellipse2D.Double(-3, -3, 6, 6));
        LineRenderer lineRenderer = new DefaultLineRenderer2D();
        lineRenderer.setSetting(LineRenderer.STROKE, new BasicStroke(2f));
        lineRenderer.setSetting(LineRenderer.GAP, 1.0);
        lineRenderer.setSetting(LineRenderer.COLOR, color);
        plot.setLineRenderer(dataSeries, lineRenderer);
    }

    /**
     * Set the line graph legend properties for the specified plot.
     *
     * @param Plot the plot
     */
    private static void setLineGraphLegendProperties(Plot Plot) {
//        Plot.getLegend().setSetting(Legend.ORIENTATION, Orientation.HORIZONTAL);
//        Plot.getLegend().setSetting(Legend.ALIGNMENT_Y, 0.05);
////        Plot.getLegend().setSetting(Legend.BORDER, new BasicStroke(0));
//        Plot.getLegend().setSetting(Legend.BACKGROUND, new Color(0, 0, 0, 0));
//        Plot.getLegend().setSetting(Legend.GAP, new Dimension2D.Double(0.5, 0.5));
//        Plot.getLegend().setSetting(Legend.SYMBOL_SIZE, new Dimension2D.Double(0.5, 0.5));
//        Plot.getLegend().setSetting(Legend.FONT, new Font("Courier New", 0, 20));
    }

    /**
     * Write the specified graph to the specified PDF file.
     *
     * @param label             the label
     * @param plot              the plot
     * @param pdfFileNamePrefix the PDF file name prefix
     */
    private static void writeLineGraphToPdfFile(String label, Plot plot, String pdfFileNamePrefix) {
        String path = FilenameUtils.getFullPath(pdfFileNamePrefix);
        String baseName = label + "/" + FilenameUtils.getBaseName(pdfFileNamePrefix);

        File file = new File(path + baseName + ".pdf");

        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                throw new RuntimeException();
            }
        }

        try {
            DrawableWriter writer = DrawableWriterFactory.getInstance().get("application/pdf");
            writer.write(plot, new FileOutputStream(file), 800, 600);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Plot the specified line graph.
     *
     * @param labelX the label X
     * @param labelY the label Y
     * @param labeledRow        the labeled row
     * @param dataSeriesList    the data series list
     * @param pdfFileNamePrefix the PDF file name prefix
     */
    public static void plotLineGraph(
            String labelX, String labelY,
            Triple<String, Table, List<Pair<String, String>>> labeledRow, List<DataSeries> dataSeriesList, String pdfFileNamePrefix
    ) {
        XYPlot plot = new XYPlot(dataSeriesList.toArray(new DataSeries[dataSeriesList.size()]));

        int i = 0;
        for (DataSeries dataSeries : dataSeriesList) {
            setLineGraphPointRendererProperties(plot, dataSeries, COLORS.get(i++));
        }

        setLineGraphPlotProperties(plot, labelX, labelY);

        setLineGraphLegendProperties(plot);

        writeLineGraphToPdfFile(labeledRow.getFirst(), plot, pdfFileNamePrefix);
    }

    /**
     * Plot the number of dynamic instructions committed per interval line graph.
     *
     * @param labeledRows       the list of labeled rows
     * @param pdfFileNamePrefix the PDF file name prefix
     */
    @SuppressWarnings("unchecked")
    public static void plotNumDynamicInstructionsCommittedPerInterval(
            final List<Triple<String, Table, List<Pair<String, String>>>> labeledRows, String pdfFileNamePrefix) {
        for (Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
            Table table = labeledRow.getSecond();
            List<Pair<String, String>> conditions = labeledRow.getThird();

            List<String> row = Table.findRow(table, conditions);

            long experimentId = Long.parseLong(Table.getValue(table, row, "Id"));

            Experiment experiment = ServiceManager.getExperimentService().getExperimentById(experimentId);

            ExperimentType experimentType = experiment.getType();

            int numIntervals = ServiceManager.getExperimentStatService().getStatsByParentAndPrefixAndKeyLike(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                    "intervalHelper/numMainThreadDynamicInstructionsCommitted").size();

            final DataTable dataTableNumMainThreadDynamicInstructionsCommitted = new DataTable(Integer.class, Long.class);
            final DataTable dataTableNumHelperThreadDynamicInstructionsCommitted = new DataTable(Integer.class, Long.class);

            for (int i = 0; i < numIntervals; i++) {
                ExperimentStat numMainThreadDynamicInstructionsCommitted = ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/numMainThreadDynamicInstructionsCommitted" + "[" + i + "]");
                ExperimentStat numHelperThreadDynamicInstructionsCommitted = ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/numHelperThreadDynamicInstructionsCommitted" + "[" + i + "]");

                dataTableNumMainThreadDynamicInstructionsCommitted.add(i, Long.parseLong(numMainThreadDynamicInstructionsCommitted.getValue()));
                dataTableNumHelperThreadDynamicInstructionsCommitted.add(i, Long.parseLong(numHelperThreadDynamicInstructionsCommitted.getValue()));
            }

            List<DataSeries> dataSeriesList = new ArrayList<DataSeries>() {{
                add(new DataSeries("MT Insts", dataTableNumMainThreadDynamicInstructionsCommitted, 0, 1));

                if(ServiceManager.getExperimentStatService().getSummaryByParent(experiment).getC1t0NumInstructions() > 0) {
                    add(new DataSeries("HT Insts", dataTableNumHelperThreadDynamicInstructionsCommitted, 0, 1));
                }
            }};

            plotLineGraph("Program Phases", "# Dynamic Instructions Committed", labeledRow, dataSeriesList, pdfFileNamePrefix);
        }
    }

    /**
     * Plot the number of L2 requests per interval line graph.
     *
     * @param labeledRows       the list of labeled rows
     * @param pdfFileNamePrefix the PDF file name prefix
     */
    @SuppressWarnings("unchecked")
    public static void plotNumL2RequestsPerInterval(
            final List<Triple<String, Table, List<Pair<String, String>>>> labeledRows, String pdfFileNamePrefix) {
        for (Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
            Table table = labeledRow.getSecond();
            List<Pair<String, String>> conditions = labeledRow.getThird();

            List<String> row = Table.findRow(table, conditions);

            long experimentId = Long.parseLong(Table.getValue(table, row, "Id"));

            Experiment experiment = ServiceManager.getExperimentService().getExperimentById(experimentId);

            ExperimentType experimentType = experiment.getType();

            int numIntervals = ServiceManager.getExperimentStatService().getStatsByParentAndPrefixAndKeyLike(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                    "intervalHelper/numMainThreadL2Hits").size();

            final DataTable dataTableNumMainThreadL2Hits = new DataTable(Integer.class, Long.class);
            final DataTable dataTableNumMainThreadL2Misses = new DataTable(Integer.class, Long.class);
            final DataTable dataTableNumHelperThreadL2Hits = new DataTable(Integer.class, Long.class);
            final DataTable dataTableNumHelperThreadL2Misses = new DataTable(Integer.class, Long.class);

            for (int i = 0; i < numIntervals; i++) {
                ExperimentStat numMainThreadL2Hits = ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/numMainThreadL2Hits" + "[" + i + "]");
                ExperimentStat numMainThreadL2Misses = ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/numMainThreadL2Misses" + "[" + i + "]");
                ExperimentStat numHelperThreadL2Hits = ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/numHelperThreadL2Hits" + "[" + i + "]");
                ExperimentStat numHelperThreadL2Misses = ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/numHelperThreadL2Misses" + "[" + i + "]");

                dataTableNumMainThreadL2Hits.add(i, Long.parseLong(numMainThreadL2Hits.getValue()));
                dataTableNumMainThreadL2Misses.add(i, Long.parseLong(numMainThreadL2Misses.getValue()));
                dataTableNumHelperThreadL2Hits.add(i, Long.parseLong(numHelperThreadL2Hits.getValue()));
                dataTableNumHelperThreadL2Misses.add(i, Long.parseLong(numHelperThreadL2Misses.getValue()));
            }

            List<DataSeries> dataSeriesList = new ArrayList<DataSeries>() {{
                add(new DataSeries("MT Hits", dataTableNumMainThreadL2Hits, 0, 1));
                add(new DataSeries("MT Misses", dataTableNumMainThreadL2Misses, 0, 1));

                if(ServiceManager.getExperimentStatService().getSummaryByParent(experiment).getNumHelperThreadL2Accesses() > 0) {
                    add(new DataSeries("HT Hits", dataTableNumHelperThreadL2Hits, 0, 1));
                    add(new DataSeries("HT Misses", dataTableNumHelperThreadL2Misses, 0, 1));
                }
            }};

            plotLineGraph("Program Phases", "# L2 Requests", labeledRow, dataSeriesList, pdfFileNamePrefix);
        }
    }

    /**
     * Plot the average L2 miss MLP-based cost per interval line graph.
     *
     * @param labeledRows       the list of labeled rows
     * @param pdfFileNamePrefix the PDF file name prefix
     */
    @SuppressWarnings("unchecked")
    public static void plotAverageL2MissMLPCostPerInterval(
            final List<Triple<String, Table, List<Pair<String, String>>>> labeledRows, String pdfFileNamePrefix) {
        for (Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
            Table table = labeledRow.getSecond();
            List<Pair<String, String>> conditions = labeledRow.getThird();

            List<String> row = Table.findRow(table, conditions);

            long experimentId = Long.parseLong(Table.getValue(table, row, "Id"));

            Experiment experiment = ServiceManager.getExperimentService().getExperimentById(experimentId);

            ExperimentType experimentType = experiment.getType();

            int numIntervals = ServiceManager.getExperimentStatService().getStatsByParentAndPrefixAndKeyLike(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                    "intervalHelper/averageL2MissMlpCost").size();

            final DataTable dataTableAverageL2MissMlpCost = new DataTable(Integer.class, Double.class);

            for (int i = 0; i < numIntervals; i++) {
                ExperimentStat averageL2MissMlpCost = ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/averageL2MissMlpCost" + "[" + i + "]");

                dataTableAverageL2MissMlpCost.add(i, Double.parseDouble(averageL2MissMlpCost.getValue()));
            }

            List<DataSeries> dataSeriesList = new ArrayList<DataSeries>() {{
                add(new DataSeries("MLPCost", dataTableAverageL2MissMlpCost, 0, 1));
            }};

            plotLineGraph("Program Phases", "Average L2 Miss MLP-Cost", labeledRow, dataSeriesList, pdfFileNamePrefix);
        }
    }

    /**
     * Plot the number of helper thread L2 requests per interval line graph.
     *
     * @param labeledRows       the list of labeled rows
     * @param pdfFileNamePrefix the PDF file name prefix
     */
    @SuppressWarnings("unchecked")
    public static void plotNumHelperThreadL2RequestsPerInterval(
            final List<Triple<String, Table, List<Pair<String, String>>>> labeledRows, String pdfFileNamePrefix) {
        for (Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
            Table table = labeledRow.getSecond();
            List<Pair<String, String>> conditions = labeledRow.getThird();

            List<String> row = Table.findRow(table, conditions);

            long experimentId = Long.parseLong(Table.getValue(table, row, "Id"));

            Experiment experiment = ServiceManager.getExperimentService().getExperimentById(experimentId);

            ExperimentType experimentType = experiment.getType();

            int numIntervals = ServiceManager.getExperimentStatService().getStatsByParentAndPrefixAndKeyLike(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                    "intervalHelper/numTimelyHelperThreadL2Requests").size();

            final DataTable dataTableNumUsefulHelperThreadL2Requests = new DataTable(Integer.class, Long.class);
            final DataTable dataTableNumUselessHelperThreadL2Requests = new DataTable(Integer.class, Long.class);

            for (int i = 0; i < numIntervals; i++) {
                ExperimentStat numTimelyHelperThreadL2Requests = ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/numTimelyHelperThreadL2Requests" + "[" + i + "]");
                ExperimentStat numLateHelperThreadL2Requests = ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/numLateHelperThreadL2Requests" + "[" + i + "]");
                ExperimentStat numUglyHelperThreadL2Requests = ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/numUglyHelperThreadL2Requests" + "[" + i + "]");
                ExperimentStat numBadHelperThreadL2Requests = ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/numBadHelperThreadL2Requests" + "[" + i + "]");
                ExperimentStat numEarlyHelperThreadL2Requests = ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/numEarlyHelperThreadL2Requests" + "[" + i + "]");
                ExperimentStat numRedundantHitToTransientTagHelperThreadL2Requests = ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/numRedundantHitToTransientTagHelperThreadL2Requests" + "[" + i + "]");
                ExperimentStat numRedundantHitToCacheHelperThreadL2Requests = ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/numRedundantHitToCacheHelperThreadL2Requests" + "[" + i + "]");

                long numUsefulHelperThreadL2Requests = Long.parseLong(numTimelyHelperThreadL2Requests.getValue()) + Long.parseLong(numLateHelperThreadL2Requests.getValue());
                long numUselessHelperThreadL2Requests = Long.parseLong(numUglyHelperThreadL2Requests.getValue()) + Long.parseLong(numBadHelperThreadL2Requests.getValue())
                        + Long.parseLong(numEarlyHelperThreadL2Requests.getValue()) + Long.parseLong(numRedundantHitToTransientTagHelperThreadL2Requests.getValue())
                        + Long.parseLong(numRedundantHitToCacheHelperThreadL2Requests.getValue());

                dataTableNumUsefulHelperThreadL2Requests.add(i, numUsefulHelperThreadL2Requests);
                dataTableNumUselessHelperThreadL2Requests.add(i, numUselessHelperThreadL2Requests);
            }

            List<DataSeries> dataSeriesList = new ArrayList<DataSeries>() {{
                add(new DataSeries("Useful", dataTableNumUsefulHelperThreadL2Requests, 0, 1));
                add(new DataSeries("Useless", dataTableNumUsefulHelperThreadL2Requests, 0, 1));
            }};

            plotLineGraph("Program Phases", "# Helper Thread L2 Requests", labeledRow, dataSeriesList, pdfFileNamePrefix);
        }
    }

    /**
     * Plot the number of useful helper thread L2 requests per interval line graph.
     *
     * @param labeledRows       the list of labeled rows
     * @param pdfFileNamePrefix the PDF file name prefix
     */
    @SuppressWarnings("unchecked")
    public static void plotNumUsefulHelperThreadL2RequestsPerInterval(
            final List<Triple<String, Table, List<Pair<String, String>>>> labeledRows, String pdfFileNamePrefix) {
        for (Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
            Table table = labeledRow.getSecond();
            List<Pair<String, String>> conditions = labeledRow.getThird();

            List<String> row = Table.findRow(table, conditions);

            long experimentId = Long.parseLong(Table.getValue(table, row, "Id"));

            Experiment experiment = ServiceManager.getExperimentService().getExperimentById(experimentId);

            ExperimentType experimentType = experiment.getType();

            int numIntervals = ServiceManager.getExperimentStatService().getStatsByParentAndPrefixAndKeyLike(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                    "intervalHelper/numTimelyHelperThreadL2Requests").size();

            final DataTable dataTableNumTimelyHelperThreadL2Requests = new DataTable(Integer.class, Long.class);
            final DataTable dataTableNumLateHelperThreadL2Requests = new DataTable(Integer.class, Long.class);

            for (int i = 0; i < numIntervals; i++) {
                ExperimentStat numTimelyHelperThreadL2Requests = ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/numTimelyHelperThreadL2Requests" + "[" + i + "]");
                ExperimentStat numLateHelperThreadL2Requests = ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/numLateHelperThreadL2Requests" + "[" + i + "]");

                dataTableNumTimelyHelperThreadL2Requests.add(i, Long.parseLong(numTimelyHelperThreadL2Requests.getValue()));
                dataTableNumLateHelperThreadL2Requests.add(i, Long.parseLong(numLateHelperThreadL2Requests.getValue()));
            }

            List<DataSeries> dataSeriesList = new ArrayList<DataSeries>() {{
                add(new DataSeries("Timely", dataTableNumTimelyHelperThreadL2Requests, 0, 1));
                add(new DataSeries("Late", dataTableNumLateHelperThreadL2Requests, 0, 1));
            }};

            plotLineGraph("Program Phases", "# Useful Helper Thread L2 Requests", labeledRow, dataSeriesList, pdfFileNamePrefix);
        }
    }

    /**
     * Plot the number of useless helper thread L2 requests per interval line graph.
     *
     * @param labeledRows       the list of labeled rows
     * @param pdfFileNamePrefix the PDF file name prefix
     */
    @SuppressWarnings("unchecked")
    public static void plotNumUselessHelperThreadL2RequestsPerInterval(
            final List<Triple<String, Table, List<Pair<String, String>>>> labeledRows, String pdfFileNamePrefix) {
        for (Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
            Table table = labeledRow.getSecond();
            List<Pair<String, String>> conditions = labeledRow.getThird();

            List<String> row = Table.findRow(table, conditions);

            long experimentId = Long.parseLong(Table.getValue(table, row, "Id"));

            Experiment experiment = ServiceManager.getExperimentService().getExperimentById(experimentId);

            ExperimentType experimentType = experiment.getType();

            int numIntervals = ServiceManager.getExperimentStatService().getStatsByParentAndPrefixAndKeyLike(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                    "intervalHelper/numTimelyHelperThreadL2Requests").size();

            final DataTable dataTableNumUglyHelperThreadL2Requests = new DataTable(Integer.class, Long.class);
            final DataTable dataTableNumBadHelperThreadL2Requests = new DataTable(Integer.class, Long.class);
            final DataTable dataTableNumEarlyHelperThreadL2Requests = new DataTable(Integer.class, Long.class);
            final DataTable dataTableNumRedundantHitToTransientTagHelperThreadL2Requests = new DataTable(Integer.class, Long.class);
            final DataTable dataTableNumRedundantHitToCacheHelperThreadL2Requests = new DataTable(Integer.class, Long.class);

            for (int i = 0; i < numIntervals; i++) {
                ExperimentStat numUglyHelperThreadL2Requests = ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/numUglyHelperThreadL2Requests" + "[" + i + "]");
                ExperimentStat numBadHelperThreadL2Requests = ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/numBadHelperThreadL2Requests" + "[" + i + "]");
                ExperimentStat numEarlyHelperThreadL2Requests = ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/numEarlyHelperThreadL2Requests" + "[" + i + "]");
                ExperimentStat numRedundantHitToTransientTagHelperThreadL2Requests = ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/numRedundantHitToTransientTagHelperThreadL2Requests" + "[" + i + "]");
                ExperimentStat numRedundantHitToCacheHelperThreadL2Requests = ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/numRedundantHitToCacheHelperThreadL2Requests" + "[" + i + "]");

                dataTableNumUglyHelperThreadL2Requests.add(i, Long.parseLong(numUglyHelperThreadL2Requests.getValue()));
                dataTableNumBadHelperThreadL2Requests.add(i, Long.parseLong(numBadHelperThreadL2Requests.getValue()));
                dataTableNumEarlyHelperThreadL2Requests.add(i, Long.parseLong(numEarlyHelperThreadL2Requests.getValue()));
                dataTableNumRedundantHitToTransientTagHelperThreadL2Requests.add(i, Long.parseLong(numRedundantHitToTransientTagHelperThreadL2Requests.getValue()));
                dataTableNumRedundantHitToCacheHelperThreadL2Requests.add(i, Long.parseLong(numRedundantHitToCacheHelperThreadL2Requests.getValue()));
            }

            List<DataSeries> dataSeriesList = new ArrayList<DataSeries>() {{
                add(new DataSeries("Ugly", dataTableNumUglyHelperThreadL2Requests, 0, 1));
                add(new DataSeries("Bad", dataTableNumBadHelperThreadL2Requests, 0, 1));
                add(new DataSeries("Early", dataTableNumEarlyHelperThreadL2Requests, 0, 1));
                add(new DataSeries("Redundant_MSHR", dataTableNumRedundantHitToTransientTagHelperThreadL2Requests, 0, 1));
                add(new DataSeries("Redundant_Cache", dataTableNumRedundantHitToCacheHelperThreadL2Requests, 0, 1));
            }};

            plotLineGraph("Program Phases", "# Useless Helper Thread L2 Requests", labeledRow, dataSeriesList, pdfFileNamePrefix);
        }
    }

    /**
     * Plot the helper thread L2 cache request quality per interval line graph.
     *
     * @param labeledRows       the list of labeled rows
     * @param pdfFileNamePrefix the PDF file name prefix
     */
    @SuppressWarnings("unchecked")
    public static void plotHelperThreadL2RequestQualityPerInterval(
            final List<Triple<String, Table, List<Pair<String, String>>>> labeledRows, String pdfFileNamePrefix) {
        for (Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
            Table table = labeledRow.getSecond();
            List<Pair<String, String>> conditions = labeledRow.getThird();

            List<String> row = Table.findRow(table, conditions);

            long experimentId = Long.parseLong(Table.getValue(table, row, "Id"));

            Experiment experiment = ServiceManager.getExperimentService().getExperimentById(experimentId);

            ExperimentType experimentType = experiment.getType();

            int numIntervals = ServiceManager.getExperimentStatService().getStatsByParentAndPrefixAndKeyLike(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                    "intervalHelper/numTimelyHelperThreadL2Requests").size();

            final DataTable dataTableHelperThreadL2RequestCoverage = new DataTable(Integer.class, Double.class);
            final DataTable dataTableHelperThreadL2RequestAccuracy = new DataTable(Integer.class, Double.class);
            final DataTable dataTableHelperThreadL2RequestLateness = new DataTable(Integer.class, Double.class);
            final DataTable dataTableHelperThreadL2RequestPollution = new DataTable(Integer.class, Double.class);

            for (int i = 0; i < numIntervals; i++) {
                long numMainThreadL2Hits = Long.parseLong(ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/numMainThreadL2Hits" + "[" + i + "]").getValue());
                long numMainThreadL2Misses = Long.parseLong(ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/numMainThreadL2Misses" + "[" + i + "]").getValue());
                long numHelperThreadL2Hits = Long.parseLong(ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/numHelperThreadL2Hits" + "[" + i + "]").getValue());
                long numHelperThreadL2Misses = Long.parseLong(ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/numHelperThreadL2Misses" + "[" + i + "]").getValue());

                long numTimelyHelperThreadL2Requests = Long.parseLong(ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/numTimelyHelperThreadL2Requests" + "[" + i + "]").getValue());
                long numLateHelperThreadL2Requests = Long.parseLong(ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/numLateHelperThreadL2Requests" + "[" + i + "]").getValue());
                long numUglyHelperThreadL2Requests = Long.parseLong(ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/numUglyHelperThreadL2Requests" + "[" + i + "]").getValue());
                long numBadHelperThreadL2Requests = Long.parseLong(ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/numBadHelperThreadL2Requests" + "[" + i + "]").getValue());
                long numEarlyHelperThreadL2Requests = Long.parseLong(ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/numEarlyHelperThreadL2Requests" + "[" + i + "]").getValue());
                long numRedundantHitToTransientTagHelperThreadL2Requests = Long.parseLong(ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/numRedundantHitToTransientTagHelperThreadL2Requests" + "[" + i + "]").getValue());
                long numRedundantHitToCacheHelperThreadL2Requests = Long.parseLong(ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/numRedundantHitToCacheHelperThreadL2Requests" + "[" + i + "]").getValue());

                HelperThreadL2RequestBreakdown breakdown = new BasicHelperThreadL2RequestBreakdown(
                        numMainThreadL2Hits,
                        numMainThreadL2Misses,
                        numHelperThreadL2Hits,
                        numHelperThreadL2Misses,
                        numRedundantHitToTransientTagHelperThreadL2Requests,
                        numRedundantHitToCacheHelperThreadL2Requests,
                        numTimelyHelperThreadL2Requests,
                        numLateHelperThreadL2Requests,
                        numBadHelperThreadL2Requests,
                        numEarlyHelperThreadL2Requests,
                        numUglyHelperThreadL2Requests
                );

                dataTableHelperThreadL2RequestCoverage.add(i, breakdown.getHelperThreadL2RequestCoverage());
                dataTableHelperThreadL2RequestAccuracy.add(i, breakdown.getHelperThreadL2RequestAccuracy());
                dataTableHelperThreadL2RequestLateness.add(i, breakdown.getHelperThreadL2RequestLateness());
                dataTableHelperThreadL2RequestPollution.add(i, breakdown.getHelperThreadL2RequestPollution());
            }

            List<DataSeries> dataSeriesList = new ArrayList<DataSeries>() {{
                add(new DataSeries("HT Coverage", dataTableHelperThreadL2RequestCoverage, 0, 1));
                add(new DataSeries("HT Accuracy", dataTableHelperThreadL2RequestAccuracy, 0, 1));
                add(new DataSeries("HT Lateness", dataTableHelperThreadL2RequestLateness, 0, 1));
                add(new DataSeries("HT Pollution", dataTableHelperThreadL2RequestPollution, 0, 1));
            }};

            plotLineGraph("Program Phases", "Helper Thread L2 Request Quality", labeledRow, dataSeriesList, pdfFileNamePrefix);
        }
    }

    /**
     * Plot the IPC per interval line graph.
     *
     * @param labeledRows       the list of labeled rows
     * @param pdfFileNamePrefix the PDF file name prefix
     */
    @SuppressWarnings("unchecked")
    public static void plotIpcPerInterval(
            final List<Triple<String, Table, List<Pair<String, String>>>> labeledRows, String pdfFileNamePrefix) {
        for (Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
            Table table = labeledRow.getSecond();
            List<Pair<String, String>> conditions = labeledRow.getThird();

            List<String> row = Table.findRow(table, conditions);

            long experimentId = Long.parseLong(Table.getValue(table, row, "Id"));

            String l2Size = Table.getValue(table, row, "L2_Size");
            if(!l2Size.equals("96KB")) {
                continue;
            }

            Experiment experiment = ServiceManager.getExperimentService().getExperimentById(experimentId);

            ExperimentType experimentType = experiment.getType();

            int numIntervals = ServiceManager.getExperimentStatService().getStatsByParentAndPrefixAndKeyLike(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                    "intervalHelper/mainThreadIpc").size();

            final DataTable dataTableMainThreadIpc = new DataTable(Integer.class, Double.class);
            final DataTable dataTableHelperThreadIpc = new DataTable(Integer.class, Double.class);

            for (int i = 0; i < numIntervals; i++) {
                ExperimentStat mainThreadIpc = ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/mainThreadIpc" + "[" + i + "]");
                ExperimentStat helperThreadIpc = ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/helperThreadIpc" + "[" + i + "]");

                dataTableMainThreadIpc.add(i, Double.parseDouble(mainThreadIpc.getValue()));
                dataTableHelperThreadIpc.add(i, Double.parseDouble(helperThreadIpc.getValue()));
            }

            List<DataSeries> dataSeriesList = new ArrayList<DataSeries>() {{
                add(new DataSeries("MT.IPC", dataTableMainThreadIpc, 0, 1));

                if(ServiceManager.getExperimentStatService().getSummaryByParent(experiment).getC1t0NumInstructions() > 0) {
                    add(new DataSeries("HT.IPC", dataTableHelperThreadIpc, 0, 1));
                }
            }};

            plotLineGraph("Program Phases", "IPC", labeledRow, dataSeriesList, pdfFileNamePrefix);
        }
    }

    /**
     * Plot the CPI per interval line graph.
     *
     * @param labeledRows       the list of labeled rows
     * @param pdfFileNamePrefix the PDF file name prefix
     */
    @SuppressWarnings("unchecked")
    public static void plotCpiPerInterval(
            final List<Triple<String, Table, List<Pair<String, String>>>> labeledRows, String pdfFileNamePrefix) {
        for (Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
            Table table = labeledRow.getSecond();
            List<Pair<String, String>> conditions = labeledRow.getThird();

            List<String> row = Table.findRow(table, conditions);

            long experimentId = Long.parseLong(Table.getValue(table, row, "Id"));

            Experiment experiment = ServiceManager.getExperimentService().getExperimentById(experimentId);

            ExperimentType experimentType = experiment.getType();

            int numIntervals = ServiceManager.getExperimentStatService().getStatsByParentAndPrefixAndKeyLike(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                    "intervalHelper/mainThreadCpi").size();

            final DataTable dataTableMainThreadCpi = new DataTable(Integer.class, Double.class);
            final DataTable dataTableHelperThreadCpi = new DataTable(Integer.class, Double.class);

            for (int i = 0; i < numIntervals; i++) {
                ExperimentStat mainThreadCpi = ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/mainThreadCpi" + "[" + i + "]");
                ExperimentStat helperThreadCpi = ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/helperThreadCpi" + "[" + i + "]");

                dataTableMainThreadCpi.add(i, Double.parseDouble(mainThreadCpi.getValue()));
                dataTableHelperThreadCpi.add(i, Double.parseDouble(helperThreadCpi.getValue()));
            }

            List<DataSeries> dataSeriesList = new ArrayList<DataSeries>() {{
                add(new DataSeries("MT.CPI", dataTableMainThreadCpi, 0, 1));

                if(ServiceManager.getExperimentStatService().getSummaryByParent(experiment).getC1t0NumInstructions() > 0) {
                    add(new DataSeries("HT.CPI", dataTableHelperThreadCpi, 0, 1));
                }
            }};

            plotLineGraph("Program Phases", "CPI", labeledRow, dataSeriesList, pdfFileNamePrefix);
        }
    }

    /**
     * Plot the L2 MPKI per interval line graph.
     *
     * @param labeledRows       the list of labeled rows
     * @param pdfFileNamePrefix the PDF file name prefix
     */
    @SuppressWarnings("unchecked")
    public static void plotL2MpkiPerInterval(
            final List<Triple<String, Table, List<Pair<String, String>>>> labeledRows, String pdfFileNamePrefix) {
        for (Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
            Table table = labeledRow.getSecond();
            List<Pair<String, String>> conditions = labeledRow.getThird();

            List<String> row = Table.findRow(table, conditions);

            long experimentId = Long.parseLong(Table.getValue(table, row, "Id"));

            Experiment experiment = ServiceManager.getExperimentService().getExperimentById(experimentId);

            ExperimentType experimentType = experiment.getType();

            int numIntervals = ServiceManager.getExperimentStatService().getStatsByParentAndPrefixAndKeyLike(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                    "intervalHelper/mainThreadMpki").size();

            final DataTable dataTableMainThreadL2Mpki = new DataTable(Integer.class, Double.class);
            final DataTable dataTableHelperThreadL2Mpki = new DataTable(Integer.class, Double.class);

            for (int i = 0; i < numIntervals; i++) {
                ExperimentStat mainThreadL2Mpki = ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/mainThreadMpki" + "[" + i + "]");
                ExperimentStat helperThreadL2Mpki = ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "intervalHelper/helperThreadMpki" + "[" + i + "]");

                dataTableMainThreadL2Mpki.add(i, Double.parseDouble(mainThreadL2Mpki.getValue()));
                dataTableHelperThreadL2Mpki.add(i, Double.parseDouble(helperThreadL2Mpki.getValue()));
            }

            List<DataSeries> dataSeriesList = new ArrayList<DataSeries>() {{
                add(new DataSeries("MT.L2_MPKI", dataTableMainThreadL2Mpki, 0, 1));

                if(ServiceManager.getExperimentStatService().getSummaryByParent(experiment).getNumHelperThreadL2Accesses() > 0) {
                    add(new DataSeries("HT.L2_MPKI", dataTableHelperThreadL2Mpki, 0, 1));
                }
            }};

            plotLineGraph("Program Phases", "L2 MPKI", labeledRow, dataSeriesList, pdfFileNamePrefix);
        }
    }

    /**
     * Create a bar graph illustrating the L2 cache instantaneous MLP distribution.
     *
     * @param labeledRow the labeled row
     * @return the newly created bar graph illustrating the L2 cache instantaneous MLP distribution
     */
    @SuppressWarnings("unchecked")
    public static BarGraph l2MlpDistribution(
            final Triple<String, Table, List<Pair<String, String>>> labeledRow
    ) {
        return new SimpleBarGraph("", "# Cycles") {
            {
                setxScale(0.67);
                setyScale(0.67);

                setyLabelShift(-2.0);

                setyFormat(YFormat.LONG_SCIENTIFIC);

                Table table = labeledRow.getSecond();
                List<Pair<String, String>> conditions = labeledRow.getThird();

                List<String> row = Table.findRow(table, conditions);

                long experimentId = Long.parseLong(Table.getValue(table, row, "Id"));

                Experiment experiment = ServiceManager.getExperimentService().getExperimentById(experimentId);

                ExperimentType experimentType = experiment.getType();

                List<Pair<Integer, Long>> numCyclesPerMlp = ServiceManager.getExperimentStatService().getStatsByParentAndPrefixAndKeyLike(
                        experiment,
                        Experiment.getMeasurementTitlePrefix(experimentType),
                        "numCyclesPerMlp"
                ).stream().map(stat -> new Pair<>(
                        Integer.parseInt(StringUtils.substringBetween(stat.getKey(), "[", "]") ), Long.parseLong(stat.getValue())
                )).collect(Collectors.toList());

                for(Pair<Integer, Long> pair : numCyclesPerMlp) {
                    getBars().add(new Pair<>(pair.getFirst() + "", pair.getSecond()));
                }
            }
        };
    }

    /**
     * Create a bar graph illustrating the L2 cache MLP-cost distribution.
     *
     * @param labeledRow the labeled row
     * @return the newly created bar graph illustrating the L2 cache MLP-cost distribution
     */
    @SuppressWarnings("unchecked")
    public static BarGraph l2MlpCostDistribution(
            final Triple<String, Table, List<Pair<String, String>>> labeledRow
    ) {
        return new SimpleBarGraph("", "# L2 Misses") {
            {
                setxScale(0.67);
                setyScale(0.67);

                setyFormat(YFormat.LONG_SCIENTIFIC);

                Table table = labeledRow.getSecond();
                List<Pair<String, String>> conditions = labeledRow.getThird();

                List<String> row = Table.findRow(table, conditions);

                long experimentId = Long.parseLong(Table.getValue(table, row, "Id"));

                Experiment experiment = ServiceManager.getExperimentService().getExperimentById(experimentId);

                ExperimentType experimentType = experiment.getType();

                int maxValue = Integer.parseInt(ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "mlpCostQuantizer/maxValue").getValue());

                int quantum = Integer.parseInt(ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                        "mlpCostQuantizer/quantum").getValue());

                for (int i = 0; i < maxValue; i++) {
                    ExperimentStat averageL2MissMlpCost = ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(experiment, Experiment.getMeasurementTitlePrefix(experimentType),
                            "numL2MissesPerMlpCostQuantum" + "[" + i + "]");

                    getBars().add(
                            new Pair<>(
                                    i == maxValue - 1 ? ">" + (i * quantum - 1) : i * quantum + "-" + ((i + 1) * quantum - 1),
                                    Double.parseDouble(averageL2MissMlpCost.getValue())
                            )
                    );
                }
            }
        };
    }

    /**
     * Create an L2 cache stack distance profile.
     *
     * @param labeledRow the labeled row
     * @return the newly created bar graph illustrating the L2 cache stack distance profile
     */
    @SuppressWarnings("unchecked")
    public static BarGraph l2StackDistanceProfile(
            final Triple<String, Table, List<Pair<String, String>>> labeledRow
    ) {
        return new SimpleBarGraph("", "L2 Stack Distance Profile") {
            {
                setxScale(0.67);
                setyScale(0.67);

                setyLabelShift(1.5);

                setyFormat(YFormat.LONG_SCIENTIFIC);

                Table table = labeledRow.getSecond();
                List<Pair<String, String>> conditions = labeledRow.getThird();

                List<String> row = Table.findRow(table, conditions);

                long experimentId = Long.parseLong(Table.getValue(table, row, "Id"));

                Experiment experiment = ServiceManager.getExperimentService().getExperimentById(experimentId);

                ExperimentType experimentType = experiment.getType();

                List<String[]> assumedNumMissesDistribution = Arrays.asList(
                        StringUtils.split(
                                StringUtils.substringBetween(
                                        ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(
                                                experiment,
                                                Experiment.getMeasurementTitlePrefix(experimentType),
                                                "stackDistanceProfilingHelper/assumedNumMissesDistribution"
                                        ).getValue(),
                                        "{",
                                        "}"),
                                ", "
                        )
                ).stream().map(t -> StringUtils.split(t, "=")).collect(Collectors.toList());

                for(String[] assumedNumMisses : assumedNumMissesDistribution) {
                    if(assumedNumMisses.length != 2) {
                        throw new IllegalArgumentException();
                    }

                    int i = Integer.parseInt(assumedNumMisses[0]);
                    long numL2Misses = Long.parseLong(assumedNumMisses[1]);

                    getBars().add(
                            new Pair<>(
                                    "" + i,
                                    numL2Misses
                            )
                    );
                }
            }
        };
    }

    /**
     * Get the list of L2 size values from the specified table.
     *
     * @param table the table
     * @return the list of L2 size values from the specified table
     */
    public static List<String> l2Sizes(Table table) {
        return table.getIdenticalCells("L2_Size");
    }

    /**
     * Get the list of helper thread stride values from the specified table.
     *
     * @param table the table
     * @return the list of helper thread stride values from the specified table
     */
    public static List<String> strides(Table table) {
        return table.getIdenticalCells("Stride");
    }

    /**
     * Get the list of helper thread lookahead values from the specified table.
     *
     * @param table the table
     * @return the list of helper thread lookahead values from the specified table
     */
    public static List<String> lookaheads(Table table) {
        return table.getIdenticalCells("Lookahead");
    }

    /**
     * Get the list of the "main thread ways in the partitioned L2 cache" values from the specified table.
     *
     * @param table the table
     * @return the list of the "main thread ways in the partitioned L2 cache" values from the specified table
     */
    public static List<String> mtWaysInPartitionedL2s(Table table) {
        return table.getIdenticalCells("MT_Ways_In_Partitioned_L2");
    }

    /**
     * Get the list of the L2 cache replacement policy values from the specified table.
     *
     * @param table the table
     * @return the list of the L2 cache replacement policy values from the specified table
     */
    public static List<String> l2ReplacementPolicies(Table table) {
        return table.getIdenticalCells("L2_Replacement");
    }

    /**
     * Plot the overall performance graphs for the specified table.
     * <p>
     * example:
     * <p>
     * plot(Table.fromCsv("./429.mcf_mlp.csv"), "./result", new LinkedHashMap<String, String>(){{
     * put("7136", "Baseline_LRU");
     * put("7137", "Baseline_LinearMLP");
     * put("7138", "HT_LRU");
     * put("7139", "HT_LinearMLP");
     * }});
     *
     * @param table                 the table
     * @param outputDirectoryPrefix the output directory prefix
     */
    public static void plot(final Table table, String outputDirectoryPrefix, Map<String, String> idToLabels) {
        List<Triple<String, Table, List<Pair<String, String>>>> labeledRows = new ArrayList<Triple<String, Table, List<Pair<String, String>>>>() {{
            for (List<String> row : table.getRows()) {
                String experimentId = Table.getValue(table, row, "Id");
                String experimentLabel = idToLabels != null && idToLabels.containsKey(experimentId) ? idToLabels.get(experimentId) : String.format("Exp_%s", experimentId);

                add(
                        new Triple<>(
                                experimentLabel,
                                new Table(table.getColumns(), new ArrayList<List<String>>() {{
                                    add(row);
                                }}),
                                new ArrayList<>()
                        )
                );
            }
        }};

        SimulationPlotHelper.numCycles(labeledRows).plot(outputDirectoryPrefix + "/numCycles");
        SimulationPlotHelper.numDynamicInstructionsCommitted(labeledRows).plot(outputDirectoryPrefix + "/numDynamicInstructionsCommitted");
        SimulationPlotHelper.numL2Requests(labeledRows).plot(outputDirectoryPrefix + "/numL2Requests");
        SimulationPlotHelper.numHelperThreadL2Requests(labeledRows).plot(outputDirectoryPrefix + "/numHelperThreadL2Requests");
        SimulationPlotHelper.numUsefulHelperThreadL2Requests(labeledRows).plot(outputDirectoryPrefix + "/numUsefulHelperThreadL2Requests");
        SimulationPlotHelper.numUselessHelperThreadL2Requests(labeledRows).plot(outputDirectoryPrefix + "/numUselessHelperThreadL2Requests");
    }

    /**
     * Export the helper threaded overall performance CSV file.
     *
     * @param benchmarks the list of benchmarks
     */
    public static void exportHelperThreadedOverallPerformanceCsv(final List<String> benchmarks) {
        for(String benchmark : benchmarks) {
            FileUtils.writeAllText(
                    table(
                            benchmark + "_ht_lru",

                            benchmark + "_baseline_lru",
                            benchmark + "_ht_lru"
                    ).toCsv(),
                    "experiment_plots/helperThreadedOverallPerformance_" + benchmark + ".csv"
            );
        }
    }

    /**
     * Export the L2 size CSV file.
     *
     * @param benchmarks the list of benchmarks
     */
    public static void exportL2SizeCsv(final List<String> benchmarks) {
        for(String benchmark : benchmarks) {
            FileUtils.writeAllText(
                    table(
                            benchmark + "_ht_lru_l2Sizes",

                            benchmark + "_baseline_lru",
                            benchmark + "_baseline_lru_l2Sizes",
                            benchmark + "_ht_lru",
                            benchmark + "_ht_lru_l2Sizes"
                    ).toCsv(),
                    "experiment_plots/l2Size_" + benchmark + ".csv"
            );
        }
    }

    /**
     * Plot the overall performance graphs with respect to the L2 size values for the specified benchmark.
     *
     * @param benchmark the benchmark
     */
    public static void l2Size(final String benchmark) {
        List<Triple<String, Table, List<Pair<String, String>>>> labeledRows = new ArrayList<Triple<String, Table, List<Pair<String, String>>>>() {{
            List<String> l2Sizes = l2Sizes(SimulationPlotHelper.table(benchmark + "_ht_lru_l2Sizes", benchmark + "_baseline_lru_l2Sizes"));

            add(
                    new Triple<>(
                            "Baseline_" + "96KB",
                            SimulationPlotHelper.table(benchmark + "_ht_lru_l2Sizes", benchmark + "_baseline_lru"),
                            new ArrayList<>()
                    )
            );

            add(
                    new Triple<>(
                            "HT_" + "96KB",
                            SimulationPlotHelper.table(benchmark + "_ht_lru_l2Sizes", benchmark + "_ht_lru"),
                            new ArrayList<>()
                    )
            );

            for (String l2Size : l2Sizes) {
                add(
                        new Triple<>(
                                "Baseline_" + l2Size,
                                SimulationPlotHelper.tableByL2Size(benchmark + "_ht_lru_l2Sizes", l2Size, benchmark + "_baseline_lru_l2Sizes"),
                                new ArrayList<>()
                        )
                );

                add(
                        new Triple<>(
                                "HT_" + l2Size,
                                SimulationPlotHelper.tableByL2Size(benchmark + "_ht_lru_l2Sizes", l2Size, benchmark + "_ht_lru_l2Sizes"),
                                new ArrayList<>()
                        )
                );
            }
        }};

        SimulationPlotHelper.numCycles(labeledRows).plot(
                "experiment_plots/" + benchmark + "/l2Size/numCycles"
        );
        SimulationPlotHelper.speedup(labeledRows).plot(
                "experiment_plots/" + benchmark + "/l2Size/speedup"
        );
        SimulationPlotHelper.ipc(labeledRows).plot(
                "experiment_plots/" + benchmark + "/l2Size/ipc"
        );
        SimulationPlotHelper.c0t0Ipc(labeledRows).plot(
                "experiment_plots/" + benchmark + "/l2Size/c0t0Ipc"
        );
        SimulationPlotHelper.cpi(labeledRows).plot(
                "experiment_plots/" + benchmark + "/l2Size/cpi"
        );
        SimulationPlotHelper.c0t0Cpi(labeledRows).plot(
                "experiment_plots/" + benchmark + "/l2Size/c0t0Cpi"
        );
        SimulationPlotHelper.l2Mpki(labeledRows).plot(
                "experiment_plots/" + benchmark + "/l2Size/l2Mpki"
        );
        SimulationPlotHelper.c0t0L2Mpki(labeledRows).plot(
                "experiment_plots/" + benchmark + "/l2Size/c0t0L2Mpki"
        );
        SimulationPlotHelper.numDynamicInstructionsCommitted(labeledRows).plot(
                "experiment_plots/" + benchmark + "/l2Size/numDynamicInstructionsCommitted"
        );
        SimulationPlotHelper.numL2Requests(labeledRows).plot(
                "experiment_plots/" + benchmark + "/l2Size/numL2Requests"
        );

        for (Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
            SimulationPlotHelper.l2MlpDistribution(labeledRow).plot(
                    "experiment_plots/" + benchmark + "/l2Size/" + labeledRow.getFirst() + "/l2MlpDistribution"
            );
            SimulationPlotHelper.l2MlpCostDistribution(labeledRow).plot(
                    "experiment_plots/" + benchmark + "/l2Size/" + labeledRow.getFirst() + "/l2MlpCostDistribution"
            );
            SimulationPlotHelper.l2StackDistanceProfile(labeledRow).plot(
                    "experiment_plots/" + benchmark + "/l2Size/" + labeledRow.getFirst() + "/l2StackDistanceProfile"
            );
        }

//        SimulationPlotHelper.plotNumDynamicInstructionsCommittedPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/l2Size/numDynamicInstructionsCommittedPerInterval"
//        );
//        SimulationPlotHelper.plotNumL2RequestsPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/l2Size/numL2RequestsPerInterval"
//        );
//        SimulationPlotHelper.plotAverageL2MissMLPCostPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/l2Size/averageL2MissMLPCostPerInterval"
//        );
        SimulationPlotHelper.plotIpcPerInterval(
                labeledRows, "experiment_plots/" + benchmark + "/l2Size/ipcPerInterval"
        );
//        SimulationPlotHelper.plotCpiPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/l2Size/cpiPerInterval"
//        );
//        SimulationPlotHelper.plotL2MpkiPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/l2Size/l2MpkiPerInterval"
//        );
    }

    /**
     * Plot the baseline performance graphs with respect to the L2 size values for the specified benchmark.
     *
     * @param benchmark the benchmark
     */
    public static void baselineL2Size(final String benchmark) {
        List<Triple<String, Table, List<Pair<String, String>>>> labeledRows = new ArrayList<Triple<String, Table, List<Pair<String, String>>>>() {{
            List<String> l2Sizes = l2Sizes(SimulationPlotHelper.table(benchmark + "_baseline_lru_l2Sizes", benchmark + "_baseline_lru_l2Sizes"));

            add(
                    new Triple<>(
                            "96KB",
                            SimulationPlotHelper.table(benchmark + "_baseline_lru_l2Sizes", benchmark + "_baseline_lru"),
                            new ArrayList<>()
                    )
            );

            for (String l2Size : l2Sizes) {
                add(
                        new Triple<>(
                                l2Size,
                                SimulationPlotHelper.tableByL2Size(benchmark + "_baseline_lru_l2Sizes", l2Size, benchmark + "_baseline_lru_l2Sizes"),
                                new ArrayList<>()
                        )
                );
            }
        }};

        SimulationPlotHelper.numCycles(labeledRows).plot(
                "experiment_plots/" + benchmark + "/baselineL2Size/numCycles"
        );
        SimulationPlotHelper.speedup(labeledRows).plot(
                "experiment_plots/" + benchmark + "/baselineL2Size/speedup"
        );
        SimulationPlotHelper.ipc(labeledRows).plot(
                "experiment_plots/" + benchmark + "/baselineL2Size/ipc"
        );
        SimulationPlotHelper.c0t0Ipc(labeledRows).plot(
                "experiment_plots/" + benchmark + "/baselineL2Size/c0t0Ipc"
        );
        SimulationPlotHelper.cpi(labeledRows).plot(
                "experiment_plots/" + benchmark + "/baselineL2Size/cpi"
        );
        SimulationPlotHelper.c0t0Cpi(labeledRows).plot(
                "experiment_plots/" + benchmark + "/baselineL2Size/c0t0Cpi"
        );
        SimulationPlotHelper.l2Mpki(labeledRows).plot(
                "experiment_plots/" + benchmark + "/baselineL2Size/l2Mpki"
        );
        SimulationPlotHelper.c0t0L2Mpki(labeledRows).plot(
                "experiment_plots/" + benchmark + "/baselineL2Size/c0t0L2Mpki"
        );
        SimulationPlotHelper.numDynamicInstructionsCommitted(labeledRows).plot(
                "experiment_plots/" + benchmark + "/baselineL2Size/numDynamicInstructionsCommitted"
        );
        SimulationPlotHelper.numL2Requests(labeledRows).plot(
                "experiment_plots/" + benchmark + "/baselineL2Size/numL2Requests"
        );

//        for (Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
//            SimulationPlotHelper.l2MlpDistribution(labeledRow).plot(
//                    "experiment_plots/" + benchmark + "/baselineL2Size/" + labeledRow.getFirst() + "/l2MlpDistribution"
//            )
//            SimulationPlotHelper.l2MlpCostDistribution(labeledRow).plot(
//                    "experiment_plots/" + benchmark + "/baselineL2Size/" + labeledRow.getFirst() + "/l2MlpCostDistribution"
//            );
//            SimulationPlotHelper.l2StackDistanceProfile(labeledRow).plot(
//                    "experiment_plots/" + benchmark + "/baselineL2Size/" + labeledRow.getFirst() + "/l2StackDistanceProfile"
//            );
//        }
//
//        SimulationPlotHelper.plotNumDynamicInstructionsCommittedPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/baselineL2Size/numDynamicInstructionsCommittedPerInterval"
//        );
//        SimulationPlotHelper.plotNumL2RequestsPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/baselineL2Size/numL2RequestsPerInterval"
//        );
//        SimulationPlotHelper.plotAverageL2MissMLPCostPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/baselineL2Size/averageL2MissMLPCostPerInterval"
//        );
//        SimulationPlotHelper.plotIpcPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/baselineL2Size/ipcPerInterval"
//        );
//        SimulationPlotHelper.plotCpiPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/baselineL2Size/cpiPerInterval"
//        );
//        SimulationPlotHelper.plotL2MpkiPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/baselineL2Size/l2MpkiPerInterval"
//        );
    }

    /**
     * Plot the helper threaded performance graphs with respect to the L2 size values for the specified benchmark.
     *
     * @param benchmark the benchmark
     */
    public static void helperThreadedL2Size(final String benchmark) {
        List<Triple<String, Table, List<Pair<String, String>>>> labeledRows = new ArrayList<Triple<String, Table, List<Pair<String, String>>>>() {{
            List<String> l2Sizes = l2Sizes(SimulationPlotHelper.table(benchmark + "_ht_lru_l2Sizes", benchmark + "_ht_lru_l2Sizes"));

            add(
                    new Triple<>(
                            "96KB",
                            SimulationPlotHelper.table(benchmark + "_ht_lru_l2Sizes", benchmark + "_ht_lru"),
                            new ArrayList<>()
                    )
            );

            for (String l2Size : l2Sizes) {
                add(
                        new Triple<>(
                                l2Size,
                                SimulationPlotHelper.tableByL2Size(benchmark + "_ht_lru_l2Sizes", l2Size, benchmark + "_ht_lru_l2Sizes"),
                                new ArrayList<>()
                        )
                );
            }
        }};

        SimulationPlotHelper.numCycles(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Size/numCycles"
        );
        SimulationPlotHelper.speedup(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Size/speedup"
        );
        SimulationPlotHelper.ipc(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Size/ipc"
        );
        SimulationPlotHelper.c0t0Ipc(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Size/c0t0Ipc"
        );
        SimulationPlotHelper.c1t0Ipc(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Size/c1t0Ipc"
        );
        SimulationPlotHelper.cpi(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Size/cpi"
        );
        SimulationPlotHelper.c0t0Cpi(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Size/c0t0Cpi"
        );
        SimulationPlotHelper.c1t0Cpi(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Size/c1t0Cpi"
        );
        SimulationPlotHelper.l2Mpki(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Size/l2Mpki"
        );
        SimulationPlotHelper.c0t0L2Mpki(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Size/c0t0L2Mpki"
        );
        SimulationPlotHelper.c1t0L2Mpki(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Size/c1t0L2Mpki"
        );
        SimulationPlotHelper.numDynamicInstructionsCommitted(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Size/numDynamicInstructionsCommitted"
        );
        SimulationPlotHelper.numL2Requests(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Size/numL2Requests"
        );
        SimulationPlotHelper.numHelperThreadL2Requests(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Size/numHelperThreadL2Requests"
        );
        SimulationPlotHelper.numUsefulHelperThreadL2Requests(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Size/numUsefulHelperThreadL2Requests"
        );
        SimulationPlotHelper.numUselessHelperThreadL2Requests(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Size/numUselessHelperThreadL2Requests"
        );
        SimulationPlotHelper.helperThreadPrefetchCoverage(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Size/helperThreadPrefetchCoverage"
        );
        SimulationPlotHelper.helperThreadPrefetchAccuracy(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Size/helperThreadPrefetchAccuracy"
        );
        SimulationPlotHelper.helperThreadPrefetchLateness(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Size/helperThreadPrefetchLateness"
        );
        SimulationPlotHelper.helperThreadPrefetchPollution(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Size/helperThreadPrefetchPollution"
        );

//        for (Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
//            SimulationPlotHelper.l2MlpDistribution(labeledRow).plot(
//                    "experiment_plots/" + benchmark + "/helperThreadedL2Size/" + labeledRow.getFirst() + "/l2MlpDistribution"
//            );
//            SimulationPlotHelper.l2MlpCostDistribution(labeledRow).plot(
//                    "experiment_plots/" + benchmark + "/helperThreadedL2Size/" + labeledRow.getFirst() + "/l2MlpCostDistribution"
//            );
//            SimulationPlotHelper.l2StackDistanceProfile(labeledRow).plot(
//                    "experiment_plots/" + benchmark + "/helperThreadedL2Size/" + labeledRow.getFirst() + "/l2StackDistanceProfile"
//            );
//        }
//
//        SimulationPlotHelper.plotNumDynamicInstructionsCommittedPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedL2Size/numDynamicInstructionsCommittedPerInterval"
//        );
//        SimulationPlotHelper.plotNumL2RequestsPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedL2Size/numL2RequestsPerInterval"
//        );
//        SimulationPlotHelper.plotAverageL2MissMLPCostPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedL2Size/averageL2MissMLPCostPerInterval"
//        );
//        SimulationPlotHelper.plotIpcPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedL2Size/ipcPerInterval"
//        );
//        SimulationPlotHelper.plotCpiPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedL2Size/cpiPerInterval"
//        );
//        SimulationPlotHelper.plotL2MpkiPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedL2Size/l2MpkiPerInterval"
//        );
//        SimulationPlotHelper.plotNumHelperThreadL2RequestsPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedL2Size/numHelperThreadL2RequestsPerInterval"
//        );
//        SimulationPlotHelper.plotNumUsefulHelperThreadL2RequestsPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedL2Size/numUsefulHelperThreadL2RequestsPerInterval"
//        );
//        SimulationPlotHelper.plotNumUselessHelperThreadL2RequestsPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedL2Size/numUselessHelperThreadL2RequestsPerInterval"
//        );
//        SimulationPlotHelper.plotHelperThreadL2RequestQualityPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedL2Size/helperThreadL2RequestQualityPerInterval"
//        );
    }

    /**
     * Export the helper threaded lookahead CSV file.
     *
     * @param benchmarks the list of benchmarks
     */
    public static void exportHelperThreadedLookaheadCsv(final List<String> benchmarks) {
        for(String benchmark : benchmarks) {
            FileUtils.writeAllText(
                    table(
                            benchmark + "_ht_lru_lookaheads",

                            benchmark + "_ht_lru",
                            benchmark + "_ht_lru_lookaheads"
                    ).toCsv(),
                    "experiment_plots/helperThreadedLookahead_" + benchmark + ".csv"
            );
        }
    }

    /**
     * Plot the overall performance graphs with respect to the helper thread lookahead values for the specified benchmark.
     *
     * @param benchmark the benchmark
     */
    public static void helperThreadedLookahead(final String benchmark) {
        List<Triple<String, Table, List<Pair<String, String>>>> labeledRows = new ArrayList<Triple<String, Table, List<Pair<String, String>>>>() {{
            List<String> lookaheads = lookaheads(SimulationPlotHelper.table(benchmark + "_ht_lru_lookaheads", benchmark + "_ht_lru", benchmark + "_ht_lru_lookaheads"));

            lookaheads.sort(Comparator.comparing(Integer::parseInt));

            for (String lookahead : lookaheads) {
                add(
                        new Triple<>(
                                lookahead,
                                SimulationPlotHelper.tableHelperThreadedByStrideAndLookahead(benchmark + "_ht_lru_lookaheads", "S=10", "L=" + lookahead, benchmark + "_ht_lru", benchmark + "_ht_lru_lookaheads"),
                                new ArrayList<>()
                        )
                );
            }
        }};

        SimulationPlotHelper.numCycles(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedLookahead/numCycles"
        );
        SimulationPlotHelper.speedup(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedLookahead/speedup"
        );
        SimulationPlotHelper.ipc(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedLookahead/ipc"
        );
        SimulationPlotHelper.c0t0Ipc(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedLookahead/c0t0Ipc"
        );
        SimulationPlotHelper.c1t0Ipc(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedLookahead/c1t0Ipc"
        );
        SimulationPlotHelper.cpi(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedLookahead/cpi"
        );
        SimulationPlotHelper.c0t0Cpi(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedLookahead/c0t0Cpi"
        );
        SimulationPlotHelper.c1t0Cpi(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedLookahead/c1t0Cpi"
        );
        SimulationPlotHelper.l2Mpki(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedLookahead/l2Mpki"
        );
        SimulationPlotHelper.c0t0L2Mpki(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedLookahead/c0t0L2Mpki"
        );
        SimulationPlotHelper.c1t0L2Mpki(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedLookahead/c1t0L2Mpki"
        );
        SimulationPlotHelper.numDynamicInstructionsCommitted(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedLookahead/numDynamicInstructionsCommitted"
        );
        SimulationPlotHelper.numL2Requests(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedLookahead/numL2Requests"
        );
        SimulationPlotHelper.numHelperThreadL2Requests(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedLookahead/numHelperThreadL2Requests"
        );
        SimulationPlotHelper.numUsefulHelperThreadL2Requests(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedLookahead/numUsefulHelperThreadL2Requests"
        );
        SimulationPlotHelper.numUselessHelperThreadL2Requests(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedLookahead/numUselessHelperThreadL2Requests"
        );
        SimulationPlotHelper.helperThreadPrefetchCoverage(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedLookahead/helperThreadPrefetchCoverage"
        );
        SimulationPlotHelper.helperThreadPrefetchAccuracy(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedLookahead/helperThreadPrefetchAccuracy"
        );
        SimulationPlotHelper.helperThreadPrefetchLateness(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedLookahead/helperThreadPrefetchLateness"
        );
        SimulationPlotHelper.helperThreadPrefetchPollution(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedLookahead/helperThreadPrefetchPollution"
        );

//        for (Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
//            SimulationPlotHelper.l2MlpDistribution(labeledRow).plot(
//                    "experiment_plots/" + benchmark + "/helperThreadedLookahead/" + labeledRow.getFirst() + "/l2MlpDistribution"
//            );
//            SimulationPlotHelper.l2MlpCostDistribution(labeledRow).plot(
//                    "experiment_plots/" + benchmark + "/helperThreadedLookahead/" + labeledRow.getFirst() + "/l2MlpCostDistribution"
//            );
//            SimulationPlotHelper.l2StackDistanceProfile(labeledRow).plot(
//                    "experiment_plots/" + benchmark + "/helperThreadedLookahead/" + labeledRow.getFirst() + "/l2StackDistanceProfile"
//            );
//        }
//
//        SimulationPlotHelper.plotNumDynamicInstructionsCommittedPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedLookahead/numDynamicInstructionsCommittedPerInterval"
//        );
//        SimulationPlotHelper.plotNumL2RequestsPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedLookahead/numL2RequestsPerInterval"
//        );
//        SimulationPlotHelper.plotAverageL2MissMLPCostPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedLookahead/averageL2MissMLPCostPerInterval"
//        );
//        SimulationPlotHelper.plotIpcPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedLookahead/ipcPerInterval"
//        );
//        SimulationPlotHelper.plotCpiPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedLookahead/cpiPerInterval"
//        );
//        SimulationPlotHelper.plotL2MpkiPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedLookahead/l2MpkiPerInterval"
//        );
//        SimulationPlotHelper.plotNumHelperThreadL2RequestsPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedLookahead/numHelperThreadL2RequestsPerInterval"
//        );
//        SimulationPlotHelper.plotNumUsefulHelperThreadL2RequestsPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedLookahead/numUsefulHelperThreadL2RequestsPerInterval"
//        );
//        SimulationPlotHelper.plotNumUselessHelperThreadL2RequestsPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedLookahead/numUselessHelperThreadL2RequestsPerInterval"
//        );
//        SimulationPlotHelper.plotHelperThreadL2RequestQualityPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedLookahead/helperThreadL2RequestQualityPerInterval"
//        );
    }

    /**
     * Export the helper threaded stride CSV file.
     *
     * @param benchmarks the list of benchmarks
     */
    public static void exportHelperThreadedStrideCsv(final List<String> benchmarks) {
        for(String benchmark : benchmarks) {
            FileUtils.writeAllText(
                    table(
                            benchmark + "_ht_lru_strides",

                            benchmark + "_ht_lru_strides"
                    ).toCsv(),
                    "experiment_plots/helperThreadedStride_" + benchmark + ".csv"
            );
        }
    }

    /**
     * Plot the overall performance graphs with respect to the helper thread stride values for the specified benchmark.
     *
     * @param benchmark the benchmark
     */
    public static void helperThreadedStride(final String benchmark) {
        List<Triple<String, Table, List<Pair<String, String>>>> labeledRows = new ArrayList<Triple<String, Table, List<Pair<String, String>>>>() {{
            List<String> strides = strides(SimulationPlotHelper.table(benchmark + "_ht_lru_strides", benchmark + "_ht_lru_strides"));

            strides.sort(Comparator.comparing(Integer::parseInt));

            for (String stride : strides) {
                add(
                        new Triple<>(
                                stride,
                                SimulationPlotHelper.tableHelperThreadedByStride(benchmark + "_ht_lru_strides", "S=" + stride, benchmark + "_ht_lru_strides"),
                                new ArrayList<>()
                        )
                );
            }
        }};

        SimulationPlotHelper.numCycles(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedStride/numCycles"
        );
        SimulationPlotHelper.speedup(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedStride/speedup"
        );
        SimulationPlotHelper.ipc(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedStride/ipc"
        );
        SimulationPlotHelper.c0t0Ipc(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedStride/c0t0Ipc"
        );
        SimulationPlotHelper.c1t0Ipc(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedStride/c1t0Ipc"
        );
        SimulationPlotHelper.cpi(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedStride/cpi"
        );
        SimulationPlotHelper.c0t0Cpi(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedStride/c0t0Cpi"
        );
        SimulationPlotHelper.c1t0Cpi(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedStride/c1t0Cpi"
        );
        SimulationPlotHelper.l2Mpki(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedStride/l2Mpki"
        );
        SimulationPlotHelper.c0t0L2Mpki(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedStride/c0t0L2Mpki"
        );
        SimulationPlotHelper.c1t0L2Mpki(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedStride/c1t0L2Mpki"
        );
        SimulationPlotHelper.numDynamicInstructionsCommitted(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedStride/numDynamicInstructionsCommitted"
        );
        SimulationPlotHelper.numL2Requests(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedStride/numL2Requests"
        );
        SimulationPlotHelper.numHelperThreadL2Requests(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedStride/numHelperThreadL2Requests"
        );
        SimulationPlotHelper.numUsefulHelperThreadL2Requests(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedStride/numUsefulHelperThreadL2Requests"
        );
        SimulationPlotHelper.numUselessHelperThreadL2Requests(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedStride/numUselessHelperThreadL2Requests"
        );
        SimulationPlotHelper.helperThreadPrefetchCoverage(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedStride/helperThreadPrefetchCoverage"
        );
        SimulationPlotHelper.helperThreadPrefetchAccuracy(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedStride/helperThreadPrefetchAccuracy"
        );
        SimulationPlotHelper.helperThreadPrefetchLateness(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedStride/helperThreadPrefetchLateness"
        );
        SimulationPlotHelper.helperThreadPrefetchPollution(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedStride/helperThreadPrefetchPollution"
        );

//        for (Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
//            SimulationPlotHelper.l2MlpDistribution(labeledRow).plot(
//                    "experiment_plots/" + benchmark + "/helperThreadedStride/" + labeledRow.getFirst() + "/l2MlpDistribution"
//            );
//            SimulationPlotHelper.l2MlpCostDistribution(labeledRow).plot(
//                    "experiment_plots/" + benchmark + "/helperThreadedStride/" + labeledRow.getFirst() + "/l2MlpCostDistribution"
//            );
//            SimulationPlotHelper.l2StackDistanceProfile(labeledRow).plot(
//                    "experiment_plots/" + benchmark + "/helperThreadedStride/" + labeledRow.getFirst() + "/l2StackDistanceProfile"
//            );
//        }
//
//        SimulationPlotHelper.plotNumDynamicInstructionsCommittedPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedStride/numDynamicInstructionsCommittedPerInterval"
//        );
//        SimulationPlotHelper.plotNumL2RequestsPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedStride/numL2RequestsPerInterval"
//        );
//        SimulationPlotHelper.plotAverageL2MissMLPCostPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedStride/averageL2MissMLPCostPerInterval"
//        );
//        SimulationPlotHelper.plotIpcPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedStride/ipcPerInterval"
//        );
//        SimulationPlotHelper.plotCpiPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedStride/cpiPerInterval"
//        );
//        SimulationPlotHelper.plotL2MpkiPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedStride/l2MpkiPerInterval"
//        );
//        SimulationPlotHelper.plotNumHelperThreadL2RequestsPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedStride/numHelperThreadL2RequestsPerInterval"
//        );
//        SimulationPlotHelper.plotNumUsefulHelperThreadL2RequestsPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedStride/numUsefulHelperThreadL2RequestsPerInterval"
//        );
//        SimulationPlotHelper.plotNumUselessHelperThreadL2RequestsPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedStride/numUselessHelperThreadL2RequestsPerInterval"
//        );
//        SimulationPlotHelper.plotHelperThreadL2RequestQualityPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedStride/helperThreadL2RequestQualityPerInterval"
//        );
    }

    /**
     * Export the helper threaded partitioned L2 CSV file.
     *
     * @param benchmarks the list of benchmarks
     */
    public static void exportHelperThreadedPartitionedL2Csv(final List<String> benchmarks) {
        for(String benchmark : benchmarks) {
            FileUtils.writeAllText(
                    table(
                            benchmark + "_ht_lru_dynamic_partitioned",

                            benchmark + "_ht_lru",
                            benchmark + "_ht_lru_static_partitioned",
                            benchmark + "_ht_lru_dynamic_partitioned"
                    ).toCsv(),
                    "experiment_plots/helperThreadedPartitionedL2_" + benchmark + ".csv"
            );
        }
    }

    /**
     * Plot the helper threaded in partitioned L2 cache graphs for the specified benchmark.
     *
     * @param benchmark the benchmark
     */
    public static void helperThreadedPartitionedL2(final String benchmark) {
        final List<String> staticPartitionedL2MtWaysInPartitionedL2s = mtWaysInPartitionedL2s(SimulationPlotHelper.table(benchmark + "_ht_lru_static_partitioned", benchmark + "_ht_lru_static_partitioned"));

        final List<String> dynamicPartitionedL2ReplacementPolicies = l2ReplacementPolicies(SimulationPlotHelper.table(benchmark + "_ht_lru_dynamic_partitioned", benchmark + "_ht_lru_dynamic_partitioned"));

        final Map<String, String> dynamicPartitionedL2ReplacementPolicyLabels = new LinkedHashMap<String, String>() {{
            put(CacheReplacementPolicyType.CPI_BASED_CACHE_PARTITIONING_LRU + "", "CPI");
            put(CacheReplacementPolicyType.MIN_MISS_CACHE_PARTITIONING_LRU + "", "MISS");
            put(CacheReplacementPolicyType.MLP_AWARE_CACHE_PARTITIONING_LRU + "", "MLP");
        }};

        List<Triple<String, Table, List<Pair<String, String>>>> labeledRows = new ArrayList<Triple<String, Table, List<Pair<String, String>>>>() {{
            add(
                    new Triple<>(
                            "NoP",
                            SimulationPlotHelper.table(benchmark + "_ht_lru_static_partitioned", benchmark + "_ht_lru"),
                            new ArrayList<>()
                    )
            );

            for (String mtWaysInPartitionedL2 : staticPartitionedL2MtWaysInPartitionedL2s) {
                add(
                        new Triple<>(
                                "P" + mtWaysInPartitionedL2,
                                SimulationPlotHelper.tableStaticPartitionedL2(benchmark + "_ht_lru_static_partitioned", "P=" + mtWaysInPartitionedL2, benchmark + "_ht_lru_static_partitioned"),
                                new ArrayList<>()
                        )
                );
            }

            for (String l2ReplacementPolicyType : dynamicPartitionedL2ReplacementPolicies) {
                add(
                        new Triple<>(
                                dynamicPartitionedL2ReplacementPolicyLabels.get(l2ReplacementPolicyType),
                                SimulationPlotHelper.tableDynamicPartitionedL2(benchmark + "_ht_lru_dynamic_partitioned", l2ReplacementPolicyType, benchmark + "_ht_lru_dynamic_partitioned"),
                                new ArrayList<>()
                        )
                );
            }
        }};

        SimulationPlotHelper.numCycles(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedPartitionedL2/numCycles"
        );
        SimulationPlotHelper.speedup(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedPartitionedL2/speedup"
        );
        SimulationPlotHelper.ipc(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedPartitionedL2/ipc"
        );
        SimulationPlotHelper.c0t0Ipc(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedPartitionedL2/c0t0Ipc"
        );
        SimulationPlotHelper.c1t0Ipc(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedPartitionedL2/c1t0Ipc"
        );
        SimulationPlotHelper.cpi(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedPartitionedL2/cpi"
        );
        SimulationPlotHelper.c0t0Cpi(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedPartitionedL2/c0t0Cpi"
        );
        SimulationPlotHelper.c1t0Cpi(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedPartitionedL2/c1t0Cpi"
        );
        SimulationPlotHelper.l2Mpki(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedPartitionedL2/l2Mpki"
        );
        SimulationPlotHelper.c0t0L2Mpki(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedPartitionedL2/c0t0L2Mpki"
        );
        SimulationPlotHelper.c1t0L2Mpki(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedPartitionedL2/c1t0L2Mpki"
        );
        SimulationPlotHelper.numDynamicInstructionsCommitted(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedPartitionedL2/numDynamicInstructionsCommitted"
        );
        SimulationPlotHelper.numL2Requests(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedPartitionedL2/numL2Requests"
        );
        SimulationPlotHelper.numHelperThreadL2Requests(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedPartitionedL2/numHelperThreadL2Requests"
        );
        SimulationPlotHelper.numUsefulHelperThreadL2Requests(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedPartitionedL2/numUsefulHelperThreadL2Requests"
        );
        SimulationPlotHelper.numUselessHelperThreadL2Requests(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedPartitionedL2/numUselessHelperThreadL2Requests"
        );
        SimulationPlotHelper.helperThreadPrefetchCoverage(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedPartitionedL2/helperThreadPrefetchCoverage"
        );
        SimulationPlotHelper.helperThreadPrefetchAccuracy(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedPartitionedL2/helperThreadPrefetchAccuracy"
        );
        SimulationPlotHelper.helperThreadPrefetchLateness(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedPartitionedL2/helperThreadPrefetchLateness"
        );
        SimulationPlotHelper.helperThreadPrefetchPollution(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedPartitionedL2/helperThreadPrefetchPollution"
        );

//            for (Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
//                SimulationPlotHelper.l2MlpDistribution(labeledRow).plot(
//                        "experiment_plots/" + benchmark + "/helperThreadedPartitionedL2/" + labeledRow.getFirst() + "/l2MlpDistribution"
//                );
//                SimulationPlotHelper.l2MlpCostDistribution(labeledRow).plot(
//                        "experiment_plots/" + benchmark + "/helperThreadedPartitionedL2/" + labeledRow.getFirst() + "/l2MlpCostDistribution"
//                );
//                SimulationPlotHelper.l2StackDistanceProfile(labeledRow).plot(
//                        "experiment_plots/" + benchmark + "/helperThreadedPartitionedL2/" + labeledRow.getFirst() + "/l2StackDistanceProfile"
//                );
//            }
//
//            SimulationPlotHelper.plotNumDynamicInstructionsCommittedPerInterval(
//                    labeledRows, "experiment_plots/" + benchmark + "/helperThreadedPartitionedL2/numDynamicInstructionsCommittedPerInterval"
//            );
//            SimulationPlotHelper.plotNumL2RequestsPerInterval(
//                    labeledRows, "experiment_plots/" + benchmark + "/helperThreadedPartitionedL2/numL2RequestsPerInterval"
//            );
//            SimulationPlotHelper.plotAverageL2MissMLPCostPerInterval(
//                    labeledRows, "experiment_plots/" + benchmark + "/helperThreadedPartitionedL2/averageL2MissMLPCostPerInterval"
//            );
//            SimulationPlotHelper.plotIpcPerInterval(
//                    labeledRows, "experiment_plots/" + benchmark + "/helperThreadedPartitionedL2/ipcPerInterval"
//            );
//            SimulationPlotHelper.plotCpiPerInterval(
//                    labeledRows, "experiment_plots/" + benchmark + "/helperThreadedPartitionedL2/cpiPerInterval"
//            );
//            SimulationPlotHelper.plotL2MpkiPerInterval(
//                    labeledRows, "experiment_plots/" + benchmark + "/helperThreadedPartitionedL2/l2MpkiPerInterval"
//            );
//            SimulationPlotHelper.plotNumHelperThreadL2RequestsPerInterval(
//                    labeledRows, "experiment_plots/" + benchmark + "/helperThreadedPartitionedL2/numHelperThreadL2RequestsPerInterval"
//            );
//            SimulationPlotHelper.plotNumUsefulHelperThreadL2RequestsPerInterval(
//                    labeledRows, "experiment_plots/" + benchmark + "/helperThreadedPartitionedL2/numUsefulHelperThreadL2RequestsPerInterval"
//            );
//            SimulationPlotHelper.plotNumUselessHelperThreadL2RequestsPerInterval(
//                    labeledRows, "experiment_plots/" + benchmark + "/helperThreadedPartitionedL2/numUselessHelperThreadL2RequestsPerInterval"
//            );
//            SimulationPlotHelper.plotHelperThreadL2RequestQualityPerInterval(
//                    labeledRows, "experiment_plots/" + benchmark + "/helperThreadedPartitionedL2/helperThreadL2RequestQualityPerInterval"
//            );
    }

    /**
     * Export the helper threaded L2 replacement CSV file.
     *
     * @param benchmarks the list of benchmarks
     */
    public static void exportHelperThreadedL2ReplacementCsv(final List<String> benchmarks) {
        for(String benchmark : benchmarks) {
            FileUtils.writeAllText(
                    table(
                            benchmark + "_ht_l2ReplacementPolicies",

                            benchmark + "_ht_lru",
                            benchmark + "_ht_l2ReplacementPolicies"
                    ).toCsv(),
                    "experiment_plots/l2Replacement_" + benchmark + ".csv"
            );
        }
    }

    /**
     * Plot the helper threaded performance graphs with respect to the L2 replacement policy values for the specified benchmark.
     *
     * @param benchmark the benchmark
     */
    public static void helperThreadedL2Replacement(final String benchmark) {
        List<Triple<String, Table, List<Pair<String, String>>>> labeledRows = new ArrayList<Triple<String, Table, List<Pair<String, String>>>>() {{
            add(
                    new Triple<>(
                            "LRU",
                            SimulationPlotHelper.table(benchmark + "_ht_l2ReplacementPolicies", benchmark + "_ht_lru"),
                            new ArrayList<>()
                    )
            );

            final Map<String, String> l2ReplacementPolicyLabels = new LinkedHashMap<String, String>() {{
                put(CacheReplacementPolicyType.LRU + "", "LRU");
                put(CacheReplacementPolicyType.PREFETCH_AWARE_RHM_LRU + "", "Pref");
                put(CacheReplacementPolicyType.REUSE_DISTANCE_PREDICTION + "", "RDP");
                put(CacheReplacementPolicyType.HT_PREF_ACC_BASED_PREF_AND_RDP_SET_DUELING + "", "PrefAcc");
                put(CacheReplacementPolicyType.HT_USEFUL_PREF_BASED_PREF_AND_RDP_SET_DUELING + "", "UsefulPref");
            }};

            List<String> l2ReplacementPolicies = l2ReplacementPolicies(SimulationPlotHelper.table(benchmark + "_ht_l2ReplacementPolicies", benchmark + "_ht_l2ReplacementPolicies"));

            for (String l2ReplacementPolicy : l2ReplacementPolicies) {
                add(
                        new Triple<>(
                                l2ReplacementPolicyLabels.get(l2ReplacementPolicy),
                                SimulationPlotHelper.tableByL2Replacement(benchmark + "_ht_l2ReplacementPolicies", l2ReplacementPolicy, benchmark + "_ht_l2ReplacementPolicies"),
                                new ArrayList<>()
                        )
                );
            }
        }};

        SimulationPlotHelper.numCycles(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Replacement/numCycles"
        );
        SimulationPlotHelper.speedup(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Replacement/speedup"
        );
        SimulationPlotHelper.ipc(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Replacement/ipc"
        );
        SimulationPlotHelper.c0t0Ipc(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Replacement/c0t0Ipc"
        );
        SimulationPlotHelper.c1t0Ipc(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Replacement/c1t0Ipc"
        );
        SimulationPlotHelper.cpi(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Replacement/cpi"
        );
        SimulationPlotHelper.c0t0Cpi(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Replacement/c0t0Cpi"
        );
        SimulationPlotHelper.c1t0Cpi(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Replacement/c1t0Cpi"
        );
        SimulationPlotHelper.l2Mpki(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Replacement/l2Mpki"
        );
        SimulationPlotHelper.c0t0L2Mpki(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Replacement/c0t0L2Mpki"
        );
        SimulationPlotHelper.c1t0L2Mpki(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Replacement/c1t0L2Mpki"
        );
        SimulationPlotHelper.numDynamicInstructionsCommitted(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Replacement/numDynamicInstructionsCommitted"
        );
        SimulationPlotHelper.numL2Requests(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Replacement/numL2Requests"
        );
        SimulationPlotHelper.numHelperThreadL2Requests(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Replacement/numHelperThreadL2Requests"
        );
        SimulationPlotHelper.numUsefulHelperThreadL2Requests(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Replacement/numUsefulHelperThreadL2Requests"
        );
        SimulationPlotHelper.numUselessHelperThreadL2Requests(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Replacement/numUselessHelperThreadL2Requests"
        );
        SimulationPlotHelper.helperThreadPrefetchCoverage(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Replacement/helperThreadPrefetchCoverage"
        );
        SimulationPlotHelper.helperThreadPrefetchAccuracy(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Replacement/helperThreadPrefetchAccuracy"
        );
        SimulationPlotHelper.helperThreadPrefetchLateness(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Replacement/helperThreadPrefetchLateness"
        );
        SimulationPlotHelper.helperThreadPrefetchPollution(labeledRows).plot(
                "experiment_plots/" + benchmark + "/helperThreadedL2Replacement/helperThreadPrefetchPollution"
        );

//        for (Triple<String, Table, List<Pair<String, String>>> labeledRow : labeledRows) {
//            SimulationPlotHelper.l2MlpDistribution(labeledRow).plot(
//                    "experiment_plots/" + benchmark + "/helperThreadedL2Replacement/" + labeledRow.getFirst() + "/l2MlpDistribution"
//            );
//            SimulationPlotHelper.l2MlpCostDistribution(labeledRow).plot(
//                    "experiment_plots/" + benchmark + "/helperThreadedL2Replacement/" + labeledRow.getFirst() + "/l2MlpCostDistribution"
//            );
//            SimulationPlotHelper.l2StackDistanceProfile(labeledRow).plot(
//                    "experiment_plots/" + benchmark + "/helperThreadedL2Replacement/" + labeledRow.getFirst() + "/l2StackDistanceProfile"
//            );
//        }
//
//        SimulationPlotHelper.plotNumDynamicInstructionsCommittedPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedL2Replacement/numDynamicInstructionsCommittedPerInterval"
//        );
//        SimulationPlotHelper.plotNumL2RequestsPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedL2Replacement/numL2RequestsPerInterval"
//        );
//        SimulationPlotHelper.plotAverageL2MissMLPCostPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedL2Replacement/averageL2MissMLPCostPerInterval"
//        );
//        SimulationPlotHelper.plotIpcPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedL2Replacement/ipcPerInterval"
//        );
//        SimulationPlotHelper.plotCpiPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedL2Replacement/cpiPerInterval"
//        );
//        SimulationPlotHelper.plotL2MpkiPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedL2Replacement/l2MpkiPerInterval"
//        );
//        SimulationPlotHelper.plotNumHelperThreadL2RequestsPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedL2Replacement/numHelperThreadL2RequestsPerInterval"
//        );
//        SimulationPlotHelper.plotNumUsefulHelperThreadL2RequestsPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedL2Replacement/numUsefulHelperThreadL2RequestsPerInterval"
//        );
//        SimulationPlotHelper.plotNumUselessHelperThreadL2RequestsPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedL2Replacement/numUselessHelperThreadL2RequestsPerInterval"
//        );
//        SimulationPlotHelper.plotHelperThreadL2RequestQualityPerInterval(
//                labeledRows, "experiment_plots/" + benchmark + "/helperThreadedL2Replacement/helperThreadL2RequestQualityPerInterval"
//        );
    }

    /**
     * Entry point.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        List<String> benchmarks = new ArrayList<String>() {{
            add("mst");
            add("em3d");
            add("429.mcf");
        }};

        exportHelperThreadedOverallPerformanceCsv(benchmarks);
        exportL2SizeCsv(benchmarks);
        exportHelperThreadedLookaheadCsv(benchmarks);
        exportHelperThreadedStrideCsv(benchmarks);
        exportHelperThreadedPartitionedL2Csv(benchmarks);
        exportHelperThreadedL2ReplacementCsv(benchmarks);

        List<Action1<String>> actions = new ArrayList<Action1<String>>() {{
            add(SimulationPlotHelper::l2Size);
            add(SimulationPlotHelper::baselineL2Size);
            add(SimulationPlotHelper::helperThreadedL2Size);
            add(SimulationPlotHelper::helperThreadedLookahead);
            add(SimulationPlotHelper::helperThreadedStride);
            add(SimulationPlotHelper::helperThreadedPartitionedL2);
            add(SimulationPlotHelper::helperThreadedL2Replacement);
        }};

        benchmarks.stream().forEach(benchmark -> actions.stream().forEach(action -> action.apply(benchmark)));
    }
}
