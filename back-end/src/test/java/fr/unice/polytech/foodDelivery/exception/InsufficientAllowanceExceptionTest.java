package fr.unice.polytech.foodDelivery.exception;

import fr.unice.polytech.foodDelivery.domain.exception.InsufficientAllowanceException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InsufficientAllowanceExceptionTest {

    @Test
    void testExceptionMessageAndGetters() {
        double required = 50.0;
        double available = 20.0;

        InsufficientAllowanceException exception = new InsufficientAllowanceException(required, available);

        String expectedMessage1 = "Insufficient allowance: required=50,00, available=20,00";
        String expectedMessage2 = "Insufficient allowance: required=50.00, available=20.00";
        assertTrue(
                exception.getMessage().equals(expectedMessage1) ||
                        exception.getMessage().equals(expectedMessage2));

        assertEquals(required, exception.getRequired(), 0.001);
        assertEquals(available, exception.getAvailable(), 0.001);
    }

    @Test
    void testExceptionThrownUsingLambda() {
        double required = 30.0;
        double available = 10.0;

        InsufficientAllowanceException thrown = assertThrows(
                InsufficientAllowanceException.class,
                () -> {
                    throw new InsufficientAllowanceException(required, available);
                },
                "Expected InsufficientAllowanceException to be thrown");

        assertEquals(required, thrown.getRequired(), 0.001);
        assertEquals(available, thrown.getAvailable(), 0.001);
    }
}
