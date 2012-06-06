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
import archimulator.sim.base.event.DumpStatsCompletedEvent;
import archimulator.sim.base.event.PollStatsCompletedEvent;
import archimulator.sim.base.event.SimulationCreatedEvent;
import archimulator.sim.base.experiment.Experiment;
import archimulator.sim.base.experiment.profile.ExperimentProfile;
import net.pickapack.DateHelper;
import archimulator.util.UpdateHelper;
import net.pickapack.action.Action1;
import net.pickapack.im.channel.CloudMessageChannel;
import net.pickapack.im.event.request.PauseExperimentRequestEvent;
import net.pickapack.im.event.request.ResumeExperimentRequestEvent;
import net.pickapack.im.event.request.StopExperimentRequestEvent;
import net.pickapack.im.sink.GracefulMessageSink;
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

            this.archimulatorService = (ArchimulatorService) factory.create(ArchimulatorService.class, ManagementStartup.SERVICE_URL);
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
            final ExperimentProfile experimentProfile = this.archimulatorService.retrieveOneExperimentProfileToRun(this.simulatorUserId);

            if (experimentProfile != null) {
                System.out.printf("[%s Contact Server] Running new experiment profile\n", DateHelper.toString(new Date()));

                final Experiment experiment = experimentProfile.createExperiment();

                this.experimentProfileIdsToExperiments.put(experimentProfile.getId(), experiment);

                experiment.start();

                experiment.getBlockingEventDispatcher().addListener(SimulationCreatedEvent.class, new Action1<SimulationCreatedEvent>() {
                    @Override
                    public void apply(SimulationCreatedEvent event) {
                        experiment.getSimulation().getBlockingEventDispatcher().addListener(PollStatsCompletedEvent.class, new Action1<PollStatsCompletedEvent>() {
                            @Override
                            public void apply(final PollStatsCompletedEvent event) {
                                try {
                                    Map<String, Object> stats = event.getStats();

                                    Map<String, String> stats1 = new LinkedHashMap<String, String>();
                                    for (String key : stats.keySet()) {
                                        stats1.put(key, stats.get(key) + "");
                                    }

                                    archimulatorService.updateExperimentStatsById(experimentProfile.getId(), stats1);
                                } catch (SQLException e) {
                                    recordException(e);
                                    //                        throw new RuntimeException(e);
                                } catch (Exception e) {
                                    recordException(e);
                                    //                        throw new RuntimeException(e);
                                }
                            }
                        });

                        experiment.getSimulation().getBlockingEventDispatcher().addListener(DumpStatsCompletedEvent.class, new Action1<DumpStatsCompletedEvent>() {
                            @Override
                            public void apply(final DumpStatsCompletedEvent event) {
                                try {
                                    Map<String, Object> stats = event.getStats();

                                    Map<String, String> stats1 = new LinkedHashMap<String, String>();
                                    for (String key : stats.keySet()) {
                                        stats1.put(key, stats.get(key) + "");
                                    }

                                    archimulatorService.updateExperimentStatsById(experimentProfile.getId(), stats1);
                                } catch (SQLException e) {
                                    recordException(e);
                                    //                        throw new RuntimeException(e);
                                } catch (Exception e) {
                                    recordException(e);
                                    //                        throw new RuntimeException(e);
                                }
                            }
                        });
                    }
                });

                experiment.join();

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

    public static void main(String[] args) {
        if (args.length >= 1 && args[0].equals("-s")) {
            UpdateHelper.update();
        } else {
            GuestStartup startup = new GuestStartup();
            startup.start();
        }
    }
}