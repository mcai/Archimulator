package archimulator.service;

import archimulator.model.experiment.ExperimentBuilder;
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
    private Dao<ExperimentBuilder.ExperimentProfile, Long> experimentProfiles;

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

            TableUtils.createTableIfNotExists(this.connectionSource, ExperimentBuilder.ExperimentProfile.class);
            TableUtils.createTableIfNotExists(this.connectionSource, User.class);

            this.experimentProfiles = DaoManager.createDao(this.connectionSource, ExperimentBuilder.ExperimentProfile.class);
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
    public void addExperimentProfile(ExperimentBuilder.ExperimentProfile experimentProfile) throws SQLException {
        this.experimentProfiles.create(experimentProfile);
    }
    
    @Override
    public List<ExperimentBuilder.ExperimentProfile> getExperimentProfilesAsList() throws SQLException {
        return this.experimentProfiles.queryForAll();
    }

    @Override
    public ExperimentBuilder.ExperimentProfile retrieveOneExperimentProfileToRun() throws SQLException {
        if(!this.runningExperimentEnabled) {
            return null;
        }

        PreparedQuery<ExperimentBuilder.ExperimentProfile> query = this.experimentProfiles.queryBuilder().where().eq("state", ExperimentBuilder.ExperimentProfileState.SUBMITTED).prepare();
        ExperimentBuilder.ExperimentProfile result = this.experimentProfiles.queryForFirst(query);
        if(result != null) {
            result.setState(ExperimentBuilder.ExperimentProfileState.RUNNING);
            this.experimentProfiles.update(result);
            return result;
        }
        
        return null;
    }

    @Override
    public void notifyExperimentStopped(long experimentProfileId) throws SQLException {
        ExperimentBuilder.ExperimentProfile profile = this.experimentProfiles.queryForId(experimentProfileId);
        if(profile == null || profile.getState() == ExperimentBuilder.ExperimentProfileState.SUBMITTED) {
            return;
        }

        profile.setState(ExperimentBuilder.ExperimentProfileState.STOPPED);
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

    public static final String DATABASE_REVISION = "18";

    //    public static final String DATABASE_URL = "jdbc:h2:mem:account";
    public static final String DATABASE_URL = "jdbc:h2:~/.archimulator/data/v" + DATABASE_REVISION;
}
