package by.issoft.training;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Passenger {
    private final int MASS_LOW = 50;
    private final int MASS_HIGH = 40;
    // mass range: 50 - 89, average: 69.5
    private final int BOARDING_TIME = 2000;
    private final TimeUnit UNIT = TimeUnit.MILLISECONDS;

    private final int mass;
    private final int origin;
    private final int destination;
    private final int id;
    private static int globalId;

    public int getMass() {
        return mass;
    }

    public int getOrigin() {
        return origin;
    }

    public int getDestination() {
        return destination;
    }

    public Passenger(int floorCount) {
        id = globalId++;
        Random r = new Random();
        mass = MASS_LOW + r.nextInt(MASS_HIGH);
        origin = r.nextInt(floorCount);
        int d = r.nextInt(floorCount - 1);
        if (d >= origin)
            d++;
        destination = d;
    }

    public Passenger(int origin, int destination, int mass) {
        if (origin == destination) {
            throw new IllegalArgumentException("origin must be different from destination");
        }
        id = globalId++;
        this.origin = origin;
        this.destination = destination;
        this.mass = mass;
    }

    public boolean enterElevator(Elevator elevator) {
        try {
            TimeUnit.NANOSECONDS.sleep((long)
            (UNIT.toNanos(BOARDING_TIME)/App.TIME_SCALE));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (elevator.tryAcceptPassenger(this)) {
            return true;
        } else {
            leaveElevator(elevator);
            return false;
        }
    }

    public void leaveElevator(Elevator elevator) {
        try {
            TimeUnit.NANOSECONDS.sleep((long)
            (UNIT.toNanos(BOARDING_TIME)/App.TIME_SCALE));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        elevator.removePassenger(this);
    }

    @Override
    public String toString() {
        return "("+id+","+mass+","+destination+")";
    }
}