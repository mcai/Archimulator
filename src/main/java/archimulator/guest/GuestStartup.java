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
package archimulator.guest;

import archimulator.model.experiment.profile.ExperimentProfile;
import archimulator.service.ArchimulatorService;
import archimulator.util.DateHelper;
import archimulator.util.UpdateHelper;
import com.caucho.hessian.HessianException;
import com.caucho.hessian.client.HessianProxyFactory;

import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.Date;

public class GuestStartup {
    private ArchimulatorService archimulatorService;

    public GuestStartup() {
        try {
            HessianProxyFactory factory = new HessianProxyFactory();
            factory.setReadTimeout(30000);
            factory.setConnectTimeout(20000);
            factory.setOverloadEnabled(true);

            this.archimulatorService = (ArchimulatorService) factory.create(ArchimulatorService.class, SERVICE_URL);
        } catch (MalformedURLException e) {
            recordException(e);
            throw new RuntimeException(e);
        }
    }

    public static void recordException(Exception e) {
        System.out.print(String.format("[%s Exception] %s\r\n", DateHelper.toString(new Date()), e));
        e.printStackTrace();
    }

    public void start() {
        Thread threadRetrieveTask = new Thread() {
            @Override
            public void run() {
                boolean hasTask = true;

                for (; ; ) {
                    try {
                        if (!hasTask) {
                            Thread.sleep(5000);
                        }
                    } catch (InterruptedException e) {
                        recordException(e);
//                        throw new RuntimeException(e);
                    }

                    try {
                        hasTask = retrieveAndExecuteNewExperimentProfile();
                    } catch (SQLException e) {
                        hasTask = false;
                        recordException(e);
//                        throw new RuntimeException(e);

                    } catch (HessianException e) {
                        hasTask = false;
                        recordException(e);
//                        throw new RuntimeException(e);

                    } catch (Exception e) {
                        hasTask = false;
                        recordException(e);
//                        throw new RuntimeException(e);
                    }
                }
            }
        };

        threadRetrieveTask.start();
    }
    
    private boolean retrieveAndExecuteNewExperimentProfile() throws SQLException {
        if(this.archimulatorService.isRunningExperimentEnabled()) {
            ExperimentProfile profile = this.archimulatorService.retrieveOneExperimentProfileToRun();
            
            if(profile != null) {
                profile.runToEnd();
                this.archimulatorService.notifyExperimentStopped(profile.getId());
                return true;
            }
            else {
                System.out.printf("[%s Contact Server] No experiment profile to run\n", DateHelper.toString(new Date()));
                return false;
            }
        }
        else {
            System.out.printf("[%s Contact Server] No experiment profile to run\n", DateHelper.toString(new Date()));
            return false;
        }
    }

//    public static final String SERVICE_URL = "http://204.152.205.131:8080/archimulator/archimulator";
//    public static final String SERVICE_URL = "http://50.117.112.114:8080/archimulator/archimulator";
    public static final String SERVICE_URL = "http://[2607:f358:10:13::2]:8080/archimulator/archimulator";

    public static void main(String[] args) {
        if (args.length >= 1 && args[0].equals("-s")) {
            UpdateHelper.update();
        } else {
            GuestStartup startup = new GuestStartup();
            startup.start();
        }
    }
}
