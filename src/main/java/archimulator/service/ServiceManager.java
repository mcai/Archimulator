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

import archimulator.service.impl.*;
import archimulator.util.PropertiesHelper;

/**
 * Helper class for retrieving services.
 *
 * @author Min Cai
 */
public class ServiceManager {
    /**
     *  To be used in benchmark arguments injection.
     */
    public static final String USER_HOME_TEMPLATE_ARG = "<user.home>";

    private static BenchmarkService benchmarkService;
    private static ArchitectureService architectureService;
    private static ExperimentMetricService experimentMetricService;
    private static ExperimentService experimentService;
    private static ExperimentStatService experimentStatService;
    private static UserService userService;
    private static SystemSettingService systemSettingService;

    static {
        benchmarkService = new BenchmarkServiceImpl();
        architectureService = new ArchitectureServiceImpl();
        experimentMetricService = new ExperimentMetricServiceImpl();
        experimentService = new ExperimentServiceImpl();
        experimentStatService = new ExperimentStatServiceImpl();
        userService = new UserServiceImpl();
        systemSettingService = new SystemSettingServiceImpl();

        benchmarkService.initialize();
        architectureService.initialize();
        experimentMetricService.initialize();
        experimentService.initialize();
        experimentStatService.initialize();
        userService.initialize();
        systemSettingService.initialize();

        System.out.println("Archimulator (version: " + PropertiesHelper.getVersion() + ") - CMP Architectural Simulator Written in Java.\n");
        System.out.println("Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).\n");
    }

    /**
     * Return the benchmark service singleton.
     *
     * @return benchmark service singleton
     */
    public static BenchmarkService getBenchmarkService() {
        return benchmarkService;
    }

    /**
     * Return the architecture service singleton.
     *
     * @return architecture service singleton
     */
    public static ArchitectureService getArchitectureService() {
        return architectureService;
    }

    /**
     * Return the experiment metric service singleton.
     *
     * @return experiment metric service singleton
     */
    public static ExperimentMetricService getExperimentMetricService() {
        return experimentMetricService;
    }

    /**
     * Return the experiment service singleton.
     *
     * @return experiment service singleton
     */
    public static ExperimentService getExperimentService() {
        return experimentService;
    }

    /**
     * Return the experiment stat service singleton.
     *
     * @return experiment stat service singleton
     */
    public static ExperimentStatService getExperimentStatService() {
        return experimentStatService;
    }

    /**
     * Return the user service singleton.
     *
     * @return user service singleton
     */
    public static UserService getUserService() {
        return userService;
    }

    /**
     * Return the system setting service singleton.
     *
     * @return system setting service singleton
     */
    public static SystemSettingService getSystemSettingService() {
        return systemSettingService;
    }

    /**
     * Return the database url to be used among archimulator services.
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
