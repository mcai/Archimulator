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
