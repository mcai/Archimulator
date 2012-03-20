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
package archimulator.sim.ext.uncore.cache.eviction;

import archimulator.sim.ext.uncore.cache.prediction.CacheBasedPredictor;
import archimulator.sim.ext.uncore.cache.prediction.Predictor;
import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.cache.Cache;
import archimulator.sim.uncore.cache.CacheGeometry;
import archimulator.util.math.Quantizer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RDMonitor {
    private Predictor<Integer> predictor;
    private RDSampler sampler;

    public RDMonitor(Cache<?, ?> cache, Quantizer quantizerRd) {
        this.predictor = new CacheBasedPredictor<Integer>(cache, cache.getName() + ".rdPredictor", new CacheGeometry(16 * 16 * cache.getGeometry().getLineSize(), 16, cache.getGeometry().getLineSize()), 0, 3);
        this.sampler = new RDSampler(4096, (quantizerRd.getMaxValue() + 1) * quantizerRd.getQuantum(), quantizerRd);
    }

    public void update(int pc, int address, CacheAccessType accessType) {
        this.sampler.update(pc, address, accessType);
    }

    public int lookup(int pc) {
        return this.predictor.predict(pc, 0);
    }

    private class RDSampler implements Serializable {
        private List<RDSamplerEntry> entries;

        private int samplingPeriod;
        private int samplingCounter;

        private Quantizer quantizerRd;

        private RDSampler(int samplingPeriod, int maxReuseDistance, Quantizer quantizerRd) {
            this.samplingPeriod = samplingPeriod;

            this.quantizerRd = quantizerRd;

            this.samplingCounter = 0;

            this.entries = new ArrayList<RDSamplerEntry>();
            for (int i = 0; i < maxReuseDistance / this.samplingPeriod; i++) {
                this.entries.add(new RDSamplerEntry());
            }
        }

        private void update(int pc, int address, CacheAccessType accessType) {
            for (int i = 0; i < this.entries.size(); i++) {
                RDSamplerEntry entry = this.entries.get(i);
                if (entry.valid && entry.address == address) {
                    entry.valid = false;
                    int reuseDistance = (i + (accessType.isDownwardWrite() ? 8 : 0)) * this.samplingPeriod;
                    predictor.update(entry.pc, this.quantizerRd.quantize(reuseDistance));
                    break;
                }
            }

            if (this.samplingCounter == 0) {
                RDSamplerEntry victimEntry = this.entries.get(this.entries.size() - 1);
                if (victimEntry.valid) {
                    predictor.update(victimEntry.pc, this.quantizerRd.getMaxValue());
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

        private class RDSamplerEntry implements Serializable {
            private boolean valid;
            private int pc;
            private int address;
        }
    }
}
