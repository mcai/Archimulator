/*******************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.util.chart;

import archimulator.sim.base.experiment.profile.ExperimentProfile;
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

import javax.swing.*;
import java.awt.*;

public class ExperimentBarPlotFrame extends ApplicationFrame {
    public ExperimentBarPlotFrame(ExperimentBarPlot experimentBarPlot) {
        super(experimentBarPlot.getTitle());
        CategoryDataset dataSet = createDataset(experimentBarPlot);
        JFreeChart chart = experimentBarPlot.isStacked() ?
                ChartFactory.createStackedBarChart(
                        experimentBarPlot.getTitle(),
                        "Experiment",
                        experimentBarPlot.getTitleY(),
                        dataSet,
                        PlotOrientation.VERTICAL,
                        true,
                        true,
                        false
                ) :
                ChartFactory.createBarChart(
                        experimentBarPlot.getTitle(),
                        "Experiment",
                        experimentBarPlot.getTitleY(),
                        dataSet,
                        PlotOrientation.VERTICAL,
                        true,
                        true,
                        false
                );

        CategoryPlot plot = chart.getCategoryPlot();

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        ChartPanel chartPanel = new ChartPanel(chart, false);
        chartPanel.setPreferredSize(new Dimension(500, 470));
        chartPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setContentPane(chartPanel);
    }

    private static CategoryDataset createDataset(ExperimentBarPlot experimentBarPlot) {
        DefaultCategoryDataset dataSet = new DefaultCategoryDataset();

        for (ExperimentProfile experimentProfile : experimentBarPlot.getExperimentProfiles()) {
            if (experimentBarPlot.getExperimentProfilePred().apply(experimentProfile)) {
                for (ExperimentBarPlot.ExperimentSubBarPlot experimentSubBarPlot : experimentBarPlot.getSubBarPlots()) {
                    dataSet.addValue(experimentSubBarPlot.getGetValueCallback().apply(experimentProfile), experimentSubBarPlot.getTitle(), "Exp #" + experimentProfile.getId());
                }
            }
        }

        return dataSet;
    }
}