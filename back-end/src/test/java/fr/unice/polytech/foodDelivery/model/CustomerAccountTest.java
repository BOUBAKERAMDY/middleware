package fr.unice.polytech.foodDelivery.model;

import fr.unice.polytech.foodDelivery.domain.model.CustomerAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerAccountTest {

    private CustomerAccount customer;

    @BeforeEach
    void setUp() {
        customer = new CustomerAccount("Alice", "Paris", 100.0f);
    }

    @Test
    void testCustomerInitialization() {
        assertNotNull(customer.getCustomerId(), "L'ID client ne doit pas être nul");
        assertEquals("Alice", customer.getName());
        assertEquals("Paris", customer.getLocation());
        assertEquals(100.0, customer.getAllowance(), 0.001f);
    }

    @Test
    void testCustomerIdIsUnique() {
        CustomerAccount customer2 = new CustomerAccount("Bob", "Lyon", 50.0f);
        assertNotEquals(customer.getCustomerId(), customer2.getCustomerId(),
                "Chaque client doit avoir un UUID unique");
    }

    @Test
    void testSetAllowance() {
        customer.setAllowance(75.5f);
        assertEquals(75.5f, customer.getAllowance(), 0.001f);
    }

    @Test
    void testNotifyOrderRegisteredDoesNotThrow() {
        assertDoesNotThrow(() -> customer.notifyOrderRegistered(),
                "notifyOrderRegistered() ne doit pas lever d'exception même si elle ne fait rien");
    }
}
