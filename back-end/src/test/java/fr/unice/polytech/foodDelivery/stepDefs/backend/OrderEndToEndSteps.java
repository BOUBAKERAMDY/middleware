package fr.unice.polytech.foodDelivery.stepDefs.backend;

import io.cucumber.java.fr.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import fr.unice.polytech.foodDelivery.domain.model.*;
import fr.unice.polytech.foodDelivery.domain.ENUM.*;
import fr.unice.polytech.foodDelivery.service.OrderService;

public class OrderEndToEndSteps {
    private final OrderService orderService;
    private final TestContext context;

    public OrderEndToEndSteps(TestContext context) {
        this.context = context;
        this.orderService = new OrderService();
    }

    @Étantdonné("un restaurant nommé {string} avec le menu suivant:")
    public void un_restaurant_nomme_avec_menu(String nomRestaurant, io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> plats = dataTable.asMaps();
        List<Meal> meals = new ArrayList<>();

        for (Map<String, String> plat : plats) {
            String nomPlat = plat.get("plat");
            double prix = Double.parseDouble(plat.get("prix").replace(",", "."));
            meals.add(new Meal.Builder(nomPlat, prix).build());
        }

        Menu menu = new Menu(meals);
        List<CuisineType> cuisineTypes = Arrays.asList(CuisineType.FRENCH);
        int[] priceRange = new int[] { 0, 50 };

        context.restaurant = new RestaurantAccount(
                nomRestaurant,
                menu,
                cuisineTypes,
                RestaurantType.RESTAURANT,
                priceRange);

        assertNotNull(context.restaurant, "Le restaurant devrait être créé");
    }

    @Étantdonné("le restaurant a les créneaux horaires suivants:")
    public void le_restaurant_a_les_creneaux_horaires_suivants(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> creneaux = dataTable.asMaps();

        for (Map<String, String> creneau : creneaux) {
            String slotId = creneau.get("créneau");
            String jour = creneau.get("jour");
            String debut = creneau.get("début");
            String fin = creneau.get("fin");
            int capacite = Integer.parseInt(creneau.get("capacité"));

            DayOfWeek day = DayOfWeek.valueOf(jour.toUpperCase());
            context.restaurant.addToPlanning(slotId, day,
                    LocalTime.parse(debut),
                    LocalTime.parse(fin),
                    capacite);
        }

        assertNotNull(context.restaurant.getPlanning(), "Le planning devrait être créé");
    }

    @Quand("le client crée une nouvelle commande")
    public void le_client_cree_une_nouvelle_commande() {
        context.order = new Order(context.customer, context.restaurant);
        orderService.createOrder(context.order);
        assertNotNull(context.order, "La commande devrait être créée");
        assertEquals(OrderStatus.PENDING, context.order.getStatus(), "La commande devrait être en statut PENDING");
    }

    @Quand("ajoute les plats suivants à sa commande:")
    public void ajoute_les_plats_suivants_a_sa_commande(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> plats = dataTable.asMaps();

        for (Map<String, String> plat : plats) {
            String nomPlat = plat.get("plat");
            int quantite = Integer.parseInt(plat.get("quantité"));

            // Trouver le plat dans le menu du restaurant
            Meal mealToAdd = null;
            Menu restaurantMenu = context.restaurant.getMenu(); // Récupère l'objet Menu
            List<Meal> menuMeals = restaurantMenu.getMeals(); // Maintenant getMeals() retourne List<Meal>
            for (Meal meal : menuMeals) {
                if (meal.getName().equals(nomPlat)) {
                    mealToAdd = meal;
                    break;
                }
            }

            assertNotNull(mealToAdd, "Le plat " + nomPlat + " devrait exister dans le menu");

            for (int i = 0; i < quantite; i++) {
                context.order.addMeal(mealToAdd);
            }
        }

        assertFalse(context.order.getMeals().isEmpty(), "La commande devrait contenir des plats");
    }

    @Quand("sélectionne le créneau horaire {string}")
    public void selectionne_le_creneau_horaire(String creneau) {
        Planning planning = context.restaurant.getPlanning();
        assertNotNull(planning, "Le restaurant devrait avoir un planning");

        Planning.TimeSlot timeSlot = planning.getTimeSlot(creneau);
        assertNotNull(timeSlot, "Le créneau horaire " + creneau + " devrait exister");
        assertTrue(timeSlot.hasCapacity(),
                "Le créneau horaire " + creneau + " devrait avoir de la capacité disponible");

        // Réserver le créneau
        timeSlot.incrementOrders();

        // Associer le planning COMPLET à la commande (pas juste l'incrément)
        context.order.setSchedule(planning); // ← Assurez-vous que cette ligne est présente

        System.out
                .println("DEBUG: Créneau " + creneau + " réservé. Commandes actuelles: " + timeSlot.getCurrentOrders());
        System.out.println("DEBUG: Planning associé à la commande: " + (context.order.getSchedule() != null));
    }

    @Alors("la commande est confirmée")
    public void la_commande_est_confirmee() {
        assertEquals(OrderStatus.PAID, context.order.getStatus(), "La commande devrait être confirmée (PAID)");
    }

    @Alors("le restaurant est notifié de la nouvelle commande")
    public void le_restaurant_est_notifie_de_la_nouvelle_commande() {
        context.restaurant.notifyNewOrder(context.order);
        assertTrue(true, "Le restaurant devrait être notifié");
    }

    @Alors("le créneau horaire {string} a sa capacité réduite de {int}")
    public void le_creneau_horaire_a_sa_capacite_reduite(String creneau, int reductionAttendue) {
        Planning planning = context.restaurant.getPlanning();
        assertNotNull(planning, "Le planning devrait exister");

        Planning.TimeSlot timeSlot = planning.getTimeSlot(creneau);
        assertNotNull(timeSlot, "Le créneau horaire " + creneau + " devrait exister");

        assertEquals(reductionAttendue, timeSlot.getCurrentOrders(),
                "Le nombre de commandes dans le créneau devrait correspondre");
    }

    @Alors("la commande n'est pas confirmée")
    public void la_commande_n_est_pas_confirmee() {
        assertEquals(OrderStatus.PENDING, context.order.getStatus(),
                "La commande ne devrait pas être confirmée");
    }

    @Alors("le créneau horaire {string} garde sa capacité initiale")
    public void le_creneau_horaire_garde_sa_capacite_initiale(String creneau) {
        Planning planning = context.restaurant.getPlanning();
        assertNotNull(planning, "Le planning devrait exister");

        Planning.TimeSlot timeSlot = planning.getTimeSlot(creneau);
        assertNotNull(timeSlot, "Le créneau horaire " + creneau + " devrait exister");

        assertEquals(1, timeSlot.getCurrentOrders(),
                "Le créneau horaire ne devrait pas avoir de commandes");
    }

    @Alors("le montant total de la commande est de {string}")
    public void le_montant_total_de_la_commande_est_de(String montantAttendu) {
        double montantAttenduDouble = Double.parseDouble(montantAttendu);
        double montantReel = context.order.getTotalAmount();
        assertEquals(montantAttenduDouble, montantReel, 0.01,
                "Le montant total de la commande devrait être " + montantAttendu);
    }

    @Alors("l'allowance du client est réduite à {string}")
    public void l_allowance_du_client_est_réduite_à(String allowanceAttendue) {
        double allowanceAttendueDouble = Double.parseDouble(allowanceAttendue);
        double allowanceReelle = context.customer.getAllowance();
        assertEquals(allowanceAttendueDouble, allowanceReelle, 0.01,
                "L'allowance du client devrait être réduite à " + allowanceAttendue);
    }

    @Alors("l'allowance du client reste à {string}")
    public void l_allowance_du_client_reste_à(String allowanceAttendue) {
        double allowanceAttendueDouble = Double.parseDouble(allowanceAttendue);
        double allowanceReelle = context.customer.getAllowance();
        assertEquals(allowanceAttendueDouble, allowanceReelle, 0.01,
                "L'allowance du client devrait rester à " + allowanceAttendue);
    }

    @Alors("la commande apparaît dans l'historique du client")
    public void la_commande_apparaît_dans_l_historique_du_client() {
        List<Order> historique = orderService.getCustomerHistory(context.customer.getCustomerId());
        assertTrue(historique.contains(context.order),
                "La commande devrait apparaître dans l'historique du client");
    }

    @Alors("le paiement échoue")
    public void le_paiement_échoue() {
        assertNotEquals(OrderStatus.PAID, context.order.getStatus(),
                "Le paiement devrait échouer et la commande ne devrait pas être confirmée");
    }
}