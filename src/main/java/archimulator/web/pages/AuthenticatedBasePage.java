package archimulator.web.pages;

import archimulator.web.application.ArchimulatorSession;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public abstract class AuthenticatedBasePage extends BasePage {
    public AuthenticatedBasePage(PageParameters parameters) {
        super(parameters);
    }

    public ArchimulatorSession getArchimulatorSession() {
        return (ArchimulatorSession) getSession();
    }
}
