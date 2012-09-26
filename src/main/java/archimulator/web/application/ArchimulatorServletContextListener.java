package archimulator.web.application;

import archimulator.service.ServiceManager;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ArchimulatorServletContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServiceManager.getExperimentService().start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServiceManager.getExperimentService().stop();
    }
}
