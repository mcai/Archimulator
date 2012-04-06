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

import archimulator.sim.base.experiment.Experiment;
import archimulator.sim.base.experiment.profile.ExperimentProfile;
import archimulator.sim.base.simulation.Simulation;
import archimulator.service.ArchimulatorService;
import archimulator.util.DateHelper;
import archimulator.util.UpdateHelper;
import archimulator.util.action.Action1;
import archimulator.util.im.channel.CloudMessageChannel;
import archimulator.util.im.event.request.PauseExperimentRequestEvent;
import archimulator.util.im.event.request.ResumeExperimentRequestEvent;
import archimulator.util.im.event.request.StopExperimentRequestEvent;
import archimulator.util.im.sink.GracefulMessageSink;
import com.caucho.hessian.HessianException;
import com.caucho.hessian.client.HessianProxyFactory;

import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.*;

public class GuestStartup {
    private ArchimulatorService archimulatorService;
    private Map<Long, Experiment> experimentProfileIdsToExperiments;
    private String simulatorUserId;

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

        this.experimentProfileIdsToExperiments = new HashMap<Long, Experiment>();

        CloudMessageChannel cloudMessageChannel = new CloudMessageChannel(UUID.randomUUID().toString(), new GracefulMessageSink(this.archimulatorService));

        this.simulatorUserId = cloudMessageChannel.getUserId();

        cloudMessageChannel.addCloudEventListener(PauseExperimentRequestEvent.class, new Action1<PauseExperimentRequestEvent>() {
            @Override
            public void apply(PauseExperimentRequestEvent event) {
                try {
                    pauseExperiment(event.getExperimentProfileId());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        cloudMessageChannel.addCloudEventListener(ResumeExperimentRequestEvent.class, new Action1<ResumeExperimentRequestEvent>() {
            @Override
            public void apply(ResumeExperimentRequestEvent event) {
                try {
                    resumeExperiment(event.getExperimentProfileId());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        cloudMessageChannel.addCloudEventListener(StopExperimentRequestEvent.class, new Action1<StopExperimentRequestEvent>() {
            @Override
            public void apply(StopExperimentRequestEvent event) {
                try {
                    stopExperiment(event.getExperimentProfileId());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        cloudMessageChannel.open();
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
        if (this.archimulatorService.isRunningExperimentEnabled()) {
            ExperimentProfile experimentProfile = this.archimulatorService.retrieveOneExperimentProfileToRun(this.simulatorUserId);

            if (experimentProfile != null) {
                System.out.printf("[%s Contact Server] Running new experiment profile\n", DateHelper.toString(new Date()));

                final Experiment experiment = experimentProfile.createExperiment();

                experiment.getBlockingEventDispatcher().addListener(Simulation.PollStatsCompletedEvent.class, new Action1<Simulation.PollStatsCompletedEvent>() {
                    @Override
                    public void apply(final Simulation.PollStatsCompletedEvent event) {
                        Thread threadNotify = new Thread() {
                            @Override
                            public void run() {
                                try {
                                    Map<String, Object> stats = event.getStats();

                                    Map<String, String> stats1 = new TreeMap<String, String>();
                                    for(String key : stats.keySet()) {
                                        stats1.put(key, stats.get(key) + "");
                                    }

                                    archimulatorService.notifyPollStatsCompletedEvent(getExperimentProfileIdFromExperimentId(experiment.getId()), stats1);
                                } catch (SQLException e) {
                                    recordException(e);
//                        throw new RuntimeException(e);
                                } catch (Exception e) {
                                    recordException(e);
//                        throw new RuntimeException(e);
                                }
                            }
                        };
                        threadNotify.setDaemon(true);
                        threadNotify.start();
                    }
                });

                experiment.getBlockingEventDispatcher().addListener(Simulation.DumpStatsCompletedEvent.class, new Action1<Simulation.DumpStatsCompletedEvent>() {
                    @Override
                    public void apply(final Simulation.DumpStatsCompletedEvent event) {
                        Thread threadNotify = new Thread() {
                            @Override
                            public void run() {
                                try {
                                    Map<String, Object> stats = event.getStats();

                                    Map<String, String> stats1 = new TreeMap<String, String>();
                                    for(String key : stats.keySet()) {
                                        stats1.put(key, stats.get(key) + "");
                                    }

                                    archimulatorService.notifyDumpStatsCompletedEvent(getExperimentProfileIdFromExperimentId(experiment.getId()), stats1);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                } catch (Exception e) {
                                    recordException(e);
//                        throw new RuntimeException(e);
                                }
                            }
                        };
                        threadNotify.setDaemon(true);
                        threadNotify.start();
                    }
                });

                this.experimentProfileIdsToExperiments.put(experimentProfile.getId(), experiment);
                experiment.runToEnd();
                this.stopExperiment(experimentProfile.getId());
                return true;
            } else {
                System.out.printf("[%s Contact Server] No experiment profile to run\n", DateHelper.toString(new Date()));
                return false;
            }
        } else {
            System.out.printf("[%s Contact Server] No experiment profile to run\n", DateHelper.toString(new Date()));
            return false;
        }
    }

    public void pauseExperiment(long experimentProfileId) throws SQLException {
        System.out.printf("[%s Contact Server] Pausing experiment\n", DateHelper.toString(new Date()));

        if (this.experimentProfileIdsToExperiments.containsKey(experimentProfileId)) {
            this.experimentProfileIdsToExperiments.get(experimentProfileId).pause();
            this.archimulatorService.notifyExperimentPaused(experimentProfileId);
        }
    }

    public void resumeExperiment(long experimentProfileId) throws SQLException {
        System.out.printf("[%s Contact Server] Resuming experiment\n", DateHelper.toString(new Date()));

        if (this.experimentProfileIdsToExperiments.containsKey(experimentProfileId)) {
            this.experimentProfileIdsToExperiments.get(experimentProfileId).resume();
            this.archimulatorService.notifyExperimentResumed(experimentProfileId);
        }
    }

    public void stopExperiment(long experimentProfileId) throws SQLException {
        System.out.printf("[%s Contact Server] Stopping experiment\n", DateHelper.toString(new Date()));

        if (this.experimentProfileIdsToExperiments.containsKey(experimentProfileId)) {
            this.experimentProfileIdsToExperiments.get(experimentProfileId).stop();
            this.experimentProfileIdsToExperiments.remove(experimentProfileId);
            this.archimulatorService.notifyExperimentStopped(experimentProfileId);
        }
    }

    public long getExperimentProfileIdFromExperimentId(long experimentId) {
        for (long experimentProfileId : this.experimentProfileIdsToExperiments.keySet()) {
            if (this.experimentProfileIdsToExperiments.get(experimentProfileId).getId() == experimentId) {
                return experimentProfileId;
            }
        }

        return -1;
    }

    //        public static final String SERVICE_URL = "http://204.152.205.131:8080/archimulator/archimulator";
//    public static final String SERVICE_URL = "http://50.117.112.114:8080/archimulator/archimulator";
//    public static final String SERVICE_URL = "http://[2607:f358:10:13::2]:8080/archimulator/archimulator";
//    public static final String SERVICE_URL = "http://localhost:8080/api";
    public static final String SERVICE_URL = "http://www.archimulator.com/api";

    public static void main(String[] args) {
        if (args.length >= 1 && args[0].equals("-s")) {
            UpdateHelper.update();
        } else {
            GuestStartup startup = new GuestStartup();
            startup.start();
        }
    }
}