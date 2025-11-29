package fr.unice.polytech.foodDelivery.service;

import fr.unice.polytech.foodDelivery.domain.model.CustomerAccount;
import fr.unice.polytech.foodDelivery.domain.ENUM.DiateryPreference;
import fr.unice.polytech.foodDelivery.domain.ENUM.MealCategory;
import fr.unice.polytech.foodDelivery.domain.ENUM.MealType;
import fr.unice.polytech.foodDelivery.domain.ENUM.OrderStatus;
import fr.unice.polytech.foodDelivery.domain.exception.MealNotInMenuException;
import fr.unice.polytech.foodDelivery.domain.exception.NoCurrentOrderException;
import fr.unice.polytech.foodDelivery.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RestaurantToCustomerIntegrationTest {

        private ApplicationService applicationService;
        private RestaurantService restaurantService;
        private RestaurantAccount restaurant;
        private UUID restaurantId;
        private CustomerAccount customer;

        @BeforeEach
        void setUp() {
                applicationService = new ApplicationService();

                // Setup restaurant
                restaurantService = new RestaurantService();
                restaurant = new RestaurantAccount("Le Gourmet", new Menu());
                restaurantService.registerRestaurant(restaurant);
                restaurantService.updatePublicInfo(restaurantId, "Fine dining experience");

                // Setup customer - using correct constructor
                customer = new CustomerAccount("John Doe", "123 Main St", 100.0f);
                restaurantId = restaurant.getRestaurantId();
        }

        @Test
        void testCustomerOrdersFromRestaurantWithValidMenu() {
                // Given: Restaurant owner adds meals to menu
                Meal burger = new Meal.Builder("Classic Burger", 12.5f)

                                .description("Beef burger with lettuce and tomato")
                                .category(MealCategory.MAIN_COURSE)
                                .type(MealType.REGULAR)
                                .addTopping("Beef Patty")
                                .addTopping("Lettuce")
                                .addTopping("Tomato")
                                .build();

                Meal fries = new Meal.Builder("French Fries", 4.5f)

                                .category(MealCategory.SIDE_DISH)
                                .type(MealType.VEGAN)
                                .addDietaryTag(DiateryPreference.VEGAN)
                                .build();

                restaurantService.addMealToMenu(restaurantId, burger);
                restaurantService.addMealToMenu(restaurantId, fries);

                // Restaurant owner sets up planning
                restaurantService.addTimeSlot(restaurantId, "12:00-12:30", DayOfWeek.FRIDAY,
                                LocalTime.of(12, 0), LocalTime.of(12, 30), 10);

                // When: Customer creates order and adds meals
                Order order = applicationService.createOrder(customer, restaurant);
                applicationService.setCurrentOrder(order);

                applicationService.addMeal(burger);
                applicationService.addMeal(fries);

                // Then: Order should contain both meals
                assertEquals(2, order.getMeals().size());
                assertEquals(17.0f, order.getAmount(), 0.01); // 12.5 + 4.5
                assertTrue(order.getMeals().contains(burger));
                assertTrue(order.getMeals().contains(fries));
        }

        @Test
        void testCustomerCannotAddMealNotInMenu() {
                // Given: Restaurant with limited menu
                Meal pizza = new Meal.Builder("Pizza", 10.0f).build();
                restaurantService.addMealToMenu(restaurantId, pizza);

                Order order = applicationService.createOrder(customer, restaurant);
                applicationService.setCurrentOrder(order);

                // When: Customer tries to add meal not in menu
                Meal sushi = new Meal.Builder("Sushi", 15.0f).build();

                // Then: Should throw exception
                assertThrows(MealNotInMenuException.class, () -> {
                        applicationService.addMeal(sushi);
                });
        }

        @Test
        void testCustomerCannotAddMealWithoutOrder() {
                // Given: No current order
                Meal meal = new Meal.Builder("Pasta", 11.0f).build();
                restaurantService.addMealToMenu(restaurantId, meal);

                // When/Then: Should throw exception
                assertThrows(NoCurrentOrderException.class, () -> {
                        applicationService.addMeal(meal);
                });
        }

        @Test
        void testRestaurantOwnerCanUpdateMenuDynamically() {
                // Given: Restaurant with initial menu
                Meal oldSalad = new Meal.Builder("Simple Salad", 6.0f)
                                .category(MealCategory.STARTER)
                                .build();
                restaurantService.addMealToMenu(restaurantId, oldSalad);

                // When: Owner updates the menu with better option
                Meal newSalad = new Meal.Builder("Premium Caesar Salad", 9.0f)
                                .category(MealCategory.STARTER)
                                .type(MealType.VEGETARIAN)
                                .addTopping("Romaine Lettuce")
                                .addTopping("Parmesan")
                                .addTopping("Croutons")
                                .addDietaryTag(DiateryPreference.VEGETARIAN)
                                .build();

                restaurantService.removeMealFromMenu(restaurantId, oldSalad);
                restaurantService.addMealToMenu(restaurantId, newSalad);

                // Then: New meal should be available for customers
                Order order = applicationService.createOrder(customer, restaurant);
                applicationService.setCurrentOrder(order);

                assertDoesNotThrow(() -> applicationService.addMeal(newSalad));
                assertEquals(1, order.getMeals().size());
                assertEquals("Premium Caesar Salad", order.getMeals().get(0).getName());
        }

        @Test
        void testMultipleCustomersOrderWithCapacityManagement() {
                // Given: Restaurant with meal and limited capacity
                Meal steak = new Meal.Builder("Grilled Steak", 25.0f)
                                .category(MealCategory.MAIN_COURSE)
                                .build();
                restaurantService.addMealToMenu(restaurantId, steak);

                restaurantService.addTimeSlot(restaurantId, "19:00-19:30", DayOfWeek.FRIDAY,
                                LocalTime.of(19, 0), LocalTime.of(19, 30), 2);

                // When: Two customers order (within capacity)
                CustomerAccount customer1 = new CustomerAccount("Alice", "456 Oak Ave", 50.0f);
                CustomerAccount customer2 = new CustomerAccount("Bob", "789 Pine Rd", 50.0f);

                Order order1 = applicationService.createOrder(customer1, restaurant);
                applicationService.setCurrentOrder(order1);
                applicationService.addMeal(steak);

                Order order2 = applicationService.createOrder(customer2, restaurant);
                applicationService.setCurrentOrder(order2);
                applicationService.addMeal(steak);

                // Then: Both orders should be successful
                assertEquals(1, order1.getMeals().size());
                assertEquals(1, order2.getMeals().size());
                assertTrue(restaurantService.isSlotAvailable(restaurantId, "19:00-19:30"));
        }

        @Test
        void testCompleteUserStoryFlow() {
                // Complete user story: Restaurant owner manages everything, customer orders

                // Step 1: Restaurant owner sets up public information
                restaurantService.updatePublicInfo(restaurantId,
                                "Family-friendly restaurant. Specializing in Italian and Mediterranean cuisine.");

                // Step 2: Restaurant owner creates comprehensive menu with categories and
                // dietary tags
                Meal appetizer = new Meal.Builder("Bruschetta", 8.0f)

                                .description("Toasted bread with tomatoes and basil")
                                .category(MealCategory.STARTER)
                                .type(MealType.VEGAN)
                                .addDietaryTag(DiateryPreference.VEGAN)
                                .addDietaryTag(DiateryPreference.VEGETARIAN)
                                .build();

                Meal mainCourse = new Meal.Builder("Lasagna", 16.0f)

                                .description("Traditional Italian lasagna")
                                .category(MealCategory.MAIN_COURSE)
                                .type(MealType.VEGETARIAN)
                                .addTopping("Cheese")
                                .addTopping("Tomato Sauce")
                                .addDietaryTag(DiateryPreference.VEGETARIAN)
                                .build();

                Meal dessert = new Meal.Builder("Panna Cotta", 6.5f)
                                .category(MealCategory.DESSERT)
                                .type(MealType.VEGETARIAN)
                                .addDietaryTag(DiateryPreference.GLUTEN_FREE)
                                .build();

                restaurantService.addMealToMenu(restaurantId, appetizer);
                restaurantService.addMealToMenu(restaurantId, mainCourse);
                restaurantService.addMealToMenu(restaurantId, dessert);

                // Step 3: Restaurant owner sets up planning with capacity
                restaurantService.addTimeSlot(restaurantId, "12:00-12:30", DayOfWeek.FRIDAY,
                                LocalTime.of(12, 0), LocalTime.of(12, 30), 15);
                restaurantService.addTimeSlot(restaurantId, "12:30-13:00", DayOfWeek.FRIDAY,
                                LocalTime.of(12, 30), LocalTime.of(13, 0), 15);
                restaurantService.addTimeSlot(restaurantId, "19:00-19:30", DayOfWeek.FRIDAY,
                                LocalTime.of(19, 0), LocalTime.of(19, 30), 20);

                // Step 4: Customer browses menu and creates order
                assertEquals(3, restaurant.getMenu().getMeals().size());
                assertTrue(restaurantService.isSlotAvailable(restaurantId, "12:00-12:30"));

                Order customerOrder = applicationService.createOrder(customer, restaurant);
                applicationService.setCurrentOrder(customerOrder);

                // Step 5: Customer adds meals to cart/order
                applicationService.addMeal(appetizer);
                applicationService.addMeal(mainCourse);
                applicationService.addMeal(dessert);

                // Verify complete order
                assertEquals(3, customerOrder.getMeals().size());
                assertEquals(30.5f, customerOrder.getAmount(), 0.01); // 8.0 + 16.0 + 6.5
                assertEquals(OrderStatus.PENDING, customerOrder.getStatus());
                assertEquals(customer, customerOrder.getCustomer());
                assertEquals(restaurant, customerOrder.getRestaurant());

                // Verify meals have correct properties
                assertTrue(customerOrder.getMeals().stream()
                                .anyMatch(m -> m.getCategory() == MealCategory.STARTER));
                assertTrue(customerOrder.getMeals().stream()
                                .anyMatch(m -> m.getCategory() == MealCategory.MAIN_COURSE));
                assertTrue(customerOrder.getMeals().stream()
                                .anyMatch(m -> m.getCategory() == MealCategory.DESSERT));
        }

        
}
