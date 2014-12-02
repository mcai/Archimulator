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
package archimulator.util.ai.aco;

import net.pickapack.util.Pair;
import org.apache.commons.math3.ml.distance.EuclideanDistance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Abstract ant colony optimization helper.
 *
 * @author Min Cai
 */
public abstract class AbstractAntColonyOptimizationHelper {
    private EuclideanDistance euclideanDistance;

    protected AbstractAntColonyOptimizationHelper() {
        this.euclideanDistance = new EuclideanDistance();
    }

    protected double[][] readMatrixFromFile(String fileName) throws IOException {
        final BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));

        final List<Pair<Double, Double>> records = new LinkedList<>();

        boolean readAhead = false;
        String line;
        while ((line = br.readLine()) != null) {

            if (line.equals("EOF")) {
                break;
            }

            if (readAhead) {
                String[] split = sweepNumbers(line.trim());
                records.add(new Pair<>(Double.parseDouble(split[1].trim()), Double
                        .parseDouble(split[2].trim())));
            }

            if (line.equals("NODE_COORD_SECTION")) {
                readAhead = true;
            }
        }

        br.close();

        final double[][] localMatrix = new double[records.size()][records.size()];

        int rIndex = 0;
        for (Pair<Double, Double> r : records) {
            int hIndex = 0;
            for (Pair<Double, Double> h : records) {
                localMatrix[rIndex][hIndex] = euclideanDistance.compute(new double[]{r.getFirst(), r.getSecond()}, new double[]{h.getFirst(), h.getSecond()});
                hIndex++;
            }
            rIndex++;
        }

        return localMatrix;
    }

    private String[] sweepNumbers(String trim) {
        String[] arr = new String[3];
        int currentIndex = 0;
        for (int i = 0; i < trim.length(); i++) {
            final char c = trim.charAt(i);
            if ((c) != 32) {
                for (int f = i + 1; f < trim.length(); f++) {
                    final char x = trim.charAt(f);
                    if (x == 32) {
                        arr[currentIndex] = trim.substring(i, f);
                        currentIndex++;
                        break;
                    } else if (f == trim.length() - 1) {
                        arr[currentIndex] = trim.substring(i, trim.length());
                        break;
                    }
                }
                i = i + arr[currentIndex - 1].length();
            }
        }
        return arr;
    }
}
