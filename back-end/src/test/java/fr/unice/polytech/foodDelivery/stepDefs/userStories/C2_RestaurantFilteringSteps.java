package fr.unice.polytech.foodDelivery.stepDefs.userStories;

import fr.unice.polytech.foodDelivery.stepDefs.userStories.context.UserStoriesContext;
import io.cucumber.java.fr.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.util.stream.Collectors;
import fr.unice.polytech.foodDelivery.domain.model.*;
import fr.unice.polytech.foodDelivery.domain.ENUM.*;
import fr.unice.polytech.foodDelivery.service.RestaurantService;

public class C2_RestaurantFilteringSteps {

    private final UserStoriesContext context;
    private final RestaurantService restaurantService;

    public C2_RestaurantFilteringSteps(UserStoriesContext context) {
        this.context = context;
        this.restaurantService = new RestaurantService();
    }

    @Étantdonné("les restaurants suivants:")
    public void les_restaurants_suivants(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> restaurants = dataTable.asMaps();
        context.restaurantService.getRestaurantMemory().findAll().clear();

        for (Map<String, String> row : restaurants) {
            String nom = row.get("nom");
            RestaurantType type = RestaurantType.valueOf(row.get("type"));
            CuisineType cuisineType = CuisineType.valueOf(row.get("cuisineType"));

            Menu menu = new Menu(new ArrayList<>());
            RestaurantAccount restaurant = new RestaurantAccount(nom, menu,
                    Arrays.asList(cuisineType), type, new int[] { 0, 100 });

            context.restaurantService.registerRestaurant(restaurant);
        }
    }

    @Étantdonné("que {string} peut préparer une commande à {string}")
    public void que_restaurant_peut_preparer_une_commande_a(String restaurantName, String time) {
        RestaurantAccount restaurant = context.restaurantService.findByName(restaurantName);
        assertNotNull(restaurant);
        String key = restaurantName + "_" + time;
        context.timeSlotCapacities.put(key, 10);
    }

    @Étantdonnéque("{string} peut préparer une commande à {string}")
    public void peut_preparer_une_commande_a(String restaurantName, String time) {
        que_restaurant_peut_preparer_une_commande_a(restaurantName, time);
    }

    @Et("que {string} ne peut pas préparer une commande à {string}")
    public void que_restaurant_ne_peut_pas_preparer_une_commande_a(String restaurantName, String time) {
        RestaurantAccount restaurant = context.restaurantService.findByName(restaurantName);
        assertNotNull(restaurant);
        String key = restaurantName + "_" + time;
        context.timeSlotCapacities.put(key, 0);
    }

    @Étantdonnéque("{string} ne peut pas préparer une commande à {string}")
    public void ne_peut_pas_preparer_une_commande_a(String restaurantName, String time) {
        que_restaurant_ne_peut_pas_preparer_une_commande_a(restaurantName, time);
    }

    @Quand("l'utilisateur filtre les restaurants disponibles à {string}")
    public void l_utilisateur_filtre_les_restaurants_disponibles_a(String time) {
        context.filteredRestaurants.clear();
        for (RestaurantAccount entry : context.restaurantService.findAll()) {
            String key = entry.getName() + "_" + time;
            Integer capacity = context.timeSlotCapacities.get(key);
            if (capacity != null && capacity > 0) {
                context.filteredRestaurants.add(entry);
            }
        }
    }

    @Étantdonné("que {string} propose des plats:")
    public void que_restaurant_propose_des_plats(String restaurantName, io.cucumber.datatable.DataTable dataTable) {
        RestaurantAccount restaurant = context.restaurantService.findByName(restaurantName);
        assertNotNull(restaurant, "Restaurant not found: " + restaurantName);

        List<Map<String, String>> plats = dataTable.asMaps();

        for (Map<String, String> row : plats) {
            String nom = row.get("nom");
            double prix = 10.0;

            if (row.containsKey("prix")) {
                prix = Double.parseDouble(row.get("prix"));
            }

            Meal.Builder builder = new Meal.Builder(nom, prix);

            if (row.containsKey("type")) {
                MealType type = MealType.valueOf(row.get("type"));
                builder.type(type);
            }

            Meal meal = builder.build();
            restaurantService.addMealToMenu(restaurant.getRestaurantId(), meal);
        }
    }

    @Étantdonnéque("{string} propose des plats:")
    public void propose_des_plats(String restaurantName, io.cucumber.datatable.DataTable dataTable) {
        que_restaurant_propose_des_plats(restaurantName, dataTable);
    }

    @Quand("l'utilisateur filtre les restaurants avec des plats {string}")
    public void l_utilisateur_filtre_les_restaurants_avec_des_plats(String filterValue) {
        context.filteredRestaurants.clear();

        // Check if it's a MealType or a dietary tag
        MealType mealType = null;
        try {
            mealType = MealType.valueOf(filterValue);
        } catch (IllegalArgumentException e) {
            // Not a meal type, might be a dietary tag
        }

        if (mealType != null) {
            // Filter by meal type
            final MealType type = mealType;
            for (RestaurantAccount restaurant : context.restaurantService.getRestaurantMemory().findAll()) {
                boolean hasType = restaurant.getMenu().getMeals().stream()
                        .anyMatch(meal -> type.equals(meal.getType()));
                if (hasType) {
                    context.filteredRestaurants.add(restaurant);
                }
            }
        } else {

            for (RestaurantAccount restaurant : context.restaurantService.getRestaurantMemory().findAll()) {
                System.out.println("DEBUG: Checking restaurant: " + restaurant.getName());
                System.out.println("DEBUG: Menu meals count: " + restaurant.getMenu().getMeals().size());
                for (Meal meal : restaurant.getMenu().getMeals()) {
                    System.out.println("DEBUG: Meal: " + meal.getName() + ", Tags: " + meal.getDietaryTags());
                }
                boolean hasTag = restaurant.getMenu().getMeals().stream()
                        .anyMatch(meal -> meal.getDietaryTags().contains(filterValue));
                System.out.println("DEBUG: Has tag '" + filterValue + "': " + hasTag);
                if (hasTag) {
                    context.filteredRestaurants.add(restaurant);
                }
            }
        }
        System.out.println("DEBUG: Filtered restaurants count: " + context.filteredRestaurants.size());
    }

    @Étantdonné("que {string} propose des plats avec les tags suivants:")
    public void que_restaurant_propose_des_plats_avec_les_tags_suivants(String restaurantName,
            io.cucumber.datatable.DataTable dataTable) {
        RestaurantAccount restaurant = context.restaurantService.findByName(restaurantName);
        System.out.println("DEBUG ADD MEAL: Restaurant '" + restaurantName + "' found: " + (restaurant != null));
        assertNotNull(restaurant, "Restaurant not found: " + restaurantName);

        List<Map<String, String>> plats = dataTable.asMaps();

        for (Map<String, String> row : plats) {
            String nom = row.get("nom");
            String tags = row.get("tags");

            // Create meal with dietary tags
            Meal.Builder builder = new Meal.Builder(nom, 10.0);
            builder.addDietaryTag(DiateryPreference.valueOf(tags));
            Meal meal = builder.build();

            System.out.println("DEBUG ADD MEAL: Adding meal '" + nom + "' with tag '" + tags + "' to restaurant '"
                    + restaurantName + "'");
            System.out.println("DEBUG ADD MEAL: Meal dietary tags after build: " + meal.getDietaryTags());
            System.out.println("DEBUG ADD MEAL: Menu size before adding: " + restaurant.getMenu().getMeals().size());

            List<String> tagList = new ArrayList<>();
            tagList.add(tags);
            context.mealTags.put(nom, tagList);

            restaurantService.addMealToMenu(restaurant.getRestaurantId(), meal);

            System.out.println("DEBUG ADD MEAL: Menu size after adding: " + restaurant.getMenu().getMeals().size());
        }
    }

    @Étantdonnéque("{string} propose des plats avec les tags suivants:")
    public void propose_des_plats_avec_les_tags_suivants(String restaurantName,
            io.cucumber.datatable.DataTable dataTable) {
        que_restaurant_propose_des_plats_avec_les_tags_suivants(restaurantName, dataTable);
    }

    @Quand("l'utilisateur filtre les restaurants avec des prix entre {double} et {double} euros")
    public void l_utilisateur_filtre_les_restaurants_avec_des_prix_entre_et_euros(Double minPrice, Double maxPrice) {
        context.filteredRestaurants.clear();

        for (RestaurantAccount restaurant : context.restaurantService.getRestaurantMemory().findAll()) {
            boolean hasMatchingPrice = restaurant.getMenu().getMeals().stream()
                    .anyMatch(meal -> meal.getPrice() >= minPrice && meal.getPrice() <= maxPrice);
            if (hasMatchingPrice) {
                context.filteredRestaurants.add(restaurant);
            }
        }
    }

    @Quand("l'utilisateur filtre les restaurants avec des prix entre {int}.{int} et {int}.{int} euros")
    public void l_utilisateur_filtre_les_restaurants_avec_des_prix_entre_et_euros_decimales(Integer minEuros,
            Integer minCentimes, Integer maxEuros, Integer maxCentimes) {
        double minPrice = minEuros + (minCentimes / 100.0);
        double maxPrice = maxEuros + (maxCentimes / 100.0);
        l_utilisateur_filtre_les_restaurants_avec_des_prix_entre_et_euros(minPrice, maxPrice);
    }

    @Quand("l'utilisateur filtre les restaurants par type de cuisine {string}")
    public void l_utilisateur_filtre_les_restaurants_par_type_de_cuisine(String cuisineTypeStr) {
        CuisineType cuisineType = CuisineType.valueOf(cuisineTypeStr);
        context.filteredRestaurants.clear();

        for (RestaurantAccount restaurant : context.restaurantService.getRestaurantMemory().findAll()) {
            if (restaurant.getCuisineTypes().contains(cuisineType)) {
                context.filteredRestaurants.add(restaurant);
            }
        }
    }

    @Quand("l'utilisateur filtre les restaurants par type d'établissement {string}")
    public void l_utilisateur_filtre_les_restaurants_par_type_d_etablissement(String typeStr) {
        RestaurantType type = RestaurantType.valueOf(typeStr);
        context.filteredRestaurants.clear();

        for (RestaurantAccount restaurant : context.restaurantService.getRestaurantMemory().findAll()) {
            if (type.equals(restaurant.getRestaurantType())) {
                context.filteredRestaurants.add(restaurant);
            }

        }
    }

    @Étantdonné("que {string} est de type {string} avec cuisine {string}")
    public void que_restaurant_est_de_type_avec_cuisine(String restaurantName, String type, String cuisine) {
        RestaurantAccount restaurant = context.restaurantService.findByName(restaurantName);
        if (restaurant != null) {
            assertEquals(RestaurantType.valueOf(type), restaurant.getRestaurantType());
            assertTrue(restaurant.getCuisineTypes().contains(CuisineType.valueOf(cuisine)));
        }
    }

    @Étantdonnéque("{string} est de type {string} avec cuisine {string}")
    public void est_de_type_avec_cuisine(String restaurantName, String type, String cuisine) {
        que_restaurant_est_de_type_avec_cuisine(restaurantName, type, cuisine);
    }

    @Quand("l'utilisateur filtre les restaurants avec:")
    public void l_utilisateur_filtre_les_restaurants_avec(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> filters = dataTable.asMaps();
        context.filteredRestaurants.clear();
        context.filteredRestaurants.addAll(context.restaurantService.getRestaurantMemory().findAll());

        for (Map<String, String> filter : filters) {
            String critere = filter.get("critère");
            String valeur = filter.get("valeur");

            if ("cuisineType".equals(critere)) {
                CuisineType cuisineType = CuisineType.valueOf(valeur);
                context.filteredRestaurants = context.filteredRestaurants.stream()
                        .filter(r -> r.getCuisineTypes().contains(cuisineType))
                        .collect(Collectors.toList());
            } else if ("type".equals(critere)) {
                RestaurantType type = RestaurantType.valueOf(valeur);
                context.filteredRestaurants = context.filteredRestaurants.stream()
                        .filter(r -> type.equals(r.getRestaurantType()))
                        .collect(Collectors.toList());
            } else if ("dietaryPref".equals(critere)) {
                MealType mealType = MealType.valueOf(valeur);
                context.filteredRestaurants = context.filteredRestaurants.stream()
                        .filter(r -> r.getMenu().getMeals().stream()
                                .anyMatch(m -> mealType.equals(m.getType())))
                        .collect(Collectors.toList());
            }
        }
    }
}
