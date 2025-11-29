package fr.unice.polytech.foodDelivery.stepDefs.backend;

import fr.unice.polytech.foodDelivery.domain.model.CustomerAccount;
import fr.unice.polytech.foodDelivery.service.OrderService;
import io.cucumber.java.fr.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import fr.unice.polytech.foodDelivery.domain.model.*;
import fr.unice.polytech.foodDelivery.domain.ENUM.*;
import fr.unice.polytech.foodDelivery.domain.exception.*;
import fr.unice.polytech.foodDelivery.service.RestaurantService;

public class CustomerOrderingSteps {

    private final TestContext context;
    private final RestaurantService restaurantService;
    private final OrderService orderService;
    private Exception caughtException;
    private Map<String, CustomerAccount> customers;
    private Map<String, Order> customerOrders;

    public CustomerOrderingSteps(TestContext context) {
        this.context = context;
        this.restaurantService = new RestaurantService();
        this.orderService = new OrderService();
        this.customers = new HashMap<>();
        this.customerOrders = new HashMap<>();
    }

    @Et("un client {string} à l'adresse {string} avec une allocation de {prix} euros")
    public void un_client_avec_allocation(String nom, String adresse, Double allocation) {
        context.customer = new CustomerAccount(nom, adresse, allocation);
        context.initialAllowance = allocation;
        assertNotNull(context.customer);
    }

    @Étantdonné("un client {string} avec une allocation de {prix} euros")
    public void un_client_avec_allocation_simple(String nom, Double allocation) {
        context.customer = new CustomerAccount(nom, "Default Address", allocation);
        context.initialAllowance = allocation;
        assertNotNull(context.customer);
    }

    @Et("le restaurant a les repas suivants dans son menu:")
    public void le_restaurant_a_les_repas_suivants(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> repas = dataTable.asMaps();

        for (Map<String, String> row : repas) {
            String nom = row.get("nom");
            double prix = Double.parseDouble(row.get("prix"));
            MealCategory categorie = MealCategory.valueOf(row.get("catégorie"));
            MealType type = MealType.valueOf(row.get("type"));

            Meal meal = new Meal.Builder(nom, prix)
                    .category(categorie)
                    .type(type)
                    .build();
            restaurantService.addMealToMenu(context.restaurant.getRestaurantId(), meal);
        }
    }

    @Et("le restaurant a un repas {string} à {prix} euros dans son menu")
    public void le_restaurant_a_un_repas_dans_son_menu(String nom, Double prix) {
        Meal meal = new Meal.Builder(nom, prix).build();
        restaurantService.addMealToMenu(context.restaurant.getRestaurantId(), meal);
    }

    @Étantdonné("une commande en cours pour le client")
    public void une_commande_en_cours_pour_le_client() {
        if (context.customer == null || context.restaurant == null) {
            throw new IllegalStateException("Customer and restaurant must be initialized first");
        }
        context.order = new Order(context.customer, context.restaurant);
        orderService.createOrder(context.order);
        assertNotNull(context.order);
    }

    @Étantdonné("une commande en cours pour ce client")
    public void une_commande_en_cours_pour_ce_client() {
        une_commande_en_cours_pour_le_client();
    }

    @Quand("le client ajoute le repas {string} à sa commande")
    public void le_client_ajoute_le_repas(String nomRepas) {
        try {
            Meal meal = restaurantService.getMealByName(context.restaurant.getRestaurantId(), nomRepas);
            context.order.addMeal(meal);
        } catch (Exception e) {
            caughtException = e;
        }
    }

    @Quand("le client ajoute les repas suivants à sa commande:")
    public void le_client_ajoute_les_repas_suivants(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> repas = dataTable.asMaps();

        for (Map<String, String> row : repas) {
            String nom = row.get("nom");
            Meal meal = restaurantService.getMealByName(context.restaurant.getRestaurantId(), nom);
            context.order.addMeal(meal);
        }
    }

    @Quand("le client tente d'ajouter un repas {string} qui n'est pas au menu")
    public void le_client_tente_ajouter_repas_pas_au_menu(String nomRepas) {
        try {
            Meal meal = restaurantService.getMealByName(context.restaurant.getRestaurantId(), nomRepas);
            context.order.addMeal(meal);
        } catch (MealNotInMenuException e) {
            caughtException = e;
        }
    }

    @Quand("le client tente d'ajouter un repas {string} sans commande en cours")
    public void le_client_tente_ajouter_sans_commande(String nomRepas) {
        try {
            if (context.order == null) {
                throw new NoCurrentOrderException();
            }
            Meal meal = restaurantService.getMealByName(context.restaurant.getRestaurantId(), nomRepas);
            context.order.addMeal(meal);
        } catch (NoCurrentOrderException e) {
            caughtException = e;
        }
    }

    @Quand("le client ajoute un repas à {prix} euros à sa commande")
    public void le_client_ajoute_repas_avec_prix(Double prix) {
        Meal meal = new Meal.Builder("Test Meal", prix).build();
        restaurantService.addMealToMenu(context.restaurant.getRestaurantId(), meal);
        context.order.addMeal(meal);
    }

    @Alors("la commande contient {int} repas")
    public void la_commande_contient_x_repas(Integer nombreRepas) {
        assertEquals(nombreRepas, context.order.getMeals().size());
    }

    @Alors("le montant total de la commande est {prix} euros")
    public void le_montant_total_de_la_commande_est(Double montant) {
        assertEquals(montant, context.order.getAmount(), 0.01);
    }

    @Alors("la commande a un montant de {prix} euros")
    public void la_commande_a_un_montant_de(Double montant) {
        assertEquals(montant, context.order.getAmount(), 0.01);
    }

    @Alors("une exception {string} est levée")
    public void une_exception_est_levee(String typeException) {
        assertNotNull(caughtException, "Une exception devrait avoir été levée");

        switch (typeException.toLowerCase()) {
            case "meal not in menu":
                assertTrue(caughtException instanceof MealNotInMenuException,
                        "L'exception devrait être de type MealNotInMenuException");
                break;
            case "no current order":
                assertTrue(caughtException instanceof NoCurrentOrderException,
                        "L'exception devrait être de type NoCurrentOrderException");
                break;
            default:
                fail("Type d'exception non reconnu: " + typeException);
        }
    }

    @Alors("la commande est valide")
    public void la_commande_est_valide() {
        assertNotNull(context.order);
        assertFalse(context.order.getMeals().isEmpty());
        assertTrue(context.order.getAmount() > 0);
    }

    @Alors("tous les repas de la commande proviennent du menu du restaurant")
    public void tous_les_repas_proviennent_du_menu() {
        for (Meal meal : context.order.getMeals()) {
            assertTrue(restaurantService.isMealInMenu(context.restaurant.getRestaurantId(), meal),
                    "Le repas " + meal.getName() + " devrait être dans le menu");
        }
    }

    @Alors("le client a une allocation suffisante: {word}")
    public void le_client_a_allocation_suffisante(String suffisant) {
        boolean expectedSuffisant = "oui".equalsIgnoreCase(suffisant);
        boolean hasSufficientAllowance = context.customer.getAllowance() >= context.order.getAmount();

        assertEquals(expectedSuffisant, hasSufficientAllowance,
                "L'allocation devrait être " + (expectedSuffisant ? "suffisante" : "insuffisante"));
    }


    @Et("un client {string} crée une commande et ajoute {string}")
    public void un_client_cree_commande_et_ajoute(String nomClient, String nomRepas) {

        CustomerAccount client = new CustomerAccount(nomClient, "Address " + nomClient, 100.0);
        customers.put(nomClient, client);


        Order order = new Order(client, context.restaurant);
        customerOrders.put(nomClient, order);

        Meal meal = restaurantService.getMealByName(context.restaurant.getRestaurantId(), nomRepas);
        order.addMeal(meal);
    }

    @Alors("les {int} commandes sont créées avec succès")
    public void les_x_commandes_sont_creees_avec_succes(Integer nombreCommandes) {
        assertEquals(nombreCommandes, customerOrders.size(),
                "Le nombre de commandes créées devrait être " + nombreCommandes);

        for (Order order : customerOrders.values()) {
            assertNotNull(order);
            assertFalse(order.getMeals().isEmpty());
        }
    }
}
