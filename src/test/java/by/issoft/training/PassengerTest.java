package by.issoft.training;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

public class PassengerTest 
{
    @Test
    public void shouldNotThrow()
    {
        assertDoesNotThrow(()->new Passenger(1,2,70));
    }

    @Test
    public void shouldThrow()
    {
        // cannot move from floor 1 to floor 1
        assertThrows(IllegalArgumentException.class, ()->new Passenger(1,1,70));
    }
}
