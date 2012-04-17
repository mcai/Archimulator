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
package archimulator.client;

import archimulator.service.ArchimulatorService;
import archimulator.sim.base.experiment.profile.ExperimentProfile;
import archimulator.sim.base.experiment.profile.ExperimentProfileState;
import archimulator.util.DateHelper;
import archimulator.util.arduino.ArduinoHelper;
import com.caucho.hessian.client.HessianProxyFactory;

import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class ArduinoBasedMonitorStartup {
    private static void recordException(Exception e) {
        System.out.print(String.format("[%s Exception] %s\r\n", DateHelper.toString(new Date()), e));
        e.printStackTrace();
    }

    public static void main(String[] args) throws SQLException {
        ArchimulatorService archimulatorService;
        try {
            HessianProxyFactory factory = new HessianProxyFactory();
            factory.setReadTimeout(30000);
            factory.setConnectTimeout(20000);
            factory.setOverloadEnabled(true);

            archimulatorService = (ArchimulatorService) factory.create(ArchimulatorService.class, ManagementStartup.SERVICE_URL);
        } catch (MalformedURLException e) {
            recordException(e);
            throw new RuntimeException(e);
        }

        final ArduinoHelper arduinoHelper = new ArduinoHelper();

        Thread threadArduino = new Thread(){
            @Override
            public void run() {
                arduinoHelper.run();
            }
        };
        threadArduino.setDaemon(true);
        threadArduino.start();

        for(;;) {
            try {
                List<ExperimentProfile> experimentProfiles = archimulatorService.getExperimentProfilesAsList();
                int numRunningExperimentProfiles = 0;

                for(ExperimentProfile experimentProfile : experimentProfiles) {
                    if(experimentProfile.getState() == ExperimentProfileState.RUNNING) {
                        numRunningExperimentProfiles++;
                    }
                }

                System.out.println("numRunningExperimentProfiles: " + numRunningExperimentProfiles);

                arduinoHelper.blink(numRunningExperimentProfiles);

                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
