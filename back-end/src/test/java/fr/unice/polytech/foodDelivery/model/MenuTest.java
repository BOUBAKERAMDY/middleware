package fr.unice.polytech.foodDelivery.model;

import fr.unice.polytech.foodDelivery.domain.model.Meal;
import fr.unice.polytech.foodDelivery.domain.model.Menu;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MenuTest {

    private Menu menu;
    private Meal burger;
    private Meal pizza;
    private Meal pasta;

    @BeforeEach
    void setUp() {
        menu = new Menu();
        burger = new Meal.Builder("Burger", 8.5)
                .description("Beef burger")
                .build();
        pizza = new Meal.Builder("Pizza", 12.0)
                .description("Margherita pizza")
                .build();
        pasta = new Meal.Builder("Pasta", 10.0)
                .description("Carbonara pasta")
                .build();
    }

    @Test
    void testEmptyMenuInitialization() {
        Menu emptyMenu = new Menu();
        assertNotNull(emptyMenu.getMeals());
        assertTrue(emptyMenu.getMeals().isEmpty());
    }

    @Test
    void testMenuInitializationWithMealsList() {
        List<Meal> meals = Arrays.asList(burger, pizza);
        Menu menuWithMeals = new Menu(meals);

        assertEquals(2, menuWithMeals.getMeals().size());
        assertTrue(menuWithMeals.containsMeal(burger));
        assertTrue(menuWithMeals.containsMeal(pizza));
    }

    @Test
    void testAddMealToMenu() {
        menu.addMeal(burger);

        assertEquals(1, menu.getMeals().size());
        assertTrue(menu.containsMeal(burger));
    }

    @Test
    void testAddMultipleMealsToMenu() {
        menu.addMeal(burger);
        menu.addMeal(pizza);
        menu.addMeal(pasta);

        assertEquals(3, menu.getMeals().size());
        assertTrue(menu.containsMeal(burger));
        assertTrue(menu.containsMeal(pizza));
        assertTrue(menu.containsMeal(pasta));
    }

    @Test
    void testAddDuplicateMealDoesNotDuplicate() {
        menu.addMeal(burger);
        menu.addMeal(burger);
        menu.addMeal(burger);

        // Should only contain one instance
        assertEquals(1, menu.getMeals().size());
    }

    @Test
    void testRemoveMealFromMenu() {
        menu.addMeal(burger);
        menu.addMeal(pizza);

        assertTrue(menu.containsMeal(burger));

        menu.removeMeal(burger);

        assertFalse(menu.containsMeal(burger));
        assertEquals(1, menu.getMeals().size());
        assertTrue(menu.containsMeal(pizza));
    }

    @Test
    void testRemoveMealThatDoesNotExist() {
        menu.addMeal(burger);

        // Removing a meal not in menu should not throw error
        assertDoesNotThrow(() -> menu.removeMeal(pizza));

        assertEquals(1, menu.getMeals().size());
        assertTrue(menu.containsMeal(burger));
    }

    @Test
    void testContainsMeal() {
        menu.addMeal(burger);

        assertTrue(menu.containsMeal(burger));
        assertFalse(menu.containsMeal(pizza));
    }

    @Test
    void testContainsMealByName() {
        menu.addMeal(burger);
        menu.addMeal(pizza);

        assertTrue(menu.containsMealByName("Burger"));
        assertTrue(menu.containsMealByName("Pizza"));
        assertFalse(menu.containsMealByName("Pasta"));
        assertFalse(menu.containsMealByName("NonExistent"));
    }

    @Test
    void testContainsMealByNameIsCaseSensitive() {
        menu.addMeal(burger);

        assertTrue(menu.containsMealByName("Burger"));
        assertFalse(menu.containsMealByName("burger"));
        assertFalse(menu.containsMealByName("BURGER"));
    }

    @Test
    void testGetMeals() {
        menu.addMeal(burger);
        menu.addMeal(pizza);

        List<Meal> meals = menu.getMeals();

        assertEquals(2, meals.size());
        assertTrue(meals.contains(burger));
        assertTrue(meals.contains(pizza));
    }

    /*@Test
    void testGetMealsReturnsDefensiveCopy() {
        menu.addMeal(burger);

        List<Meal> meals1 = menu.getMeals();
        List<Meal> meals2 = menu.getMeals();

        // Should return different list instances (defensive copy)
        assertNotSame(meals1, meals2);

        // Modifying returned list should not affect menu
        meals1.add(pizza);
        assertEquals(1, menu.getMeals().size());
        assertFalse(menu.containsMeal(pizza));
    }*/

    @Test
    void testGetMealByName() {
        menu.addMeal(burger);
        menu.addMeal(pizza);

        Meal found = menu.getMealByName("Burger");

        assertNotNull(found);
        assertEquals("Burger", found.getName());
        assertEquals(burger, found);
    }

    @Test
    void testGetMealByNameNotFound() {
        menu.addMeal(burger);

        Meal notFound = menu.getMealByName("Pasta");

        assertNull(notFound);
    }

    @Test
    void testGetMealByNameWithMultipleMeals() {
        menu.addMeal(burger);
        menu.addMeal(pizza);
        menu.addMeal(pasta);

        Meal foundPizza = menu.getMealByName("Pizza");
        assertNotNull(foundPizza);
        assertEquals("Pizza", foundPizza.getName());
        assertEquals(12.0, foundPizza.getPrice(), 0.01);
    }

    @Test
    void testMenuOperationsInSequence() {
        // Start empty
        assertTrue(menu.getMeals().isEmpty());

        // Add meals
        menu.addMeal(burger);
        menu.addMeal(pizza);
        assertEquals(2, menu.getMeals().size());

        // Check contains
        assertTrue(menu.containsMealByName("Burger"));
        assertTrue(menu.containsMealByName("Pizza"));

        // Get by name
        assertNotNull(menu.getMealByName("Burger"));

        // Remove one
        menu.removeMeal(burger);
        assertEquals(1, menu.getMeals().size());
        assertFalse(menu.containsMealByName("Burger"));
        assertTrue(menu.containsMealByName("Pizza"));

        // Add it back
        menu.addMeal(burger);
        assertEquals(2, menu.getMeals().size());
        assertTrue(menu.containsMealByName("Burger"));
    }

    @Test
    void testEmptyMenuBehavior() {
        Menu emptyMenu = new Menu();

        assertFalse(emptyMenu.containsMeal(burger));
        assertFalse(emptyMenu.containsMealByName("Burger"));
        assertNull(emptyMenu.getMealByName("Burger"));
        assertTrue(emptyMenu.getMeals().isEmpty());
    }
}

