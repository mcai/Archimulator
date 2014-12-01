/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.web.component.help;

import archimulator.model.ExperimentState;
import archimulator.service.ServiceManager;
import archimulator.web.application.ArchimulatorSession;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.util.Pair;
import net.pickapack.util.StorageUnit;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * System status panel.
 *
 * @author Min Cai
 */
public class PanelSystemStatus extends Panel {
    /**
     * Create a system status panel.
     *
     * @param id the markup ID of the panel that is to be created
     */
    public PanelSystemStatus(String id) {
        super(id);

        final IDataProvider<Pair<String, String>> dataProvider = new IDataProvider<Pair<String, String>>() {
            private List<Pair<String, String>> systemStatusList;

            @Override
            public Iterator<? extends Pair<String, String>> iterator(long first, long count) {
                return getSystemStatusList().iterator();
            }

            @Override
            public long size() {
                return getSystemStatusList().size();
            }

            @Override
            public IModel<Pair<String, String>> model(final Pair<String, String> object) {
                return new LoadableDetachableModel<Pair<String, String>>(object) {
                    @Override
                    protected Pair<String, String> load() {
                        return object;
                    }
                };
            }

            @Override
            public void detach() {
                systemStatusList = null;
            }

            public List<Pair<String, String>> getSystemStatusList() {
                if (systemStatusList == null) {
                    systemStatusList = new ArrayList<>();

                    systemStatusList.add(new Pair<>("OS Arch", ManagementFactory.getOperatingSystemMXBean().getArch()));
                    systemStatusList.add(new Pair<>("OS Name", ManagementFactory.getOperatingSystemMXBean().getName()));
                    systemStatusList.add(new Pair<>("OS Version", ManagementFactory.getOperatingSystemMXBean().getVersion()));
                    systemStatusList.add(new Pair<>("Current Time", DateHelper.toString(new Date())));
                    systemStatusList.add(new Pair<>("# Processors", Runtime.getRuntime().availableProcessors() + ""));
                    systemStatusList.add(new Pair<>("JVM Max Memory", StorageUnit.toString(Runtime.getRuntime().maxMemory())));
                    systemStatusList.add(new Pair<>("JVM Total Memory", StorageUnit.toString(Runtime.getRuntime().totalMemory())));
                    systemStatusList.add(new Pair<>("JVM Free Memory", StorageUnit.toString(Runtime.getRuntime().freeMemory())));

                    if (ArchimulatorSession.get().isSignedIn()) {
                        systemStatusList.add(new Pair<>("# Running Experiments", ServiceManager.getExperimentService().getNumAllExperimentsByState(ExperimentState.RUNNING) + ""));
                    }
                }

                return systemStatusList;
            }
        };

        add(new DataView<Pair<String, String>>("systemStatus", dataProvider) {
            {
                setItemsPerPage(10);
            }

            protected void populateItem(Item<Pair<String, String>> item) {
                Pair<String, String> pair = item.getModelObject();

                item.setDefaultModel(new CompoundPropertyModel<>(pair));

                item.add(new Label("first"));
                item.add(new Label("second"));
            }
        });
    }
}
