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
package archimulator.service;

import archimulator.model.*;
import net.pickapack.service.Service;

import java.util.List;

/**
 * Service for managing experiments.
 *
 * @author Min Cai
 */
public interface ExperimentService extends Service {
    /**
     *
     * @return
     */
    List<Experiment> getAllExperiments();

    /**
     *
     * @param first
     * @param count
     * @return
     */
    List<Experiment> getAllExperiments(long first, long count);

    /**
     *
     * @return
     */
    long getNumAllExperiments();

    /**
     *
     * @param experimentState
     * @return
     */
    long getNumAllExperimentsByState(ExperimentState experimentState);

    /**
     *
     * @param id
     * @return
     */
    Experiment getExperimentById(long id);

    /**
     *
     * @param title
     * @return
     */
    List<Experiment> getExperimentsByTitle(String title);

    /**
     *
     * @param title
     * @return
     */
    Experiment getFirstExperimentByTitle(String title);

    /**
     *
     * @param title
     * @return
     */
    Experiment getLatestExperimentByTitle(String title);

    /**
     *
     * @param benchmark
     * @return
     */
    List<Experiment> getExperimentsByBenchmark(Benchmark benchmark);

    /**
     *
     * @param architecture
     * @return
     */
    List<Experiment> getExperimentsByArchitecture(Architecture architecture);

    /**
     *
     * @param parent
     * @return
     */
    List<Experiment> getExperimentsByParent(ExperimentPack parent);

    /**
     *
     * @param parent
     * @param first
     * @param count
     * @return
     */
    List<Experiment> getExperimentsByParent(ExperimentPack parent, long first, long count);

    /**
     *
     * @param experiment
     */
    void addExperiment(Experiment experiment);

    /**
     *
     * @param id
     */
    void removeExperimentById(long id);

    /**
     *
     * @param experiment
     */
    void updateExperiment(Experiment experiment);

    /**
     *
     * @return
     */
    Experiment getFirstExperimentToRun();

    /**
     *
     * @param parent
     * @return
     */
    List<Experiment> getStoppedExperimentsByParent(ExperimentPack parent);

    /**
     *
     * @param parent
     * @return
     */
    Experiment getFirstStoppedExperimentByParent(ExperimentPack parent);

    /**
     *
     * @return
     */
    List<ExperimentPack> getAllExperimentPacks();

    /**
     *
     * @param first
     * @param count
     * @return
     */
    List<ExperimentPack> getAllExperimentPacks(long first, long count);

    /**
     *
     * @param id
     * @return
     */
    ExperimentPack getExperimentPackById(long id);

    /**
     *
     * @param title
     * @return
     */
    ExperimentPack getExperimentPackByTitle(String title);

    /**
     *
     * @param experimentPack
     */
    void addExperimentPack(ExperimentPack experimentPack);

    /**
     *
     * @param id
     */
    void removeExperimentPackById(long id);

    /**
     *
     * @param experimentPack
     */
    void updateExperimentPack(ExperimentPack experimentPack);

    /**
     *
     * @param experimentSpec
     */
    void addExperimentSpec(ExperimentSpec experimentSpec);

    /**
     *
     * @param id
     */
    void removeExperimentSpecById(long id);

    /**
     *
     * @param parent
     * @return
     */
    ExperimentSpec getExperimentSpecByParent(ExperimentPack parent);

    /**
     *
     * @param parent
     * @return
     */
    long getNumExperimentsByParent(ExperimentPack parent);

    /**
     *
     * @param parent
     * @param experimentState
     * @return
     */
    long getNumExperimentsByParentAndState(ExperimentPack parent, ExperimentState experimentState);

    /**
     *
     * @param experimentPack
     */
    void startExperimentPack(ExperimentPack experimentPack);

    /**
     *
     * @param experimentPack
     */
    void stopExperimentPack(ExperimentPack experimentPack);

    /**
     *
     * @param parent
     */
    void resetCompletedExperimentsByParent(ExperimentPack parent);

    /**
     *
     * @param parent
     */
    void resetAbortedExperimentsByParent(ExperimentPack parent);

    /**
     *
     * @param experiment
     */
    void startExperiment(Experiment experiment);

    /**
     *
     */
    void start();
}
