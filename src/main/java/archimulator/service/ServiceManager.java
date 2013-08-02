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
import archimulator.util.plugin.PluginHelper;
import archimulator.util.serialization.XMLSerializationHelper;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

    private static BenchmarkService benchmarkService;
    private static ArchitectureService architectureService;
    private static ExperimentService experimentService;
    private static ExperimentStatService experimentStatService;
    private static UserService userService;
    private static SystemSettingService systemSettingService;

    private static PluginHelper pluginHelper;

    /**
     * Static constructor.
     */
    static {
        System.out.println("Archimulator (version: " + PropertiesHelper.getVersion() + ") - CMP Architectural Simulator Written in Java.\n");
        System.out.println("Copyright (c) 2010-2013 by Min Cai (min.cai.china@gmail.com).\n");

        pluginHelper = new PluginHelper();

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
                List<File> files = new ArrayList<File>(FileUtils.listFiles(fileTaskInputs, new String[]{"xml"}, true));

                Collections.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
                    }
                });

                for (File file : files) {
                    String text = FileUtils.readFileToString(file);

                    Task task = XMLSerializationHelper.deserialize(Task.class, text);

                    if (task != null && task.isActive()) {
                        for(String tag : task.getTags()) {
                            List<ExperimentPack> experimentPacks = experimentService.getExperimentPacksByTag(tag);
                            for(ExperimentPack experimentPack : experimentPacks) {
                                if(task.isReset()) {
                                    experimentService.resetAbortedExperimentsByParent(experimentPack);
                                    experimentService.resetCompletedExperimentsByParent(experimentPack);
                                }
                                experimentService.startExperimentPack(experimentPack);
                            }
                        }

                        for(String experimentPackTitle : task.getExperimentPackTitles()) {
                            ExperimentPack experimentPack = experimentService.getExperimentPackByTitle(experimentPackTitle);
                            if(task.isReset()) {
                                experimentService.resetAbortedExperimentsByParent(experimentPack);
                                experimentService.resetCompletedExperimentsByParent(experimentPack);
                            }
                            experimentService.startExperimentPack(experimentPack);
                        }
                    }
                }
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
     * Get the plugin helper.
     *
     * @return the plugin helper
     */
    public static PluginHelper getPluginHelper() {
        return pluginHelper;
    }

    /**
     * Get the database url to be used among archimulator services.
     *
     * @return database url
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
