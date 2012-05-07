package archimulator.util.event.future;

import archimulator.util.action.Action;
import archimulator.util.action.Action1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Future {
    private boolean completed;
    private boolean error;

    private List<Action> onCompletedCallbacks;
    private List<Action1<Exception>> onFailedCallbacks;

    private Semaphore completedSem = new Semaphore(0, true);

    public Future() {
        this.onCompletedCallbacks = new ArrayList<Action>();
        this.onFailedCallbacks = new ArrayList<Action1<Exception>>();
    }

    public boolean awaitForCompletion() {
        try {
            this.completedSem.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return !error;
    }

    public void notifyCompletion() {
        for (Action onCompletedCallback : this.onCompletedCallbacks) {
            onCompletedCallback.apply();
        }
        this.completed = true;
        this.error = false;

        this.completedSem.release();
    }

    public void notifyFailure(Exception e) {
        for (Action1<Exception> onFailedCallback : this.onFailedCallbacks) {
            onFailedCallback.apply(e);
        }
        this.completed = true;
        this.error = true;

        this.completedSem.release();
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isError() {
        return error;
    }

    public Future addOnCompletedCallback(Action onCompletedCallback) {
        this.onCompletedCallbacks.add(onCompletedCallback);
        return this;
    }

    public Future addOnFailedCallback(Action1<Exception> onFailedCallback) {
        this.onFailedCallbacks.add(onFailedCallback);
        return this;
    }
}
