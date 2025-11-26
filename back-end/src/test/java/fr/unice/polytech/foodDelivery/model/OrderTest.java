package fr.unice.polytech.foodDelivery.model;

import fr.unice.polytech.foodDelivery.domain.model.CustomerAccount;
import fr.unice.polytech.foodDelivery.domain.ENUM.OrderStatus;
import fr.unice.polytech.foodDelivery.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class OrderTest {

    private Order order;
    private CustomerAccount customer;
    private RestaurantAccount restaurant;

    @BeforeEach
    void setUp() {
        Menu menu = new Menu();
        customer = new CustomerAccount("Alice", "Polytech", 20);
        restaurant = new RestaurantAccount("Pizza Palace", menu);
        order = new Order(customer, restaurant);
    }

    @Test
    void testOrderInitialization() {
        assertNotNull(order.getOrderId(), "L'ID de commande ne doit pas être nul");
        assertEquals(customer, order.getCustomer());
        assertEquals(restaurant, order.getRestaurant());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertTrue(order.getMeals().isEmpty(), "La liste de repas doit être vide à l'initialisation");
        assertEquals(0.0, order.getAmount(), 0.001, "Le montant initial doit être 0");
    }

    @Test
    void testAddMealIncreasesAmount() {
        Meal pizza = new Meal.Builder("Pizza Margherita", 10.0).build();
        Meal pasta = new Meal.Builder("Pasta Carbonara", 12.5).build();

        order.addMeal(pizza);
        assertEquals(10.0, order.getAmount(), 0.001);

        order.addMeal(pasta);
        assertEquals(22.5, order.getAmount(), 0.001);
    }

    @Test
    void testCalculateValueOrderWithNoMeals() {
        order.calculateValueOrder();
        assertEquals(0.0, order.getAmount(), 0.001);
    }

    @Test
    void testSetAndGetStatus() {
        order.setStatus(OrderStatus.VALIDATED);
        assertEquals(OrderStatus.VALIDATED, order.getStatus());
    }

    @Test
    void testSetAndGetSchedule() {
        Planning planning = new Planning();
        planning.addTimeSlot("2025-10-15 19:00", DayOfWeek.MONDAY, LocalTime.of(19, 0), LocalTime.of(21, 0), 10);
        order.setSchedule(planning);
        assertTrue(order.getSchedule().getTimeSlots().keySet().contains("2025-10-15 19:00"));
    }

    @Test
    void testGetOrderIdIsUnique() {
        Order order2 = new Order(customer, restaurant);
        assertNotEquals(order.getOrderId(), order2.getOrderId(), "Chaque commande doit avoir un UUID unique");
    }

    @Test
    void testPayWithAllowanceSuccessful() {
        customer.setAllowance(50.0);
        order.addMeal(new Meal.Builder("Pizza", 10).build());
        boolean paid = order.payWithAllowance();

        assertTrue(paid);
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals(40.0, customer.getAllowance(), 0.01);
    }

    @Test
    void testPayWithAllowanceInsufficient() {
        customer.setAllowance(5.0);
        order.addMeal(new Meal.Builder("Pizza", 10).build());
        boolean paid = order.payWithAllowance();

        assertFalse(paid);
        assertEquals(OrderStatus.PENDING, order.getStatus()); // Vérifier comportement exact
    }

    @Test
    void testLibererCreneauWithNullSchedule() {
        order.setSchedule(null);
        order.libererCreneau(); // juste vérifier que ça ne lance pas d'exception
    }

    @Test
    void testAnnulerCommandePaid() {
        order.addMeal(new Meal.Builder("Pizza", 10).build());
        order.setStatus(OrderStatus.PAID);
        customer.setAllowance(20.0);

        order.annulerCommande();

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(30.0, customer.getAllowance(), 0.01);
    }

    @Test
    void testAnnulerCommandePending() {
        order.setStatus(OrderStatus.PENDING);
        order.annulerCommande();
        assertEquals(OrderStatus.PENDING, order.getStatus()); // rien ne change
    }

    @Test
    void testLibererCreneauWithNoOrders() {
        Planning planning = new Planning();
        planning.addTimeSlot("slot1", DayOfWeek.MONDAY, null, null, 0);
        order.setSchedule(planning);
        order.libererCreneau(); 
    }

}
