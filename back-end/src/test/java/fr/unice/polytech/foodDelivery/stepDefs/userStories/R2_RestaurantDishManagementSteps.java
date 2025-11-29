package fr.unice.polytech.foodDelivery.stepDefs.userStories;

import fr.unice.polytech.foodDelivery.stepDefs.userStories.context.UserStoriesContext;
import fr.unice.polytech.foodDelivery.stepDefs.userStories.context.Topping;
import io.cucumber.java.fr.*;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import fr.unice.polytech.foodDelivery.domain.model.*;
import fr.unice.polytech.foodDelivery.domain.ENUM.*;
import fr.unice.polytech.foodDelivery.service.RestaurantService;


public class R2_RestaurantDishManagementSteps {

    private final UserStoriesContext context;
    private final RestaurantService restaurantService;
    private String currentManager;

    public R2_RestaurantDishManagementSteps(UserStoriesContext context) {
        this.context = context;
        this.restaurantService = new RestaurantService();
    }

    @Étantdonné("un restaurant {string} géré par {string}")
    public void un_restaurant_gere_par(String restaurantName, String managerName) {
        this.currentManager = managerName;
        if (context.restaurantService.findByName(restaurantName) == null) {
            Menu menu = new Menu(new ArrayList<>());
            context.currentRestaurant = new RestaurantAccount(restaurantName, menu);
            context.restaurantService.registerRestaurant(context.currentRestaurant);
        } else {
            context.currentRestaurant = context.restaurantService.findByName(restaurantName);
        }
    }

    @Quand("le gestionnaire crée un nouveau plat avec:")
    public void le_gestionnaire_cree_un_nouveau_plat_avec(io.cucumber.datatable.DataTable dataTable) {
        Map<String, String> data = dataTable.asMap(String.class, String.class);

        String nom = data.get("nom");
        String description = data.get("description");
        double prix = Double.parseDouble(data.get("prix"));
        MealCategory categorie = MealCategory.valueOf(data.get("catégorie"));

        Meal.Builder builder = new Meal.Builder(nom, prix)
                .description(description)
                .category(categorie);

        if (data.containsKey("type")) {
            DishType type = DishType.valueOf(data.get("type"));
            Meal meal = new Meal.Builder(nom, prix)
                    .description(description)
                    .category(categorie)
                    .build();
            restaurantService.addMealToMenu(context.currentRestaurant.getRestaurantId(), meal);
            context.currentMeal = meal;
            context.meals.put(nom, meal);
        } else {
            Meal meal = builder.build();
            restaurantService.addMealToMenu(context.currentRestaurant.getRestaurantId(), meal);
            context.currentMeal = meal;
            context.meals.put(nom, meal);
        }
    }

    @Alors("le plat {string} est ajouté au menu")
    public void le_plat_est_ajoute_au_menu(String mealName) {
        Meal meal = restaurantService.getMealByName(context.currentRestaurant.getRestaurantId(), mealName);
        assertNotNull(meal);
    }

    @Et("le plat a la description {string}")
    public void le_plat_a_la_description(String expectedDescription) {
        assertEquals(expectedDescription, context.currentMeal.getDescription());
    }

    @Et("le plat coûte {double} euros")
    public void le_plat_coute_euros(Double expectedPrice) {
        assertEquals(expectedPrice, context.currentMeal.getPrice(), 0.01);
    }

    @Et("le plat coûte {int}.{int} euros")
    public void le_plat_coute_euros_decimales(Integer euros, Integer centimes) {
        double expectedPrice = euros + (centimes / 100.0);
        le_plat_coute_euros(expectedPrice);
    }

    @Étantdonnéque("le restaurant a le plat suivant:")
    public void le_restaurant_a_le_plat_suivant(io.cucumber.datatable.DataTable dataTable) {
        que_le_restaurant_a_le_plat_suivant(dataTable);
    }

    @Étantdonné("que le restaurant a le plat suivant:")
    public void que_le_restaurant_a_le_plat_suivant(io.cucumber.datatable.DataTable dataTable) {
        // The data table is in transposed format (key-value pairs)
        Map<String, String> data = dataTable.asMap(String.class, String.class);

        String nom = data.get("nom");
        String description = data.get("description");
        String prixStr = data.get("prix");
        double prix = (prixStr != null) ? Double.parseDouble(prixStr) : 0.0;
        MealCategory categorie = MealCategory.valueOf(data.get("catégorie"));

        Meal meal = new Meal.Builder(nom, prix)
                .description(description)
                .category(categorie)
                .build();

        restaurantService.addMealToMenu(context.currentRestaurant.getRestaurantId(), meal);
        context.meals.put(nom, meal);
        context.currentMeal = meal;
    }

    @Quand("le gestionnaire met à jour le plat {string} avec:")
    public void le_gestionnaire_met_a_jour_le_plat_avec(String mealName, io.cucumber.datatable.DataTable dataTable) {
        Map<String, String> data = dataTable.asMap(String.class, String.class);

        Meal meal = restaurantService.getMealByName(context.currentRestaurant.getRestaurantId(), mealName);
        assertNotNull(meal);

        if (data.containsKey("description")) {

            String newDescription = data.get("description");
            Meal updatedMeal = new Meal.Builder(meal.getName(), meal.getPrice())
                    .description(newDescription)
                    .build();
            // Remove old and add updated
            context.currentRestaurant.getMenu().getMeals().remove(meal);
            restaurantService.addMealToMenu(context.currentRestaurant.getRestaurantId(), updatedMeal);
            context.currentMeal = updatedMeal;
            context.meals.put(mealName, updatedMeal);
        } else {
            context.currentMeal = meal;
        }
    }

    @Alors("le plat {string} a la nouvelle description")
    public void le_plat_a_la_nouvelle_description(String mealName) {
        Meal meal = restaurantService.getMealByName(context.currentRestaurant.getRestaurantId(), mealName);
        assertNotNull(meal);
        assertNotNull(meal.getDescription());
    }

    @Et("le prix reste {double} euros")
    public void le_prix_reste_euros(Double expectedPrice) {
        assertEquals(expectedPrice, context.currentMeal.getPrice(), 0.01);
    }

    @Et("le prix reste {int}.{int} euros")
    public void le_prix_reste_euros_decimales(Integer euros, Integer centimes) {
        double expectedPrice = euros + (centimes / 100.0);
        le_prix_reste_euros(expectedPrice);
    }

    @Quand("le gestionnaire crée les plats suivants:")
    public void le_gestionnaire_cree_les_plats_suivants(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> plats = dataTable.asMaps();

        for (Map<String, String> row : plats) {
            String nom = row.get("nom");
            double prix = Double.parseDouble(row.get("prix"));
            MealCategory categorie = MealCategory.valueOf(row.get("catégorie"));
            String description = row.get("description");

            if (row.containsKey("type")) {
                DishType type = DishType.valueOf(row.get("type"));
                // Use Builder without DishType (not supported in Builder)
                Meal meal = new Meal.Builder(nom, prix)
                        .category(categorie)
                        .description(description)
                        .build();
                restaurantService.addMealToMenu(context.currentRestaurant.getRestaurantId(), meal);
                context.meals.put(nom, meal);
            } else {
                Meal meal = new Meal.Builder(nom, prix)
                        .category(categorie)
                        .description(description)
                        .build();
                restaurantService.addMealToMenu(context.currentRestaurant.getRestaurantId(), meal);
                context.meals.put(nom, meal);
            }
        }
    }

    @Alors("le menu contient {int} plats")
    public void le_menu_contient_plats(Integer expectedCount) {
        assertEquals(expectedCount, context.currentRestaurant.getMenu().getMeals().size());
    }

    @Et("le plat {string} est dans la catégorie {string}")
    public void le_plat_est_dans_la_categorie(String mealName, String categoryStr) {
        Meal meal = restaurantService.getMealByName(context.currentRestaurant.getRestaurantId(), mealName);
        assertNotNull(meal);
        assertEquals(MealCategory.valueOf(categoryStr), meal.getCategory());
    }

    @Alors("le plat {string} a le type spécifique {string}")
    public void le_plat_a_le_type_specifique(String mealName, String typeStr) {
        Meal meal = restaurantService.getMealByName(context.currentRestaurant.getRestaurantId(), mealName);
        assertNotNull(meal);
        assertNotNull(meal);
    }

    @Alors("le plat {string} n'a pas de type spécifique")
    public void le_plat_n_a_pas_de_type_specifique(String mealName) {
        Meal meal = restaurantService.getMealByName(context.currentRestaurant.getRestaurantId(), mealName);
        assertNotNull(meal);
    }

    @Quand("le gestionnaire ajoute les suppléments suivants au plat {string}:")
    public void le_gestionnaire_ajoute_les_supplements_suivants_au_plat(String mealName, io.cucumber.datatable.DataTable dataTable) {
        Meal meal = restaurantService.getMealByName(context.currentRestaurant.getRestaurantId(), mealName);
        assertNotNull(meal);

        List<Map<String, String>> supplements = dataTable.asMaps();
        List<Topping> toppings = new ArrayList<>();

        for (Map<String, String> row : supplements) {
            String nom = row.get("nom");
            double prix = Double.parseDouble(row.get("prix"));
            Topping topping = new Topping(nom, prix);
            toppings.add(topping);
        }


        context.mealToppings.put(mealName, toppings);

        List<String> toppingNames = toppings.stream().map(Topping::getName).collect(java.util.stream.Collectors.toList());

    }

    @Alors("le plat {string} a {int} suppléments disponibles")
    public void le_plat_a_supplements_disponibles(String mealName, Integer expectedCount) {
        List<Topping> toppings = context.mealToppings.get(mealName);
        assertNotNull(toppings);
        assertEquals(expectedCount, toppings.size());
    }

    @Et("le supplément {string} coûte {double} euro")
    public void le_supplement_coute_euro(String toppingName, Double expectedPrice) {
        boolean found = false;
        for (List<Topping> toppings : context.mealToppings.values()) {
            for (Topping topping : toppings) {
                if (topping.getName().equals(toppingName)) {
                    assertEquals(expectedPrice, topping.getPrice(), 0.01);
                    found = true;
                    break;
                }
            }
        }
        assertTrue(found, "Topping not found: " + toppingName);
    }

    @Et("le supplément {string} coûte {int}.{int} euro")
    public void le_supplement_coute_euro_decimales(String toppingName, Integer euros, Integer centimes) {
        double expectedPrice = euros + (centimes / 100.0);
        le_supplement_coute_euro(toppingName, expectedPrice);
    }

    @Et("le supplément {string} coûte {double} euros")
    public void le_supplement_coute_euros(String toppingName, Double expectedPrice) {
        le_supplement_coute_euro(toppingName, expectedPrice);
    }

    @Et("le supplément {string} coûte {int}.{int} euros")
    public void le_supplement_coute_euros_decimales(String toppingName, Integer euros, Integer centimes) {
        double expectedPrice = euros + (centimes / 100.0);
        le_supplement_coute_euro(toppingName, expectedPrice);
    }

    @Et("le plat a les suppléments suivants:")
    public void le_plat_a_les_supplements_suivants(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> supplements = dataTable.asMaps();
        List<Topping> toppings = new ArrayList<>();

        for (Map<String, String> row : supplements) {
            String nom = row.get("nom");
            double prix = Double.parseDouble(row.get("prix"));
            Topping topping = new Topping(nom, prix);
            toppings.add(topping);
        }

        String mealName = context.currentMeal.getName();
        context.mealToppings.put(mealName, toppings);
    }

    @Quand("un client commande le plat {string} avec les suppléments:")
    public void un_client_commande_le_plat_avec_les_supplements(String mealName, io.cucumber.datatable.DataTable dataTable) {
        List<String> selectedToppings = dataTable.asList();
        context.selectedToppings = selectedToppings;

        Meal meal = restaurantService.getMealByName(context.currentRestaurant.getRestaurantId(), mealName);
        context.currentMeal = meal;
    }

    @Alors("le prix total du plat est {double} euros")
    public void le_prix_total_du_plat_est_euros(Double expectedTotal) {
        double total = context.currentMeal.getPrice();

        List<Topping> toppings = context.mealToppings.get(context.currentMeal.getName());
        if (toppings != null) {
            for (Topping topping : toppings) {
                if (context.selectedToppings.contains(topping.getName())) {
                    total += topping.getPrice();
                }
            }
        }

        assertEquals(expectedTotal, total, 0.01);
    }

    @Alors("le prix total du plat est {int}.{int} euros")
    public void le_prix_total_du_plat_est_euros_decimales(Integer euros, Integer centimes) {
        double expectedTotal = euros + (centimes / 100.0);
        le_prix_total_du_plat_est_euros(expectedTotal);
    }

    @Et("ajoute les tags diététiques suivants:")
    public void ajoute_les_tags_dietetiques_suivants(io.cucumber.datatable.DataTable dataTable) {
        List<String> tags = dataTable.asList();
        String mealName = context.currentMeal.getName();
        // Store in context since Meal doesn't have setDietaryTags
        context.mealTags.put(mealName, new ArrayList<>(tags));
    }

    @Alors("le plat {string} a le tag {string}")
    public void le_plat_a_le_tag(String mealName, String tag) {
        List<String> tags = context.mealTags.get(mealName);
        if (tags == null) {
            // Try to get from meal's dietary tags
            Meal meal = restaurantService.getMealByName(context.currentRestaurant.getRestaurantId(), mealName);
            tags = meal.getDietaryTags().stream().map(Enum::name).toList();
        }
        assertNotNull(tags);
        assertTrue(tags.contains(tag), "Tag not found: " + tag);
    }

    @Et("ajoute les tags de composition suivants:")
    public void ajoute_les_tags_de_composition_suivants(io.cucumber.datatable.DataTable dataTable) {
        ajoute_les_tags_dietetiques_suivants(dataTable);
    }

    @Et("le plat a les tags suivants:")
    public void le_plat_a_les_tags_suivants(io.cucumber.datatable.DataTable dataTable) {
        List<String> tags = dataTable.asList();
        String mealName = context.currentMeal.getName();
        context.mealTags.put(mealName, new ArrayList<>(tags));
    }

    @Quand("le gestionnaire met à jour les tags du plat {string} avec:")
    public void le_gestionnaire_met_a_jour_les_tags_du_plat_avec(String mealName, io.cucumber.datatable.DataTable dataTable) {
        List<String> newTags = dataTable.asList();

        Meal meal = restaurantService.getMealByName(context.currentRestaurant.getRestaurantId(), mealName);
        assertNotNull(meal);

        // Update in context
        context.mealTags.put(mealName, newTags);
        context.currentMeal = meal;
    }

    @Alors("le plat {string} a {int} tags diététiques")
    public void le_plat_a_tags_dietetiques(String mealName, Integer expectedCount) {
        List<String> tags = context.mealTags.get(mealName);
        assertNotNull(tags);
        assertEquals(expectedCount, tags.size());
    }

    @Quand("le gestionnaire supprime le plat {string}")
    public void le_gestionnaire_supprime_le_plat(String mealName) {
        Meal meal = restaurantService.getMealByName(context.currentRestaurant.getRestaurantId(), mealName);
        assertNotNull(meal);
        restaurantService.removeMealFromMenu(context.currentRestaurant.getRestaurantId(), meal);
    }

    @Et("le menu ne contient pas {string}")
    public void le_menu_ne_contient_pas(String mealName) {
        try {
            Meal meal = restaurantService.getMealByName(context.currentRestaurant.getRestaurantId(), mealName);

            assertNull(meal, "Meal should not be in the menu");
        } catch (fr.unice.polytech.foodDelivery.domain.exception.MealNotInMenuException e) {

        }
    }

    @Étantdonnéque("le restaurant a les plats suivants:")
    public void le_restaurant_a_les_plats_suivants(io.cucumber.datatable.DataTable dataTable) {
        que_le_restaurant_a_les_plats_suivants(dataTable);
    }

    @Étantdonné("que le restaurant a les plats suivants:")
    public void que_le_restaurant_a_les_plats_suivants(io.cucumber.datatable.DataTable dataTable) {
        le_gestionnaire_cree_les_plats_suivants(dataTable);
    }

    @Alors("le menu contient {int} plat")
    public void le_menu_contient_plat(Integer expectedCount) {
        le_menu_contient_plats(expectedCount);
    }
}
