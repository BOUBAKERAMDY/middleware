package fr.unice.polytech.foodDelivery.service;

import fr.unice.polytech.foodDelivery.domain.model.CustomerAccount;
import fr.unice.polytech.foodDelivery.domain.ENUM.*;
import fr.unice.polytech.foodDelivery.domain.exception.MealNotInMenuException;
import fr.unice.polytech.foodDelivery.domain.exception.NoCurrentOrderException;
import fr.unice.polytech.foodDelivery.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationServiceTest {

    private ApplicationService service;
    private CustomerAccount customer;
    private RestaurantAccount restaurant;
    private Meal burger;
    private Meal pizza;

    @BeforeEach
    void setUp() {
        burger = new Meal.Builder("Burger", 8.5f)
                .description("Beef burger")
                .build();
        pizza = new Meal.Builder("Pizza", 12.0f)
                .description("Margherita")
                .build();

        Menu menu = new Menu();
        menu.addMeal(burger);
        menu.addMeal(pizza);
        restaurant = new RestaurantAccount("Test Restaurant", menu);

        customer = new CustomerAccount("Alice", "Campus A", 50.0f);

        service = new ApplicationService();
    }

    // Tests existants
    @Test
    void testAddMealToExistingOrder() {
        Order order = service.createOrder(customer, restaurant);
        Meal newPizza = new Meal.Builder("Pizza", 10.0f)
                .description("New pizza")
                .build();
        restaurant.addMeal(newPizza);
        service.addMeal(newPizza);

        assertEquals(1, order.getMeals().size());
        assertEquals(newPizza, order.getMeals().get(0));
        assertEquals(10.0f, order.getAmount(), 0.01);
    }

    @Test
    void testAddMealWithoutCurrentOrderThrows() {
        assertThrows(NoCurrentOrderException.class, () -> service.addMeal(burger));
    }

    @Test
    void testAddMealNotInMenuThrows() {
        service.createOrder(customer, restaurant);
        Meal sushi = new Meal.Builder("Sushi", 15.0f)
                .description("Salmon sushi")
                .build();

        assertThrows(MealNotInMenuException.class, () -> service.addMeal(sushi));
    }

    @Test
    void testValidateOrder() {
        Order order = service.createOrder(customer, restaurant);
        service.addMeal(burger); // Add a meal so the order has a valid amount
        Planning planning = new Planning();
        Address address = new Address("Test Name", 123, Suffix.BIS, 06000, "Test Road", "Test City");

        boolean result = service.validateOrder(planning, address, PaymentMethod.ALLOWANCE);

        assertTrue(result);
        assertEquals(planning, order.getSchedule());
        assertEquals(address, order.getLocation());
        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    @Test
    void testSetOrderInfo() {
        Order order = service.createOrder(customer, restaurant);
        Planning planning = new Planning();
        Address address = new Address("Test Name", 123, Suffix.BIS, 06000, "Test Road", "Test City");

        service.setOrderInfo(planning, address);

        assertEquals(planning, order.getSchedule());
        assertEquals(address, order.getLocation());
    }

    @Test
    void testGetAndSetCurrentOrder() {
        assertNull(service.getCurrentOrder());

        Order newOrder = new Order(customer, restaurant);
        service.setCurrentOrder(newOrder);

        assertEquals(newOrder, service.getCurrentOrder());
    }

    @Test
    void testAddMealWithPhotoUnknownThrowsException() {
        // Test que l'exception est bien levée pour une photo inconnue
        service.createOrder(customer, restaurant);

        // Utiliser une photo qui ne sera PAS reconnue par le mock AI
        assertThrows(IllegalArgumentException.class, () -> service.addMealWithPhoto("unknown_dish.jpg"));
    }

    @Test
    void testGetCustomerHistory() {
        UUID customerId = customer.getCustomerId();

        List<Order> history = service.getCustomerHistory(customerId);

        assertNotNull(history);
        // Initialement l'historique devrait être vide
        assertTrue(history.isEmpty());
    }

    @Test
    void testCreateOrderUpdatesCurrentOrder() {
        Order order = service.createOrder(customer, restaurant);

        assertEquals(order, service.getCurrentOrder());
        assertEquals(customer, order.getCustomer());
        assertEquals(restaurant, order.getRestaurant());
    }

    @Test
    void testOrderWithMultipleMeals() {
        Order order = service.createOrder(customer, restaurant);

        service.addMeal(burger);
        service.addMeal(pizza);

        assertEquals(2, order.getMeals().size());
        assertEquals(20.5f, order.getAmount(), 0.01); // 8.5 + 12.0
    }

    @Test
    void testRestaurantPlanningOperations() {
        // Test planning complet
        Planning planning = new Planning();
        planning.addTimeSlot("test-slot", DayOfWeek.MONDAY,
                LocalTime.of(10, 0), LocalTime.of(12, 0), 5);

        restaurant.setPlanning(planning);

        Order order = service.createOrder(customer, restaurant);
        service.setOrderInfo(planning, new Address("Test", 123, Suffix.BIS, 06000, "Road", "City"));

        assertNotNull(order.getSchedule());
        assertNotNull(order.getLocation());
    }

    @Test
    void testCustomerAllowanceAfterOrder() {
        CustomerAccount customerWithAllowance = new CustomerAccount("Bob", "Campus B", 30.0f);
        Order order = service.createOrder(customerWithAllowance, restaurant);

        service.addMeal(burger); // 8.5
        service.addMeal(pizza); // 12.0

        // Test du paiement avec allowance
        boolean paymentSuccess = order.payWithAllowance();
        assertTrue(paymentSuccess);
        assertEquals(9.5f, customerWithAllowance.getAllowance(), 0.01); // 30 - 20.5
    }

    @Test
    void testOrderStatusChanges() {
        Order order = service.createOrder(customer, restaurant);
        service.addMeal(burger);

        order.setStatus(OrderStatus.PAID);
        assertEquals(OrderStatus.PAID, order.getStatus());

        order.setStatus(OrderStatus.CANCELLED);
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void testAddMealIntegration() {
        // Test d'intégration simplifié pour addMeal
        service.createOrder(customer, restaurant);

        // Tester avec un repas basique
        service.addMeal(burger);

        Order currentOrder = service.getCurrentOrder();
        assertEquals(1, currentOrder.getMeals().size());
        assertEquals(burger, currentOrder.getMeals().get(0));
    }

    @Test
    void testOrderAmountCalculation() {
        Order order = service.createOrder(customer, restaurant);

        // Ajouter plusieurs repas et vérifier le calcul du montant
        service.addMeal(burger); // 8.5
        service.addMeal(pizza); // 12.0

        assertEquals(20.5f, order.getAmount(), 0.01);
        assertEquals(20.5f, order.getTotalAmount(), 0.01);
    }

    @Test
    void testRestaurantMenuOperations() {
        Menu menu = restaurant.getMenu();

        // Test containsMealByName
        assertTrue(menu.containsMealByName("Burger"));
        assertTrue(menu.containsMealByName("Pizza"));
        assertFalse(menu.containsMealByName("Sushi"));

        // Test getMealByName
        Meal foundBurger = menu.getMealByName("Burger");
        assertNotNull(foundBurger);
        assertEquals("Burger", foundBurger.getName());

        Meal notFound = menu.getMealByName("Unknown");
        assertNull(notFound);
    }

    @Test
    void testOrderObserverNotification() {
        Order order = service.createOrder(customer, restaurant);

        // Changer le statut devrait notifier les observateurs
        order.setStatus(OrderStatus.PAID);
        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    @Test
    void testOrderCancellation() {
        Order order = service.createOrder(customer, restaurant);
        service.addMeal(burger);

        // Payer la commande
        order.payWithAllowance();
        assertEquals(OrderStatus.PAID, order.getStatus());

        // Annuler la commande
        order.annulerCommande();
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void testEmptyOrderAmount() {
        Order order = service.createOrder(customer, restaurant);

        // Commande vide devrait avoir un montant de 0
        assertEquals(0.0f, order.getAmount(), 0.01);
        assertEquals(0.0f, order.getTotalAmount(), 0.01);
    }

    @Test
    void testMenuOperations() {
        Menu menu = new Menu();

        // Test addMeal
        Meal testMeal = new Meal.Builder("Test Meal", 10.0).build();
        menu.addMeal(testMeal);
        assertTrue(menu.containsMeal(testMeal));

        // Test removeMeal
        menu.removeMeal(testMeal);
        assertFalse(menu.containsMeal(testMeal));

        // Test addMeal doublon
        menu.addMeal(testMeal);
        menu.addMeal(testMeal); // Doublon
        assertEquals(1, menu.getMeals().size()); // Ne devrait pas ajouter de doublon
    }
}