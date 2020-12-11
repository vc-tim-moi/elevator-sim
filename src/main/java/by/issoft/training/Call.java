package by.issoft.training;

import java.util.function.Consumer;

/**
 * Represents a pressed button on the landing.
 * Stores floor and direction.
 */
public class Call {
    public enum Direction {
        UP,
        DOWN
    }

    private final Direction direction;
    private final int floor;
    private final Consumer<CallCompleteArgs> callback;
    
    public Direction getDirection() { return direction; }
    public int getFloor() { return floor; }
    public Consumer<CallCompleteArgs> getCallback() { return callback; }

    private Call(int floor, Direction direction, Consumer<CallCompleteArgs> callback) {
        this.floor = floor;
        this.direction = direction;
        this.callback = callback;
    }

    public Call(int floor) {
        this.floor = floor;
        this.direction = Direction.DOWN;
        this.callback = (a)->{};
    }

    public static Call from(Passenger passenger, Consumer<CallCompleteArgs> callback) {
        return new Call(
            passenger.getOrigin(),
            passenger.getDestination() > passenger.getOrigin()
                ? Direction.UP : Direction.DOWN, callback);
    }

    @Override
    public String toString() {
        return floor + (direction == Direction.UP ? "↑" : "↓");
    }
}