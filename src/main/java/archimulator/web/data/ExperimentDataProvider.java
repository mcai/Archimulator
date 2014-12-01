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
package archimulator.web.data;

import archimulator.model.Experiment;
import archimulator.model.ExperimentPack;
import archimulator.service.ServiceManager;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import java.util.Iterator;

/**
 * Experiment data provider.
 *
 * @author Min Cai
 */
public class ExperimentDataProvider implements IDataProvider<Experiment> {
    private final long experimentPackId;

    /**
     * Create an experiment data provider.
     *
     * @param experimentPackId the experiment pack ID
     */
    public ExperimentDataProvider(long experimentPackId) {
        this.experimentPackId = experimentPackId;
    }

    @Override
    public Iterator<? extends Experiment> iterator(long first, long count) {
        if (experimentPackId != -1) {
            ExperimentPack experimentPack = ServiceManager.getExperimentService().getExperimentPackById(experimentPackId);
            return ServiceManager.getExperimentService().getExperimentsByParent(experimentPack, first, count).iterator();
        }

        return ServiceManager.getExperimentService().getAllExperiments(first, count).iterator();
    }

    @Override
    public long size() {
        if (experimentPackId != -1) {
            ExperimentPack experimentPack = ServiceManager.getExperimentService().getExperimentPackById(experimentPackId);
            return ServiceManager.getExperimentService().getNumExperimentsByParent(experimentPack);
        }

        return ServiceManager.getExperimentService().getNumAllExperiments();
    }

    @Override
    public IModel<Experiment> model(final Experiment object) {
        return new LoadableDetachableModel<Experiment>(object) {
            @Override
            protected Experiment load() {
                return object;
            }
        };
    }

    @Override
    public void detach() {
    }
}
