package by.issoft.training;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class ElevatorControllerTest 
{
    @Test
    public void shouldChooseTheClosest()
    {
        Call callFloor3 = new Call(3);
        Elevator elevatorFloor2 = new Elevator(2);
        Elevator elevatorFloor1 = new Elevator(1);
        ElevatorController ec = new ElevatorController(Arrays.asList(
            elevatorFloor2,
            elevatorFloor1
        ));
        ec.processCall(callFloor3);
        assertEquals(Elevator.State.MOVING, elevatorFloor2.getState());
        assertEquals(Elevator.State.IDLE, elevatorFloor1.getState());
    }
}
