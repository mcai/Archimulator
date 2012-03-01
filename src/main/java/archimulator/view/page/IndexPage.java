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

import archimulator.service.ArchimulatorService;
import archimulator.service.ArchimulatorServiceImpl;
import archimulator.service.ArchimulatorServletContextListener;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import javax.servlet.http.HttpSession;
import java.sql.SQLException;

public class IndexPage extends GenericForwardComposer<Window> {
    Checkbox checkboxRunningExperimentEnabled;
    Textbox textboxChangePassword;

    @Override
    public void doAfterCompose(Window comp) throws Exception {
        super.doAfterCompose(comp);

        HttpSession httpSession = (HttpSession) session.getNativeSession();
        ArchimulatorService archimulatorService = ArchimulatorServletContextListener.getArchimulatorService(httpSession.getServletContext());

        this.checkboxRunningExperimentEnabled.setChecked(archimulatorService.isRunningExperimentEnabled());
    }

    public void onCheck$checkboxRunningExperimentEnabled(Event event) {
        HttpSession httpSession = (HttpSession) session.getNativeSession();
        ArchimulatorService archimulatorService = ArchimulatorServletContextListener.getArchimulatorService(httpSession.getServletContext());
        archimulatorService.setRunningExperimentEnabled(this.checkboxRunningExperimentEnabled.isChecked());
        Executions.sendRedirect(null);
    }
    
    public void onClick$buttonChangePassword(Event event) throws SQLException {
        String text = this.textboxChangePassword.getText();
        if(text != null && !text.isEmpty()) {
            HttpSession httpSession = (HttpSession) session.getNativeSession();
            ArchimulatorService archimulatorService = ArchimulatorServletContextListener.getArchimulatorService(httpSession.getServletContext());
            archimulatorService.setUserPassword(ArchimulatorServiceImpl.USER_ID_ADMIN, text);

            Messagebox.show("密码修改成功！", "修改密码", Messagebox.OK, Messagebox.EXCLAMATION, new EventListener<Event>() {
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
            Messagebox.show("新密码不能为空！", "修改密码", Messagebox.OK, Messagebox.EXCLAMATION);
        }
    }

    public void onClick$buttonClearData(Event event) {
        Messagebox.show("数据清空后将无法恢复，确认清空数据吗？", "清空数据", Messagebox.YES | Messagebox.NO, Messagebox.EXCLAMATION,
                new EventListener<Event>() {
                    public void onEvent(Event evt) throws SQLException {
                        switch ((Integer) evt.getData()) {
                            case Messagebox.YES:
                                clearData();
                                break;
                        }
                    }
                }
        );
    }

    private void clearData() throws SQLException {
        HttpSession httpSession = (HttpSession) this.session.getNativeSession();
        ArchimulatorServletContextListener.getArchimulatorService(httpSession.getServletContext()).clearData();

        Executions.sendRedirect(null);
    }
}
