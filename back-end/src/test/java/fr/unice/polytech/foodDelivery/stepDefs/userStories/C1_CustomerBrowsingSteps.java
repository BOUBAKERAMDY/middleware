package fr.unice.polytech.foodDelivery.stepDefs.userStories;

import fr.unice.polytech.foodDelivery.stepDefs.userStories.context.UserStoriesContext;
import io.cucumber.java.fr.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import fr.unice.polytech.foodDelivery.domain.model.*;
import fr.unice.polytech.foodDelivery.domain.ENUM.*;
import fr.unice.polytech.foodDelivery.service.RestaurantService;

public class C1_CustomerBrowsingSteps {

    private final UserStoriesContext context;
    private final RestaurantService restaurantService;
    private List<RestaurantAccount> browsedRestaurants;
    private Meal browsedMeal;

    public C1_CustomerBrowsingSteps(UserStoriesContext context) {
        this.context = context;
        this.restaurantService = new RestaurantService();
        this.browsedRestaurants = new ArrayList<>();
    }

    @Étantdonné("les restaurants suivants sur le campus:")
    public void les_restaurants_suivants_sur_le_campus(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> restaurants = dataTable.asMaps();
        context.restaurantService.clearData();

        for (Map<String, String> row : restaurants) {
            String nom = row.get("nom");
            RestaurantType type = RestaurantType.valueOf(row.get("type"));

            Menu menu = new Menu(new ArrayList<>());
            RestaurantAccount restaurant = new RestaurantAccount(nom, menu,
                    new ArrayList<>(), type, new int[] { 0, 100 });

            context.restaurantService.registerRestaurant(restaurant);
        }
    }

    @Et("le restaurant {string} propose les plats suivants:")
    public void le_restaurant_propose_les_plats_suivants(String restaurantName,
            io.cucumber.datatable.DataTable dataTable) {
        RestaurantAccount restaurant = context.restaurantService.getRestaurantMemory().findByName(restaurantName).get();
        assertNotNull(restaurant, "Restaurant not found: " + restaurantName);

        List<Map<String, String>> plats = dataTable.asMaps();

        for (Map<String, String> row : plats) {
            String nom = row.get("nom");
            double prix = Double.parseDouble(row.get("prix"));

            Meal.Builder mealBuilder = new Meal.Builder(nom, prix);

            // Handle optional category
            if (row.containsKey("catégorie") && row.get("catégorie") != null) {
                MealCategory categorie = MealCategory.valueOf(row.get("catégorie"));
                mealBuilder.category(categorie);
            }

            Meal meal = mealBuilder.build();
            restaurantService.addMealToMenu(restaurant.getRestaurantId(), meal);
            context.meals.put(nom, meal);
        }
    }

    @Quand("l'utilisateur consulte les restaurants disponibles")
    public void l_utilisateur_consulte_les_restaurants_disponibles() {
        browsedRestaurants = new ArrayList<>(context.restaurantService.getRestaurantMemory().findAll());
        context.filteredRestaurants = browsedRestaurants; // Store in shared context
        assertNotNull(browsedRestaurants);
    }

    @Et("l'utilisateur peut voir tous les plats de chaque restaurant")
    public void l_utilisateur_peut_voir_tous_les_plats_de_chaque_restaurant() {
        for (RestaurantAccount restaurant : browsedRestaurants) {
            assertNotNull(restaurant.getMenu());
            assertNotNull(restaurant.getMenu().getMeals());
        }
    }

    @Étantdonné("un restaurant {string} avec le plat suivant:")
    public void un_restaurant_avec_le_plat_suivant(String restaurantName, io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> plats = dataTable.asMaps();
        Map<String, String> platData = plats.get(0);

        String nom = platData.get("nom");
        double prix = Double.parseDouble(platData.get("prix"));
        MealCategory categorie = MealCategory.valueOf(platData.get("catégorie"));
        String description = platData.get("description");

        Meal meal = new Meal.Builder(nom, prix)
                .category(categorie)
                .description(description)
                .build();

        Menu menu = new Menu(Arrays.asList(meal));
        RestaurantAccount restaurant = new RestaurantAccount(restaurantName, menu);
        context.restaurantService.registerRestaurant(restaurant);

        context.restaurantService.addMealToMenu(restaurant.getRestaurantId(), meal);
        context.currentMeal = meal;
    }

    @Quand("l'utilisateur consulte les détails du plat {string}")
    public void l_utilisateur_consulte_les_details_du_plat(String mealName) {
        for (RestaurantAccount restaurant : context.restaurantService.getRestaurantMemory().findAll()) {
            Meal meal = restaurantService.getMealByName(restaurant.getRestaurantId(), mealName);
            if (meal != null) {
                browsedMeal = meal;
                context.currentMeal = meal;
                break;
            }
        }
        assertNotNull(browsedMeal, "Meal not found: " + mealName);
    }

    @Alors("l'utilisateur voit le nom {string}")
    public void l_utilisateur_voit_le_nom(String expectedName) {
        assertEquals(expectedName, browsedMeal.getName());
    }

    @Et("l'utilisateur voit le prix {double} euros")
    public void l_utilisateur_voit_le_prix_euros(Double expectedPrice) {
        assertEquals(expectedPrice, browsedMeal.getPrice(), 0.01);
    }

    @Et("l'utilisateur voit le prix {int}.{int} euros")
    public void l_utilisateur_voit_le_prix_avec_decimales_euros(Integer euros, Integer centimes) {
        double expectedPrice = euros + (centimes / 100.0);
        assertEquals(expectedPrice, browsedMeal.getPrice(), 0.01);
    }

    @Et("l'utilisateur voit la description {string}")
    public void l_utilisateur_voit_la_description(String expectedDescription) {
        assertEquals(expectedDescription, browsedMeal.getDescription());
    }
}
