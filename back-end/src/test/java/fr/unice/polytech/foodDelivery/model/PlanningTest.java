package fr.unice.polytech.foodDelivery.model;

import fr.unice.polytech.foodDelivery.domain.model.Planning;
import fr.unice.polytech.foodDelivery.domain.model.Planning.TimeSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PlanningTest {

    private Planning planning;

    @BeforeEach
    void setUp() {
        planning = new Planning();
    }

    @Test
    void testEmptyPlanningInitialization() {
        Map<String, TimeSlot> slots = planning.getTimeSlots();
        assertNotNull(slots);
        assertTrue(slots.isEmpty());
    }

    @Test
    void testAddTimeSlot() {
        planning.addTimeSlot("12:00-12:30", DayOfWeek.FRIDAY, LocalTime.of(12, 0), LocalTime.of(12, 30), 10);

        TimeSlot slot = planning.getTimeSlot("12:00-12:30");
        assertNotNull(slot);
        assertEquals(DayOfWeek.FRIDAY, slot.getDay());
        assertEquals(LocalTime.of(12, 0), slot.getStartTime());
        assertEquals(LocalTime.of(12, 30), slot.getEndTime());
        assertEquals(10, slot.getCapacity());
        assertEquals(0, slot.getCurrentOrders());
    }

    @Test
    void testAddMultipleTimeSlots() {
        planning.addTimeSlot("12:00-12:30", DayOfWeek.MONDAY, LocalTime.of(12, 0), LocalTime.of(12, 30), 10);
        planning.addTimeSlot("12:30-13:00", DayOfWeek.SATURDAY, LocalTime.of(12, 30), LocalTime.of(13, 0), 15);
        planning.addTimeSlot("18:00-18:30", DayOfWeek.THURSDAY, LocalTime.of(18, 0), LocalTime.of(18, 30), 20);

        assertEquals(3, planning.getTimeSlots().size());
        assertNotNull(planning.getTimeSlot("12:00-12:30"));
        assertNotNull(planning.getTimeSlot("12:30-13:00"));
        assertNotNull(planning.getTimeSlot("18:00-18:30"));
    }

    @Test
    void testIsAvailableForEmptySlot() {
        planning.addTimeSlot("12:00-12:30", DayOfWeek.SUNDAY, LocalTime.of(12, 0), LocalTime.of(12, 30), 5);

        assertTrue(planning.isAvailable("12:00-12:30"));
    }

    @Test
    void testIsAvailableReturnsFalseForNonExistentSlot() {
        assertFalse(planning.isAvailable("non-existent-slot"));
    }

    @Test
    void testIsAvailableWithPartialCapacity() {
        planning.addTimeSlot("12:00-12:30", DayOfWeek.FRIDAY, LocalTime.of(12, 0), LocalTime.of(12, 30), 5);
        TimeSlot slot = planning.getTimeSlot("12:00-12:30");

        // Reserve 3 out of 5 spots
        slot.incrementOrders();
        slot.incrementOrders();
        slot.incrementOrders();

        assertTrue(planning.isAvailable("12:00-12:30"));
        assertEquals(3, slot.getCurrentOrders());
    }

    @Test
    void testIsAvailableReturnsFalseWhenFull() {
        planning.addTimeSlot("12:00-12:30", DayOfWeek.FRIDAY, LocalTime.of(12, 0), LocalTime.of(12, 30), 2);
        TimeSlot slot = planning.getTimeSlot("12:00-12:30");

        // Fill capacity
        slot.incrementOrders();
        slot.incrementOrders();

        assertFalse(planning.isAvailable("12:00-12:30"));
        assertEquals(2, slot.getCurrentOrders());
    }

    @Test
    void testUpdateCapacity() {
        planning.addTimeSlot("12:00-12:30", DayOfWeek.FRIDAY, LocalTime.of(12, 0), LocalTime.of(12, 30), 10);

        TimeSlot slot = planning.getTimeSlot("12:00-12:30");
        assertEquals(10, slot.getCapacity());

        planning.updateCapacity("12:00-12:30", 20);
        assertEquals(20, slot.getCapacity());
    }

    @Test
    void testUpdateCapacityForNonExistentSlotDoesNotThrow() {
        assertDoesNotThrow(() -> planning.updateCapacity("non-existent", 15));
    }

    @Test
    void testUpdateCapacityToZero() {
        planning.addTimeSlot("12:00-12:30", DayOfWeek.FRIDAY, LocalTime.of(12, 0), LocalTime.of(12, 30), 10);
        planning.updateCapacity("12:00-12:30", 0);

        TimeSlot slot = planning.getTimeSlot("12:00-12:30");
        assertEquals(0, slot.getCapacity());
        assertFalse(planning.isAvailable("12:00-12:30"));
    }

    @Test
    void testGetTimeSlotsReturnsDefensiveCopy() {
        planning.addTimeSlot("12:00-12:30", DayOfWeek.FRIDAY, LocalTime.of(12, 0), LocalTime.of(12, 30), 10);

        Map<String, TimeSlot> slots1 = planning.getTimeSlots();
        Map<String, TimeSlot> slots2 = planning.getTimeSlots();

        assertNotSame(slots1, slots2);
        assertEquals(slots1.size(), slots2.size());
    }

    @Test
    void testGetTimeSlot() {
        planning.addTimeSlot("12:00-12:30", DayOfWeek.FRIDAY, LocalTime.of(12, 0), LocalTime.of(12, 30), 10);

        TimeSlot slot = planning.getTimeSlot("12:00-12:30");
        assertNotNull(slot);
        assertEquals(LocalTime.of(12, 0), slot.getStartTime());
        assertEquals(LocalTime.of(12, 30), slot.getEndTime());
    }

    @Test
    void testGetTimeSlotReturnsNullForNonExistent() {
        TimeSlot slot = planning.getTimeSlot("non-existent");
        assertNull(slot);
    }

    // TimeSlot nested class tests

    @Test
    void testTimeSlotInitialization() {
        TimeSlot slot = new TimeSlot(DayOfWeek.FRIDAY, LocalTime.of(12, 0), LocalTime.of(12, 30), 10);

        assertEquals(LocalTime.of(12, 0), slot.getStartTime());
        assertEquals(LocalTime.of(12, 30), slot.getEndTime());
        assertEquals(10, slot.getCapacity());
        assertEquals(0, slot.getCurrentOrders());
        assertTrue(slot.hasCapacity());
    }

    @Test
    void testTimeSlotHasCapacity() {
        TimeSlot slot = new TimeSlot(DayOfWeek.FRIDAY, LocalTime.of(12, 0), LocalTime.of(12, 30), 3);

        assertTrue(slot.hasCapacity());

        slot.incrementOrders();
        assertTrue(slot.hasCapacity());

        slot.incrementOrders();
        assertTrue(slot.hasCapacity());

        slot.incrementOrders();
        assertFalse(slot.hasCapacity());
    }

    @Test
    void testTimeSlotIncrementOrders() {
        TimeSlot slot = new TimeSlot(DayOfWeek.FRIDAY, LocalTime.of(12, 0), LocalTime.of(12, 30), 5);

        assertEquals(0, slot.getCurrentOrders());

        slot.incrementOrders();
        assertEquals(1, slot.getCurrentOrders());

        slot.incrementOrders();
        assertEquals(2, slot.getCurrentOrders());
    }

    @Test
    void testTimeSlotIncrementOrdersDoesNotExceedCapacity() {
        TimeSlot slot = new TimeSlot(DayOfWeek.FRIDAY, LocalTime.of(12, 0), LocalTime.of(12, 30), 2);

        slot.incrementOrders();
        slot.incrementOrders();
        assertEquals(2, slot.getCurrentOrders());

        // Try to increment beyond capacity
        slot.incrementOrders();
        slot.incrementOrders();

        // Should stay at capacity
        assertEquals(2, slot.getCurrentOrders());
    }

    @Test
    void testTimeSlotSetCapacity() {
        TimeSlot slot = new TimeSlot(DayOfWeek.FRIDAY, LocalTime.of(12, 0), LocalTime.of(12, 30), 10);
        assertEquals(10, slot.getCapacity());

        slot.setCapacity(20);
        assertEquals(20, slot.getCapacity());

        slot.setCapacity(5);
        assertEquals(5, slot.getCapacity());
    }

    @Test
    void testTimeSlotReduceCapacityBelowCurrentOrders() {
        TimeSlot slot = new TimeSlot(DayOfWeek.FRIDAY, LocalTime.of(12, 0), LocalTime.of(12, 30), 10);

        // Add 5 orders
        for (int i = 0; i < 5; i++) {
            slot.incrementOrders();
        }
        assertEquals(5, slot.getCurrentOrders());
        assertTrue(slot.hasCapacity());

        // Reduce capacity below current orders
        slot.setCapacity(3);
        assertEquals(3, slot.getCapacity());
        assertEquals(5, slot.getCurrentOrders());
        assertFalse(slot.hasCapacity());
    }

    @Test
    void testTimeSlotWithZeroCapacity() {
        TimeSlot slot = new TimeSlot(DayOfWeek.FRIDAY, LocalTime.of(12, 0), LocalTime.of(12, 30), 0);

        assertEquals(0, slot.getCapacity());
        assertFalse(slot.hasCapacity());

        // Try to increment
        slot.incrementOrders();
        assertEquals(0, slot.getCurrentOrders());
    }

    @Test
    void testComplexPlanningScenario() {
        // Add morning slots
        planning.addTimeSlot("11:30-12:00", DayOfWeek.FRIDAY, LocalTime.of(11, 30), LocalTime.of(12, 0), 15);
        planning.addTimeSlot("12:00-12:30", DayOfWeek.FRIDAY, LocalTime.of(12, 0), LocalTime.of(12, 30), 20);
        planning.addTimeSlot("12:30-13:00", DayOfWeek.FRIDAY, LocalTime.of(12, 30), LocalTime.of(13, 0), 20);

        // Add evening slots
        planning.addTimeSlot("18:00-18:30", DayOfWeek.FRIDAY, LocalTime.of(18, 0), LocalTime.of(18, 30), 25);
        planning.addTimeSlot("18:30-19:00", DayOfWeek.FRIDAY, LocalTime.of(18, 30), LocalTime.of(19, 0), 30);

        // Verify all slots exist and are available
        assertEquals(5, planning.getTimeSlots().size());
        assertTrue(planning.isAvailable("11:30-12:00"));
        assertTrue(planning.isAvailable("12:00-12:30"));
        assertTrue(planning.isAvailable("12:30-13:00"));
        assertTrue(planning.isAvailable("18:00-18:30"));
        assertTrue(planning.isAvailable("18:30-19:00"));

        // Fill one slot
        TimeSlot lunchSlot = planning.getTimeSlot("12:00-12:30");
        for (int i = 0; i < 20; i++) {
            lunchSlot.incrementOrders();
        }
        assertFalse(planning.isAvailable("12:00-12:30"));

        // Others should still be available
        assertTrue(planning.isAvailable("11:30-12:00"));
        assertTrue(planning.isAvailable("12:30-13:00"));

        // Update capacity for evening
        planning.updateCapacity("18:00-18:30", 35);
        assertEquals(35, planning.getTimeSlot("18:00-18:30").getCapacity());
    }
}
