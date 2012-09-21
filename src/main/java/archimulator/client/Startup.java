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
import archimulator.model.ExperimentState;
import archimulator.model.ExperimentPack;
import archimulator.service.ServiceManager;

import java.io.IOException;

public class Startup { //TODO: to be removed, only exposed by web UI
    public static void main(String[] args) throws IOException {
        if(args == null || args.length == 0) {
            return;
        }

        for(String arg : args) {
            ExperimentPack experimentPack = ServiceManager.getExperimentService().getExperimentPackByTitle(arg);
            if (experimentPack != null) {
                ServiceManager.getExperimentService().runExperimentPackByTitle(arg);
            } else {
                Experiment experiment = ServiceManager.getExperimentService().getFirstExperimentByTitle(arg);
                if (experiment != null && experiment.getState() == ExperimentState.READY_TO_RUN) {
                    ServiceManager.getExperimentService().runExperimentByTitle(arg);
                }
            }
        }
    }
}
