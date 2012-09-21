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
package archimulator.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Repeater {
    public static void run(Runnable runnable) {
        run(runnable, Runtime.getRuntime().availableProcessors());
    }

    public static void run(final Runnable runnable, final int numThreads) {
        ExecutorService downloadDocumentsService = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {
            downloadDocumentsService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        runnable.run();
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                    }
                }
            });
        }

        downloadDocumentsService.shutdown();
//        while (!downloadDocumentsService.isTerminated()) {
//            try {
//                downloadDocumentsService.awaitTermination(1, TimeUnit.SECONDS);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
    }
}
