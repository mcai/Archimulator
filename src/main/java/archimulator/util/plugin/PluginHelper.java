package archimulator.util.plugin;

import ro.fortsoft.pf4j.DefaultPluginManager;
import ro.fortsoft.pf4j.PluginManager;

import java.util.List;

/**
 * Plugin helper.
 *
 * @author Min Cai
 */
public class PluginHelper {
    private PluginManager pluginManager;

    /**
     * Create a plugin helper.
     */
    public PluginHelper() {
        this.pluginManager = new DefaultPluginManager();
        this.pluginManager.loadPlugins();
        this.pluginManager.startPlugins();
    }

    /**
     * Return the plugin manager.
     *
     * @return the plugin manager
     */
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    /**
     * Entry point.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        PluginHelper pluginHelper = new PluginHelper();

        List<SimulationExtensionPoint> simulationExtensionPoints = pluginHelper.getPluginManager().getExtensions(SimulationExtensionPoint.class);
        for (SimulationExtensionPoint simulationExtensionPoint : simulationExtensionPoints) {
            simulationExtensionPoint.onStarted(null);
        }
    }
}
