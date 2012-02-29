package archimulator.service;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ArchimulatorServletContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ArchimulatorService archimulatorService = new ArchimulatorServiceImpl();
        sce.getServletContext().setAttribute("archimulatorService", archimulatorService);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        getArchimulatorService(sce.getServletContext()).stop();
        sce.getServletContext().removeAttribute("archimulatorService");
    }

    @SuppressWarnings("unchecked")
    public static ArchimulatorService getArchimulatorService(ServletContext context) {
        return (ArchimulatorService) context.getAttribute("archimulatorService");
    }
}
