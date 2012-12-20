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
package archimulator.service.impl;

import archimulator.model.Experiment;
import archimulator.model.metric.ExperimentGauge;
import archimulator.model.metric.ExperimentGaugeType;
import archimulator.service.ExperimentMetricService;
import archimulator.service.ServiceManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import net.pickapack.model.ModelElement;
import net.pickapack.service.AbstractService;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Min Cai
 */
public class ExperimentMetricServiceImpl extends AbstractService implements ExperimentMetricService {
    private Dao<ExperimentGaugeType, Long> gaugeTypes;
    private Dao<ExperimentGauge, Long> gauges;

    /**
     *
     */
    @SuppressWarnings("unchecked")
    public ExperimentMetricServiceImpl() {
        super(ServiceManager.getDatabaseUrl(), Arrays.<Class<? extends ModelElement>>asList(ExperimentGaugeType.class, ExperimentGauge.class));

        this.gaugeTypes = createDao(ExperimentGaugeType.class);
        this.gauges = createDao(ExperimentGauge.class);
    }

    @Override
    public void initialize() {
        if (this.getFirstGaugeType() == null) {
            this.addGaugeType(new ExperimentGaugeType(ExperimentGaugeType.RUNTIME, "runtimeHelper", ""));
            this.addGaugeType(new ExperimentGaugeType(ExperimentGaugeType.SIMULATION, "", ""));
            this.addGaugeType(new ExperimentGaugeType(ExperimentGaugeType.MAIN_MEMORY, "processor/kernel/memories", "id") {
                {
                    setMultipleNodes(true);
                }
            });
            this.addGaugeType(new ExperimentGaugeType(ExperimentGaugeType.CORE, "processor/cores", "name") {
                {
                    setMultipleNodes(true);
                }
            });
            this.addGaugeType(new ExperimentGaugeType(ExperimentGaugeType.THREAD, "processor/threads", "name") {
                {
                    setMultipleNodes(true);
                }
            });
            this.addGaugeType(new ExperimentGaugeType(ExperimentGaugeType.TLB, "processor/memoryHierarchy/tlbs", "name") {
                {
                    setMultipleNodes(true);
                }
            });
            this.addGaugeType(new ExperimentGaugeType(ExperimentGaugeType.CACHE_CONTROLLER, "processor/memoryHierarchy/cacheControllers", "name") {
                {
                    setMultipleNodes(true);
                }
            });
            this.addGaugeType(new ExperimentGaugeType(ExperimentGaugeType.MEMORY_CONTROLLER, "processor/memoryHierarchy/memoryController", ""));
            this.addGaugeType(new ExperimentGaugeType(ExperimentGaugeType.HOTSPOT, "hotspotProfilingHelper", ""));
            this.addGaugeType(new ExperimentGaugeType(ExperimentGaugeType.HELPER_THREAD, "helperThreadL2CacheRequestProfilingHelper", ""));
        }

        if (this.getFirstGauge() == null) {
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.SIMULATION), "beginTimeAsString"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.SIMULATION), "endTimeAsString"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.SIMULATION), "duration"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.SIMULATION), "durationInSeconds"));

            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.RUNTIME), "maxMemory"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.RUNTIME), "totalMemory"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.RUNTIME), "usedMemory"));

            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.SIMULATION), "cycleAccurateEventQueue/currentCycle"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.SIMULATION), "totalInstructions"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.SIMULATION), "instructionsPerCycle"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.SIMULATION), "cyclesPerInstruction"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.SIMULATION), "cyclesPerSecond"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.SIMULATION), "instructionsPerSecond"));

            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.MAIN_MEMORY), "numPages"));

            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.THREAD), "totalInstructions"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.THREAD), "executedMnemonics"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.THREAD), "executedSystemCalls"));

            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.CORE), "functionalUnitPool/noFreeFunctionalUnit"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.CORE), "functionalUnitPool/acquireFailedOnNoFreeFunctionalUnit"));

            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.TLB), "hitRatio"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.TLB), "numAccesses"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.TLB), "numHits"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.TLB), "numMisses"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.TLB), "numEvictions"));

            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.THREAD), "branchPredictor/type"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.THREAD), "branchPredictor/hitRatio"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.THREAD), "branchPredictor/numAccesses"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.THREAD), "branchPredictor/numHits"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.THREAD), "branchPredictor/numMisses"));

            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.THREAD), "decodeBufferFull"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.THREAD), "reorderBufferFull"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.THREAD), "loadStoreQueueFull"));

            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.THREAD), "intPhysicalRegisterFileFull"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.THREAD), "fpPhysicalRegisterFileFull"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.THREAD), "miscPhysicalRegisterFileFull"));

            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.THREAD), "fetchStallsOnDecodeBufferIsFull"));

            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.THREAD), "registerRenameStallsOnDecodeBufferIsEmpty"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.THREAD), "registerRenameStallsOnReorderBufferIsFull"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.THREAD), "registerRenameStallsOnLoadStoreQueueFull"));

            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.THREAD), "selectionStallOnCanNotLoad"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.THREAD), "selectionStallOnCanNotStore"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.THREAD), "selectionStallOnNoFreeFunctionalUnit"));

            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.CACHE_CONTROLLER), "hitRatio"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.CACHE_CONTROLLER), "numDownwardAccesses"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.CACHE_CONTROLLER), "numDownwardHits"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.CACHE_CONTROLLER), "numDownwardMisses"));

            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.CACHE_CONTROLLER), "numDownwardReadHits"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.CACHE_CONTROLLER), "numDownwardReadMisses"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.CACHE_CONTROLLER), "numDownwardWriteHits"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.CACHE_CONTROLLER), "numDownwardWriteMisses"));

            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.CACHE_CONTROLLER), "numEvictions"));

            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.CACHE_CONTROLLER), "occupancyRatio"));

            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.MEMORY_CONTROLLER), "numAccesses"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.MEMORY_CONTROLLER), "numReads"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.MEMORY_CONTROLLER), "numWrites"));

            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.HOTSPOT), "numCallsPerFunctions"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.HOTSPOT), "loadsInHotspotFunction"));

            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.HOTSPOT), "statL2CacheHitReuseDistances"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.HOTSPOT), "statL2CacheMissReuseDistances"));

            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.HELPER_THREAD), "numMainThreadL2CacheHits"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.HELPER_THREAD), "numMainThreadL2CacheMisses"));

            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.HELPER_THREAD), "numHelperThreadL2CacheHits"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.HELPER_THREAD), "numHelperThreadL2CacheMisses"));

            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.HELPER_THREAD), "numTotalHelperThreadL2CacheRequests"));

            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.HELPER_THREAD), "numRedundantHitToTransientTagHelperThreadL2CacheRequests"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.HELPER_THREAD), "numRedundantHitToCacheHelperThreadL2CacheRequests"));

            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.HELPER_THREAD), "numUsefulHelperThreadL2CacheRequests"));

            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.HELPER_THREAD), "numTimelyHelperThreadL2CacheRequests"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.HELPER_THREAD), "numLateHelperThreadL2CacheRequests"));

            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.HELPER_THREAD), "numBadHelperThreadL2CacheRequests"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.HELPER_THREAD), "numUglyHelperThreadL2CacheRequests"));

            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.HELPER_THREAD), "helperThreadL2CacheRequestCoverage"));
            this.addGauge(new ExperimentGauge(getGaugeTypeByTitle(ExperimentGaugeType.HELPER_THREAD), "helperThreadL2CacheRequestAccuracy"));

            //TODO: integrate the above gauges into Simulation and expose their management and usages in experiments via web UI.
        }
    }

    @Override
    public List<ExperimentGaugeType> getAllGaugeTypes() {
        return this.getAllItems(this.gaugeTypes);
    }

    @Override
    public ExperimentGaugeType getGaugeTypeById(long id) {
        return this.getItemById(this.gaugeTypes, id);
    }

    @Override
    public ExperimentGaugeType getGaugeTypeByTitle(String title) {
        return this.getFirstItemByTitle(this.gaugeTypes, title);
    }

    @Override
    public ExperimentGaugeType getFirstGaugeType() {
        return this.getFirstItem(this.gaugeTypes);
    }

    @Override
    public void addGaugeType(ExperimentGaugeType gaugeType) {
        this.addItem(this.gaugeTypes, gaugeType);
    }

    @Override
    public void removeGaugeTypeById(long id) {
        this.removeItemById(this.gaugeTypes, id);
    }

    @Override
    public void updateGaugeType(ExperimentGaugeType gaugeType) {
        this.updateItem(this.gaugeTypes, gaugeType);
    }

    /**
     * @return
     */
    @Override
    public List<ExperimentGauge> getAllGauges() {
        return this.getAllItems(this.gauges);
    }

    /**
     * @param id
     * @return
     */
    @Override
    public ExperimentGauge getGaugeById(long id) {
        return this.getItemById(this.gauges, id);
    }

    /**
     * @param title
     * @return
     */
    @Override
    public ExperimentGauge getGaugeByTitle(String title) {
        return this.getFirstItemByTitle(this.gauges, title);
    }

    /**
     * @param type
     * @return
     */
    @Override
    public List<ExperimentGauge> getGaugesByType(ExperimentGaugeType type) {
        try {
            PreparedQuery<ExperimentGauge> query = this.gauges.queryBuilder().where().eq("typeId", type.getId()).prepare();
            return this.gauges.query(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ExperimentGauge> getGaugesByExperiment(Experiment experiment) {
        try {
            PreparedQuery<ExperimentGauge> query = this.gauges.queryBuilder().where().in("id", experiment.getGaugeIds()).prepare();
            return this.gauges.query(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ExperimentGauge> getGaugesByExperimentAndType(Experiment experiment, ExperimentGaugeType type) {
        try {
            PreparedQuery<ExperimentGauge> query = this.gauges.queryBuilder().where().in("id", experiment.getGaugeIds()).and().eq("typeId", type.getId()).prepare();
            return this.gauges.query(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ExperimentGauge> getGaugesByExperimentAndType(Experiment experiment, String typeTitle) {
        return getGaugesByExperimentAndType(experiment, getGaugeTypeByTitle(typeTitle));
    }

    /**
     * @return
     */
    @Override
    public ExperimentGauge getFirstGauge() {
        return this.getFirstItem(this.gauges);
    }

    /**
     * @param gauge
     */
    @Override
    public void addGauge(ExperimentGauge gauge) {
        this.addItem(this.gauges, gauge);
    }

    /**
     * @param id
     */
    @Override
    public void removeGaugeById(long id) {
        this.removeItemById(this.gauges, id);
    }

    /**
     * @param gauge
     */
    @Override
    public void updateGauge(ExperimentGauge gauge) {
        this.updateItem(this.gauges, gauge);
    }
}
