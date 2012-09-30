/*******************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.web.components.help;

import archimulator.model.ExperimentState;
import archimulator.service.ServiceManager;
import archimulator.web.application.ArchimulatorSession;
import net.pickapack.Pair;
import net.pickapack.StorageUnit;
import net.pickapack.dateTime.DateHelper;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class PanelSystemStatus extends Panel {
    public PanelSystemStatus(String id) {
        super(id);

        final List<Pair<String, String>> systemStatusList = new ArrayList<Pair<String, String>>();
        systemStatusList.add(new Pair<String, String>("OS Arch", ManagementFactory.getOperatingSystemMXBean().getArch()));
        systemStatusList.add(new Pair<String, String>("OS Name", ManagementFactory.getOperatingSystemMXBean().getName()));
        systemStatusList.add(new Pair<String, String>("OS Version", ManagementFactory.getOperatingSystemMXBean().getVersion()));
        systemStatusList.add(new Pair<String, String>("Current Time", DateHelper.toString(new Date())));
        systemStatusList.add(new Pair<String, String>("# Processors", Runtime.getRuntime().availableProcessors() + ""));
        systemStatusList.add(new Pair<String, String>("JVM Max Memory", StorageUnit.toString(Runtime.getRuntime().maxMemory())));
        systemStatusList.add(new Pair<String, String>("JVM Total Memory", StorageUnit.toString(Runtime.getRuntime().totalMemory())));
        systemStatusList.add(new Pair<String, String>("JVM Free Memory", StorageUnit.toString(Runtime.getRuntime().freeMemory())));

        if(ArchimulatorSession.get().isSignedIn()) {
            systemStatusList.add(new Pair<String, String>("# Running Experiments", ServiceManager.getExperimentService().getNumAllExperimentsByState(ExperimentState.RUNNING) + ""));
        }

        IDataProvider<Pair<String, String>> dataProvider = new IDataProvider<Pair<String, String>>() {
            @Override
            public Iterator<? extends Pair<String, String>> iterator(long first, long count) {
                return systemStatusList.iterator();
            }

            @Override
            public long size() {
                return systemStatusList.size();
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
            }
        };

        final DataView<Pair<String, String>> rowExperiment = new DataView<Pair<String, String>>("row_system_status", dataProvider) {
            protected void populateItem(Item<Pair<String, String>> item) {
                Pair<String, String> pair = item.getModelObject();

                item.add(new Label("cell_key", pair.getFirst()));
                item.add(new Label("cell_value", pair.getSecond()));
            }
        };
        rowExperiment.setItemsPerPage(10);

        add(new WebMarkupContainer("table_system_status") {{
            add(rowExperiment);
        }});
    }
}
