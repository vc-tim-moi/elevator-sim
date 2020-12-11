package by.issoft.training;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

public class PassengerControllerTest {
    @Test
    public void shouldFillQueue() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException, NoSuchFieldException
    {
        PassengerController pc = new PassengerController(new ElevatorController(1), 5);
        Method m = PassengerController.class.getDeclaredMethod("spawn", Passenger.class);
        m.setAccessible(true);
        m.invoke(pc, new Passenger(5));
        Field f = PassengerController.class.getDeclaredField("floorQueues");
        f.setAccessible(true);
        FloorQueue[] queue = (FloorQueue[])f.get(pc);
        int total = 0;
        for (FloorQueue floorQueue : queue) {
            if (floorQueue.up() != null) {
                total += floorQueue.up().size();
            }
            if (floorQueue.down() != null) {
                total += floorQueue.down().size();
            }
            assertTrue(total == 0 || total == 1);
        }
        assertTrue(total == 1);
    }
}
