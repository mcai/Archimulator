package archimulator.util;

import java.util.Random;
import javax.swing.JFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

public class TimeSeriesChartHelper {
    static TimeSeries ts = new TimeSeries("data");

    public static void main(String[] args) throws InterruptedException {
        gen myGen = new gen();
        new Thread(myGen).start();

        TimeSeriesCollection dataset = new TimeSeriesCollection(ts);
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            "GraphTest",
            "Time",
            "Value",
            dataset,
            true,
            true,
            false
        );
        final XYPlot plot = chart.getXYPlot();
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis.setFixedAutoRange(60000.0);

        JFrame frame = new JFrame("GraphTest");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ChartPanel label = new ChartPanel(chart);
        frame.getContentPane().add(label);
        //Suppose I add combo boxes and buttons here later

        frame.pack();
        frame.setVisible(true);
    }

    static class gen implements Runnable {
        private Random randGen = new Random();

        public void run() {
            while(true) {
                int num = randGen.nextInt(1000);
                System.out.println(num);
                ts.addOrUpdate(new Millisecond(), num);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    System.out.println(ex);
                }
            }
        }
    }
}
