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

import archimulator.sim.base.experiment.profile.ExperimentProfile;
import archimulator.sim.base.experiment.profile.ExperimentProfileState;
import archimulator.sim.base.experiment.profile.ProcessorProfile;
import archimulator.sim.base.simulation.SimulatedProgram;
import archimulator.sim.base.user.User;
import archimulator.util.im.channel.CloudMessageChannel;
import archimulator.util.im.event.request.PauseExperimentRequestEvent;
import archimulator.util.im.event.request.ResumeExperimentRequestEvent;
import archimulator.util.im.event.request.StopExperimentRequestEvent;
import archimulator.util.im.sink.MessageSink;
import archimulator.util.im.sink.MessageSinkImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.table.TableUtils;
import it.sauronsoftware.cron4j.Scheduler;

import java.sql.SQLException;
import java.util.*;

public class ArchimulatorServiceImpl implements ArchimulatorService {
    private Dao<SimulatedProgram, Long> simulatedPrograms;
    private Dao<ProcessorProfile, Long> processorProfiles;
    private Dao<ExperimentProfile, Long> experimentProfiles;

    private Dao<User, String> users;

    private transient Scheduler scheduler;

    private JdbcPooledConnectionSource connectionSource;
    
    private boolean runningExperimentEnabled;
    
    private MessageSink messageSinkProxy;
    
    private CloudMessageChannel cloudMessageChannel;
    
    private boolean loadingProgramStalled;

    @SuppressWarnings("unchecked")
    public ArchimulatorServiceImpl() {
        try {
            this.connectionSource = new JdbcPooledConnectionSource(DATABASE_URL);
            this.connectionSource.setCheckConnectionsEveryMillis(0);
            this.connectionSource.setTestBeforeGet(true);

            TableUtils.createTableIfNotExists(this.connectionSource, SimulatedProgram.class);
            TableUtils.createTableIfNotExists(this.connectionSource, ProcessorProfile.class);
            TableUtils.createTableIfNotExists(this.connectionSource, ExperimentProfile.class);
            TableUtils.createTableIfNotExists(this.connectionSource, User.class);

            this.simulatedPrograms = DaoManager.createDao(this.connectionSource, SimulatedProgram.class);
            this.processorProfiles = DaoManager.createDao(this.connectionSource, ProcessorProfile.class);
            this.experimentProfiles = DaoManager.createDao(this.connectionSource, ExperimentProfile.class);
            this.users = DaoManager.createDao(this.connectionSource, User.class);
            
            this.runningExperimentEnabled = false;

            if (!this.users.idExists(USER_ID_ADMIN)) {
                this.setUserPassword(USER_ID_ADMIN, USER_INITIAL_PASSWORD_ADMIN);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        
        this.messageSinkProxy = new MessageSinkImpl();

        this.cloudMessageChannel = new CloudMessageChannel("#admin", this);
        this.cloudMessageChannel.open();

        this.scheduler = new Scheduler();
        this.scheduler.schedule("* * * * *", new Runnable() {
            @Override
            public void run() {
                try {
                    doHousekeeping();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        this.scheduler.start();
    }
    
    @Override
    public void stop() {
        try {
            this.scheduler.stop();
            this.connectionSource.close();
            
            this.cloudMessageChannel.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void clearData() throws SQLException {
        this.simulatedPrograms.delete(this.simulatedPrograms.deleteBuilder().prepare());
        this.processorProfiles.delete(this.processorProfiles.deleteBuilder().prepare());
        this.experimentProfiles.delete(this.experimentProfiles.deleteBuilder().prepare());
    }

    @Override
    public List<SimulatedProgram> getSimulatedProgramsAsList() throws SQLException {
        return this.simulatedPrograms.queryForAll();
    }

    @Override
    public SimulatedProgram getSimulatedProgramById(long simulatedProgramId) throws SQLException {
        return this.simulatedPrograms.queryForId(simulatedProgramId);
    }
    
    @Override
    public SimulatedProgram getSimulatedProgramByTitle(String simulatedProgramTitle) throws SQLException {
        PreparedQuery<SimulatedProgram> query = this.simulatedPrograms.queryBuilder().where().eq("title", simulatedProgramTitle).prepare();
        return this.simulatedPrograms.queryForFirst(query);
    }

    @Override
    public void addSimulatedProgram(SimulatedProgram simulatedProgram) throws SQLException {
        this.simulatedPrograms.create(simulatedProgram);
    }

    @Override
    public void removeSimulatedProgramById(long simulatedProgramId) throws SQLException {
        this.simulatedPrograms.deleteById(simulatedProgramId);
    }

    @Override
    public void updateSimulatedProgram(SimulatedProgram simulatedProgram) throws SQLException {
        this.simulatedPrograms.update(simulatedProgram);
    }

    @Override
    public List<ProcessorProfile> getProcessorProfilesAsList() throws SQLException {
        return this.processorProfiles.queryForAll();
    }

    @Override
    public ProcessorProfile getProcessorProfileById(long processorProfileId) throws SQLException {
        return this.processorProfiles.queryForId(processorProfileId);
    }

    @Override
    public ProcessorProfile getProcessorProfileByTitle(String processorProfileTitle) throws SQLException {
        PreparedQuery<ProcessorProfile> query = this.processorProfiles.queryBuilder().where().eq("title", processorProfileTitle).prepare();
        return this.processorProfiles.queryForFirst(query);
    }

    @Override
    public void addProcessorProfile(ProcessorProfile processorProfile) throws SQLException {
        this.processorProfiles.create(processorProfile);
    }

    @Override
    public void removeProcessorProfileById(long processorProfileId) throws SQLException {
        this.processorProfiles.deleteById(processorProfileId);
    }

    @Override
    public void updateProcessorProfile(ProcessorProfile processorProfile) throws SQLException {
        this.processorProfiles.update(processorProfile);
    }

    @Override
    public List<ExperimentProfile> getExperimentProfilesAsList() throws SQLException {
        return this.experimentProfiles.queryForAll();
    }
    
    @Override
    public ExperimentProfile getExperimentProfileById(long experimentProfileId) throws SQLException {
        return this.experimentProfiles.queryForId(experimentProfileId);
    }

    @Override
    public ExperimentProfile getExperimentProfileByTitle(String experimentProfileTitle) throws SQLException {
        PreparedQuery<ExperimentProfile> query = this.experimentProfiles.queryBuilder().where().eq("title", experimentProfileTitle).prepare();
        return this.experimentProfiles.queryForFirst(query);
    }

    @Override
    public void addExperimentProfile(ExperimentProfile experimentProfile) throws SQLException {
        this.experimentProfiles.create(experimentProfile);
    }

    @Override
    public void removeExperimentProfileById(long experimentProfileId) throws SQLException {
        this.experimentProfiles.deleteById(experimentProfileId);
    }

    @Override
    public void updateExperimentProfile(ExperimentProfile experimentProfile) throws SQLException {
        this.experimentProfiles.update(experimentProfile);
    }

    @Override
    public ExperimentProfile retrieveOneExperimentProfileToRun(String simulatorUserId) throws SQLException {
        if(!this.runningExperimentEnabled || loadingProgramStalled) {
            return null;
        }

        PreparedQuery<ExperimentProfile> query = this.experimentProfiles.queryBuilder().where().eq("state", ExperimentProfileState.SUBMITTED).prepare();
        ExperimentProfile result = this.experimentProfiles.queryForFirst(query);
        if(result != null) {
            result.setState(ExperimentProfileState.RUNNING);
            result.setSimulatorUserId(simulatorUserId);
            this.experimentProfiles.update(result);
            
            Thread thread = new Thread(){
                @Override
                public void run() {
                    try {
                        Thread.sleep(20000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    loadingProgramStalled = false;
                }
            };
            thread.setDaemon(true);
            thread.start();

            loadingProgramStalled = true;
            
            return result;
        }
        
        return null;
    }
    
    @Override
    public void notifyExperimentPaused(long experimentProfileId) throws SQLException {
        ExperimentProfile profile = this.experimentProfiles.queryForId(experimentProfileId);
        if(profile == null || profile.getState() != ExperimentProfileState.RUNNING) {
            return;
        }

        profile.setState(ExperimentProfileState.PAUSED);
        this.experimentProfiles.update(profile);
    }
    
    @Override
    public void notifyExperimentResumed(long experimentProfileId) throws SQLException {
        ExperimentProfile profile = this.experimentProfiles.queryForId(experimentProfileId);
        if(profile == null || profile.getState() != ExperimentProfileState.PAUSED) {
            return;
        }

        profile.setState(ExperimentProfileState.RUNNING);
        this.experimentProfiles.update(profile);
    }

    @Override
    public void notifyExperimentStopped(long experimentProfileId) throws SQLException {
        ExperimentProfile profile = this.experimentProfiles.queryForId(experimentProfileId);
        if(profile == null || profile.getState() == ExperimentProfileState.SUBMITTED) {
            return;
        }

        profile.setState(ExperimentProfileState.STOPPED);
        this.experimentProfiles.update(profile);
    }

    @Override
    public boolean isRunningExperimentEnabled() {
        return runningExperimentEnabled;
    }

    @Override
    public void setRunningExperimentEnabled(boolean runningExperimentEnabled) {
        this.runningExperimentEnabled = runningExperimentEnabled;
    }

    @Override
    public void setUserPassword(String userId, String password) throws SQLException {
        if (!this.users.idExists(userId)) {
            this.users.create(new User(userId, password));
        } else {
            User user = this.users.queryForId(userId);
            user.setPassword(password);
            this.users.update(user);
        }
    }

    @Override
    public boolean authenticateUser(String userId, String password) throws SQLException {
        return this.users.idExists(userId) && this.users.queryForId(userId).getPassword().equals(password);
    }

    @Override
    public Set<String> getUserIds() {
        return this.messageSinkProxy.getUserIds();
    }

    @Override
    public void send(String fromUserId, String toUserId, String message) {
        this.messageSinkProxy.send(fromUserId, toUserId, message);
    }

    @Override
    public String receive(String userId) {
        return this.messageSinkProxy.receive(userId);
    }

    @Override
    public void pauseExperimentById(long experimentProfileId) throws SQLException {
        if(!this.experimentProfiles.idExists(experimentProfileId)) {
            return;
        }
        
        ExperimentProfile experimentProfile = this.getExperimentProfileById(experimentProfileId);
        if(experimentProfile.getState() == ExperimentProfileState.RUNNING) {
            this.cloudMessageChannel.send(experimentProfile.getSimulatorUserId(), new PauseExperimentRequestEvent(experimentProfile.getId()));
        }
    }
    
    @Override
    public void resumeExperimentById(long experimentProfileId) throws SQLException {
        if(!this.experimentProfiles.idExists(experimentProfileId)) {
            return;
        }

        ExperimentProfile experimentProfile = this.getExperimentProfileById(experimentProfileId);
        if(experimentProfile.getState() == ExperimentProfileState.PAUSED) {
            this.cloudMessageChannel.send(experimentProfile.getSimulatorUserId(), new ResumeExperimentRequestEvent(experimentProfile.getId()));
        }
    }

    @Override
    public void stopExperimentById(long experimentProfileId) throws SQLException {
        if(!this.experimentProfiles.idExists(experimentProfileId)) {
            return;
        }

        ExperimentProfile experimentProfile = this.getExperimentProfileById(experimentProfileId);
        if(experimentProfile.getState() != ExperimentProfileState.SUBMITTED) {
            this.cloudMessageChannel.send(experimentProfile.getSimulatorUserId(), new StopExperimentRequestEvent(experimentProfile.getId()));
        }
    }

    @Override
    public void resetExperimentById(long experimentProfileId) throws SQLException {
        if(!this.experimentProfiles.idExists(experimentProfileId)) {
            return;
        }

        ExperimentProfile experimentProfile = this.getExperimentProfileById(experimentProfileId);
        if(experimentProfile.getState() != ExperimentProfileState.SUBMITTED) {
            this.cloudMessageChannel.send(experimentProfile.getSimulatorUserId(), new StopExperimentRequestEvent(experimentProfile.getId()));
        }
        experimentProfile.setState(ExperimentProfileState.SUBMITTED);
        experimentProfile.getStats().clear();
        this.experimentProfiles.update(experimentProfile);
    }

    @Override
    public void notifyPollStatsCompletedEvent(long experimentProfileId, Map<String, Object> stats) throws SQLException {
        if(!this.experimentProfiles.idExists(experimentProfileId)) {
            return;
        }

        ExperimentProfile experimentProfile = this.getExperimentProfileById(experimentProfileId);
        experimentProfile.getStats().putAll(stats);
        this.experimentProfiles.update(experimentProfile);
    }

    @Override
    public void notifyDumpStatsCompletedEvent(long experimentProfileId, Map<String, Object> stats) throws SQLException {
        if(!this.experimentProfiles.idExists(experimentProfileId)) {
            return;
        }

        ExperimentProfile experimentProfile = this.getExperimentProfileById(experimentProfileId);
        experimentProfile.getStats().putAll(stats);
        this.experimentProfiles.update(experimentProfile);
    }
    
    @Override
    public Map<String, Object> getExperimentStatsById(long experimentProfileId) throws SQLException {
        if(!this.experimentProfiles.idExists(experimentProfileId)) {
            return new HashMap<String, Object>();
        }

        ExperimentProfile experimentProfile = this.getExperimentProfileById(experimentProfileId);
        return experimentProfile.getStats();
    }

    private void doHousekeeping() throws SQLException {
    }

    public static final String USER_ID_ADMIN = "itecgo";
    public static final String USER_INITIAL_PASSWORD_ADMIN = "bywwnss";

    public static final String DATABASE_REVISION = "31";

    //    public static final String DATABASE_URL = "jdbc:h2:mem:account";
    public static final String DATABASE_URL = "jdbc:h2:~/.archimulator/data/v" + DATABASE_REVISION;
}
