package archimulator.util.chart;

import java.awt.*;
import java.util.Random;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class ExperimentBarPlotFrame extends ApplicationFrame {
    public ExperimentBarPlotFrame(String title) {
        super(title);
        CategoryDataset dataSet = createDataset();
//        JFreeChart chart = ChartFactory.createBarChart(
        JFreeChart chart = ChartFactory.createStackedBarChart(
                title,
                "Experiment",
                "# HT Requests",
                dataSet,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        CategoryPlot plot = chart.getCategoryPlot();
//        plot.getRenderer().setSeriesPaint(0, new Color(128, 0, 0));
//        plot.getRenderer().setSeriesPaint(1, new Color(0, 128, 0));
//        plot.getRenderer().setSeriesPaint(2, new Color(0, 0, 128));

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        ChartPdfExporter.exportPdf(chart, 600, 400, "/home/itecgo/Desktop/12.pdf"); //TODO: for demo only.
        
        ChartPanel chartPanel = new ChartPanel(chart, false);
        chartPanel.setPreferredSize(new Dimension(500, 270));
        setContentPane(chartPanel);
    }

    private static CategoryDataset createDataset() {
        String goodHtRequest = "Good HT Request";
        String badHtRequest = "Bad HT Request";
        String uglyHtrequest = "Ugly HT Request";

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        Random random = new Random();
        
        for(int i = 0; i < 16; i++) {
            String exp = "Exp #" + i;
            dataset.addValue(500 + random.nextInt(500), goodHtRequest, exp);
            dataset.addValue(500 + random.nextInt(500), badHtRequest, exp);
            dataset.addValue(500 + random.nextInt(500), uglyHtrequest, exp);
        }
        
        return dataset;
    }

    public static void main(String[] args) {
        ExperimentBarPlotFrame experimentBarPlotFrame = new ExperimentBarPlotFrame("Good, Bad and Ugly HT Request Distribution");
        experimentBarPlotFrame.pack();
        RefineryUtilities.centerFrameOnScreen(experimentBarPlotFrame);
        experimentBarPlotFrame.setVisible(true);
    }
}