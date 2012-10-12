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
package archimulator.model.metric;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.model.ModelElement;

import java.util.Date;

/**
 *
 * @author Min Cai
 */
@DatabaseTable(tableName = "ExperimentGaugeType")
public class ExperimentGaugeType implements ModelElement {
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
     *
     */
    public ExperimentGaugeType() {
    }

    /**
     *
     * @param title
     * @param nodeExpression
     * @param keyExpression
     */
    public ExperimentGaugeType(String title, String nodeExpression, String keyExpression) {
        this.title = title;
        this.nodeExpression = nodeExpression;
        this.keyExpression = keyExpression;
        this.createTime = DateHelper.toTick(new Date());
    }

    /**
     *
     * @return
     */
    public long getId() {
        return id;
    }

    /**
     *
     * @return
     */
    @Override
    public long getParentId() {
        return -1;
    }

    /**
     *
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     *
     * @return
     */
    @Override
    public long getCreateTime() {
        return createTime;
    }

    /**
     *
     * @return
     */
    public String getNodeExpression() {
        return nodeExpression;
    }

    /**
     *
     * @return
     */
    public String getKeyExpression() {
        return keyExpression;
    }

    /**
     *
     * @param keyExpression
     */
    public void setKeyExpression(String keyExpression) {
        this.keyExpression = keyExpression;
    }

    /**
     *
     * @param nodeExpression
     */
    public void setNodeExpression(String nodeExpression) {
        this.nodeExpression = nodeExpression;
    }

    /**
     *
     * @return
     */
    public boolean isMultipleNodes() {
        return multipleNodes;
    }

    /**
     *
     * @param multipleNodes
     */
    public void setMultipleNodes(boolean multipleNodes) {
        this.multipleNodes = multipleNodes;
    }

    @Override
    public String toString() {
        return String.format("ExperimentGaugeType{id=%d, title='%s', createTime=%s, nodeExpression='%s', keyExpression='%s', multipleNodes=%s}", id, title, DateHelper.toString(createTime), nodeExpression, keyExpression, multipleNodes);
    }

    /**
     *
     */
    public static String RUNTIME = "runtime";
    /**
     *
     */
    public static String SIMULATION = "simulation";
    /**
     *
     */
    public static String MAIN_MEMORY = "mainMemory";
    /**
     *
     */
    public static String CORE = "core";
    /**
     *
     */
    public static String THREAD = "thread";
    /**
     *
     */
    public static String TLB = "tlb";
    /**
     *
     */
    public static String CACHE_CONTROLLER = "cacheController";
    /**
     *
     */
    public static String MEMORY_CONTROLLER = "memoryController";
    /**
     *
     */
    public static String HELPER_THREAD = "helperThread";
}
