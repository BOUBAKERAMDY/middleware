package fr.unice.polytech.foodDelivery.service;

import fr.unice.polytech.foodDelivery.domain.exception.CapacityExceededException;
import fr.unice.polytech.foodDelivery.domain.exception.PlanningException;
import fr.unice.polytech.foodDelivery.domain.model.Menu;
import fr.unice.polytech.foodDelivery.domain.model.Planning;
import fr.unice.polytech.foodDelivery.domain.model.RestaurantAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RestaurantServiceCapacityTest {

    private RestaurantService restaurantService;
    private RestaurantAccount restaurant;
    UUID restaurantId ;

    @BeforeEach
    void setUp() {
        restaurantService = new RestaurantService();
        restaurantService.clearData();
        restaurant = new RestaurantAccount("Trattoria Roma", new Menu());
        restaurantId = restaurant.getRestaurantId();
        restaurantService.registerRestaurant(restaurant);
    }

    @Test
    void addTimeSlot_and_check_availability() {
        restaurantService.addTimeSlot(restaurantId, "12:00-12:30", DayOfWeek.FRIDAY, LocalTime.of(12, 0),
                LocalTime.of(12, 30), 5);

        assertTrue(restaurantService.isSlotAvailable(restaurantId, "12:00-12:30"));

        Planning.TimeSlot slot = restaurant.getPlanning().getTimeSlot("12:00-12:30");
        assertNotNull(slot);
        assertEquals(5, slot.getCapacity());
        assertEquals(0, slot.getCurrentOrders());
    }

    @Test
    void reserve_until_capacity_exhausted_and_prevent_extra_reservations() {
        restaurantService.addTimeSlot(restaurantId, "18:00-18:30", DayOfWeek.FRIDAY, LocalTime.of(18, 0),
                LocalTime.of(18, 30), 2);

        // reserve twice
        restaurantService.reserveSlotForOrder(restaurantId, "18:00-18:30");
        restaurantService.reserveSlotForOrder(restaurantId, "18:00-18:30");

        Planning.TimeSlot slot = restaurant.getPlanning().getTimeSlot("18:00-18:30");
        assertNotNull(slot);
        assertEquals(2, slot.getCurrentOrders());
        assertFalse(restaurantService.isSlotAvailable(restaurantId, "18:00-18:30"));

        // third reservation should throw CapacityExceededException
        assertThrows(CapacityExceededException.class,
                () -> restaurantService.reserveSlotForOrder(restaurantId, "18:00-18:30"));
    }

    @Test
    void update_capacity_increase_should_allow_more_reservations() {
        restaurantService.addTimeSlot(restaurantId, "19:00-19:30", DayOfWeek.FRIDAY, LocalTime.of(19, 0),
                LocalTime.of(19, 30), 10);

        Planning.TimeSlot slot = restaurant.getPlanning().getTimeSlot("19:00-19:30");
        for (int i = 0; i < 8; i++)
            slot.incrementOrders();

        assertEquals(8, slot.getCurrentOrders());
        assertTrue(slot.getCurrentOrders() < slot.getCapacity());

        restaurantService.updateSlotCapacity(restaurantId, "19:00-19:30", 15);

        assertEquals(15, slot.getCapacity());
        assertTrue(restaurantService.isSlotAvailable(restaurantId, "19:00-19:30"));
        assertEquals(7, slot.getCapacity() - slot.getCurrentOrders());
    }

    @Test
    void reduce_capacity_below_existing_reservations_is_allowed_but_no_new_orders() {
        restaurantService.addTimeSlot(restaurantId, "14:00-14:30", DayOfWeek.FRIDAY, LocalTime.of(14, 0),
                LocalTime.of(14, 30), 20);

        Planning.TimeSlot slot = restaurant.getPlanning().getTimeSlot("14:00-14:30");
        for (int i = 0; i < 5; i++)
            slot.incrementOrders();

        assertEquals(5, slot.getCurrentOrders());

        restaurantService.updateSlotCapacity(restaurantId, "14:00-14:30", 10);
        assertEquals(10, slot.getCapacity());
        assertTrue(restaurantService.isSlotAvailable(restaurantId, "14:00-14:30"));
        assertEquals(5, slot.getCapacity() - slot.getCurrentOrders());

        restaurantService.updateSlotCapacity(restaurantId, "14:00-14:30", 4);
        assertEquals(4, slot.getCapacity());

        assertFalse(restaurantService.isSlotAvailable(restaurantId, "14:00-14:30"));

        assertThrows(CapacityExceededException.class,
                () -> restaurantService.reserveSlotForOrder(restaurantId, "14:00-14:30"));
    }

    @Test
    void adding_time_slot_with_non_positive_capacity_throws_planning_exception() {
        assertThrows(PlanningException.class,
                () -> restaurantService.addTimeSlot(restaurantId, "bad", DayOfWeek.FRIDAY,
                        LocalTime.of(10, 0), LocalTime.of(10, 30), 0));
        assertThrows(PlanningException.class,
                () -> restaurantService.addTimeSlot(restaurantId, "bad2", DayOfWeek.FRIDAY, LocalTime.of(10, 0),
                        LocalTime.of(10, 30), -5));
    }

}
