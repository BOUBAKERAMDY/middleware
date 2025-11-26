package fr.unice.polytech.foodDelivery.service;

import fr.unice.polytech.foodDelivery.domain.ENUM.CuisineType;
import fr.unice.polytech.foodDelivery.domain.ENUM.DiateryPreference;
import fr.unice.polytech.foodDelivery.domain.ENUM.MealCategory;
import fr.unice.polytech.foodDelivery.domain.ENUM.MealType;
import fr.unice.polytech.foodDelivery.domain.ENUM.RestaurantType;
import fr.unice.polytech.foodDelivery.domain.exception.CapacityExceededException;
import fr.unice.polytech.foodDelivery.domain.exception.MealNotInMenuException;
import fr.unice.polytech.foodDelivery.domain.exception.PlanningException;
import fr.unice.polytech.foodDelivery.domain.model.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RestaurantManagementTest {

    private RestaurantService restaurantService;
    private RestaurantAccount restaurant;
    UUID restaurantId;

    @BeforeEach
    void setUp() {
        restaurantService = new RestaurantService();
        restaurantService.clearData();
        restaurant = new RestaurantAccount("Test Restaurant", new Menu());
        restaurantId = restaurant.getRestaurantId();
        restaurantService.registerRestaurant(restaurant);
    }

    @Test
    void testAddMealToMenu() {
        Meal meal = new Meal.Builder("Pizza Margherita", 12.5f)

                .description("Classic Italian pizza")
                .category(MealCategory.MAIN_COURSE)
                .type(MealType.VEGETARIAN)
                .addTopping("Mozzarella")
                .addTopping("Tomato Sauce")
                .addTopping("Basil")
                .addDietaryTag(DiateryPreference.VEGETARIAN)
                .build();

        restaurantService.addMealToMenu(restaurantId, meal);

        assertTrue(restaurantService.isMealInMenu(restaurantId, meal));
        assertEquals(1, restaurant.getMenu().getMeals().size());
    }

    @Test
    void testRemoveMealFromMenu() {
        // Given: A restaurant with a meal in the menu
        Meal meal = new Meal.Builder("Burger", 10.0f)
                .category(MealCategory.MAIN_COURSE)
                .build();
        restaurantService.addMealToMenu(restaurantId, meal);

        restaurantService.removeMealFromMenu(restaurantId, meal);

        assertFalse(restaurantService.isMealInMenu(restaurantId, meal));
        assertEquals(0, restaurant.getMenu().getMeals().size());
    }

    @Test
    void testUpdateMealInMenu() {

        Meal oldMeal = new Meal.Builder("Old Salad", 8.0f)
                .category(MealCategory.STARTER)
                .build();
        restaurantService.addMealToMenu(restaurantId, oldMeal);

        Meal newMeal = new Meal.Builder("Caesar Salad", 10.0f)
                .category(MealCategory.STARTER)
                .type(MealType.VEGETARIAN)
                .addDietaryTag(DiateryPreference.GLUTEN_FREE)
                .build();
        restaurantService.updateMeal(restaurantId, oldMeal, newMeal);

        assertFalse(restaurantService.isMealInMenu(restaurantId, oldMeal));
        assertTrue(restaurantService.isMealInMenu(restaurantId, newMeal));
    }

    @Test
    void testGetMealByNameSuccess() {
        // Given: A meal in the menu
        Meal meal = new Meal.Builder("Pasta Carbonara", 14.0f)
                .description("Creamy pasta with bacon")
                .build();
        restaurantService.addMealToMenu(restaurantId, meal);

        // When: Getting meal by name
        Meal foundMeal = restaurantService.getMealByName(restaurantId, "Pasta Carbonara");

        // Then: Should return the correct meal
        assertNotNull(foundMeal);
        assertEquals("Pasta Carbonara", foundMeal.getName());
        assertEquals(14.0f, foundMeal.getPrice());
    }

    @Test
    void testGetMealByNameThrowsExceptionWhenNotFound() {
        // Given: An empty menu

        // When/Then: Should throw exception
        assertThrows(MealNotInMenuException.class, () -> {
            restaurantService.getMealByName(restaurantId, "Non-existent Meal");
        });
    }

    // Public information management tests
    @Test
    void testUpdatePublicInfo() {
        // Given: Restaurant with no public info

        // When: Updating public info
        String info = "Best Italian restaurant in town. Open daily 11-22h.";
        restaurantService.updatePublicInfo(restaurantId, info);

        // Then: Public info should be updated
        assertEquals(info, restaurant.getPublicInfo());
    }

    @Test
    void testUpdateRestaurantName() {
        // Given: Restaurant with original name
        assertEquals("Test Restaurant", restaurant.getName());

        // When: Updating name
        restaurantService.updateRestaurantName(restaurantId, "New Restaurant Name");

        // Then: Name should be updated
        assertEquals("New Restaurant Name", restaurant.getName());
    }

    // R5: Planning and capacity management tests
    @Test
    void testAddTimeSlot() {
        // Given: A restaurant with no time slots

        // When: Adding a time slot with capacity
        LocalTime start = LocalTime.of(12, 0);
        LocalTime end = LocalTime.of(12, 30);
        restaurantService.addTimeSlot(restaurantId, "12:00-12:30", DayOfWeek.FRIDAY, start, end, 10);

        // Then: Time slot should be available
        assertTrue(restaurantService.isSlotAvailable(restaurantId, "12:00-12:30"));
    }

    @Test
    void testAddTimeSlotWithInvalidCapacityThrowsException() {
        // Given: Invalid capacity values
        LocalTime start = LocalTime.of(12, 0);
        LocalTime end = LocalTime.of(12, 30);

        // When/Then: Should throw exception for zero or negative capacity
        assertThrows(PlanningException.class, () -> {
            restaurantService.addTimeSlot(restaurantId, "slot1", DayOfWeek.FRIDAY, start, end, 0);
        });

        assertThrows(PlanningException.class, () -> {
            restaurantService.addTimeSlot(restaurantId, "slot2", DayOfWeek.FRIDAY, start, end, -5);
        });
    }

    @Test
    void testUpdateSlotCapacity() {
        // Given: A time slot with initial capacity
        LocalTime start = LocalTime.of(18, 0);
        LocalTime end = LocalTime.of(18, 30);
        restaurantService.addTimeSlot(restaurantId, "18:00-18:30", DayOfWeek.FRIDAY, start, end, 15);

        // When: Updating capacity
        restaurantService.updateSlotCapacity(restaurantId, "18:00-18:30", 20);

        // Then: Slot should still be available with new capacity
        assertTrue(restaurantService.isSlotAvailable(restaurantId, "18:00-18:30"));
    }

    @Test
    void testUpdateSlotCapacityWithNegativeValueThrowsException() {
        // Given: A time slot
        LocalTime start = LocalTime.of(19, 0);
        LocalTime end = LocalTime.of(19, 30);
        restaurantService.addTimeSlot(restaurantId, "19:00-19:30", DayOfWeek.FRIDAY, start, end, 10);

        // When/Then: Should throw exception for negative capacity
        assertThrows(PlanningException.class, () -> {
            restaurantService.updateSlotCapacity(restaurantId, "19:00-19:30", -1);
        });
    }

    // C5-C7: Order capacity validation tests
    @Test
    void testValidateOrderCapacitySuccess() {
        // Given: A time slot with available capacity
        LocalTime start = LocalTime.of(13, 0);
        LocalTime end = LocalTime.of(13, 30);
        restaurantService.addTimeSlot(restaurantId, "13:00-13:30", DayOfWeek.FRIDAY, start, end, 5);

        // When/Then: Should not throw exception
        assertDoesNotThrow(() -> {
            restaurantService.validateOrderCapacity(restaurantId, "13:00-13:30");
        });
    }

    @Test
    void testReserveSlotForOrder() {
        // Given: A time slot with capacity
        LocalTime start = LocalTime.of(14, 0);
        LocalTime end = LocalTime.of(14, 30);
        restaurantService.addTimeSlot(restaurantId, "14:00-14:30", DayOfWeek.FRIDAY, start, end, 3);

        // When: Reserving slots for orders
        restaurantService.reserveSlotForOrder(restaurantId, "14:00-14:30");
        restaurantService.reserveSlotForOrder(restaurantId, "14:00-14:30");

        // Then: Slot should still be available (2 out of 3 used)
        assertTrue(restaurantService.isSlotAvailable(restaurantId, "14:00-14:30"));
    }

    @Test
    void testReserveSlotThrowsExceptionWhenCapacityExceeded() {
        // Given: A time slot with limited capacity
        LocalTime start = LocalTime.of(20, 0);
        LocalTime end = LocalTime.of(20, 30);
        restaurantService.addTimeSlot(restaurantId, "20:00-20:30", DayOfWeek.FRIDAY, start, end, 2);

        // When: Filling up the capacity
        restaurantService.reserveSlotForOrder(restaurantId, "20:00-20:30");
        restaurantService.reserveSlotForOrder(restaurantId, "20:00-20:30");

        // Then: Should throw exception when capacity exceeded
        assertThrows(CapacityExceededException.class, () -> {
            restaurantService.reserveSlotForOrder(restaurantId, "20:00-20:30");
        });
    }

    // Integration test: Complete restaurant setup
    @Test
    void testCompleteRestaurantSetup() {
        // Given: A new restaurant

        RestaurantAccount newRestaurant = new RestaurantAccount("Bella Italia", new Menu());
        restaurantService.registerRestaurant(newRestaurant);

        // When: Setting up complete restaurant profile
        restaurantService.updatePublicInfo(newRestaurant.getRestaurantId(), "Authentic Italian cuisine");

        // Add multiple meals to menu
        Meal pizza = new Meal.Builder("Pizza Quattro Formaggi", 15.0f)
                .category(MealCategory.MAIN_COURSE)
                .type(MealType.VEGETARIAN)
                .addDietaryTag(DiateryPreference.VEGAN)
                .build();

        Meal pasta = new Meal.Builder("Spaghetti Bolognese", 13.0f)
                .category(MealCategory.MAIN_COURSE)
                .type(MealType.REGULAR)
                .build();

        Meal tiramisu = new Meal.Builder("Tiramisu", 7.0f)
                .category(MealCategory.DESSERT)
                .type(MealType.VEGETARIAN)
                .addDietaryTag(DiateryPreference.CONTAIN_ALCOHOL)
                .build();

        restaurantService.addMealToMenu(newRestaurant.getRestaurantId(), pizza);
        restaurantService.addMealToMenu(newRestaurant.getRestaurantId(), pasta);
        restaurantService.addMealToMenu(newRestaurant.getRestaurantId(), tiramisu);

        // Add time slots with capacities
        restaurantService.addTimeSlot(newRestaurant.getRestaurantId(), "12:00-12:30", DayOfWeek.FRIDAY,
                LocalTime.of(12, 0), LocalTime.of(12, 30), 20);
        restaurantService.addTimeSlot(newRestaurant.getRestaurantId(), "12:30-13:00", DayOfWeek.FRIDAY,
                LocalTime.of(12, 30), LocalTime.of(13, 0), 20);

        // Then: Restaurant should be fully configured
        newRestaurant = restaurantService.findByName(newRestaurant.getName());
        assertEquals("Bella Italia", newRestaurant.getName());
        assertEquals("Authentic Italian cuisine", newRestaurant.getPublicInfo());
        assertEquals(3, newRestaurant.getMenu().getMeals().size());
        assertTrue(restaurantService.isSlotAvailable(newRestaurant.getRestaurantId(), "12:00-12:30"));
        assertTrue(restaurantService.isSlotAvailable(newRestaurant.getRestaurantId(), "12:30-13:00"));
    }

    @Test
    void testFindAllReturnsRegisteredRestaurants() {
        RestaurantAccount r2 = new RestaurantAccount("Another Restaurant", new Menu());
        restaurantService.registerRestaurant(r2);

        var all = restaurantService.findAll();

        assertEquals(2, all.size());
        assertTrue(all.stream().anyMatch(r -> r.getName().equals("Test Restaurant")));
        assertTrue(all.stream().anyMatch(r -> r.getName().equals("Another Restaurant")));
    }

    @Test
    void testFindByNameReturnsCorrectRestaurant() {
        RestaurantAccount found = restaurantService.findByName("Test Restaurant");
        assertNotNull(found);
        assertEquals(restaurantId, found.getRestaurantId());
    }

    @Test
    void testFindByNameReturnsNullIfNotFound() {
        RestaurantAccount found = restaurantService.findByName("Nonexistent");
        assertNull(found);
    }

    @Test
    void testGetRestaurantMenuReturnsMenu() {
        Meal meal = new Meal.Builder("Soup", 5.0f).category(MealCategory.STARTER).build();
        restaurantService.addMealToMenu(restaurantId, meal);

        Menu menu = restaurantService.getRestaurantMenu(restaurantId);
        assertNotNull(menu);
        assertEquals(1, menu.getMeals().size());
        assertEquals("Soup", menu.getMeals().get(0).getName());
    }

    @Test
    void testSearchByNameReturnsMatchingRestaurant() {
        List<RestaurantAccount> result = restaurantService.searchForARestaurant("Test Restaurant", null, null, false, 0,
                0);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Restaurant", result.get(0).getName());
    }

    @Test
    void testSearchWithoutFiltersReturnsAllRestaurants() {
        RestaurantAccount r2 = new RestaurantAccount("R2", new Menu());
        restaurantService.registerRestaurant(r2);

        var results = restaurantService.searchForARestaurant(null, null, null, false, 0, 0);

        assertTrue(results.size() >= 2);
    }

    @Test
    void testSearchByCuisineTypeRestaurantTypeAndPriceRange() {
        int[] price = { 5, 20 };
        RestaurantAccount r3 = new RestaurantAccount("R3", new Menu(), List.of(CuisineType.ITALIAN),
                RestaurantType.FAST_FOOD, price);
        restaurantService.registerRestaurant(r3);

        var results = restaurantService.searchForARestaurant(
                null,
                List.of(CuisineType.ITALIAN),
                RestaurantType.FAST_FOOD,
                false,
                0,
                50);

        assertNotNull(results);
        assertTrue(results instanceof List);
    }

}
