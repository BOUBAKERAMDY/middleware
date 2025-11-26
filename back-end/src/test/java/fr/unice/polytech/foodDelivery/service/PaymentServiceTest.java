package fr.unice.polytech.foodDelivery.service;

import fr.unice.polytech.foodDelivery.domain.model.CustomerAccount;
import fr.unice.polytech.foodDelivery.domain.ENUM.OrderStatus;
import fr.unice.polytech.foodDelivery.domain.ENUM.PaymentMethod;
import fr.unice.polytech.foodDelivery.domain.ENUM.PaymentStatus;
import fr.unice.polytech.foodDelivery.domain.exception.PaymentException;
import fr.unice.polytech.foodDelivery.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentServiceTest {

    private PaymentService paymentService;
    private CustomerAccount customer;
    private RestaurantAccount restaurant;
    private Order order;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService();
        customer = new CustomerAccount("Alice", "Nice", 100.0f);
        restaurant = new RestaurantAccount("Pizza Planet", null);
        order = new Order(customer, restaurant);

        // Ajoute un repas pour fixer le montant
        Meal pizza = new Meal.Builder("Pizza", 20.0f).build();
        order.addMeal(pizza);
    }

    // -------- TESTS --------

    @Test
    void testPaymentWithAllowanceSuccess() throws PaymentException {
        double initialAllowance = customer.getAllowance();

        Payment payment = paymentService.pay(order, PaymentMethod.ALLOWANCE);

        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals(initialAllowance - order.getAmount(), customer.getAllowance(), 0.001);
    }

    @Test
    void testPaymentWithAllowanceFailure() {
        customer.setAllowance(5.0f); // Trop faible pour payer la commande

        PaymentException exception = assertThrows(PaymentException.class, () ->
                paymentService.pay(order, PaymentMethod.ALLOWANCE)
        );

        assertTrue(exception.getMessage().contains("Payment not validated"));
        assertEquals(OrderStatus.FAILED, order.getStatus());
    }

    @Test
    void testPaymentWithCardAlwaysReturnsPayment() {
        // Comme simulateExternalPayment utilise Math.random(), on ne peut pas prévoir succès ou échec.
        // On teste donc les deux branches.
        try {
            Payment payment = paymentService.pay(order, PaymentMethod.CARD);
            assertNotNull(payment);
            assertTrue(payment.getStatus() == PaymentStatus.SUCCESS || payment.getStatus() == PaymentStatus.FAILED);
        } catch (PaymentException e) {
            // OK si échec
            assertEquals(OrderStatus.FAILED, order.getStatus());
        }
    }

    @Test
    void testPaymentWithPaypal() {
        try {
            Payment payment = paymentService.pay(order, PaymentMethod.PAYPAL);
            assertNotNull(payment);
            assertTrue(payment.getStatus() == PaymentStatus.SUCCESS || payment.getStatus() == PaymentStatus.FAILED);
        } catch (PaymentException e) {
            assertEquals(OrderStatus.FAILED, order.getStatus());
        }
    }

    @Test
    void testPaymentWithUnsupportedMethodFails() {
        PaymentMethod unsupported = null;
        PaymentException exception = assertThrows(PaymentException.class, () ->
                paymentService.pay(order, unsupported)
        );

        assertTrue(exception.getMessage().contains("Payment not validated"));
    }
}
