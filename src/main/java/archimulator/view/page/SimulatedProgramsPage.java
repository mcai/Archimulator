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

import archimulator.model.simulation.SimulatedProgram;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Window;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SimulatedProgramsPage extends GenericForwardComposer<Window> {
    private Button buttonAddSimulatedProgram;

    public void onClick$buttonAddSimulatedProgram(Event event) throws SQLException {
        Map<String, Object> arg = new HashMap<String, Object>();

        arg.put("create", true);
        arg.put("simulatedProgram", new SimulatedProgram(UUID.randomUUID().toString(), "", "", ""));

        Window win = (Window) Executions.createComponents("/edit/editSimulatedProgram.zul", null, arg);
        win.doModal();
    }
}
