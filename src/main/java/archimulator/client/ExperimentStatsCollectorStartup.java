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
package archimulator.client;

import archimulator.model.Experiment;
import archimulator.service.ServiceManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ExperimentStatsCollectorStartup {
    public static void main(String[] args) {
        for(Experiment experiment : ServiceManager.getExperimentService().getAllExperiments()) {
            List<Experiment> duplicates = ServiceManager.getExperimentService().getExperimentsByTitle(experiment.getTitle());
            if(duplicates.size() > 1) {
                System.out.println(experiment.getTitle() + ": " + duplicates.size() + " duplicates");
                System.out.println();

                Experiment lastExperiment = duplicates.get(duplicates.size() - 1);

                for(Experiment duplicate : duplicates) {
                    if(duplicate != lastExperiment && duplicate.isStopped()) {
                        ServiceManager.getExperimentService().removeExperimentById(duplicate.getId());
                    }
                }
            }
        }

        if (args.length >= 1) {
            for (String arg : args) {
                ExperimentPack experimentPack = ServiceManager.getExperimentService().getExperimentPackByTitle(arg);
                if (experimentPack != null) {
                    ServiceManager.getExperimentService().dumpExperimentPack(experimentPack, true, true);
                } else {
                    Experiment experiment = ServiceManager.getExperimentService().getFirstExperimentByTitle(arg);
                    if (experiment != null) {
                        ServiceManager.getExperimentService().dumpExperiment(experiment);
                    } else {
                        System.err.println("Experiment pack or experiment \"" + arg + "\" do not exist");
                    }
                }
            }
        } else {
            for (ExperimentPack experimentPack : ServiceManager.getExperimentService().getAllExperimentPacks()) {
                ServiceManager.getExperimentService().dumpExperimentPack(experimentPack, true, true);
                System.out.println();
            }

            //TODO: "mst_ht_2048" should not be hardcoded!!!
            tableSummary("mst_baseline_2048");
            tableSummary("mst_ht_2048");
        }
    }

    private static void tableSummary(String titlePrefix) {
        List<Experiment> experiments = ServiceManager.getExperimentService().getExperimentsByTitlePrefix(titlePrefix, true);

        Collections.sort(experiments, new Comparator<Experiment>() {
            @Override
            public int compare(Experiment o1, Experiment o2) {
                long totalCycle1 = Long.parseLong(o1.getStatValue(o1.getMeasurementTitlePrefix() + "cycleAccurateEventQueue/currentCycle"));
                long totalCycle2 = Long.parseLong(o2.getStatValue(o2.getMeasurementTitlePrefix() + "cycleAccurateEventQueue/currentCycle"));
                return (int) (totalCycle1 - totalCycle2);
            }
        });

        ServiceManager.getExperimentService().tableSummary(titlePrefix, experiments.get(0), experiments);
    }
}
