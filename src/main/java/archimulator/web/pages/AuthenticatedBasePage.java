package archimulator.web.pages;

import archimulator.web.application.ArchimulatorSession;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public abstract class AuthenticatedBasePage extends BasePage {
    public AuthenticatedBasePage(PageType pageType, PageParameters parameters) {
        super(pageType, parameters);
    }

    public ArchimulatorSession getArchimulatorSession() {
        return (ArchimulatorSession) getSession();
    }
}
