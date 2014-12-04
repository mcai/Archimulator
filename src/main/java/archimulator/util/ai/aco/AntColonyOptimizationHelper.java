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

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Ant colony optimization helper.
 *
 * @author Min Cai
 */
public final class AntColonyOptimizationHelper extends AbstractAntColonyOptimizationHelper {
    public static final int NUM_AGENTS = 2048 * 20;
    private static final int POOL_SIZE = Runtime.getRuntime().availableProcessors();

    protected final Object[][] mutexes;

    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(POOL_SIZE);

    private final ExecutorCompletionService<WalkedWay> agentCompletionService = new ExecutorCompletionService<>(
            THREAD_POOL);

    public AntColonyOptimizationHelper(String fileName) throws IOException {
        super(fileName);
        this.mutexes = initializeMutexObjects();
    }

    private Object[][] initializeMutexObjects() {
        final Object[][] localMatrix = new Object[matrix.length][matrix.length];
        int rows = matrix.length;
        for (int columns = 0; columns < matrix.length; columns++) {
            for (int i = 0; i < rows; i++) {
                localMatrix[columns][i] = new Object();
            }
        }

        return localMatrix;
    }

    final void adjustPheromone(int x, int y, double newPheromone) {
        synchronized (mutexes[x][y]) {
            final double result = calculatePheromones(pheromones[x][y], newPheromone);
            if (result >= 0.0d) {
                pheromones[x][y] = result;
            } else {
                pheromones[x][y] = 0;
            }
        }
    }

    private double calculatePheromones(double current, double newPheromone) {
        return (1 - AntColonyOptimizationHelper.PHEROMONE_PERSISTENCE) * current + newPheromone;
    }

    final double start() throws InterruptedException, ExecutionException {
        WalkedWay bestDistance = null;

        int numAgentsSent = 0;
        int numAgentsDone = 0;
        int numAgentsWorking = 0;
        for (int agentNumber = 0; agentNumber < NUM_AGENTS; agentNumber++) {
            agentCompletionService.submit(new Agent(this, getGaussianDistributionRowIndex()));
            numAgentsSent++;
            numAgentsWorking++;
            while (numAgentsWorking >= POOL_SIZE) {
                WalkedWay way = agentCompletionService.take().get();
                if (bestDistance == null || way.distance < bestDistance.distance) {
                    bestDistance = way;
                    System.out.println("Agent returned with new best distance of: " + way.distance);
                }
                numAgentsDone++;
                numAgentsWorking--;
            }
        }
        final int left = numAgentsSent - numAgentsDone;
        System.out.println("Waiting for " + left + " agents to finish their random walk!");

        for (int i = 0; i < left; i++) {
            WalkedWay way = agentCompletionService.take().get();
            if (bestDistance == null || way.distance < bestDistance.distance) {
                bestDistance = way;
                System.out.println("Agent returned with new best distance of: " + way.distance);
            }
        }

        THREAD_POOL.shutdownNow();
        System.out.println("Found best so far: " + bestDistance.distance);
        System.out.println(Arrays.toString(bestDistance.way));

        return bestDistance.distance;
    }

    static class WalkedWay {
        int[] way;
        double distance;

        public WalkedWay(int[] way, double distance) {
            this.way = way;
            this.distance = distance;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        System.out.println("Result was: " + new AntColonyOptimizationHelper("files/berlin52.tsp").start());
    }
}
