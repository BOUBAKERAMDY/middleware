package fr.unice.polytech.foodDelivery.service;

import fr.unice.polytech.foodDelivery.domain.model.CustomerAccount;
import fr.unice.polytech.foodDelivery.domain.exception.MealNotInMenuException;
import fr.unice.polytech.foodDelivery.domain.exception.NoCurrentOrderException;
import fr.unice.polytech.foodDelivery.domain.model.*;
import fr.unice.polytech.foodDelivery.repository.impl.InMemoryOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Spy
    private InMemoryOrderRepository orderRepository = (InMemoryOrderRepository) InMemoryOrderRepository.getInstance(); 

    private OrderService orderService;
    private CustomerAccount customer;
    private RestaurantAccount restaurant;
    private Meal burger;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        orderService = new OrderService();

        // 🔥 Injecte ton spy dans le champ privé du service via réflexion :
        Field repoField = OrderService.class.getDeclaredField("orderRepository");
        repoField.setAccessible(true);
        repoField.set(orderService, orderRepository);

        // --- initialisation données ---
        customer = new CustomerAccount("Alice", "Campus A", 50.0f);
        burger = new Meal.Builder("Burger", 8.5f).build();
        Menu menu = new Menu();
        menu.addMeal(burger);
        restaurant = new RestaurantAccount("Test Restaurant", menu);
    }

    @Test
    void createOrder_saves_and_returns_order() {
        // Arrange
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        doAnswer(invocation -> invocation.getArgument(0))
                .when(orderRepository).save(any(Order.class));

        // Act
        Order created = orderService.createOrder(customer, restaurant);

        // Assert
        assertNotNull(created);
        verify(orderRepository).save(captor.capture());
        Order saved = captor.getValue();
        assertEquals(created.getCustomer(), saved.getCustomer());
        assertEquals(created.getRestaurant(), saved.getRestaurant());
    }

    @Test
    void createOrder_with_nulls_throws() {
        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(null, restaurant));
        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(customer, null));
    }

    @Test
    void addMealToOrder_happy_path_saves_order() {
        // Arrange
        Order order = new Order(customer, restaurant);
        doReturn(order).when(orderRepository).save(any(Order.class));

        // Act
        orderService.addMealToOrder(order, burger);

        // Assert
        assertEquals(1, order.getMeals().size());
        verify(orderRepository, atLeastOnce()).save(order);
    }

    @Test
    void addMealToOrder_null_order_throws() {
        assertThrows(NoCurrentOrderException.class, () -> orderService.addMealToOrder(null, burger));
    }

    @Test
    void addMealToOrder_null_meal_throws() {
        Order order = new Order(customer, restaurant);
        assertThrows(IllegalArgumentException.class, () -> orderService.addMealToOrder(order, null));
    }

    @Test
    void addMealToOrder_meal_not_in_menu_throws() {
        Order order = new Order(customer, restaurant);
        Meal sushi = new Meal.Builder("Sushi", 12.0f).build();
        assertFalse(restaurant.propose(sushi));
        assertThrows(MealNotInMenuException.class, () -> orderService.addMealToOrder(order, sushi));
    }
}
