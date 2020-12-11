package by.issoft.training;

/**
 * Arguments to callback which is called upon elevator arrival
 */
public class CallCompleteArgs {
    private final Elevator elevator;
    private final Object monitor;

    public Elevator getElevator() {
        return elevator;
    }

    public Object getMonitor() {
        return monitor;
    }

    public CallCompleteArgs(Elevator elevator, Object monitor) {
        this.elevator = elevator;
        this.monitor = monitor;
    }
}