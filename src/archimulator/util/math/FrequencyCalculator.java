/*******************************************************************************
 * Copyright (c) 2010-2011 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.util.math;

import archimulator.util.Triple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FrequencyCalculator<SampleT extends Comparable<SampleT>> {
    private List<SampleT> samples;

    public FrequencyCalculator() {
        this.samples = new ArrayList<SampleT>();
    }

    public void addSample(SampleT sample) {
        this.samples.add(sample);
    }

    public List<Triple<SampleT, Integer, Float>> getFrequencies() {
        List<Triple<SampleT, Integer, Float>> frequencies = new ArrayList<Triple<SampleT, Integer, Float>>();

        int numSamples = this.samples.size();

        for (SampleT sample : this.samples) {
            int frequency = Collections.frequency(this.samples, sample);
            frequencies.add(new Triple<SampleT, Integer, Float>(sample, frequency, (float) frequency / numSamples));
        }

        Collections.sort(frequencies, new Comparator<Triple<SampleT, Integer, Float>>() {
            public int compare(Triple<SampleT, Integer, Float> o1, Triple<SampleT, Integer, Float> o2) {
                return o1.getSecond().compareTo(o2.getSecond());
            }
        });

        Collections.reverse(frequencies);

        return frequencies;
    }
}
