package archimulator.util.chart;

import archimulator.util.DateHelper;
import archimulator.util.action.Function;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class ExperimentLinePlotFrame extends ApplicationFrame {
    public int numSubPlots;
    private List<TimeSeriesCollection> dataSets;
    private List<Map<ExperimentLinePlot.ExperimentSubLinePlotLine, Function<Double>>> dataSinks;
    private ExperimentLinePlot experimentLinePlot;

    public ExperimentLinePlotFrame(ExperimentLinePlot experimentLinePlot) throws SQLException {
        super(experimentLinePlot.getTitle());
        this.experimentLinePlot = experimentLinePlot;

        this.numSubPlots = experimentLinePlot.getSubLinePlots().size();

        CombinedDomainXYPlot plot = new CombinedDomainXYPlot(new DateAxis("Time"));
        this.dataSets = new ArrayList<TimeSeriesCollection>();
        this.dataSinks = new ArrayList<Map<ExperimentLinePlot.ExperimentSubLinePlotLine, Function<Double>>>();

        for (ExperimentLinePlot.ExperimentSubLinePlot experimentSubLinePlot : experimentLinePlot.getSubLinePlots()) {
            TimeSeriesCollection dataSetsPerSubPlot = new TimeSeriesCollection();
            this.dataSets.add(dataSetsPerSubPlot);

            HashMap<ExperimentLinePlot.ExperimentSubLinePlotLine, Function<Double>> dataSinksPerSubPlot = new HashMap<ExperimentLinePlot.ExperimentSubLinePlotLine, Function<Double>>();
            this.dataSinks.add(dataSinksPerSubPlot);

            for (ExperimentLinePlot.ExperimentSubLinePlotLine experimentSubLinePlotLine : experimentSubLinePlot.getLines()) {
                TimeSeries timeSeries = new TimeSeries(experimentSubLinePlotLine.getTitle());
                dataSetsPerSubPlot.addSeries(timeSeries);
                dataSinksPerSubPlot.put(experimentSubLinePlotLine, experimentSubLinePlotLine.getGetValueCallback());
            }

            NumberAxis rangeAxis = new NumberAxis(experimentSubLinePlot.getTitleY());
            rangeAxis.setAutoRangeIncludesZero(false);
            XYPlot subplot = new XYPlot(dataSetsPerSubPlot, null, rangeAxis, new StandardXYItemRenderer());
            subplot.setBackgroundPaint(Color.lightGray);
            subplot.setDomainGridlinePaint(Color.white);
            subplot.setRangeGridlinePaint(Color.white);
            plot.add(subplot);
        }

        JFreeChart chart = new JFreeChart(experimentLinePlot.getTitle(), plot);
        chart.setBorderPaint(Color.black);
        chart.setBorderVisible(true);
        chart.setBackgroundPaint(Color.white);

        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis.setFixedAutoRange(3600000.0);

        JPanel content = new JPanel(new BorderLayout());

        ChartPanel chartPanel = new ChartPanel(chart);
        content.add(chartPanel);

        chartPanel.setPreferredSize(new java.awt.Dimension(500, 470));
        chartPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setContentPane(content);

        DataSink dataSink = new DataSink();
        new Thread(dataSink).start();
    }

    private class DataSink implements Runnable {
        public void run() {
            while (true) {
                for (int i = 0; i < numSubPlots; i++) {
                    TimeSeriesCollection timeSeriesCollection = dataSets.get(i);

                    ExperimentLinePlot.ExperimentSubLinePlot experimentSubLinePlot = experimentLinePlot.getSubLinePlots().get(i);
                    for (int j = 0; j < experimentSubLinePlot.getLines().size(); j++) {
                        Object seriesObj = timeSeriesCollection.getSeries().get(j);
                        TimeSeries series = (TimeSeries) seriesObj;
                        ExperimentLinePlot.ExperimentSubLinePlotLine experimentSubLinePlotLine = experimentSubLinePlot.getLines().get(j);
                        Double value = dataSinks.get(i).get(experimentSubLinePlotLine).apply();
                        System.out.printf("[%s] '%s'.'%s' = %s\n", DateHelper.toString(new Date()), experimentSubLinePlot.getTitleY(), experimentSubLinePlotLine.getTitle(), value);
                        series.addOrUpdate(new Millisecond(), value);
                    }
                }

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    System.out.println(ex);
                }
            }
        }
    }
}