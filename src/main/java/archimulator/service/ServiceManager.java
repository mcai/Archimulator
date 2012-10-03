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

public class ServiceManager {
    public static final String DATABASE_URL = "jdbc:mysql://localhost/archimulator?user=root&password=1026@ustc";
    public static final String USER_HOME_TEMPLATE_ARG = "<user.home>";

    private static BenchmarkService benchmarkService;
    private static ArchitectureService architectureService;
    private static ExperimentService experimentService;
    private static UserService userService;
    private static SystemSettingService systemSettingService;
    private static ExperimentMetricService experimentMetricService;

    static {
        benchmarkService = new BenchmarkServiceImpl();
        architectureService = new ArchitectureServiceImpl();
        experimentService = new ExperimentServiceImpl();
        userService = new UserServiceImpl();
        systemSettingService = new SystemSettingServiceImpl();
        experimentMetricService = new ExperimentMetricServiceImpl();
    }

    public static BenchmarkService getBenchmarkService() {
        return benchmarkService;
    }

    public static ArchitectureService getArchitectureService() {
        return architectureService;
    }

    public static ExperimentService getExperimentService() {
        return experimentService;
    }

    public static UserService getUserService() {
        return userService;
    }

    public static SystemSettingService getSystemSettingService() {
        return systemSettingService;
    }

    public static ExperimentMetricService getExperimentMetricService() {
        return experimentMetricService;
    }

    static {
        System.out.println("Archimulator (version: " + PropertiesHelper.getVersion() + ") - CMP Architectural Simulator Written in Java.\n");
        System.out.println("Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).\n");
    }
}
