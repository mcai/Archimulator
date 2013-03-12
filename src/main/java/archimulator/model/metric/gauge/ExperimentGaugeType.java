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
package archimulator.model.metric.gauge;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.model.WithCreateTime;
import net.pickapack.model.WithId;
import net.pickapack.model.WithTitle;

import java.util.Date;

/**
 * Experiment gauge type.
 *
 * @author Min Cai
 */
@DatabaseTable(tableName = "ExperimentGaugeType")
public class ExperimentGaugeType implements WithId, WithTitle, WithCreateTime {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField
    private String title;

    @DatabaseField
    private long createTime;

    @DatabaseField
    private String nodeExpression;

    @DatabaseField
    private String keyExpression;

    @DatabaseField
    private boolean multipleNodes;

    /**
     * Create an experiment gauge type. Reserved for ORM only.
     */
    public ExperimentGaugeType() {
    }

    /**
     * Create an experiment gauge type.
     *
     * @param title          the title of the gauge type
     * @param nodeExpression the node expression
     * @param keyExpression  the key expression
     */
    public ExperimentGaugeType(String title, String nodeExpression, String keyExpression) {
        this.title = title;
        this.nodeExpression = nodeExpression;
        this.keyExpression = keyExpression;
        this.createTime = DateHelper.toTick(new Date());
    }

    /**
     * Get the experiment gauge type's ID.
     *
     * @return the experiment gauge type's ID
     */
    @Override
    public long getId() {
        return id;
    }

    /**
     * Get the experiment gauge type's title.
     *
     * @return the experiment gauge type's title
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * Get the time in ticks when the experiment gauge type is created.
     *
     * @return the time in ticks when the experiment gauge type is created
     */
    @Override
    public long getCreateTime() {
        return createTime;
    }

    /**
     * Get the node expression.
     *
     * @return the node expression
     */
    public String getNodeExpression() {
        return nodeExpression;
    }

    /**
     * Get the key expression.
     *
     * @return the key expression
     */
    public String getKeyExpression() {
        return keyExpression;
    }

    /**
     * Set the key expression.
     *
     * @param keyExpression the key expression
     */
    public void setKeyExpression(String keyExpression) {
        this.keyExpression = keyExpression;
    }

    /**
     * Set the node expression.
     *
     * @param nodeExpression the node expression
     */
    public void setNodeExpression(String nodeExpression) {
        this.nodeExpression = nodeExpression;
    }

    /**
     * Get a value indicating whether it is for multiple nodes or not.
     *
     * @return a value indicating whether it is for multiple nodes or not
     */
    public boolean isMultipleNodes() {
        return multipleNodes;
    }

    /**
     * Set a value indicating whether it is for multiple nodes or not.
     *
     * @param multipleNodes a value indicating whether it is for multiple nodes or not
     */
    public void setMultipleNodes(boolean multipleNodes) {
        this.multipleNodes = multipleNodes;
    }

    @Override
    public String toString() {
        return String.format("ExperimentGaugeType{id=%d, title='%s', createTime=%s, nodeExpression='%s', keyExpression='%s', multipleNodes=%s}", id, title, DateHelper.toString(createTime), nodeExpression, keyExpression, multipleNodes);
    }

    /**
     * Runtime.
     */
    public static String RUNTIME = "runtime";

    /**
     * Simulation.
     */
    public static String SIMULATION = "simulation";

    /**
     * Main memory.
     */
    public static String MAIN_MEMORY = "mainMemory";

    /**
     * Processor Core.
     */
    public static String CORE = "core";

    /**
     * Thread.
     */
    public static String THREAD = "thread";

    /**
     * Translation lookaside buffer (TLB).
     */
    public static String TLB = "tlb";

    /**
     * Cache controller.
     */
    public static String CACHE_CONTROLLER = "cacheController";

    /**
     * Memory controller.
     */
    public static String MEMORY_CONTROLLER = "memoryController";

    /**
     * Stack distance.
     */
    public static String STACK_DISTANCE = "stackDistance";

    /**
     * Hotspot function.
     */
    public static String HOTSPOT = "hotspot";

    /**
     * Helper thread.
     */
    public static String HELPER_THREAD = "helperThread";

    /**
     * Cache interaction.
     */
    public static String CACHE_INTERACTION = "cacheInteraction";

    /**
     * Feedback directed helper threading.
     */
    public static String FEEDBACK_DIRECTED_HELPER_THREADING = "feedbackDirectedHelperThreading";

    /**
     * MLP aware cache partitioning.
     */
    public static String MLP_AWARE_CACHE_PARTITIONING = "mlpAwareCachePartitioning";
}
