package fr.unice.polytech.foodDelivery.repository;

import fr.unice.polytech.foodDelivery.domain.ENUM.OrderStatus;
import fr.unice.polytech.foodDelivery.domain.model.CustomerAccount;
import fr.unice.polytech.foodDelivery.domain.model.Meal;
import fr.unice.polytech.foodDelivery.domain.model.Menu;
import fr.unice.polytech.foodDelivery.domain.model.Order;
import fr.unice.polytech.foodDelivery.domain.model.RestaurantAccount;
import fr.unice.polytech.foodDelivery.repository.impl.InMemoryOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderRepositoryTest {

    private OrderRepository orderRepository;
    private CustomerAccount customer1;
    private CustomerAccount customer2;
    private RestaurantAccount restaurant1;
    private RestaurantAccount restaurant2;
    private Order order1;
    private Order order2;
    private Order order3;

    @BeforeEach
    void setUp() {
        orderRepository = InMemoryOrderRepository.getInstance();
        ((InMemoryOrderRepository) orderRepository).clear();

        customer1 = new CustomerAccount("Alice", "Campus A", 100.0f);
        customer2 = new CustomerAccount("Bob", "Campus B", 150.0f);

        Menu menu1 = new Menu();
        menu1.addMeal(new Meal.Builder("Burger", 10.0f).build());
        restaurant1 = new RestaurantAccount("Restaurant 1", menu1);

        Menu menu2 = new Menu();
        menu2.addMeal(new Meal.Builder("Pizza", 12.0f).build());
        restaurant2 = new RestaurantAccount("Restaurant 2", menu2);

        // Create orders
        order1 = new Order(customer1, restaurant1);
        order2 = new Order(customer1, restaurant2);
        order3 = new Order(customer2, restaurant1);
    }

    @Test
    void save_shouldStoreOrder() {
        Order savedOrder = orderRepository.save(order1);

        assertNotNull(savedOrder);
        assertEquals(order1.getOrderId(), savedOrder.getOrderId());
        assertEquals(1, orderRepository.count());
    }

    @Test
    void save_shouldThrowExceptionWhenOrderIsNull() {
        assertThrows(IllegalArgumentException.class, () -> orderRepository.save(null));
    }

    @Test
    void save_shouldUpdateExistingOrder() {
        orderRepository.save(order1);
        order1.setStatus(OrderStatus.VALIDATED);

        Order updatedOrder = orderRepository.save(order1);

        assertEquals(OrderStatus.VALIDATED, updatedOrder.getStatus());
        assertEquals(1, orderRepository.count());
    }

    @Test
    void findById_shouldReturnOrderWhenExists() {
        orderRepository.save(order1);

        UUID orderId = order1.getOrderId();

        Optional<Order> found = orderRepository.findById(orderId);


        assertTrue(found.isPresent());
        assertEquals(order1.getOrderId(), found.get().getOrderId());
    }

    @Test
    void findById_shouldReturnEmptyWhenNotExists() {
        Optional<Order> found = orderRepository.findById(UUID.randomUUID());

        assertFalse(found.isPresent());
    }

    @Test
    void findByCustomerId_shouldReturnAllOrdersForCustomer() {
        // Arrange
        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(order3);

        // Act
        List<Order> orders = orderRepository.findByCustomerId(customer1.getCustomerId());

        // Assert
        assertEquals(2, orders.size());
        assertTrue(orders.stream().allMatch(o -> o.getCustomer().getCustomerId().equals(customer1.getCustomerId())));
    }

    @Test
    void findByCustomerId_shouldReturnEmptyListWhenNoOrders() {
        List<Order> orders = orderRepository.findByCustomerId(UUID.randomUUID());

        assertTrue(orders.isEmpty());
    }

    @Test
    void findByRestaurantId_shouldReturnAllOrdersForRestaurant() {
        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(order3);
        UUID restaurantId = restaurant1.getRestaurantId();

        // Act
        List<Order> orders = orderRepository.findByRestaurantId(restaurantId);

        // Assert
        assertEquals(2, orders.size());
        assertTrue(orders.stream()
                .allMatch(o -> o.getRestaurant().getRestaurantId().equals(restaurant1.getRestaurantId())));
    }

    @Test
    void findByRestaurantIdShouldReturnEmptyListWhenNoOrders() {
        // Act

        List<Order> orders = orderRepository.findByRestaurantId(UUID.randomUUID()); // Use a random UUID instead of invalid string


        // Assert
        assertTrue(orders.isEmpty());
    }

    @Test
    void findAll_shouldReturnAllOrders() {
        // Arrange
        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(order3);

        // Act
        List<Order> orders = orderRepository.findAll();

        // Assert
        assertEquals(3, orders.size());
    }

    @Test
    void findAll_shouldReturnEmptyListWhenNoOrders() {
        // Act
        List<Order> orders = orderRepository.findAll();

        // Assert
        assertTrue(orders.isEmpty());
    }

    @Test
    void deleteById_shouldRemoveOrderAndReturnTrue() {
        // Arrange
        orderRepository.save(order1);
        UUID orderId = order1.getOrderId();

        // Act
        boolean deleted = orderRepository.deleteById(orderId);

        // Assert
        assertTrue(deleted);
        assertEquals(0, orderRepository.count());
        assertFalse(orderRepository.findById(orderId).isPresent());
    }

    @Test
    void deleteById_shouldReturnFalseWhenOrderNotExists() {
        // Act

        boolean deleted = orderRepository.deleteById(UUID.randomUUID()); // Use a random UUID instead of invalid string


        // Assert
        assertFalse(deleted);
    }

    @Test
    void count_shouldReturnCorrectNumberOfOrders() {
        // Assert - initially empty
        assertEquals(0, orderRepository.count());

        // Act & Assert - add orders
        orderRepository.save(order1);
        assertEquals(1, orderRepository.count());

        orderRepository.save(order2);
        assertEquals(2, orderRepository.count());

        orderRepository.save(order3);
        assertEquals(3, orderRepository.count());
    }

    @Test
    void clear_shouldRemoveAllOrders() {
        // Arrange
        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(order3);
        assertEquals(3, orderRepository.count());

        // Act
        ((InMemoryOrderRepository) orderRepository).clear();

        // Assert
        assertEquals(0, orderRepository.count());
        assertTrue(orderRepository.findAll().isEmpty());
    }

    @Test
    void repository_shouldHandleMultipleOperations() {
        // Arrange & Act
        orderRepository.save(order1);
        orderRepository.save(order2);

        List<Order> allOrders = orderRepository.findAll();
        assertEquals(2, allOrders.size());

        List<Order> customer1Orders = orderRepository.findByCustomerId(customer1.getCustomerId());
        assertEquals(2, customer1Orders.size());

        UUID order1Id = order1.getOrderId();
        orderRepository.deleteById(order1Id);

        assertEquals(1, orderRepository.count());
        assertFalse(orderRepository.findById(order1Id).isPresent());

        List<Order> remainingCustomer1Orders = orderRepository.findByCustomerId(customer1.getCustomerId());
        assertEquals(1, remainingCustomer1Orders.size());
    }
}
