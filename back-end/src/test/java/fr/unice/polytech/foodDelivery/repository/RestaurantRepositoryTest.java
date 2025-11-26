package fr.unice.polytech.foodDelivery.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.unice.polytech.foodDelivery.domain.ENUM.CuisineType;
import fr.unice.polytech.foodDelivery.domain.model.Meal;
import fr.unice.polytech.foodDelivery.domain.model.Menu;
import fr.unice.polytech.foodDelivery.domain.model.Planning;
import fr.unice.polytech.foodDelivery.domain.model.RestaurantAccount;
import fr.unice.polytech.foodDelivery.domain.ENUM.RestaurantType;
import fr.unice.polytech.foodDelivery.repository.impl.InMemoryRestaurantRepository;

public class RestaurantRepositoryTest {

        private InMemoryRestaurantRepository restaurantRepository;

        private RestaurantAccount restaurant1;
        private RestaurantAccount restaurant2;
        private Meal burger;
        private Meal pizza;
        private Meal saladNotInMenu;

        @BeforeEach
        void setUp() {
                burger = new Meal.Builder("Burger", 8.5f)
                                .description("Beef burger")
                                .build();
                pizza = new Meal.Builder("Pizza", 12.0f)
                                .description("Margherita")
                                .build();
                saladNotInMenu = new Meal.Builder("Salad", 6.0f)
                                .description("Not in menu")
                                .build();

                Menu menu1 = new Menu();
                menu1.addMeal(burger);
                menu1.addMeal(pizza);
                restaurant1 = new RestaurantAccount("Test Restaurant", menu1,
                                Arrays.asList(CuisineType.ITALIAN, CuisineType.INDIAN),
                                RestaurantType.CROUS,
                                new int[] { 5, 15 });

                Menu menu2 = new Menu();
                menu2.addMeal(burger);
                restaurant2 = new RestaurantAccount("Test Restaurant 2", menu2,
                                Arrays.asList(CuisineType.ITALIAN),
                                RestaurantType.FOOD_TRUCK,
                                new int[] { 18, 30 });

                Planning planning1 = new Planning();
                planning1.addTimeSlot("slot1", DayOfWeek.TUESDAY, LocalTime.of(8, 0), LocalTime.of(18, 0), 5);
                restaurant1.setPlanning(planning1);

                Planning planning2 = new Planning();
                planning2.addTimeSlot("slot2", DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(18, 0), 5);
                restaurant2.setPlanning(planning2);

                restaurantRepository = InMemoryRestaurantRepository.getInstance();
                restaurantRepository.clear();
                restaurantRepository.save(restaurant1);
                restaurantRepository.save(restaurant2);
        }

        @Test
        void testFindAll() {
                List<RestaurantAccount> restaurants = restaurantRepository.findAll();
                assertTrue(restaurants.size()== 2);
                assertTrue(restaurants.contains(restaurant1));
                assertTrue(restaurants.contains(restaurant2));
        }

        @Test
        void testFindByName() {
                assertEquals(restaurantRepository.findByName("Test Restaurant").get(), restaurant1);
                assertFalse(restaurantRepository.findByName("Nonexistent Restaurant").isPresent());
        }

        @Test
        void testGetByCuisineType() {
                List<RestaurantAccount> italianRestaurants = restaurantRepository
                                .getByCuisineType(Arrays.asList(CuisineType.ITALIAN));
                assertTrue(2 == italianRestaurants.size());
                assertTrue(italianRestaurants.contains(restaurant1));
                assertTrue(italianRestaurants.contains(restaurant2));

                List<RestaurantAccount> indianRestaurants = restaurantRepository
                                .getByCuisineType(Arrays.asList(CuisineType.INDIAN));
                assertEquals(1, indianRestaurants.size());
                assertTrue(indianRestaurants.contains(restaurant1));

                List<RestaurantAccount> chineseRestaurants = restaurantRepository
                                .getByCuisineType(Arrays.asList(CuisineType.CHINESE));
                assertTrue(chineseRestaurants.isEmpty());
        }

        @Test
        void testFindByRestaurantType() {
                List<RestaurantAccount> crousRestaurants = restaurantRepository
                                .findByRestaurantType(RestaurantType.CROUS);
                assertTrue(crousRestaurants.size() == 1);
                assertTrue(crousRestaurants.contains(restaurant1));
                List<RestaurantAccount> foodTruckRestaurants = restaurantRepository
                                .findByRestaurantType(RestaurantType.FOOD_TRUCK);
                assertTrue(foodTruckRestaurants.size() == 1);
                assertTrue(foodTruckRestaurants.contains(restaurant2));
                List<RestaurantAccount> restaurantRestaurant = restaurantRepository
                                .findByRestaurantType(RestaurantType.RESTAURANT);
                assertTrue(restaurantRestaurant.isEmpty());
        }

        @Test
        void testFindByIsOpen() {
                LocalTime now = LocalTime.now();
                LocalDate today = LocalDate.now();
                DayOfWeek dayOfWeek = today.getDayOfWeek();

                List<RestaurantAccount> openRestaurants = restaurantRepository.findByIsOpen();

                if (dayOfWeek == DayOfWeek.MONDAY && now.isAfter(LocalTime.of(8, 0))
                                && now.isBefore(LocalTime.of(18, 0))) {
                        assertEquals(1, openRestaurants.size());
                        assertTrue(openRestaurants.contains(restaurant2));
                } else if (dayOfWeek == DayOfWeek.TUESDAY && now.isAfter(LocalTime.of(8, 0))
                                && now.isBefore(LocalTime.of(18, 0))) {
                        assertEquals(1, openRestaurants.size());
                        assertTrue(openRestaurants.contains(restaurant1));
                } else {
                        assertTrue(openRestaurants.isEmpty());
                }
        }

        @Test
        void testFindByPriceRange() {
                List<RestaurantAccount> cheapRestaurants = restaurantRepository.findByPriceRange(0, 15);
                assertEquals(1, cheapRestaurants.size());
                assertTrue(cheapRestaurants.contains(restaurant1));

                List<RestaurantAccount> wideRangeRestaurants = restaurantRepository.findByPriceRange(0, 100);
                assertEquals(2, wideRangeRestaurants.size());
                assertTrue(wideRangeRestaurants.contains(restaurant1));
                assertTrue(wideRangeRestaurants.contains(restaurant2));

                List<RestaurantAccount> midRangeRestaurants = restaurantRepository.findByPriceRange(15, 35);
                assertEquals(1, midRangeRestaurants.size());
                assertTrue(midRangeRestaurants.contains(restaurant2));

                List<RestaurantAccount> expensiveRestaurants = restaurantRepository.findByPriceRange(40, 50);
                assertTrue(expensiveRestaurants.isEmpty());
        }
}
