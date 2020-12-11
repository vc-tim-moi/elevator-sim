package by.issoft.training;

import java.util.Queue;

/**
 * 2 queues storing passengers heading up and down from a specific floor
 */
public class FloorQueue {
    private final Queue<Passenger> up;
    private final Queue<Passenger> down;

    public Queue<Passenger> up() { return up; }

    public Queue<Passenger> down() { return down; }

    public FloorQueue(Queue<Passenger> up, Queue<Passenger> down) {
        this.up = up;
        this.down = down;
    }
}