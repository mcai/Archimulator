package archimulator.service;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class ArchimulatorServer {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            return;
        }

        Server server = new Server(Integer.parseInt(args[0]));

        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");

        webapp.setWar(args[1]);
        webapp.setExtractWAR(false);

        server.setHandler(webapp);

        server.start();
        server.join();
    }
}
