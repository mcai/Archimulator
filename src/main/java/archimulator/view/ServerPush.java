package archimulator.view;

import archimulator.util.action.Action1;
import org.zkoss.lang.Threads;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Messagebox;

public class ServerPush {
    public static <ComponentT extends Component> void start(ComponentT info, Action1<ComponentT> onUpdateCallback) throws InterruptedException {
        Desktop desktop = Executions.getCurrent().getDesktop();
        if (desktop.isServerPushEnabled()) {
            Messagebox.show("Already started");
        } else {
            desktop.enableServerPush(true);
            new WorkingThread<ComponentT>(info, onUpdateCallback).start();
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

//    public static void updateInfo(Component info) {
//        Integer i = (Integer) info.getAttribute("count");
//        int v = i == null ? 0 : i + 1;
//
//        while (info.getChildren().size() > 10) {
//            info.getChildren().get(0).detach();
//            info.getChildren().get(0).detach();
//        }
//        info.setAttribute("count", new Integer(v));
//
//        info.appendChild(new Label(" " + v));
//
//        Separator separator = new Separator();
//        separator.setBar(true);
//        info.appendChild(separator);
//    }
    
    private static class WorkingThread<ComponentT extends Component> extends Thread {
        private Desktop desktop;
        private ComponentT info;
        private Action1<ComponentT> onUpdateCallback;

        private WorkingThread(ComponentT info, Action1<ComponentT> onUpdateCallback) {
            this.onUpdateCallback = onUpdateCallback;
            this.desktop = info.getDesktop();
            this.info = info;
        }

        public void run() {
            try {
                while (desktop.isAlive() && desktop.isServerPushEnabled()) {
                    Executions.activate(desktop);

                    try {
                        onUpdateCallback.apply(info);
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