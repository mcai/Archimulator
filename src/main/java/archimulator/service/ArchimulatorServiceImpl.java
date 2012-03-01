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

import archimulator.model.experiment.profile.ExperimentProfile;
import archimulator.model.experiment.profile.ExperimentProfileState;
import archimulator.model.experiment.profile.ProcessorProfile;
import archimulator.model.simulation.SimulatedProgram;
import archimulator.model.user.User;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.table.TableUtils;
import it.sauronsoftware.cron4j.Scheduler;

import java.sql.SQLException;
import java.util.List;

public class ArchimulatorServiceImpl implements ArchimulatorService {
    private Dao<SimulatedProgram, Long> simulatedPrograms;
    private Dao<ProcessorProfile, Long> processorProfiles;
    private Dao<ExperimentProfile, Long> experimentProfiles;

    private Dao<User, String> users;

    private transient Scheduler scheduler;

    private JdbcPooledConnectionSource connectionSource;
    
    private boolean runningExperimentEnabled;

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
                this.setUserPassword(USER_ID_ADMIN, USER_PASSWORD_ADMIN);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void clearData() throws SQLException {
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
    public ExperimentProfile retrieveOneExperimentProfileToRun() throws SQLException {
        if(!this.runningExperimentEnabled) {
            return null;
        }

        PreparedQuery<ExperimentProfile> query = this.experimentProfiles.queryBuilder().where().eq("state", ExperimentProfileState.SUBMITTED).prepare();
        ExperimentProfile result = this.experimentProfiles.queryForFirst(query);
        if(result != null) {
            result.setState(ExperimentProfileState.RUNNING);
            this.experimentProfiles.update(result);
            return result;
        }
        
        return null;
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

    private void doHousekeeping() throws SQLException {
    }

    public static final String USER_ID_ADMIN = "itecgo";
    public static final String USER_PASSWORD_ADMIN = "1026@ustc";

    public static final String DATABASE_REVISION = "19";

    //    public static final String DATABASE_URL = "jdbc:h2:mem:account";
    public static final String DATABASE_URL = "jdbc:h2:~/.archimulator/data/v" + DATABASE_REVISION;
}
