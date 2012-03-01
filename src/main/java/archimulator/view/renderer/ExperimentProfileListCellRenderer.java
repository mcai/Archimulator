package archimulator.view.renderer;

import archimulator.model.experiment.profile.ExperimentProfile;
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
        item.appendChild(new Listcell(data.getPthreadSpawnedIndex() + ""));
        item.appendChild(new Listcell(data.getMaxInsts() + ""));
        item.appendChild(new Listcell(data.getCreatedTimeAsString() + ""));

        Listcell listCellEdit = new Listcell();
        item.appendChild(listCellEdit);

        Button buttonEdit = new Button("编辑");
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
        listCellEdit.appendChild(buttonEdit);

        Listcell listCellRemove = new Listcell();
        item.appendChild(listCellRemove);

        Button buttonRemove = new Button("删除");

        buttonRemove.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                Messagebox.show("确认删除编号为" + data.getId() + "的实验？", "删除实验", Messagebox.YES | Messagebox.NO, Messagebox.QUESTION,
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
        listCellRemove.appendChild(buttonRemove);
    }
}
