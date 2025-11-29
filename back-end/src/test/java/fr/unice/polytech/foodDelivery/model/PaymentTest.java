package fr.unice.polytech.foodDelivery.model;

import fr.unice.polytech.foodDelivery.domain.model.CustomerAccount;
import fr.unice.polytech.foodDelivery.domain.ENUM.CuisineType;
import fr.unice.polytech.foodDelivery.domain.ENUM.PaymentMethod;
import fr.unice.polytech.foodDelivery.domain.ENUM.PaymentStatus;
import fr.unice.polytech.foodDelivery.domain.ENUM.RestaurantType;
import fr.unice.polytech.foodDelivery.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentTest {

    private Payment payment;
    private Order order;
    private CustomerAccount customer;
    private RestaurantAccount restaurant;

    @BeforeEach
    void setUp() {
        customer = new CustomerAccount("Alice", "Paris", 100.0f);

        // Utilisation du nouveau constructeur de RestaurantAccount
        restaurant = new RestaurantAccount(
                "La Table d'Or", // name
                new Menu(), // menu
                Arrays.asList(CuisineType.FRENCH), // cuisineTypes
                RestaurantType.RESTAURANT, // restaurantType
                new int[]{20, 100} // priceRange
        );

        order = new Order(customer, restaurant);
        payment = new Payment(order, 45.5f, PaymentMethod.CARD);
    }

    @Test
    void testPaymentInitialization() {
        assertNotNull(payment.getId(), "L'ID du paiement ne doit pas être nul");
        assertEquals(order, payment.getOrder(), "Le paiement doit être associé à la commande passée en paramètre");
        assertEquals(45.5, payment.getAmount(), 0.001, "Le montant doit correspondre à celui fourni au constructeur");
        assertEquals(PaymentMethod.CARD, payment.getMethod(),
                "Le mode de paiement doit être celui passé au constructeur");
        assertEquals(PaymentStatus.PENDING, payment.getStatus(), "Le statut initial doit être PENDING");
    }

    @Test
    void testUniquePaymentIds() {
        Payment payment2 = new Payment(order, 30.0, PaymentMethod.ALLOWANCE);
        assertNotEquals(payment.getId(), payment2.getId(), "Chaque paiement doit avoir un UUID unique");
    }

    @Test
    void testSetAndGetStatus() {
        payment.setStatus(PaymentStatus.SUCCESS);
        assertEquals(PaymentStatus.SUCCESS, payment.getStatus(), "Le statut du paiement doit être mis à jour");
    }

    @Test
    void testGetAmountIsAccurate() {
        assertEquals(45.5, payment.getAmount(), 0.001, "Le montant doit être exact");
    }

    @Test
    void testPaymentWithDifferentMethod() {
        Payment cashPayment = new Payment(order, 20.0, PaymentMethod.ALLOWANCE);
        assertEquals(PaymentMethod.ALLOWANCE, cashPayment.getMethod());
        assertEquals(20.0, cashPayment.getAmount(), 0.001);
    }

    @Test
    void testRestaurantHasUUID() {
        assertNotNull(restaurant.getRestaurantId(), "Le restaurant doit avoir un UUID généré automatiquement");
        assertTrue(restaurant.getRestaurantId() instanceof UUID, "L'ID du restaurant doit être un UUID");
    }

    @Test
    void testOrderHasUUID() {
        assertNotNull(order.getOrderId(), "La commande doit avoir un UUID généré automatiquement");
        assertTrue(order.getOrderId() instanceof UUID, "L'ID de la commande doit être un UUID");
    }

    @Test
    void testPaymentWithDifferentRestaurantTypes() {
        // Test avec différents types de restaurants
        RestaurantAccount fastFood = new RestaurantAccount(
                "Fast Food",
                new Menu(),
                Arrays.asList(CuisineType.AMERICAN),
                RestaurantType.FAST_FOOD,
                new int[]{5, 15}
        );

        RestaurantAccount cafe = new RestaurantAccount(
                "Café",
                new Menu(),
                Arrays.asList(CuisineType.ITALIAN),
                RestaurantType.CAFE,
                new int[]{3, 10}
        );

        Order order1 = new Order(customer, fastFood);
        Order order2 = new Order(customer, cafe);

        Payment payment1 = new Payment(order1, 12.0, PaymentMethod.CARD);
        Payment payment2 = new Payment(order2, 5.5, PaymentMethod.PAYPAL);

        assertEquals(fastFood, payment1.getOrder().getRestaurant());
        assertEquals(cafe, payment2.getOrder().getRestaurant());
        assertEquals(RestaurantType.FAST_FOOD, payment1.getOrder().getRestaurant().getRestaurantType());
        assertEquals(RestaurantType.CAFE, payment2.getOrder().getRestaurant().getRestaurantType());
    }

    @Test
    void testPaymentWithDifferentCuisineTypes() {
        RestaurantAccount multiCuisine = new RestaurantAccount(
                "Multi Cuisine",
                new Menu(),
                Arrays.asList(CuisineType.ITALIAN, CuisineType.FRENCH),
                RestaurantType.RESTAURANT,
                new int[]{15, 50}
        );

        Order order = new Order(customer, multiCuisine);
        Payment payment = new Payment(order, 35.0, PaymentMethod.CARD);

        assertEquals(2, payment.getOrder().getRestaurant().getCuisineTypes().size());
        assertTrue(payment.getOrder().getRestaurant().getCuisineTypes().contains(CuisineType.ITALIAN));
        assertTrue(payment.getOrder().getRestaurant().getCuisineTypes().contains(CuisineType.FRENCH));
    }

    @Test
    void testPaymentWithPriceRange() {
        RestaurantAccount expensiveRestaurant = new RestaurantAccount(
                "Restaurant Gastronomique",
                new Menu(),
                Arrays.asList(CuisineType.FRENCH),
                RestaurantType.RESTAURANT,
                new int[]{50, 200}
        );

        Order order = new Order(customer, expensiveRestaurant);
        Payment payment = new Payment(order, 150.0, PaymentMethod.CARD);

        assertArrayEquals(new int[]{50, 200}, payment.getOrder().getRestaurant().getPriceRange());
    }
}