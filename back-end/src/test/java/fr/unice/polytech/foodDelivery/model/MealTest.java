package fr.unice.polytech.foodDelivery.model;

import fr.unice.polytech.foodDelivery.domain.ENUM.DiateryPreference;
import fr.unice.polytech.foodDelivery.domain.ENUM.DishType;
import fr.unice.polytech.foodDelivery.domain.model.Meal;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MealTest {

    @Test
    void testMealBuilderWithBasicFields() {
        Meal meal = new Meal.Builder("Burger", 8.5)
                .description("Beef burger with cheese")
                .build();

        assertEquals("Burger", meal.getName());
        assertEquals(8.5, meal.getPrice(), 0.01);
        assertEquals("Beef burger with cheese", meal.getDescription());
    }

    @Test
    void testMealBuilderWithCategoryAndType() {
        Meal meal = new Meal.Builder("Caesar Salad", 7.0)
                .description("Fresh Caesar salad")
                .build();
        assertEquals(0, meal.getDietaryTags().size());
    }

    @Test
    void testSimpleConstructorWithNameAndPrice() {
        Meal meal = new Meal.Builder("Soup", 4.5).build();

        assertEquals("Soup", meal.getName());
        assertEquals(4.5, meal.getPrice(), 0.01);
        assertNotNull(meal.getToppings());
        assertNotNull(meal.getDietaryTags());
        assertTrue(meal.getToppings().isEmpty());
        assertTrue(meal.getDietaryTags().isEmpty());
    }

    @Test
    void testConstructorWithIdNamePriceDescription() {
        Meal meal = new Meal.Builder("Steak", 18.5)
                .description("Grilled ribeye steak")
                .build();

        assertEquals("Steak", meal.getName());
        assertEquals(18.5, meal.getPrice(), 0.01);
        assertEquals("Grilled ribeye steak", meal.getDescription());
    }

    @Test
    void testGetMealPriceReturnsCorrectValue() {
        Meal meal = new Meal.Builder("Dessert", 6.0).build();
        assertEquals(6.0, meal.getPrice(), 0.01);
    }

    @Test
    void testGetMealDescriptionReturnsCorrectValue() {
        Meal meal = new Meal.Builder("Ice Cream", 4.0f)
                .description("Vanilla ice cream")
                .build();
        assertEquals("Vanilla ice cream", meal.getDescription());
    }

    @Test
    void testGetToppingsReturnsDefensiveCopy() {
        Meal meal = new Meal.Builder("Pizza", 10.0)
                .addTopping("Cheese")
                .build();

        List<String> toppings1 = meal.getToppings();
        List<String> toppings2 = meal.getToppings();

        // Should return different list instances (defensive copy)
        assertNotSame(toppings1, toppings2);
        assertEquals(toppings1, toppings2);
    }

    @Test
    void testGetDietaryTagsReturnsDefensiveCopy() {
        Meal meal = new Meal.Builder("Salad", 7.0)
                .addDietaryTag(DiateryPreference.GLUTEN_FREE)
                .build();

        List<DiateryPreference> tags1 = meal.getDietaryTags();
        List<DiateryPreference> tags2 = meal.getDietaryTags();

        // Should return different list instances (defensive copy)
        assertNotSame(tags1, tags2);
        assertEquals(tags1, tags2);
    }

    @Test
    void testEmptyToppingsAndTagsWhenNotSet() {
        Meal meal = new Meal.Builder("Simple Meal", 5.0).build();

        assertNotNull(meal.getToppings());
        assertNotNull(meal.getDietaryTags());
        assertTrue(meal.getToppings().isEmpty());
        assertTrue(meal.getDietaryTags().isEmpty());
    }

    @Test
    void testConstructorWithDietaryTagsAndDishType() {
        List<DiateryPreference> tags = List.of(DiateryPreference.VEGAN);
        Meal meal = new Meal.Builder("Tofu Stir Fry", 12.0)
                .description("Asian style")
                .dietaryTags(tags)
                .build();

        assertEquals("Tofu Stir Fry", meal.getName());
        assertEquals(tags, meal.getDietaryTags());
    }

    @Test
    void testBuilderMealIdAndToppingsList() {
        List<String> toppings = List.of("Tomate", "Basilic");
        Meal meal = new Meal.Builder("Pizza", 10)
                .mealId()
                .toppings(toppings)
                .dietaryTags(List.of(DiateryPreference.GLUTEN_FREE))
                .build();

        assertNotNull(meal.getMealId());
        assertEquals(toppings, meal.getToppings());
        assertEquals(List.of(DiateryPreference.GLUTEN_FREE), meal.getDietaryTags());
    }
}
