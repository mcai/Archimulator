package archimulator.web.application;

import archimulator.model.User;
import archimulator.service.ServiceManager;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.Request;

public class ArchimulatorSession extends AuthenticatedWebSession {
    private User user;

    public ArchimulatorSession(Request request) {
        super(request);
    }

    @Override
    public Roles getRoles() {
        return null;
    }

    @Override
    public boolean authenticate(String username, String password) {
        User userFound = ServiceManager.getUserService().getUserByEmail(username);

        if(userFound != null && userFound.getPassword().equals(password)) {
            user = userFound;
            return true;
        }

        return false;
    }

    @Override
    public void signOut() {
        super.signOut();
        user = null;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
