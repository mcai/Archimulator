/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.web.component.experiment;

import archimulator.model.Experiment;
import archimulator.model.ExperimentSummary;
import archimulator.service.ServiceManager;
import net.pickapack.util.StorageUnit;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;

/**
 * Summary panel.
 *
 * @author Min Cai
 */
public class PanelSummary extends Panel {
    /**
     * Create a summary panel.
     *
     * @param id         the markup ID of the panel that is to be created
     * @param experiment the experiment
     */
    public PanelSummary(String id, Experiment experiment) {
        super(id);

        ExperimentSummary summary = ServiceManager.getExperimentStatService().getSummaryByParent(experiment);

        setDefaultModel(new CompoundPropertyModel<>(summary));

        this.add(new Label("id"));
        this.add(new Label("title"));

        this.add(new Label("type"));
        this.add(new Label("state"));

        this.add(new Label("beginTimeAsString"));
        this.add(new Label("endTimeAsString"));
        this.add(new Label("duration"));
        this.add(new Label("durationInSeconds"));

        this.add(new Label("l2Size", Model.of(StorageUnit.KILOBYTE.getValue(summary.getL2Size()) + "KB")));
        this.add(new Label("l2Associativity"));
        this.add(new Label("l2ReplacementPolicyType"));

        this.add(new Label("helperThreadLookahead"));
        this.add(new Label("helperThreadStride"));

        this.add(new Label("numInstructions"));
        this.add(new Label("c0t0NumInstructions"));
        this.add(new Label("c1t0NumInstructions"));
        this.add(new Label("numCycles"));

        this.add(new Label("ipc"));
        this.add(new Label("c0t0Ipc"));
        this.add(new Label("c1t0Ipc"));
        this.add(new Label("cpi"));

        this.add(new Label("numMainThreadL2Hits"));
        this.add(new Label("numMainThreadL2Misses"));
        this.add(new Label("numHelperThreadL2Hits"));
        this.add(new Label("numHelperThreadL2Misses"));

        this.add(new Label("numL2Evictions"));
        this.add(new Label("l2HitRatio"));
        this.add(new Label("l2OccupancyRatio"));

        this.add(new Label("helperThreadL2RequestCoverage"));
        this.add(new Label("helperThreadL2RequestAccuracy"));
        this.add(new Label("helperThreadL2RequestLateness"));
        this.add(new Label("helperThreadL2RequestPollution"));

        this.add(new Label("numLateHelperThreadL2Requests"));
        this.add(new Label("numTimelyHelperThreadL2Requests"));
        this.add(new Label("numBadHelperThreadL2Requests"));
        this.add(new Label("numEarlyHelperThreadL2Requests"));
        this.add(new Label("numUglyHelperThreadL2Requests"));
        this.add(new Label("numRedundantHitToTransientTagHelperThreadL2Requests"));
        this.add(new Label("numRedundantHitToCacheHelperThreadL2Requests"));
    }
}
