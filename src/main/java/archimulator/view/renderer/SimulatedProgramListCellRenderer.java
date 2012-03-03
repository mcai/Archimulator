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
package archimulator.view.renderer;

import archimulator.model.simulation.SimulatedProgram;
import archimulator.service.ArchimulatorService;
import archimulator.service.ArchimulatorServletContextListener;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.*;

import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SimulatedProgramListCellRenderer implements ListitemRenderer<SimulatedProgram> {
    @Override
    public void render(Listitem item, final SimulatedProgram data, int index) throws Exception {
        Session session = Executions.getCurrent().getSession();
        HttpSession httpSession = (HttpSession) session.getNativeSession();
        final ArchimulatorService archimulatorService = ArchimulatorServletContextListener.getArchimulatorService(httpSession.getServletContext());

        item.appendChild(new Listcell(data.getId() + ""));
        item.appendChild(new Listcell(data.getCwd() + ""));
        item.appendChild(new Listcell(data.getExe() + ""));
        item.appendChild(new Listcell(data.getArgs() + ""));
        item.appendChild(new Listcell(data.getStdin() + ""));
        item.appendChild(new Listcell(data.getCreatedTimeAsString() + ""));

        Listcell listCellEdit = new Listcell();
        item.appendChild(listCellEdit);

        Button buttonEdit = new Button("编辑");
        buttonEdit.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                Map<String, Object> arg = new HashMap<String, Object>();

                arg.put("create", false);
                arg.put("simulatedProgram", data);

                Window win = (Window) Executions.createComponents("/edit/editSimulatedProgram.zul", null, arg);
                win.doModal();
            }
        });
        listCellEdit.appendChild(buttonEdit);

        Listcell listCellRemove = new Listcell();
        item.appendChild(listCellRemove);

        Button buttonRemove = new Button("删除");

        buttonRemove.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                Messagebox.show("确认删除编号为" + data.getId() + "的评测程序？", "删除评测程序", Messagebox.YES | Messagebox.NO, Messagebox.QUESTION,
                        new EventListener<Event>() {
                            public void onEvent(Event evt) throws SQLException {
                                switch ((Integer) evt.getData()) {
                                    case Messagebox.YES:
                                        archimulatorService.removeSimulatedProgramById(data.getId());
                                        Executions.sendRedirect(null);
                                        break;
                                }
                            }
                        });
            }
        });
        listCellRemove.appendChild(buttonRemove);
    }
}
