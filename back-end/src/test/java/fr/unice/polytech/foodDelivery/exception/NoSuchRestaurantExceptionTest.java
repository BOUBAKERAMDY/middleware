package fr.unice.polytech.foodDelivery.exception;
import fr.unice.polytech.foodDelivery.domain.exception.NoSuchRestaurantException;
import fr.unice.polytech.foodDelivery.service.RestaurantService;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NoSuchRestaurantExceptionTest {

    @Test
    public void testException(){
        RestaurantService restaurantService = new RestaurantService();
        UUID id = UUID.randomUUID();
        NoSuchRestaurantException exception = assertThrows(NoSuchRestaurantException.class,()->restaurantService.getRestaurantMenu(id));
        assertEquals(exception.getMessage(),"No restaurant matches in the database with the UUID:"+id);
    }
}
