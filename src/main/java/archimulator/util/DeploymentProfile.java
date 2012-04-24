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
package archimulator.util;

public class DeploymentProfile {
    private String serverIpv6Address;
    private String serverIpv4Address;

    private String serverUserId;
    private String serverUserPassword;

    private String archimulatorDirectoryPath;

    private String description;

    public DeploymentProfile(String serverIpv6Address, String serverIpv4Address, String serverUserId, String serverUserPassword, String archimulatorDirectoryPath, String description) {
        this.serverIpv6Address = serverIpv6Address;
        this.serverIpv4Address = serverIpv4Address;
        this.serverUserId = serverUserId;
        this.serverUserPassword = serverUserPassword;
        this.archimulatorDirectoryPath = archimulatorDirectoryPath;
        this.description = description;
    }

    public String getServerIpv6Address() {
        return serverIpv6Address;
    }

    public String getServerIpv4Address() {
        return serverIpv4Address;
    }

    public String getServerUserId() {
        return serverUserId;
    }

    public String getServerUserPassword() {
        return serverUserPassword;
    }

    public static DeploymentProfile getCurrent() {
//        return SERVER_2;
        return SERVER_1;
    }

    public String getArchimulatorDirectoryPath() {
        return archimulatorDirectoryPath;
    }

    public String getDescription() {
        return description;
    }

    public static final DeploymentProfile DEV = new DeploymentProfile("localhost", "localhost", "itecgo", "bywwnss",
            "Archimulator/target",
            "i5 2430M 8G RAM");

    public static final DeploymentProfile SERVER_1 = new DeploymentProfile("2607:f358:10:13::2", "50.117.112.114", "server577user", "1026@ustc",
            "Archimulator/target",
            "i7 2600 16G RAM 100M/50T");

    public static final DeploymentProfile SERVER_2 = new DeploymentProfile("N/A", "204.152.205.131", "itecgo", "1026@ustc",
            "Archimulator/target",
            "E3 1230 16G RAM 15M");

    public static final DeploymentProfile SERVER_3 = new DeploymentProfile("N/A", "110.80.31.30", "xmttb", "1026@ustc",
            "Archimulator/target",
            "i7 2600 16G RAM 4M");

    public static final String DATABASE_REVISION = "18";
}
