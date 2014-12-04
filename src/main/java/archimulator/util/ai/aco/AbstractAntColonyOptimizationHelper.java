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

import cern.jet.random.Uniform;
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
    // greedy
    public static final double ALPHA = -0.2d;
    // rapid selection
    public static final double BETA = 9.6d;

    // heuristic parameters
    public static final double Q = 0.0001d; // somewhere between 0 and 1
    public static final double PHEROMONE_PERSISTENCE = 0.3d; // between 0 and 1
    public static final double INITIAL_PHEROMONES = 0.8d; // can be anything

    protected final double[][] matrix;
    protected final double[][] invertedMatrix;

    protected final double[][] pheromones;

    private EuclideanDistance euclideanDistance;

    private Uniform uniform;

    protected AbstractAntColonyOptimizationHelper(String fileName) throws IOException {
        this.euclideanDistance = new EuclideanDistance();

        this.matrix = readMatrixFromFile(fileName);
        this.invertedMatrix = invertMatrix();
        this.pheromones = initializePheromones();

        // (double min, double max, int seed)
        this.uniform = new Uniform(0, matrix.length - 1, (int) System.currentTimeMillis());
    }

    final double readPheromone(int x, int y) {
        return pheromones[x][y];
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
                String[] split = line.trim().split(" ");
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

    private double[][] initializePheromones() {
        final double[][] localMatrix = new double[matrix.length][matrix.length];
        int rows = matrix.length;
        for (int columns = 0; columns < matrix.length; columns++) {
            for (int i = 0; i < rows; i++) {
                localMatrix[columns][i] = INITIAL_PHEROMONES;
            }
        }

        return localMatrix;
    }

    private double[][] invertMatrix() {
        double[][] local = new double[matrix.length][matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                local[i][j] = invertDouble(matrix[i][j]);
            }
        }
        return local;
    }

    private double invertDouble(double distance) {
        return distance == 0d ? 0d : 1.0d / distance;
    }

    protected int getGaussianDistributionRowIndex() {
        return uniform.nextInt();
    }
}
