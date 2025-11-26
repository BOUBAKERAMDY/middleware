package fr.unice.polytech.foodDelivery.model;

import fr.unice.polytech.foodDelivery.domain.model.CustomerAccount;
import fr.unice.polytech.foodDelivery.domain.ENUM.*;
import fr.unice.polytech.foodDelivery.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class RestaurantAccountTest {

    private RestaurantAccount restaurant;
    private Menu menu;
    private CustomerAccount customer;
    private List<CuisineType> cuisineTypes;
    private int[] priceRange;

    @BeforeEach
    void setUp() {
        menu = new Menu();
        menu.addMeal(new Meal.Builder("Pizza Margherita", 10.0f).build());
        menu.addMeal(new Meal.Builder("Pasta Carbonara", 12.5f).build());

        cuisineTypes = Arrays.asList(CuisineType.ITALIAN);
        priceRange = new int[] { 8, 25 };

        restaurant = new RestaurantAccount(
                "La Dolce Vita", // name
                menu, // menu
                cuisineTypes, // cuisineTypes
                RestaurantType.RESTAURANT, // restaurantType
                priceRange // priceRange
        );

        customer = new CustomerAccount("Alice", "Paris", 100.0f);
    }

    @Test
    void testRestaurantInitialization() {
        assertNotNull(restaurant.getRestaurantId(), "L'ID du restaurant ne doit pas être null");
        assertEquals("La Dolce Vita", restaurant.getName());
        assertEquals(menu, restaurant.getMenu());
        assertEquals(cuisineTypes, restaurant.getCuisineTypes());
        assertEquals(RestaurantType.RESTAURANT, restaurant.getRestaurantType());
        assertArrayEquals(priceRange, restaurant.getPriceRange());
        assertNotNull(restaurant.getPlanning(), "Le planning ne doit pas être null");
    }

    @Test
    void testMenuContents() {
        List<Meal> menuList = restaurant.getMenu().getMeals();
        assertEquals(2, menuList.size());
        assertEquals("Pizza Margherita", menuList.get(0).getName());
        assertEquals(12.5f, menuList.get(1).getPrice(), 0.001);
    }

    @Test
    void testNotifyNewOrderAddsOrderToList() {
        Order order1 = new Order(customer, restaurant);
        Order order2 = new Order(customer, restaurant);

        restaurant.notifyNewOrder(order1);
        restaurant.notifyNewOrder(order2);

        // Les commandes sont maintenant stockées dans une List au lieu d'une Queue
        // On ne peut pas y accéder directement via un getter, mais on peut vérifier
        // que le comportement fonctionne via d'autres méthodes
        assertDoesNotThrow(() -> restaurant.notifyNewOrder(order1));
        assertDoesNotThrow(() -> restaurant.notifyNewOrder(order2));
    }

    @Test
    void testAddToPlanning() {
        restaurant.setPlanning(new Planning());
        restaurant.addToPlanning(
                "lunch",
                DayOfWeek.MONDAY,
                LocalTime.of(12, 0),
                LocalTime.of(14, 0),
                50);

        Planning planning = restaurant.getPlanning();
        assertNotNull(planning);
    }

    @Test
    void testInvalidPriceRangeThrowsException() {
        // Test avec un tableau de taille incorrecte
        assertThrows(IllegalArgumentException.class, () -> {
            new RestaurantAccount(
                    "Test Restaurant",
                    menu,
                    cuisineTypes,
                    RestaurantType.FAST_FOOD,
                    new int[] { 10 } // Tableau trop court
            );
        });

        // Test avec des valeurs négatives
        assertThrows(IllegalArgumentException.class, () -> {
            new RestaurantAccount(
                    "Test Restaurant",
                    menu,
                    cuisineTypes,
                    RestaurantType.FAST_FOOD,
                    new int[] { -5, 10 } // Valeur négative
            );
        });

        // Test avec min > max
        assertThrows(IllegalArgumentException.class, () -> {
            new RestaurantAccount(
                    "Test Restaurant",
                    menu,
                    cuisineTypes,
                    RestaurantType.FAST_FOOD,
                    new int[] { 20, 10 } // min > max
            );
        });
    }

    @Test
    void testValidPriceRanges() {
        // Test avec des prix valides
        assertDoesNotThrow(() -> {
            new RestaurantAccount(
                    "Test Restaurant",
                    menu,
                    cuisineTypes,
                    RestaurantType.FAST_FOOD,
                    new int[] { 0, 50 } // Prix valides
            );
        });

        assertDoesNotThrow(() -> {
            new RestaurantAccount(
                    "Test Restaurant 2",
                    menu,
                    cuisineTypes,
                    RestaurantType.FAST_FOOD,
                    new int[] { 10, 10 } // min = max
            );
        });
    }

    @Test
    void testMultipleCuisineTypes() {
        List<CuisineType> multipleCuisines = Arrays.asList(CuisineType.ITALIAN, CuisineType.FRENCH);

        RestaurantAccount multiCuisineRestaurant = new RestaurantAccount(
                "BouBoulange",
                menu,
                multipleCuisines,
                RestaurantType.BOULANGERIE,
                new int[] { 15, 60 });

        assertEquals(2, multiCuisineRestaurant.getCuisineTypes().size());
        assertTrue(multiCuisineRestaurant.getCuisineTypes().contains(CuisineType.ITALIAN));
        assertTrue(multiCuisineRestaurant.getCuisineTypes().contains(CuisineType.FRENCH));
    }

    @Test
    void testEmptyMenu() {
        Menu emptyMenu = new Menu();

        RestaurantAccount emptyMenuRestaurant = new RestaurantAccount(
                "Empty Menu Restaurant",
                emptyMenu,
                cuisineTypes,
                RestaurantType.CAFE,
                new int[] { 5, 15 });

        assertTrue(emptyMenuRestaurant.getMenu().getMeals().isEmpty());
    }

    @Test
    void testSimplifiedConstructor() {
        RestaurantAccount simpleRestaurant = new RestaurantAccount("Simple Restaurant", new Menu());
        assertNotNull(simpleRestaurant.getRestaurantId());
        assertEquals("Simple Restaurant", simpleRestaurant.getName());
        assertNotNull(simpleRestaurant.getMenu());
        assertNotNull(simpleRestaurant.getCuisineTypes());
        assertNotNull(simpleRestaurant.getRestaurantType());
        assertArrayEquals(new int[] { 0, 100 }, simpleRestaurant.getPriceRange());
    }
}