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
package archimulator.service;

import archimulator.model.*;
import archimulator.service.impl.*;
import archimulator.util.PropertiesHelper;
import archimulator.util.serialization.XMLSerializationHelper;
import net.pickapack.event.BlockingEvent;
import net.pickapack.event.BlockingEventDispatcher;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Helper class for retrieving services.
 *
 * @author Min Cai
 */
public class ServiceManager {
    /**
     * User home template argument. To be used in benchmark arguments injection.
     */
    public static final String USER_HOME_TEMPLATE_ARG = "<user.home>";

    private static BlockingEventDispatcher<BlockingEvent> blockingEventDispatcher;

    private static BenchmarkService benchmarkService;
    private static ArchitectureService architectureService;
    private static ExperimentService experimentService;
    private static ExperimentStatService experimentStatService;
    private static UserService userService;
    private static SystemSettingService systemSettingService;

    private static Map<Long, Task> experimentPacksToTasksMap;

    /**
     * Static constructor.
     */
    static {
        System.out.println("Archimulator (version: " + PropertiesHelper.getVersion() + ") - CMP Architectural Simulator Written in Java.\n");
        System.out.println("Copyright (c) 2010-2013 by Min Cai (min.cai.china@gmail.com).\n");

        blockingEventDispatcher = new BlockingEventDispatcher<BlockingEvent>();

        benchmarkService = new BenchmarkServiceImpl();
        architectureService = new ArchitectureServiceImpl();
        experimentService = new ExperimentServiceImpl();
        experimentStatService = new ExperimentStatServiceImpl();
        userService = new UserServiceImpl();
        systemSettingService = new SystemSettingServiceImpl();

        benchmarkService.initialize();
        architectureService.initialize();
        experimentService.initialize();
        experimentStatService.initialize();
        userService.initialize();
        systemSettingService.initialize();

        experimentPacksToTasksMap = new LinkedHashMap<>();

        startTasks();

        System.out.println("Archimulator initialized successfully.\n");
    }

    /**
     * Start tasks.
     */
    private static void startTasks() {
        try {
            File fileTaskInputs = new File("experiment_tasks");

            if(fileTaskInputs.exists()) {
                List<File> files = new ArrayList<>(FileUtils.listFiles(fileTaskInputs, new String[]{"xml"}, true));

                files.sort(Comparator.comparing(File::getAbsolutePath));

                for (File file : files) {
                    String text = FileUtils.readFileToString(file);

                    Task task = XMLSerializationHelper.deserialize(Task.class, text);

                    if (task != null && task.isActive()) {
                        System.out.printf("Running task %s\n\n", task.getTitle());

                        for(String tag : task.getTags()) {
                            List<ExperimentPack> experimentPacks = experimentService.getExperimentPacksByTag(tag);
                            for(ExperimentPack experimentPack : experimentPacks) {
                                processExperimentPack(task, experimentPack);
                            }
                        }

                        for(String experimentPackTitle : task.getExperimentPackTitles()) {
                            ExperimentPack experimentPack = experimentService.getExperimentPackByTitle(experimentPackTitle);
                            processExperimentPack(task, experimentPack);
                        }
                    }
                }

                getBlockingEventDispatcher().addListener(ExperimentStartedEvent.class, event -> printStats(event.getSender().getParent()));
                getBlockingEventDispatcher().addListener(ExperimentStoppedEvent.class, event -> printStats(event.getSender().getParent()));
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Process the specified experiment pack.
     *
     * @param experimentPack the experiment pack
     */
    private static void processExperimentPack(Task task, ExperimentPack experimentPack) {
        experimentPacksToTasksMap.put(experimentPack.getId(), task);

        printStats(experimentPack);

        if(task.isReset()) {
            experimentService.resetAbortedExperimentsByParent(experimentPack);
            experimentService.resetCompletedExperimentsByParent(experimentPack);
        }
        experimentService.startExperimentPack(experimentPack);
    }

    /**
     * Print the statistics for the specified experiment pack.
     *
     * @param experimentPack the experiment pack
     */
    private static void printStats(ExperimentPack experimentPack) {
        System.out.println(experimentPack.getTitle());

        for(ExperimentState experimentState : ExperimentState.values()) {
            System.out.printf("  %s: %d\n", experimentState, experimentService.getNumExperimentsByParentAndState(experimentPack, experimentState));
        }

        System.out.printf("  %% completion: %.4f\n", (double) experimentService.getNumExperimentsByParentAndState(experimentPack, ExperimentState.COMPLETED) / experimentService.getNumExperimentsByParent(experimentPack));

        if(experimentPacksToTasksMap.containsKey(experimentPack.getId())) {
            printStats(experimentPacksToTasksMap.get(experimentPack.getId()));
        }

        System.out.println();
    }

    /**
     * Print the statistics for the specified task.
     *
     * @param task the task
     */
    private static void printStats(Task task) {
        long numCompleted = 0;
        long numTotal = 0;

        for(String tag : task.getTags()) {
            List<ExperimentPack> experimentPacks = experimentService.getExperimentPacksByTag(tag);
            for(ExperimentPack experimentPack : experimentPacks) {
                numCompleted += experimentService.getNumExperimentsByParentAndState(experimentPack, ExperimentState.COMPLETED);
                numTotal += experimentService.getNumExperimentsByParent(experimentPack);
            }
        }

        for(String experimentPackTitle : task.getExperimentPackTitles()) {
            ExperimentPack experimentPack = experimentService.getExperimentPackByTitle(experimentPackTitle);
            numCompleted += experimentService.getNumExperimentsByParentAndState(experimentPack, ExperimentState.COMPLETED);
            numTotal += experimentService.getNumExperimentsByParent(experimentPack);
        }

        System.out.printf("  %% completion of task %s: %.4f\n\n", task.getTitle(), (double) numCompleted / numTotal);
    }

    /**
     * Get the blocking event dispatcher.
     *
     * @return the blocking event dispatcher
     */
    public static BlockingEventDispatcher<BlockingEvent> getBlockingEventDispatcher() {
        return blockingEventDispatcher;
    }

    /**
     * Get the benchmark service singleton.
     *
     * @return benchmark service singleton
     */
    public static BenchmarkService getBenchmarkService() {
        return benchmarkService;
    }

    /**
     * Get the architecture service singleton.
     *
     * @return architecture service singleton
     */
    public static ArchitectureService getArchitectureService() {
        return architectureService;
    }

    /**
     * Get the experiment service singleton.
     *
     * @return experiment service singleton
     */
    public static ExperimentService getExperimentService() {
        return experimentService;
    }

    /**
     * Get the experiment stat service singleton.
     *
     * @return experiment stat service singleton
     */
    public static ExperimentStatService getExperimentStatService() {
        return experimentStatService;
    }

    /**
     * Get the user service singleton.
     *
     * @return user service singleton
     */
    public static UserService getUserService() {
        return userService;
    }

    /**
     * Get the system setting service singleton.
     *
     * @return system setting service singleton
     */
    public static SystemSettingService getSystemSettingService() {
        return systemSettingService;
    }

    /**
     * Get the database URL to be used among archimulator services.
     *
     * @return database URL
     */
    public static String getDatabaseUrl() {
        return "jdbc:mysql://" +
                PropertiesHelper.getProperties().getProperty("archimulator.database.host") +
                "/" +
                PropertiesHelper.getProperties().getProperty("archimulator.database.name") +
                "?user=" +
                PropertiesHelper.getProperties().getProperty("archimulator.database.user") +
                "&password=" +
                PropertiesHelper.getProperties().getProperty("archimulator.database.password");
    }
}
