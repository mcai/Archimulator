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

import archimulator.model.experiment.profile.ExperimentProfile;
import archimulator.model.experiment.profile.ProcessorProfile;
import archimulator.service.ArchimulatorService;
import archimulator.service.ArchimulatorServletContextListener;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ExperimentProfilesPage extends GenericForwardComposer<Window> {
    private Button buttonAddExperimentProfile;

    public void onClick$buttonAddExperimentProfile(Event event) throws SQLException {
        HttpSession httpSession = (HttpSession) session.getNativeSession();
        ArchimulatorService archimulatorService = ArchimulatorServletContextListener.getArchimulatorService(httpSession.getServletContext());

        if(archimulatorService.getProcessorProfilesAsList().isEmpty()) {
            Messagebox.show("Processor profile list is empty, please add one processor profile and try again!", "Add Experiment Profile", Messagebox.OK, Messagebox.EXCLAMATION, new EventListener<Event>() {
                @Override
                public void onEvent(Event event) throws Exception {
                    switch ((Integer) event.getData()) {
                        case Messagebox.OK:
                            Executions.sendRedirect(null);
                            break;
                    }
                }
            });
        }
        else {
            ProcessorProfile processorProfile = archimulatorService.getProcessorProfilesAsList().get(0);

            Map<String, Object> arg = new HashMap<String, Object>();

            arg.put("create", true);
            ExperimentProfile experimentProfile = new ExperimentProfile(processorProfile);
            experimentProfile.setPthreadSpawnedIndex(3720);
            experimentProfile.setMaxInsts(2000000000);
            arg.put("experimentProfile", experimentProfile);

            Window win = (Window) Executions.createComponents("/edit/editExperimentProfile.zul", null, arg);
            win.doModal();
        }
    }
}
