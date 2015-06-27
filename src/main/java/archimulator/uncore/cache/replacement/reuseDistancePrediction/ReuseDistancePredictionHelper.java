/*******************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.uncore.cache.replacement.reuseDistancePrediction;

import archimulator.common.Simulation;
import archimulator.common.report.ReportNode;
import archimulator.common.report.Reportable;
import archimulator.uncore.cache.EvictableCache;
import archimulator.uncore.cache.prediction.CacheBasedPredictor;
import archimulator.uncore.cache.prediction.Predictor;
import archimulator.uncore.coherence.event.GeneralCacheControllerServiceNonblockingRequestEvent;
import archimulator.uncore.coherence.msi.state.DirectoryControllerState;
import archimulator.util.math.Quantizer;

/**
 * Reuse distance prediction helper.
 *
 * @author Min Cai
 */
public class ReuseDistancePredictionHelper implements Reportable {
    private Quantizer reuseDistanceQuantizer;

    private Predictor<Integer> reuseDistancePredictor;

    private ReuseDistanceSampler reuseDistanceSampler;

    /**
     * Create a reuse distance prediction helper.
     *
     * @param simulation the simulation
     */
    public ReuseDistancePredictionHelper(Simulation simulation) {
        final EvictableCache<DirectoryControllerState> cache = simulation.getProcessor().getMemoryHierarchy().getL2Controller().getCache();

        this.reuseDistanceQuantizer = new Quantizer(15, 8192);

        this.reuseDistancePredictor = new CacheBasedPredictor<>(
                cache,
                cache.getName() + ".reuseDistancePredictor",
                16,
                0,
                3,
                0
        );

        this.reuseDistanceSampler = new ReuseDistanceSampler(
                cache,
                cache.getName() + ".reuseDistanceSampler",
                4096,
                (reuseDistanceQuantizer.getMaxValue() + 1) * reuseDistanceQuantizer.getQuantum(),
                reuseDistanceQuantizer
        );

        cache.getBlockingEventDispatcher().addListener(GeneralCacheControllerServiceNonblockingRequestEvent.class, event -> {
            if (event.getCacheController().getCache() == cache) {
                reuseDistanceSampler.update(event.getAccess().getThread().getId(), event.getAccess().getVirtualPc(), event.getTag());
            }
        });

        cache.getBlockingEventDispatcher().addListener(ReuseDistanceSampler.ReuseDistanceSampledEvent.class, event -> {
            if (event.getSender() == reuseDistanceSampler) {
                reuseDistancePredictor.update(event.getPc(), (int) (Math.log(event.getReuseDistance()) / Math.log(2)));
            }
        });
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, "reuseDistancePredictionHelper") {{
            getReuseDistancePredictor().dumpStats(this);
        }});
    }

    /**
     * Get the reuse distance quantizer.
     *
     * @return the reuse distance quantizer
     */
    public Quantizer getReuseDistanceQuantizer() {
        return reuseDistanceQuantizer;
    }

    /**
     * Get the reuse distance predictor.
     *
     * @return the reuse distance predictor
     */
    public Predictor<Integer> getReuseDistancePredictor() {
        return reuseDistancePredictor;
    }

    /**
     * Get the reuse distance sampler.
     *
     * @return the reuse distance sampler
     */
    public ReuseDistanceSampler getReuseDistanceSampler() {
        return reuseDistanceSampler;
    }
}
