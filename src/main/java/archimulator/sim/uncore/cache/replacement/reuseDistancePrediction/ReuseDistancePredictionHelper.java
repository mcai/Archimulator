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
package archimulator.sim.uncore.cache.replacement.reuseDistancePrediction;

import archimulator.sim.common.Simulation;
import archimulator.sim.common.report.ReportNode;
import archimulator.sim.common.report.Reportable;
import archimulator.sim.uncore.cache.CacheGeometry;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.prediction.CacheBasedPredictor;
import archimulator.sim.uncore.cache.prediction.Predictor;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerServiceNonblockingRequestEvent;
import archimulator.sim.uncore.coherence.msi.state.DirectoryControllerState;
import net.pickapack.action.Action1;
import net.pickapack.math.Quantizer;

/**
 * Reuse distance prediction helper.
 *
 * @author Min Cai
 */
public class ReuseDistancePredictionHelper implements Reportable {
    private Quantizer reuseDistanceQuantizer;

    private Predictor<Integer> reuseDistancePredictor;

    private Predictor<Integer> helperThreadL2RequestReuseDistancePredictor;

    private HelperThreadAwareReuseDistanceSampler reuseDistanceSampler;

    /**
     * Create a reuse distance prediction helper.
     * @param simulation  the simulation
     */
    public ReuseDistancePredictionHelper(Simulation simulation) {
        final EvictableCache<DirectoryControllerState> cache = simulation.getProcessor().getMemoryHierarchy().getL2CacheController().getCache();

        this.reuseDistanceQuantizer = new Quantizer(15, 8192);

        this.reuseDistancePredictor = new CacheBasedPredictor<Integer>(cache, cache.getName() + ".reuseDistancePredictor", new CacheGeometry(16 * 16 * cache.getGeometry().getLineSize(), 16, cache.getGeometry().getLineSize()), 0, 3, 0);

        this.helperThreadL2RequestReuseDistancePredictor = new CacheBasedPredictor<Integer>(cache, cache.getName() + ".helperThreadL2RequestReuseDistancePredictor", new CacheGeometry(16 * 16 * cache.getGeometry().getLineSize(), 16, cache.getGeometry().getLineSize()), 0, 3, 0);

        this.reuseDistanceSampler = new HelperThreadAwareReuseDistanceSampler(cache, cache.getName() + ".reuseDistanceSampler", 4096, (reuseDistanceQuantizer.getMaxValue() + 1) * reuseDistanceQuantizer.getQuantum(), reuseDistanceQuantizer);

        cache.getBlockingEventDispatcher().addListener(GeneralCacheControllerServiceNonblockingRequestEvent.class, new Action1<GeneralCacheControllerServiceNonblockingRequestEvent>() {
            @Override
            public void apply(GeneralCacheControllerServiceNonblockingRequestEvent event) {
                if(event.getCacheController().getCache() == cache) {
                    reuseDistanceSampler.update(event.getAccess().getThread().getId(), event.getAccess().getVirtualPc(), event.getTag());
                }
            }
        });

        cache.getBlockingEventDispatcher().addListener(ReuseDistanceSampler.ReuseDistanceSampledEvent.class, new Action1<ReuseDistanceSampler.ReuseDistanceSampledEvent>() {
            @Override
            public void apply(ReuseDistanceSampler.ReuseDistanceSampledEvent event) {
                if (event.getSender() == reuseDistanceSampler) {
                    reuseDistancePredictor.update(event.getPc(), event.getReuseDistance());
                }
            }
        });

        cache.getBlockingEventDispatcher().addListener(HelperThreadAwareReuseDistanceSampler.HelperThreadL2RequestReuseDistanceSampledEvent.class, new Action1<HelperThreadAwareReuseDistanceSampler.HelperThreadL2RequestReuseDistanceSampledEvent>() {
            @Override
            public void apply(HelperThreadAwareReuseDistanceSampler.HelperThreadL2RequestReuseDistanceSampledEvent event) {
                if (event.getSender() == reuseDistanceSampler) {
                    helperThreadL2RequestReuseDistancePredictor.update(event.getPc(), event.getReuseDistance());
                }
            }
        });
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, "reuseDistancePredictionHelper") {{
            getReuseDistancePredictor().dumpStats(this);
            getHelperThreadL2RequestReuseDistancePredictor().dumpStats(this);
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
     * Get the helper thread L2 request reuse distance predictor.
     *
     * @return the helper thread L2 request reuse distance predictor
     */
    public Predictor<Integer> getHelperThreadL2RequestReuseDistancePredictor() {
        return helperThreadL2RequestReuseDistancePredictor;
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
