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
     * Get all experiments.
     *
     * @return all experiments
     */
    List<Experiment> getAllExperiments();

    /**
     * Get all experiments by offset and count.
     *
     * @param first offset
     * @param count count
     * @return all experiments by offset and count
     */
    List<Experiment> getAllExperiments(long first, long count);

    /**
     * Get the number of all experiments.
     *
     * @return the number of all experiments
     */
    long getNumAllExperiments();

    /**
     * Get the number of all experiments in the specified state.
     *
     * @param experimentState the experiment's state
     * @return the number of all experiments in the specified state
     */
    long getNumAllExperimentsByState(ExperimentState experimentState);

    /**
     * Get the experiment by id.
     *
     * @param id the experiment's id
     * @return the experiment matching the id if any; otherwise null
     */
    Experiment getExperimentById(long id);

    /**
     * Get the list of experiments by title.
     *
     * @param title the experiment's title
     * @return a list of experiments matching the title
     */
    List<Experiment> getExperimentsByTitle(String title);

    /**
     * Get the first experiment by title.
     *
     * @param title the experiment's title
     * @return first experiment matching the specified title if any; otherwise null
     */
    Experiment getFirstExperimentByTitle(String title);

    /**
     * Get the first experiment under the specified parent experiment pack.
     *
     * @param parent parent experiment pack
     * @return the first experiment under the specified parent experiment pack
     */
    Experiment getFirstExperimentByParent(ExperimentPack parent);

    /**
     * Get the latest experiment by title.
     *
     * @param title the experiment's title
     * @return the latest experiment matching the specified title if any; otherwise null
     */
    Experiment getLatestExperimentByTitle(String title);

    /**
     * Get the list of experiments by benchmark.
     *
     * @param benchmark the benchmark
     * @return the list of experiments using the benchmark
     */
    List<Experiment> getExperimentsByBenchmark(Benchmark benchmark);

    /**
     * Get the list of experiments by architecture.
     *
     * @param architecture the architecture
     * @return the list of experiments targeting the architecture
     */
    List<Experiment> getExperimentsByArchitecture(Architecture architecture);

    /**
     * Get the list of experiments by parent experiment pack.
     *
     * @param parent the parent experiment pack
     * @return the list of experiments under the parent experiment pack
     */
    List<Experiment> getExperimentsByParent(ExperimentPack parent);

    /**
     * Get the list of experiments by parent experiment pack, offset and count.
     *
     * @param parent the parent experiment pack
     * @param first offset
     * @param count count
     * @return the list of experiments under the parent experiment pack with the specified offset and count
     */
    List<Experiment> getExperimentsByParent(ExperimentPack parent, long first, long count);

    /**
     * Add an experiment.
     *
     * @param experiment the experiment
     */
    void addExperiment(Experiment experiment);

    /**
     * Remove the experiment by id.
     *
     * @param id the experiment's id
     */
    void removeExperimentById(long id);

    /**
     * Update the specified experiment.
     *
     * @param experiment the experiment
     */
    void updateExperiment(Experiment experiment);

    /**
     * Get the first experiment to run.
     *
     * @return the first experiment to run if any; otherwise null.
     */
    Experiment getFirstExperimentToRun();

    /**
     * Get the list of experiments in the stopped state under the specified parent experiment pack.
     *
     * @param parent parent experiment pack
     * @return the list of experiments in the stopped state under the specified parent experiment pack
     */
    List<Experiment> getStoppedExperimentsByParent(ExperimentPack parent);

    /**
     * Get the first experiment in the stopped state under the specified parent experiment pack.
     *
     * @param parent parent experiment pack
     * @return the first experiment in the stopped state under the specified parent experiment pack
     */
    Experiment getFirstStoppedExperimentByParent(ExperimentPack parent);

    /**
     * Get all experiment packs.
     *
     * @return all experiment packs
     */
    List<ExperimentPack> getAllExperimentPacks();

    /**
     * Get all experiment packs by offset and count.
     *
     * @param first offset
     * @param count count
     * @return all experiment packs by offset and count
     */
    List<ExperimentPack> getAllExperimentPacks(long first, long count);

    /**
     * Get the experiment pack by id.
     *
     * @param id the experiment pack's id
     * @return the experiment pack matching the specified id
     */
    ExperimentPack getExperimentPackById(long id);

    /**
     * Get the experiment pack by title.
     *
     * @param title the experiment pack's title
     * @return the experiment pack matching the specified title
     */
    ExperimentPack getExperimentPackByTitle(String title);

    /**
     * Get the experiment packs by benchmark.
     *
     * @param benchmark benchmark
     * @return experiment packs
     */
    List<ExperimentPack> getExperimentPacksByBenchmark(Benchmark benchmark);

    /**
     * Get the first experiment pack by benchmark.
     *
     * @param benchmark benchmark
     * @return experiment pack
     */
    ExperimentPack getFirstExperimentPackByBenchmark(Benchmark benchmark);

    /**
     * Get first experiment pack.
     *
     * @return experiment pack
     */
    ExperimentPack getFirstExperimentPack();

    /**
     * Add an experiment pack.
     *
     * @param experimentPack the experiment pack to be added
     */
    void addExperimentPack(ExperimentPack experimentPack);

    /**
     * Remove the experiment pack by id.
     *
     * @param id the id of the experiment pack which is to be removed
     */
    void removeExperimentPackById(long id);

    /**
     * Update the experiment pack.
     *
     * @param experimentPack the experiment pack
     */
    void updateExperimentPack(ExperimentPack experimentPack);

    /**
     * Add an experiment specification.
     *
     * @param experimentSpec the experiment specification to be added
     */
    void addExperimentSpec(ExperimentSpec experimentSpec);

    /**
     * Remove the experiment specification by id.
     *
     * @param id the experiment specification's id
     */
    void removeExperimentSpecById(long id);

    /**
     * Get the experiment specification by parent experiment pack.
     *
     * @param parent the parent experiment pack
     * @return the experiment specification owned by the specified parent experiment pack
     */
    ExperimentSpec getExperimentSpecByParent(ExperimentPack parent);

    /**
     * Get the number of experiments by the parent experiment pack.
     *
     * @param parent the parent experiment pack
     * @return the number of experiments owned by the parent experiment pack
     */
    long getNumExperimentsByParent(ExperimentPack parent);

    /**
     * Get the number of experiments by the parent experiment pack and state.
     *
     * @param parent the parent experiment pack
     * @param experimentState the experiment's state
     * @return the number of experiments in the specified state and owned by the specified parent experiment pack
     */
    long getNumExperimentsByParentAndState(ExperimentPack parent, ExperimentState experimentState);

    /**
     * Start the specified experiment pack.
     *
     * @param experimentPack the experiment pack to be started
     */
    void startExperimentPack(ExperimentPack experimentPack);

    /**
     * Stop the specified experiment pack.
     *
     * @param experimentPack the experiment pack to be stopped
     */
    void stopExperimentPack(ExperimentPack experimentPack);

    /**
     * Stop the completed experiments owned by the parent experiment pack to the pending state.
     *
     * @param parent the parent experiment pack
     */
    void resetCompletedExperimentsByParent(ExperimentPack parent);

    /**
     * Reset the aborted experiments owned by the parent experiment pack to the pending state.
     *
     * @param parent the parent experiment pack
     */
    void resetAbortedExperimentsByParent(ExperimentPack parent);

    /**
     * Start the experiment.
     *
     * @param experiment the experiment to be started.
     */
    void startExperiment(Experiment experiment);

    /**
     *  Start the experiment runner.
     */
    void start();
}
