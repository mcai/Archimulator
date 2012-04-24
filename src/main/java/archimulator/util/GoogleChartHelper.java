package archimulator.util;

import com.googlecode.charts4j.*;

import static com.googlecode.charts4j.Color.*;

public class GoogleChartHelper {
    public static void main(String[] args) {
        // EXAMPLE CODE START
        // Defining data plots.
        BarChartPlot team1 = Plots.newBarChartPlot(Data.newData(25, 43, 12, 30), BLUEVIOLET, "Team A");
        BarChartPlot team2 = Plots.newBarChartPlot(Data.newData(8, 35, 11, 5), ORANGERED, "Team B");
        BarChartPlot team3 = Plots.newBarChartPlot(Data.newData(10, 20, 30, 30), LIMEGREEN, "Team C");

        // Instantiating chart.
        BarChart chart = GCharts.newBarChart(team1, team2, team3);

        // Defining axis info and styles
        AxisStyle axisStyle = AxisStyle.newAxisStyle(BLACK, 13, AxisTextAlignment.CENTER);
        AxisLabels score = AxisLabelsFactory.newAxisLabels("Score", 50.0);
        score.setAxisStyle(axisStyle);
        AxisLabels year = AxisLabelsFactory.newAxisLabels("Year", 50.0);
        year.setAxisStyle(axisStyle);

        // Adding axis info to chart.
        chart.addXAxisLabels(AxisLabelsFactory.newAxisLabels("2002", "2003", "2004", "2005"));
        chart.addYAxisLabels(AxisLabelsFactory.newNumericRangeAxisLabels(0, 100));
        chart.addYAxisLabels(score);
        chart.addXAxisLabels(year);

        chart.setSize(600, 450);
        chart.setBarWidth(100);
        chart.setSpaceWithinGroupsOfBars(20);
        chart.setDataStacked(true);
        chart.setTitle("Team Scores", BLACK, 16);
        chart.setGrid(100, 10, 3, 2);
        chart.setBackgroundFill(Fills.newSolidFill(ALICEBLUE));
        LinearGradientFill fill = Fills.newLinearGradientFill(0, LAVENDER, 100);
        fill.addColorAndOffset(WHITE, 0);
        chart.setAreaFill(fill);
        String url = chart.toURLString();


        System.out.println(UrlUtil.normalize(url));
    }
}
