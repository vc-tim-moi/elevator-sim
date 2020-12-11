package by.issoft.training;

import static by.issoft.training.App.schedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import by.issoft.training.Call.Direction;

/**
 * Creates and controls people who interact with the elevators
 */
public class PassengerController {
    private final ElevatorController elevatorController;
    private final FloorQueue[] floorQueues;
    private final Map<Elevator, List<Passenger>> passengersInElevator = new HashMap<>();
    
    public FloorQueue[] getFloorQueues() {
        return floorQueues;
    }

    public PassengerController(ElevatorController elevatorController, int floorCount) {
        this.elevatorController = elevatorController;
        floorQueues = new FloorQueue[floorCount];
        floorQueues[0] = new FloorQueue(
            new ConcurrentLinkedQueue<Passenger>(), null);
        for (int i = 1; i < floorQueues.length-1; i++) {
            floorQueues[i] = new FloorQueue(
                new ConcurrentLinkedQueue<Passenger>(),
                new ConcurrentLinkedQueue<Passenger>());
        }
        floorQueues[floorQueues.length-1]= new FloorQueue(
            null, new ConcurrentLinkedQueue<Passenger>());
    }

    // create a passenger
    private void spawn(Passenger passenger) {
        Call call = Call.from(passenger, this::onCallComplete);
        Queue<Passenger> q = (call.getDirection() == Direction.UP)
            ? floorQueues[passenger.getOrigin()].up()
            : floorQueues[passenger.getOrigin()].down();
        if (q.isEmpty()) {
            elevatorController.processCall(call);
        }
        q.add(passenger);
    }

    // automatically create passengers at random intevals
    public void startSpawning(long origin, long bound, TimeUnit unit) {
        Passenger passenger = new Passenger(floorQueues.length);
        spawn(passenger);
        long delay = ThreadLocalRandom.current().nextLong(origin, bound);
        App.logger.info("Spawned passenger "+passenger+". Waiting "+delay+" "+unit+" before spawning another passenger");
        schedule(()->startSpawning(origin, bound, unit), delay, unit);
    }

    // callback to process entering and exiting elevators which arrive on the destination floor
    private void onCallComplete(CallCompleteArgs args) {
        Elevator elevator = args.getElevator();
        Call call = elevator.getCall();
        if (call == null) {
            List<Passenger> passengers = passengersInElevator.get(elevator);
            // passengers are exiting elevator
            for (int i = passengers.size() - 1; i >= 0; i--) {
                Passenger p = passengers.get(i);
                if (p.getDestination() == elevator.getCurrentFloor()) {
                    passengers.remove(i);
                    p.leaveElevator(elevator);
                }   
            }
        }
        else {
            // while passengers are entering elevator
            while (true) {
                Queue<Passenger> q = (call.getDirection() == Direction.UP)
                    ? floorQueues[call.getFloor()].up()
                    : floorQueues[call.getFloor()].down();
                Passenger passenger = q.peek();
                if (passenger == null) {
                    App.logger.info("No more passengers in queue");
                    break;
                }else {
                    App.logger.info("Found passenger "+passenger+" in queue");
                }
                CompletableFuture<Boolean> future = CompletableFuture
                    .supplyAsync(() -> passenger.enterElevator(elevator));
                try {
                    if (!future.get()) {
                        App.logger.info("Passenger "+passenger+" was unable to enter " + elevator);
                        elevatorController.processCall(call);
                        break;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                App.logger.info("Passenger "+ passenger+" successfully entered " + elevator);
                if (passengersInElevator.containsKey(elevator)) {
                    passengersInElevator.get(elevator).add(passenger);
                } else {
                    List<Passenger> hashSet = new ArrayList<>();
                    hashSet.add(passenger);
                    passengersInElevator.put(elevator, hashSet);
                }
                q.remove();
            }
        }
        Object monitor = args.getMonitor();
        synchronized (monitor) {
            args.getMonitor().notify();
        }
    }
}