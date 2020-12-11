package by.issoft.training;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Represents a real elevator.
 */
public class Elevator {
    public enum State {
        IDLE, MOVING, OPENING, OPEN, CLOSING
    }

    private static final int CAPACITY = 500;
    private static final long TRAVEL_TIME = 3000;
    private static final int OPEN_TIME = 2000;
    private static final int CLOSE_TIME = 2000;
    private static final TimeUnit UNIT = TimeUnit.MILLISECONDS;

    // autoincrementing id
    private static int globalId = 0;
    private final int id;

    private final int capacity; // mass threshold

    private final long travelTime; // between 2 adjacent floors
    private final long openTime;
    private final long closeTime;
    private final TimeUnit unit;

    private final Object monitor = new Object();
    // represents pressed buttons inside elevator
    private final Collection<Integer> orders = new HashSet<>();
    
    private ElevatorController elevatorController;
    private State state = State.IDLE;
    private int currentFloor;
    private Call call;
    private Consumer<CallCompleteArgs> callback;
    private int mass;
    private int closestOrder;
    private int done;
    private boolean boardingFailure;

    public State getState() {
        return state;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public Call getCall() {
        return call;
    }

    public Collection<Integer> getOrders() {
        return orders;
    }

    public int getMass() {
        return mass;
    }

    public int getDone() {
        return done;
    }

    public void bindController(ElevatorController elevatorController) {
        this.elevatorController = elevatorController;
    }

    public Elevator(ElevatorController elevatorController) {
        this(0, CAPACITY, TRAVEL_TIME, OPEN_TIME, CLOSE_TIME, UNIT, elevatorController);
    }

    public Elevator(int floor) {
        this(floor, CAPACITY, TRAVEL_TIME, OPEN_TIME, CLOSE_TIME, UNIT, null);
    }

    public Elevator(int floor, int capacity, long travelTime,
            long openTime, long closeTime, TimeUnit unit,
            ElevatorController elevatorController) {
        id = globalId++;
        this.capacity = capacity;
        this.travelTime = travelTime;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.unit = unit;
        this.elevatorController = elevatorController;
        currentFloor = floor;
    }

    public void dispatchTo(Call call) {
        this.call = call;
        callback = call.getCallback();
        if (call.getFloor() == currentFloor) {
            openDoors();
        } else {
            state = State.MOVING;
            App.logger.info(this + " is now moving");
            moveToNextFloor();
        }
    }

    private void moveToNextFloor() {
        App.schedule(this::onArrivedOnNextFloor, travelTime, unit);
    }

    private void openDoors() {
        state = State.OPENING;
        App.schedule(this::onDoorsOpen, openTime, unit);
    }

    private void onArrivedOnNextFloor() {
        int destination = (call == null) ? closestOrder : call.getFloor();
        if (currentFloor < destination) {
            currentFloor++;
        } else {
            currentFloor--;
        }
        App.logger.info(this + " arrived at floor " + currentFloor);
        if (destination == currentFloor) {
            if (call == null) {
                orders.remove(closestOrder);
                if (!orders.isEmpty()) {
                    recalculateClosestOrder();
                }
            }
            openDoors();
        } else {
            moveToNextFloor();
        }
    }

    private void onDoorsOpen() {
        App.logger.info(this+" opened doors");
        state = State.OPEN;
        CompletableFuture<Void> future = CompletableFuture
            .runAsync(() -> callback.accept(new CallCompleteArgs(this, monitor)));
        try {
            synchronized(monitor) {
                monitor.wait();
            }
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        closeDoors();
    }

    private void closeDoors() {
        state = State.CLOSING;
        App.schedule(this::onDoorsClosed, closeTime, unit);
    }

    private void recalculateClosestOrder() {
        closestOrder = orders.stream().min((a,b)-> Integer.compare(
            Math.abs(a - currentFloor),
            Math.abs(b - currentFloor))).get();
    }

    private void onDoorsClosed() {
        App.logger.info(this+" closed doors");
        call = null;
        if (orders.isEmpty()) {
            if (elevatorController.tryTakeNewCall(this)) {
                return;
            } else {
                App.logger.info(this+" is now IDLE");
                state = State.IDLE;
            }
        } else {
            recalculateClosestOrder();
            state = State.MOVING;
            moveToNextFloor();
        }
    }

    public boolean tryAcceptPassenger(Passenger passenger) {
        mass += passenger.getMass();
        App.logger.info(this+" mass increased to "+mass);
        if (mass < capacity) {
            orders.add(passenger.getDestination());
            return true;
        }
        boardingFailure = true;
        return false;
    }

    public void removePassenger(Passenger passenger) {
        if (boardingFailure) {
            boardingFailure = false;
        } else {
            done++;
        }
        mass -= passenger.getMass();
        App.logger.info(this+" mass decreased to "+mass);
    }

    public String toString() { return "Elevator " + id; }
}