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
package archimulator.sim.uncore.cache.replacement.reuseDistance;

import archimulator.sim.uncore.cache.Cache;
import archimulator.sim.uncore.cache.CacheGeometry;
import archimulator.sim.uncore.cache.prediction.CacheBasedPredictor;
import archimulator.sim.uncore.cache.prediction.Predictor;
import net.pickapack.math.Quantizer;

import java.util.ArrayList;
import java.util.List;

/**
 * Reuse distance monitor.
 *
 * @author Min Cai
 */
public class ReuseDistanceMonitor {
    private Predictor<Integer> predictor;
    private ReuseDistanceSampler sampler;

    public ReuseDistanceMonitor(Cache<?> cache, Quantizer quantizerRd) {
        this.predictor = new CacheBasedPredictor<Integer>(cache, cache.getName() + ".rdPredictor", new CacheGeometry(16 * 16 * cache.getGeometry().getLineSize(), 16, cache.getGeometry().getLineSize()), 0, 3);
        this.sampler = new ReuseDistanceSampler(4096, (quantizerRd.getMaxValue() + 1) * quantizerRd.getQuantum(), quantizerRd);
    }

    public void update(int pc, int address) {
        this.sampler.update(pc, address);
    }

    public int lookup(int pc) {
        return this.predictor.predict(pc, 0);
    }

    private class ReuseDistanceSampler {
        private List<ReuseDistanceSamplerEntry> entries;

        private int samplingPeriod;
        private int samplingCounter;

        private Quantizer quantizerReuseDistance;

        private ReuseDistanceSampler(int samplingPeriod, int maxReuseDistance, Quantizer quantizerReuseDistance) {
            this.samplingPeriod = samplingPeriod;

            this.quantizerReuseDistance = quantizerReuseDistance;

            this.samplingCounter = 0;

            this.entries = new ArrayList<ReuseDistanceSamplerEntry>();
            for (int i = 0; i < maxReuseDistance / this.samplingPeriod; i++) {
                this.entries.add(new ReuseDistanceSamplerEntry());
            }
        }

        private void update(int pc, int address) {
            for (int i = 0; i < this.entries.size(); i++) {
                ReuseDistanceSamplerEntry entry = this.entries.get(i);
                if (entry.valid && entry.address == address) {
                    entry.valid = false;
                    int reuseDistance = (i + 8) * this.samplingPeriod;
                    predictor.update(entry.pc, this.quantizerReuseDistance.quantize(reuseDistance));
                    break;
                }
            }

            if (this.samplingCounter == 0) {
                ReuseDistanceSamplerEntry victimEntry = this.entries.get(this.entries.size() - 1);
                if (victimEntry.valid) {
                    predictor.update(victimEntry.pc, this.quantizerReuseDistance.getMaxValue());
                }

                this.entries.remove(victimEntry);
                this.entries.add(0, victimEntry);

                victimEntry.valid = true;
                victimEntry.pc = pc;
                victimEntry.address = address;

                this.samplingCounter = this.samplingPeriod - 1;
            } else {
                samplingCounter--;
            }
        }

        private class ReuseDistanceSamplerEntry {
            private boolean valid;
            private int pc;
            private int address;
        }
    }
}
