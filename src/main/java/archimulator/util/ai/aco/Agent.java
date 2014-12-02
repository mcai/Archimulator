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

import java.util.Random;
import java.util.concurrent.Callable;

/**
 * Ant Colony Optimization helper.
 *
 * @author Min Cai
 */
public final class Agent implements Callable<AntColonyOptimizationHelper.WalkedWay> {
    private final AntColonyOptimizationHelper instance;
    private double distanceWalked = 0.0d;
    private final int start;
    private final boolean[] visited;
    private final int[] way;
    private int toVisit;
    private final Random random = new Random(System.nanoTime());

    public Agent(AntColonyOptimizationHelper instance, int start) {
        this.instance = instance;
        this.visited = new boolean[instance.matrix.length];
        visited[start] = true;
        toVisit = visited.length - 1;
        this.start = start;
        this.way = new int[visited.length];
    }

    // TODO really needs improvement
    private int getNextProbableNode(int y) {
        if (toVisit > 0) {
            int danglingUnvisited = -1;
            final double[] weights = new double[visited.length];

            double columnSum = 0.0d;
            for (int i = 0; i < visited.length; i++) {
                columnSum += Math.pow(instance.readPheromone(y, i),
                        AntColonyOptimizationHelper.ALPHA)
                        * Math.pow(instance.invertedMatrix[y][i],
                        AntColonyOptimizationHelper.BETA);
            }

            double sum = 0.0d;
            for (int x = 0; x < visited.length; x++) {
                if (!visited[x]) {
                    weights[x] = calculateProbability(x, y, columnSum);
                    sum += weights[x];
                    danglingUnvisited = x;
                }
            }

            if (sum == 0.0d)
                return danglingUnvisited;

            // weighted indexing stuff
            double pSum = 0.0d;
            for (int i = 0; i < visited.length; i++) {
                pSum += weights[i] / sum;
                weights[i] = pSum;
            }

            final double r = random.nextDouble();
            for (int i = 0; i < visited.length; i++) {
                if (!visited[i]) {
                    if (r <= weights[i]) {
                        return i;
                    }
                }
            }

        }
        return -1;
    }

    /*
     * (pheromones ^ ALPHA) * ((1/length) ^ BETA) divided by the sum of all rows.
     */
    private double calculateProbability(int row, int column, double sum) {
        return Math.pow(instance.readPheromone(column, row),
                AntColonyOptimizationHelper.ALPHA) * Math.pow(instance.invertedMatrix[column][row], AntColonyOptimizationHelper.BETA) / sum;
    }

    @Override
    public final AntColonyOptimizationHelper.WalkedWay call() throws Exception {
        int lastNode = start;
        int next;
        int i = 0;
        while ((next = getNextProbableNode(lastNode)) != -1) {
            way[i] = lastNode;
            i++;
            distanceWalked += instance.matrix[lastNode][next];
            final double pheromone = (AntColonyOptimizationHelper.Q / (distanceWalked));
            instance.adjustPheromone(lastNode, next, pheromone);
            visited[next] = true;
            lastNode = next;
            toVisit--;
        }
        distanceWalked += instance.matrix[lastNode][start];
        way[i] = lastNode;

        return new AntColonyOptimizationHelper.WalkedWay(way, distanceWalked);
    }
}
