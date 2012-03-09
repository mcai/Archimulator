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

import archimulator.model.experiment.profile.ExperimentProfile;
import archimulator.model.experiment.profile.ExperimentProfileState;
import archimulator.model.experiment.profile.ExperimentProfileType;
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

public class ExperimentProfileListCellRenderer implements ListitemRenderer<ExperimentProfile> {
    @Override
    public void render(Listitem item, final ExperimentProfile data, int index) throws Exception {
        Session session = Executions.getCurrent().getSession();
        HttpSession httpSession = (HttpSession) session.getNativeSession();
        final ArchimulatorService archimulatorService = ArchimulatorServletContextListener.getArchimulatorService(httpSession.getServletContext());

        item.appendChild(new Listcell(data.getId() + ""));
        item.appendChild(new Listcell(data.getType() + ""));
        item.appendChild(new Listcell(data.getType() == ExperimentProfileType.CHECKPOINTED_EXPERIMENT ? data.getPthreadSpawnedIndex() + "" : "N/A"));
        item.appendChild(new Listcell(data.getType() == ExperimentProfileType.CHECKPOINTED_EXPERIMENT ? data.getMaxInsts() + "" : "N/A"));
        item.appendChild(new Listcell(data.getCreatedTimeAsString() + ""));
        item.appendChild(new Listcell(data.getState() + ""));

        Listcell listCellOperations = new Listcell();
        item.appendChild(listCellOperations);

        addButtonEdit(listCellOperations, data);
        addButtonRemove(listCellOperations, data, archimulatorService);
        
        if(data.getState() == ExperimentProfileState.RUNNING) {
            addButtonPause(listCellOperations, data, archimulatorService);
        }

        if(data.getState() == ExperimentProfileState.PAUSED) {
            addButtonResume(listCellOperations, data, archimulatorService);
        }

        if(data.getState() == ExperimentProfileState.RUNNING || data.getState() == ExperimentProfileState.PAUSED) {
            addButtonStop(listCellOperations, data, archimulatorService);
        }

        if(data.getState() != ExperimentProfileState.SUBMITTED) {
            addButtonStats(listCellOperations, data, archimulatorService);
        }
    }

    private void addButtonEdit(Listcell listCellOperations, final ExperimentProfile data) {
        Button buttonEdit = new Button("Edit");
        buttonEdit.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                Map<String, Object> arg = new HashMap<String, Object>();

                arg.put("create", false);
                arg.put("experimentProfile", data);

                Window win = (Window) Executions.createComponents("/edit/editExperimentProfile.zul", null, arg);
                win.doModal();
            }
        });
        listCellOperations.appendChild(buttonEdit);
    }

    private void addButtonRemove(Listcell listCellOperations, final ExperimentProfile data, final ArchimulatorService archimulatorService) {
        Button buttonRemove = new Button("Remove");
        buttonRemove.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                Messagebox.show("Are you sure to remove experiment profile (id: " + data.getId() + ")?", "Remove Experiment Profile", Messagebox.YES | Messagebox.NO, Messagebox.QUESTION,
                        new EventListener<Event>() {
                            public void onEvent(Event evt) throws SQLException {
                                switch ((Integer) evt.getData()) {
                                    case Messagebox.YES:
                                        archimulatorService.removeExperimentProfileById(data.getId());
                                        Executions.sendRedirect(null);
                                        break;
                                }
                            }
                        });
            }
        });
        listCellOperations.appendChild(buttonRemove);
    }

    private void addButtonPause(Listcell listCellOperations, final ExperimentProfile data, final ArchimulatorService archimulatorService) {
        Button buttonPause = new Button("Pause");

        buttonPause.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                Messagebox.show("Are you sure to pause experiment profile (id: " + data.getId() + ")?", "Pause Experiment Profile", Messagebox.YES | Messagebox.NO, Messagebox.QUESTION,
                        new EventListener<Event>() {
                            public void onEvent(Event evt) throws SQLException {
                                switch ((Integer) evt.getData()) {
                                    case Messagebox.YES:
                                        archimulatorService.pauseExperimentById(data.getId());
                                        Executions.sendRedirect(null);
                                        break;
                                }
                            }
                        });
            }
        });
        listCellOperations.appendChild(buttonPause);
    }

    private void addButtonResume(Listcell listCellOperations, final ExperimentProfile data, final ArchimulatorService archimulatorService) {
        Button buttonResume = new Button("Resume");

        buttonResume.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                Messagebox.show("Are you sure to resume experiment profile (id: " + data.getId() + ")?", "Resume Experiment Profile", Messagebox.YES | Messagebox.NO, Messagebox.QUESTION,
                        new EventListener<Event>() {
                            public void onEvent(Event evt) throws SQLException {
                                switch ((Integer) evt.getData()) {
                                    case Messagebox.YES:
                                        archimulatorService.resumeExperimentById(data.getId());
                                        Executions.sendRedirect(null);
                                        break;
                                }
                            }
                        });
            }
        });
        listCellOperations.appendChild(buttonResume);
    }

    private void addButtonStop(Listcell listCellOperations, final ExperimentProfile data, final ArchimulatorService archimulatorService) {
        Button buttonStop = new Button("Stop");

        buttonStop.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                Messagebox.show("Are you sure to stop experiment profile (id: " + data.getId() + ")?", "Stop Experiment Profile", Messagebox.YES | Messagebox.NO, Messagebox.QUESTION,
                        new EventListener<Event>() {
                            public void onEvent(Event evt) throws SQLException {
                                switch ((Integer) evt.getData()) {
                                    case Messagebox.YES:
                                        archimulatorService.stopExperimentById(data.getId());
                                        Executions.sendRedirect(null);
                                        break;
                                }
                            }
                        });
            }
        });
        listCellOperations.appendChild(buttonStop);
    }

    private void addButtonStats(Listcell listCellOperations, final ExperimentProfile data, final ArchimulatorService archimulatorService) {
        Button buttonStats = new Button("Stats");

        buttonStats.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                Map<String, Object> arg = new HashMap<String, Object>();

                arg.put("experimentProfile", data);

                Window win = (Window) Executions.createComponents("/edit/viewExperimentStats.zul", null, arg);
                win.doModal();
            }
        });
        listCellOperations.appendChild(buttonStats);
    }
}
