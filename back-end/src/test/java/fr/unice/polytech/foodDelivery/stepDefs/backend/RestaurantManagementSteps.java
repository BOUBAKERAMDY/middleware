package fr.unice.polytech.foodDelivery.stepDefs.backend;

import io.cucumber.java.fr.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import fr.unice.polytech.foodDelivery.domain.model.*;
import fr.unice.polytech.foodDelivery.domain.ENUM.*;
import fr.unice.polytech.foodDelivery.domain.exception.*;
import fr.unice.polytech.foodDelivery.service.RestaurantService;

public class RestaurantManagementSteps {

    private final TestContext context;
    private final RestaurantService restaurantService;
    private Exception caughtException;
    private int totalMealsConfigured = 0;
    private int totalSlotsConfigured = 0;

    public RestaurantManagementSteps(TestContext context) {
        this.context = context;
        this.restaurantService = new RestaurantService();
    }

    @Étantdonné("un restaurant {string} avec l'identifiant {int}")
    public void un_restaurant_avec_identifiant(String nomRestaurant, Integer identifiant) {
        Menu menu = new Menu(new ArrayList<>());
        List<CuisineType> cuisineTypes = Arrays.asList(CuisineType.ITALIAN);
        int[] priceRange = new int[] { 0, 100 };

        context.restaurant = new RestaurantAccount(
                nomRestaurant,
                menu,
                cuisineTypes,
                RestaurantType.RESTAURANT,
                priceRange);
        restaurantService.registerRestaurant(context.restaurant);
        assertNotNull(context.restaurant);
    }

    @Quand("le propriétaire ajoute un repas {string} à {prix} euros avec:")
    public void le_proprietaire_ajoute_un_repas_avec_details(String nomRepas, Double prix,
            io.cucumber.datatable.DataTable dataTable) {

        List<List<String>> rows = dataTable.asLists();

        Meal.Builder mealBuilder = new Meal.Builder(nomRepas, prix);

        for (List<String> row : rows) {
            if (row.size() >= 2) {
                String key = row.get(0);
                String value = row.get(1);

                // Parse based on the key
                switch (key) {
                    case "catégorie":
                        mealBuilder.category(MealCategory.valueOf(value));
                        break;
                    case "type":
                        mealBuilder.type(MealType.valueOf(value));
                        break;
                    case "topping":
                        mealBuilder.addTopping(value);
                        break;
                    case "dietary_tag":
                        mealBuilder.addDietaryTag(DiateryPreference.valueOf(value));
                        break;
                }
            }
        }

        Meal meal = mealBuilder.build();
        restaurantService.addMealToMenu(context.restaurant.getRestaurantId(), meal);
    }

    @Etantdonné("un repas {string} à {prix} euros dans le menu")
    public void un_repas_dans_le_menu(String nomRepas, Double prix) {
        Meal meal = new Meal.Builder(nomRepas, prix).build();
        restaurantService.addMealToMenu(context.restaurant.getRestaurantId(), meal);
    }

    @Quand("le propriétaire supprime le repas {string} du menu")
    public void le_proprietaire_supprime_le_repas(String nomRepas) {
        Meal meal = restaurantService.getMealByName(context.restaurant.getRestaurantId(), nomRepas);
        restaurantService.removeMealFromMenu(context.restaurant.getRestaurantId(), meal);
    }

    @Alors("le menu du restaurant contient {int} repas")
    public void le_menu_contient_x_repas(Integer nombreRepas) {
        assertEquals(nombreRepas, context.restaurant.getMenu().getMeals().size());
    }

    @Alors("le repas {string} est dans le menu")
    public void le_repas_est_dans_le_menu(String nomRepas) {
        assertTrue(context.restaurant.getMenu().containsMealByName(nomRepas));
    }

    @Alors("le repas {string} n'est pas dans le menu")
    public void le_repas_nest_pas_dans_le_menu(String nomRepas) {
        assertFalse(context.restaurant.getMenu().containsMealByName(nomRepas));
    }

    @Quand("le propriétaire met à jour les informations publiques avec {string}")
    public void le_proprietaire_met_a_jour_les_informations(String info) {
        restaurantService.updatePublicInfo(context.restaurant.getRestaurantId(), info);
    }

    @Alors("les informations publiques du restaurant sont {string}")
    public void les_informations_publiques_sont(String expectedInfo) {
        assertEquals(expectedInfo, context.restaurant.getPublicInfo());
    }

    @Quand("le propriétaire ajoute un créneau {string} de {heure} à {heure} avec une capacité de {int}")
    public void le_proprietaire_ajoute_un_creneau(String slotId, LocalTime debut, LocalTime fin, Integer capacite) {
        try {
            restaurantService.addTimeSlot(context.restaurant.getRestaurantId(), slotId, DayOfWeek.MONDAY, debut, fin, capacite);
        } catch (Exception e) {
            caughtException = e;
        }
    }

    @Quand("le propriétaire tente d'ajouter un créneau {string} de {heure} à {heure} avec une capacité de {int}")
    public void le_proprietaire_tente_ajouter_un_creneau(String slotId, LocalTime debut, LocalTime fin,
            Integer capacite) {
        try {
            restaurantService.addTimeSlot(context.restaurant.getRestaurantId(), slotId, DayOfWeek.MONDAY, debut, fin, capacite);
        } catch (Exception e) {
            caughtException = e;
        }
    }

    @Etantdonné("un créneau {string} de {heure} à {heure} avec une capacité de {int}")
    public void un_creneau_avec_capacite(String slotId, LocalTime debut, LocalTime fin, Integer capacite) {
        restaurantService.addTimeSlot(context.restaurant.getRestaurantId(), slotId, DayOfWeek.MONDAY, debut, fin, capacite);
    }

    @Quand("le propriétaire met à jour la capacité du créneau {string} à {int}")
    public void le_proprietaire_met_a_jour_la_capacite(String slotId, Integer nouvelleCapacite) {
        restaurantService.updateSlotCapacity(context.restaurant.getRestaurantId(), slotId, nouvelleCapacite);
    }

    @Alors("le créneau {string} est disponible")
    public void le_creneau_est_disponible(String slotId) {
        assertTrue(restaurantService.isSlotAvailable(context.restaurant.getRestaurantId(), slotId));
    }

    @Alors("une exception de planning est levée avec le message {string}")
    public void une_exception_de_planning_est_levee(String expectedMessage) {
        assertNotNull(caughtException, "Une exception devrait avoir été levée");
        assertTrue(caughtException instanceof PlanningException, "L'exception devrait être de type PlanningException");
        assertEquals(expectedMessage, caughtException.getMessage());
    }

    @Quand("une commande est réservée pour le créneau {string}")
    public void une_commande_est_reservee(String slotId) {
        restaurantService.reserveSlotForOrder(context.restaurant.getRestaurantId(), slotId);
    }

    @Quand("une autre commande est réservée pour le créneau {string}")
    public void une_autre_commande_est_reservee(String slotId) {
        restaurantService.reserveSlotForOrder(context.restaurant.getRestaurantId(), slotId);
    }

    @Alors("le créneau {string} est toujours disponible")
    public void le_creneau_est_toujours_disponible(String slotId) {
        assertTrue(restaurantService.isSlotAvailable(context.restaurant.getRestaurantId(), slotId));
    }

    @Quand("une troisième commande tente de réserver le créneau {string}")
    public void une_troisieme_commande_tente_de_reserver(String slotId) {
        try {
            restaurantService.reserveSlotForOrder(context.restaurant.getRestaurantId(), slotId);
        } catch (Exception e) {
            caughtException = e;
        }
    }

    @Alors("une exception de capacité dépassée est levée")
    public void une_exception_de_capacite_depassee_est_levee() {
        assertNotNull(caughtException, "Une exception devrait avoir été levée");
        assertTrue(caughtException instanceof CapacityExceededException,
                "L'exception devrait être de type CapacityExceededException");
    }

    @Quand("le propriétaire ajoute un repas {string} à {prix} euros de catégorie {string} et type {string}")
    public void le_proprietaire_ajoute_un_repas_simple(String nom, Double prix, String categorie, String type) {
        Meal meal = new Meal.Builder(nom, prix)
                .category(MealCategory.valueOf(categorie))
                .type(MealType.valueOf(type))
                .build();
        restaurantService.addMealToMenu(context.restaurant.getRestaurantId(), meal);
    }

    @Alors("le repas {string} a une catégorie {string}")
    public void le_repas_a_une_categorie(String nomRepas, String categorie) {
        Meal meal = restaurantService.getMealByName(context.restaurant.getRestaurantId(), nomRepas);
        assertEquals(MealCategory.valueOf(categorie), meal.getCategory());
    }

    @Quand("le propriétaire configure le restaurant avec:")
    public void le_proprietaire_configure_le_restaurant(io.cucumber.datatable.DataTable dataTable) {
        Map<String, String> config = dataTable.asMap(String.class, String.class);
        if (config.containsKey("informations")) {
            restaurantService.updatePublicInfo(context.restaurant.getRestaurantId(), config.get("informations"));
        }
    }

    @Quand("ajoute les repas suivants au menu:")
    public void ajoute_les_repas_suivants(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> repas = dataTable.asMaps();
        totalMealsConfigured = repas.size();

        for (Map<String, String> row : repas) {
            String nom = row.get("nom");
            double prix = Double.parseDouble(row.get("prix").replace(",", "."));
            MealCategory categorie = MealCategory.valueOf(row.get("catégorie"));
            MealType type = MealType.valueOf(row.get("type"));

            Meal meal = new Meal.Builder(nom, prix)
                    .category(categorie)
                    .type(type)
                    .build();
            restaurantService.addMealToMenu(context.restaurant.getRestaurantId(), meal);
        }
    }

    @Quand("configure les créneaux horaires:")
    public void configure_les_creneaux_horaires(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> creneaux = dataTable.asMaps();
        totalSlotsConfigured = creneaux.size();

        for (Map<String, String> row : creneaux) {
            String slotId = row.get("créneau");
            LocalTime debut = LocalTime.parse(row.get("début"));
            LocalTime fin = LocalTime.parse(row.get("fin"));
            int capacite = Integer.parseInt(row.get("capacité"));

            restaurantService.addTimeSlot(context.restaurant.getRestaurantId(), slotId, DayOfWeek.MONDAY, debut, fin, capacite);
        }
    }

    @Alors("le restaurant est complètement configuré avec {int} repas et {int} créneaux")
    public void le_restaurant_est_completement_configure(Integer nombreRepas, Integer nombreCreneaux) {
        assertEquals(nombreRepas, context.restaurant.getMenu().getMeals().size());
        assertEquals(nombreCreneaux, context.restaurant.getPlanning().getTimeSlots().size());
        assertEquals(totalMealsConfigured, nombreRepas);
        assertEquals(totalSlotsConfigured, nombreCreneaux);
    }
}
