package archimulator.model.base;

public class SimulationObjectLogger extends Logger {
    private SimulationObject simulationObject;

    public SimulationObjectLogger(SimulationObject simulationObject) {
        this.simulationObject = simulationObject;
    }

    @Override
    protected long getCurrentCycle() {
        return this.simulationObject.getCycleAccurateEventQueue().getCurrentCycle();
    }
}
