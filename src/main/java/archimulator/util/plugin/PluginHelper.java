/*******************************************************************************
 * Copyright (c) 2010-2013 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.util.plugin;

import ro.fortsoft.pf4j.*;

import java.util.List;

/**
 * Plugin helper.
 *
 * @author Min Cai
 */
public class PluginHelper {
    /**
     * Entry point.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        PluginManager pluginManager = new DefaultPluginManager();
        pluginManager.loadPlugins();
        pluginManager.startPlugins();

        List<SimulatorExtensionPoint> simulatorExtensionPoints = pluginManager.getExtensions(SimulatorExtensionPoint.class);
        for (SimulatorExtensionPoint simulatorExtensionPoint : simulatorExtensionPoints) {
            System.out.println(simulatorExtensionPoint.getName() + ": ");
            simulatorExtensionPoint.start();
        }

        for (SimulatorExtensionPoint simulatorExtensionPoint : simulatorExtensionPoints) {
            System.out.println(simulatorExtensionPoint.getName() + ": ");
            simulatorExtensionPoint.stop();
        }
    }

    /**
     * Simulator extension point.
     */
    public static interface SimulatorExtensionPoint extends ExtensionPoint {
        /**
         * Get the name.
         *
         * @return the name
         */
        String getName();

        /**
         * Start.
         */
        void start();

        /**
         * Stop.
         */
        void stop();
    }

    /**
     * Sample simulator plugin.
     */
    public static class SampleSimulatorPlugin extends Plugin {
        /**
         * Create a sample simulator plugin.
         *
         * @param wrapper the plugin wrapper
         */
        public SampleSimulatorPlugin(PluginWrapper wrapper) {
            super(wrapper);
        }

        /**
         * Sample simulator extension.
         */
        @Extension
        public static class SampleSimulatorExtension implements SimulatorExtensionPoint {
            public String getName() {
                return "Sample Simulator Extension";
            }

            @Override
            public void start() {
                System.out.println("  Started.");
            }

            @Override
            public void stop() {
                System.out.println("  Stopped.");
            }
        }
    }
}
