package archimulator.view;

import org.zkoss.lang.Threads;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Separator;

public class ServerPush {
    public static void start(Component info) throws InterruptedException {
        Desktop desktop = Executions.getCurrent().getDesktop();
        if (desktop.isServerPushEnabled()) {
            Messagebox.show("Already started");
        } else {
            desktop.enableServerPush(true);
            new WorkingThread(info).start();
        }
    }

    public static void stop() throws InterruptedException {
        Desktop desktop = Executions.getCurrent().getDesktop();
        if (desktop.isServerPushEnabled()) {
            desktop.enableServerPush(false);
        } else {
            Messagebox.show("Already stopped");
        }
    }

    public static void updateInfo(Component info) {
        Integer i = (Integer) info.getAttribute("count");
        int v = i == null ? 0 : i + 1;

        while (info.getChildren().size() > 10) {
            info.getChildren().get(0).detach();
            info.getChildren().get(0).detach();
        }
        info.setAttribute("count", new Integer(v));

        info.appendChild(new Label(" " + v));

        Separator separator = new Separator();
        separator.setBar(true);
        info.appendChild(separator);
    }
    
    private static class WorkingThread extends Thread {
        private final Desktop desktop;
        private final Component info;

        private WorkingThread(Component info) {
            this.desktop = info.getDesktop();
            this.info = info;
        }

        public void run() {
            try {
                while (desktop.isAlive() && desktop.isServerPushEnabled()) {
                    Executions.activate(desktop);

                    try {
                        updateInfo(info);
                    } catch (RuntimeException ex) {
                        System.out.println(ex);
                        throw ex;
                    } catch (Error ex) {
                        System.out.println(ex);
                        throw ex;
                    } finally {
                        Executions.deactivate(desktop);
                    }

                    Threads.sleep(2000); //Update every two seconds
                }
            } catch (InterruptedException ex) {
                System.out.println(ex);
            }
        }
    }
}