package by.issoft.training;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Model of a real elevator controller.
 * Keeps track of all active calls and state of every elevator.
 */
public class ElevatorController {
    private final Queue<Call> pendingCalls = new ArrayDeque<Call>();
    private final List<Elevator> elevators;

    public List<Elevator> getElevators() {
        return elevators;
    }

    public ElevatorController(int elevatorCount) {
        elevators = IntStream.range(0, elevatorCount)
            .mapToObj(i -> new Elevator(this))
            .collect(Collectors.toList());
    }

    public ElevatorController(Collection<Elevator> elevators) {
        this.elevators = new ArrayList<>(elevators);
        for (Elevator elevator : this.elevators) {
            elevator.bindController(this);
        }
    }

    /**
     * Response to a button press
     */
    public void processCall(Call call) {
        try {
            Elevator closestIdle = elevators.stream()
            .filter(e -> e.getState() == Elevator.State.IDLE)
            .min((a,b)-> Integer.compare(
                Math.abs(a.getCurrentFloor() - call.getFloor()),
                Math.abs(b.getCurrentFloor() - call.getFloor()))).get();
            App.logger.info("Closest idle elevator for call " + call + " is " + closestIdle + " at floor " + closestIdle.getCurrentFloor());
            closestIdle.dispatchTo(call);
        } catch (NoSuchElementException e) {
            App.logger.info("No idle elevators to respond to call " + call);
            pendingCalls.add(call);
        }
    }

    /**
     * Called by elevators when they complete all tasks
     */
    public boolean tryTakeNewCall(Elevator elevator) {
        if (pendingCalls.isEmpty()) {
            return false;
        }
        elevator.dispatchTo(pendingCalls.remove());
        return true;
    }
}