package fr.unice.polytech.foodDelivery.stepDefs.backend;

import fr.unice.polytech.foodDelivery.domain.model.CustomerAccount;
import fr.unice.polytech.foodDelivery.service.OrderService;
import io.cucumber.java.fr.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import fr.unice.polytech.foodDelivery.domain.model.*;
import fr.unice.polytech.foodDelivery.domain.ENUM.*;

public class CommonSteps {

    private final TestContext context;

    public CommonSteps(TestContext context) {
        this.context = context;
        this.context.orderService = new OrderService();
    }

    @Etantdonné("un client nommé {string} situé à {string} avec une allowance de {string}")
    public void creer_client_avec_allowance(String name, String location, String allowanceStr) {
        double allowance = Double.parseDouble(allowanceStr.replace(",", "."));
        context.customer = new CustomerAccount(name, location, allowance);
        context.initialAllowance = allowance;
        assertNotNull(context.customer);
    }

    @Et("un restaurant nommé {string} proposant un repas {string} à {string}")
    public void creer_restaurant_avec_repas(String restaurantName, String mealName, String priceStr) {
        double price = Double.parseDouble(priceStr.replace(",", "."));
        context.selectedMeal = new Meal.Builder(mealName, price).build();
        context.restaurant = new RestaurantAccount(
                restaurantName,
                new Menu(Arrays.asList(context.selectedMeal)),
                Arrays.asList(CuisineType.FRENCH),
                RestaurantType.FAST_FOOD,
                new int[] { (int) price, (int) price + 10 });
        assertNotNull(context.restaurant);
    }

    @Et("une commande en cours pour ce client la")
    public void une_commande_en_cours_pour_le_client() {
        if (context.customer == null || context.restaurant == null) {
            throw new IllegalStateException("Customer and restaurant must be initialized first");
        }
        context.order = new Order(context.customer, context.restaurant);
        context.orderService.createOrder(context.order);
        assertNotNull(context.order);
    }

    @Quand("le client ajoute {string} à sa commande")
    public void client_ajoute_repas_commande(String mealName) {
        if (context.order == null) {
            context.order = new Order(context.customer, context.restaurant);
        }
        context.order.addMeal(context.selectedMeal);
        assertFalse(context.order.getMeals().isEmpty());
    }

    @Et("le restaurant enregistre la commande")
    public void restaurant_enregistre_commande() {
        context.restaurant.notifyNewOrder(context.order);
    }

    @Alors("la commande doit avoir le statut {string}")
    public void commande_doit_avoir_statut(String expectedStatus) {
        assertEquals(expectedStatus, context.order.getStatus().name());
    }

    @Et("le montant total doit être de {string}")
    public void montant_total_commande(String expectedAmountStr) {
        double expectedAmount = Double.parseDouble(expectedAmountStr.replace(",", "."));
        assertEquals(expectedAmount, context.order.getAmount(), 0.001);
    }

    @Et("la commande doit apparaître dans l'historique du client")
    public void commande_dans_historique_client() {
        assertTrue(context.orderService.getCustomerHistory(context.customer.getCustomerId()).contains(context.order));
    }

    @Et("le solde du client doit être réduit de {string}")
    public void solde_client_reduit_de(String montantStr) {
        double montant = Double.parseDouble(montantStr.replace(",", "."));
        double allowanceAfterPayment = context.customer.getAllowance();
        double expectedAllowance = context.initialAllowance - montant;
        assertEquals(expectedAllowance, allowanceAfterPayment, 0.001,
                "L'allowance du client n'a pas été correctement réduite");
    }

    @Et("l'allowance du client doit être de {string}")
    public void allowance_client_doit_etre(String expectedAllowanceStr) {
        double expectedAllowance = Double.parseDouble(expectedAllowanceStr.replace(",", "."));
        assertEquals(expectedAllowance, context.customer.getAllowance(), 0.001);
    }
}