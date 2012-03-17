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
package archimulator.view.page;

import archimulator.sim.base.experiment.profile.ProcessorProfile;
import archimulator.sim.uncore.cache.eviction.EvictionPolicy;
import archimulator.sim.uncore.cache.eviction.LeastRecentlyUsedEvictionPolicy;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Window;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProcessorProfilesPage extends GenericForwardComposer<Window> {
    private Button buttonAddProcessorProfile;

    public void onClick$buttonAddProcessorProfile(Event event) throws SQLException {
        Map<String, Object> arg = new HashMap<String, Object>();

        arg.put("create", true);
        Class<? extends EvictionPolicy> l2EvictionPolicyClz = LeastRecentlyUsedEvictionPolicy.class;
        arg.put("processorProfile", new ProcessorProfile(UUID.randomUUID().toString(), 2, 2, 1024 * 1024 * 4, 8, l2EvictionPolicyClz));

        Window win = (Window) Executions.createComponents("/edit/editProcessorProfile.zul", null, arg);
        win.doModal();
    }
}
