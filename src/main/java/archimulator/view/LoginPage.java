package archimulator.view;

import archimulator.service.ArchimulatorService;
import archimulator.service.ArchimulatorServletContextListener;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import javax.servlet.http.HttpSession;
import java.sql.SQLException;

public class LoginPage extends GenericForwardComposer<Window> {
    Textbox textboxUserId;
    Textbox textboxPassword;

    public void onOK() throws SQLException {
        HttpSession httpSession = (HttpSession) session.getNativeSession();
        ArchimulatorService archimulatorService = ArchimulatorServletContextListener.getArchimulatorService(httpSession.getServletContext());
        if (archimulatorService.authenticateUser(textboxUserId.getText(), textboxPassword.getText())) {
            Session session = Sessions.getCurrent();
            session.setAttribute("userId", textboxUserId.getValue());
            Executions.sendRedirect("index.zul");
        } else {
            Messagebox.show("用户名或密码错误，请重新输入！", "用户登录", Messagebox.OK, Messagebox.EXCLAMATION, new EventListener<Event>() {
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
    }

    public void onCancel() {
    }
}
