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
package archimulator.service.impl;

import archimulator.model.Experiment;
import archimulator.service.ExperimentReportService;
import archimulator.service.ServiceManager;
import archimulator.sim.common.report.ReportNode;
import net.pickapack.model.WithId;
import net.pickapack.service.AbstractService;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * Experiment report service implementation.
 *
 * @author Min Cai
 */
public class ExperimentReportServiceImpl extends AbstractService implements ExperimentReportService {
    private Map<Long, ReportNode> reportNodes;

    /**
     * Create an experiment report service implementation.
     */
    @SuppressWarnings("unchecked")
    public ExperimentReportServiceImpl() {
        super(ServiceManager.getDatabaseUrl(), Arrays.<Class<? extends WithId>>asList());

        this.reportNodes = new TreeMap<Long, ReportNode>();
    }

    @Override
    public void initialize() {
    }

    @Override
    public void setReportNodeByParent(Experiment parent, ReportNode reportNode) {
        this.reportNodes.put(parent.getId(), reportNode);
    }

    @Override
    public ReportNode getReportNodeByParent(Experiment parent) {
        return this.reportNodes.containsKey(parent.getId()) ? this.reportNodes.get(parent.getId()) : null;
    }

    @Override
    public ReportNode getReportNodeByParent(Experiment parent, String... path) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    //TODO: add (1) in-memory storage for when the experiment is running and (2) XML based persistence for when the experiment is stopped
}
