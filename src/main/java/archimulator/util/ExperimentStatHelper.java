/*******************************************************************************
 * Copyright (c) 2010-2013 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.util;

import archimulator.model.Experiment;
import archimulator.model.ExperimentStat;
import archimulator.model.ExperimentState;
import archimulator.service.ServiceManager;
import net.pickapack.io.serialization.JsonSerializationHelper;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Experiment statistics helper.
 *
 * @author Min Cai
 */
public class ExperimentStatHelper {
    /**
     * Entry point.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        List<Experiment> experiments = ServiceManager.getExperimentService().getAllExperiments();

        for (Experiment experiment : experiments) {
            if (experiment.getState() == ExperimentState.COMPLETED) {
                List<ExperimentStat> stats = new ArrayList<ExperimentStat>();

                List<String> statPrefixes = JedisHelper.getStatPrefixesByParent(experiment.getId());

                for (String statPrefix : statPrefixes) {
                    stats.addAll(JedisHelper.getStatsByParentAndPrefix(experiment.getId(), statPrefix));
                }

                String json = JsonSerializationHelper.serialize(
                        new ExperimentStat.ExperimentStatListContainer(
                                experiment.getParent().getTitle(), experiment.getTitle(), stats
                        )
                );

                String path = "experiment_stats/" + experiment.getId() + ".json";
                File file = new File(path);

                if (!file.getParentFile().exists()) {
                    if (!file.getParentFile().mkdirs()) {
                        throw new RuntimeException();
                    }
                }

                try {
                    FileUtils.writeStringToFile(file, json);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                System.out.println("Experiment #" + experiment.getId() + "'s statistics has been written to " + path);
            }
        }
    }
}
