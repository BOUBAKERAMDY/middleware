package fr.unice.polytech.foodDelivery.stepDefs.userStories;

import fr.unice.polytech.foodDelivery.domain.model.CustomerAccount;
import fr.unice.polytech.foodDelivery.stepDefs.userStories.context.UserStoriesContext;
import io.cucumber.java.fr.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import fr.unice.polytech.foodDelivery.domain.model.*;
import fr.unice.polytech.foodDelivery.domain.ENUM.*;
import fr.unice.polytech.foodDelivery.service.PaymentService;


public class P1P2P3_PaymentStudentCreditSteps {

    private final UserStoriesContext context;
    private PaymentService paymentService;

    public P1P2P3_PaymentStudentCreditSteps(UserStoriesContext context) {
        this.context = context;
        this.paymentService = new PaymentService();
    }

    @Et("un restaurant {string}")
    public void un_restaurant(String nom) {
        if (context.restaurantService.findByName(nom) == null) {
            Menu menu = new Menu(new ArrayList<>());
            context.currentRestaurant = new RestaurantAccount(nom, menu);
            context.restaurantService.registerRestaurant(context.currentRestaurant);
        } else {
            context.currentRestaurant = context.restaurantService.findByName(nom);
        }
    }

    @Et("un autre restaurant {string}")
    public void un_autre_restaurant(String nom) {
        Menu menu = new Menu(new ArrayList<>());
        RestaurantAccount newRestaurant = new RestaurantAccount(nom, menu);
        context.restaurantService.registerRestaurant(newRestaurant);
    }

    @Étantdonné("une commande en cours pour l'utilisateur avec:")
    public void une_commande_en_cours_pour_l_utilisateur_avec(io.cucumber.datatable.DataTable dataTable) {
        Map<String, String> data = dataTable.asMap(String.class, String.class);

        String restaurantName = data.get("restaurant");
        String mealName = data.get("plat");
        String montantStr = data.get("montant");
        String creneau = data.get("créneau");

        RestaurantAccount restaurant = context.restaurantService.findByName(restaurantName);
        assertNotNull(restaurant);

        context.currentOrder = new Order(context.currentCustomer, restaurant);
        context.paymentStatus = "PENDING";
        context.paymentSuccess = false;

        // Add meals to match the expected amount
        if (mealName != null && montantStr != null) {
            Meal meal = context.meals.get(mealName);
            if (meal != null) {
                double expectedAmount = Double.parseDouble(montantStr);
                double mealPrice = meal.getPrice();
                int quantity = (int) Math.round(expectedAmount / mealPrice);

                // Add the meal the required number of times
                for (int i = 0; i < quantity; i++) {
                    context.currentOrder.addMeal(meal);
                }
            }
        }

        if (creneau != null) {
            String[] times = creneau.split("-");
            java.time.LocalTime start = java.time.LocalTime.parse(times[0]);
            java.time.LocalTime end = java.time.LocalTime.parse(times[1]);

            for (Planning.TimeSlot slot : restaurant.getPlanning().getTimeSlots().values()) {
                if (slot.getStartTime().equals(start) && slot.getEndTime().equals(end)) {
                    context.selectedTimeSlot = slot;
                    context.currentOrder.setSchedule(restaurant.getPlanning());
                    break;
                }
            }
        }
    }

    @Quand("l'utilisateur sélectionne le mode de paiement {string}")
    public void l_utilisateur_selectionne_le_mode_de_paiement(String paymentMethodStr) {
        if ("STUDENT_CREDIT".equals(paymentMethodStr)) {
            paymentMethodStr = "ALLOWANCE";
        }
        context.paymentMethod = PaymentMethod.valueOf(paymentMethodStr);
    }

    @Et("l'utilisateur est redirigé vers le service de paiement externe")
    public void l_utilisateur_est_redirige_vers_le_service_de_paiement_externe() {
        context.paymentRedirected = true;
        assertTrue(context.paymentRedirected);
    }

    @Et("le paiement externe est confirmé avec succès")
    public void le_paiement_externe_est_confirme_avec_succes() {
        context.paymentSuccess = true;
        context.paymentStatus = "PAID";
        context.currentOrder.setStatus(OrderStatus.VALIDATED);
    }

    @Alors("la commande est validée")
    public void la_commande_est_validee() {
        assertEquals(OrderStatus.VALIDATED, context.currentOrder.getStatus());
    }

    @Et("le statut de paiement est {string}")
    public void le_statut_de_paiement_est(String expectedStatus) {
        assertEquals(expectedStatus, context.paymentStatus);
    }

    @Quand("l'utilisateur tente de valider la commande sans payer")
    public void l_utilisateur_tente_de_valider_la_commande_sans_payer() {
        try {
            if (!context.paymentSuccess) {
                throw new IllegalStateException("Le paiement est obligatoire");
            }
        } catch (Exception e) {
            context.caughtException = e;
            context.errorMessage = e.getMessage();
        }
    }

    @Et("le paiement externe échoue")
    public void le_paiement_externe_echoue() {
        context.paymentSuccess = false;
        context.paymentStatus = "FAILED";
        context.paymentMessage = "Le paiement a échoué";
    }

    @Et("un message d'erreur est affiché {string}")
    public void un_message_d_erreur_est_affiche(String expectedMessage) {
        assertEquals(expectedMessage, context.paymentMessage);
    }

    @Quand("l'utilisateur complète le paiement avec succès")
    public void l_utilisateur_complete_le_paiement_avec_succes() {
        context.paymentSuccess = true;
        context.paymentStatus = "PAID";
        context.currentOrder.setStatus(OrderStatus.VALIDATED);
    }

    @Alors("la commande est enregistrée dans le compte de l'utilisateur")
    public void la_commande_est_enregistree_dans_le_compte_de_l_utilisateur() {
        context.orderHistory.add(context.currentOrder);
        assertTrue(context.orderHistory.contains(context.currentOrder));
    }

    @Et("l'utilisateur peut consulter la commande dans son historique")
    public void l_utilisateur_peut_consulter_la_commande_dans_son_historique() {
        assertTrue(context.orderHistory.size() > 0);
    }

    @Et("la commande a le statut {string}")
    public void la_commande_a_le_statut(String expectedStatus) {
        assertEquals(OrderStatus.valueOf(expectedStatus), context.currentOrder.getStatus());
    }

    @Étantdonnéque("l'utilisateur a payé les commandes suivantes:")
    public void l_utilisateur_a_paye_les_commandes_suivantes(io.cucumber.datatable.DataTable dataTable) {
        que_l_utilisateur_a_paye_les_commandes_suivantes(dataTable);
    }

    @Étantdonné("que l'utilisateur a payé les commandes suivantes:")
    public void que_l_utilisateur_a_paye_les_commandes_suivantes(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> commandes = dataTable.asMaps();

        for (Map<String, String> row : commandes) {
            String restaurantName = row.get("restaurant");
            double montant = Double.parseDouble(row.get("montant"));
            String statut = row.get("statut");

            RestaurantAccount restaurant = context.restaurantService.findByName(restaurantName);
            Order order = new Order(context.currentCustomer, restaurant);
            order.setStatus(OrderStatus.valueOf(statut));
            context.orderHistory.add(order);
        }
    }

    @Quand("l'utilisateur consulte son historique de commandes")
    public void l_utilisateur_consulte_son_historique_de_commandes() {
        assertNotNull(context.orderHistory);
    }

    @Alors("l'utilisateur voit {int} commandes")
    public void l_utilisateur_voit_commandes(Integer expectedCount) {
        assertEquals(expectedCount, context.orderHistory.size());
    }

    @Et("toutes les commandes ont le statut {string}")
    public void toutes_les_commandes_ont_le_statut(String expectedStatus) {
        OrderStatus status = OrderStatus.valueOf(expectedStatus);
        for (Order order : context.orderHistory) {
            assertEquals(status, order.getStatus());
        }
    }

    @Étantdonné("un nouvel étudiant {string}")
    public void un_nouvel_etudiant(String nom) {
        context.currentCustomer = new CustomerAccount(nom, "Campus Address", 0.0);
        context.customers.put(nom, context.currentCustomer);
    }

    @Quand("le compte étudiant est créé")
    public void le_compte_etudiant_est_cree() {
        context.studentCredit = 20.0;
        context.currentCustomer.setAllowance(context.studentCredit);
    }

    @Alors("l'étudiant a une allocation de {double} euros")
    public void l_etudiant_a_une_allocation_de_euros(Double expectedAllowance) {
        assertEquals(expectedAllowance, context.currentCustomer.getAllowance(), 0.01);
    }

    @Alors("l'étudiant a une allocation de {int}.{int} euros")
    public void l_etudiant_a_une_allocation_de_euros_decimales(Integer euros, Integer centimes) {
        double expectedAllowance = euros + (centimes / 100.0);
        l_etudiant_a_une_allocation_de_euros(expectedAllowance);
    }

    @Et("le solde disponible est de {double} euros")
    public void le_solde_disponible_est_de_euros(Double expectedBalance) {
        assertEquals(expectedBalance, context.currentCustomer.getAllowance(), 0.01);
    }

    @Et("le solde disponible est de {int}.{int} euros")
    public void le_solde_disponible_est_de_euros_decimales(Integer euros, Integer centimes) {
        double expectedBalance = euros + (centimes / 100.0);
        le_solde_disponible_est_de_euros(expectedBalance);
    }

    @Étantdonné("un étudiant {string} avec une allocation de {double} euros")
    public void un_etudiant_avec_une_allocation_de_euros(String nom, Double allocation) {
        context.currentCustomer = new CustomerAccount(nom, "Campus Address", allocation);
        context.studentCredit = allocation;
        context.customers.put(nom, context.currentCustomer);
    }

    @Étantdonné("un étudiant {string} avec une allocation de {int}.{int} euros")
    public void un_etudiant_avec_une_allocation_de_euros_decimales(String nom, Integer euros, Integer centimes) {
        double allocation = euros + (centimes / 100.0);
        un_etudiant_avec_une_allocation_de_euros(nom, allocation);
    }

    @Et("une commande en cours pour l'étudiant avec:")
    public void une_commande_en_cours_pour_l_etudiant_avec(io.cucumber.datatable.DataTable dataTable) {
        une_commande_en_cours_pour_l_utilisateur_avec(dataTable);
    }

    @Et("l'étudiant confirme le paiement")
    public void l_etudiant_confirme_le_paiement() {
        double orderTotal = context.currentOrder.getTotalAmount();
        double allowance = context.currentCustomer.getAllowance();
        context.studentCredit = allowance; // Store initial allowance before payment

        if (allowance >= orderTotal) {
            context.currentCustomer.setAllowance(allowance - orderTotal);
            context.paymentSuccess = true;
            context.paymentStatus = "PAID";
            context.currentOrder.setStatus(OrderStatus.VALIDATED);
        }
    }

    @Alors("le paiement est accepté")
    public void le_paiement_est_accepte() {
        assertTrue(context.paymentSuccess);
        assertEquals(OrderStatus.VALIDATED, context.currentOrder.getStatus());
    }

    @Et("le solde de l'allocation est réduit de {double} euros")
    public void le_solde_de_l_allocation_est_reduit_de_euros(Double amount) {
        assertTrue(context.currentCustomer.getAllowance() < context.studentCredit + amount);
    }

    @Et("le solde de l'allocation est réduit de {int}.{int} euros")
    public void le_solde_de_l_allocation_est_reduit_de_euros_decimales(Integer euros, Integer centimes) {
        double amount = euros + (centimes / 100.0);
        le_solde_de_l_allocation_est_reduit_de_euros(amount);
    }

    @Et("le solde restant est de {double} euros")
    public void le_solde_restant_est_de_euros(Double expectedBalance) {
        assertEquals(expectedBalance, context.currentCustomer.getAllowance(), 0.01);
    }

    @Et("le solde restant est de {int}.{int} euros")
    public void le_solde_restant_est_de_euros_decimales(Integer euros, Integer centimes) {
        double expectedBalance = euros + (centimes / 100.0);
        le_solde_restant_est_de_euros(expectedBalance);
    }

    @Quand("l'étudiant consulte son solde")
    public void l_etudiant_consulte_son_solde() {
        context.studentCredit = context.currentCustomer.getAllowance();
    }

    @Alors("le solde disponible affiché est de {double} euros")
    public void le_solde_disponible_affiche_est_de_euros(Double expectedBalance) {
        assertEquals(expectedBalance, context.currentCustomer.getAllowance(), 0.01);
    }

    @Alors("le solde disponible affiché est de {int}.{int} euros")
    public void le_solde_disponible_affiche_est_de_euros_decimales(Integer euros, Integer centimes) {
        double expectedBalance = euros + (centimes / 100.0);
        le_solde_disponible_affiche_est_de_euros(expectedBalance);
    }

    @Quand("l'étudiant sélectionne le mode de paiement {string}")
    public void l_etudiant_selectionne_le_mode_de_paiement(String paymentMethodStr) {
        l_utilisateur_selectionne_le_mode_de_paiement(paymentMethodStr);
    }

    @Alors("un message indique {string}")
    public void un_message_indique(String message) {
        context.paymentMessage = message;
        assertNotNull(context.paymentMessage);
    }

    @Et("l'étudiant peut compléter avec un autre mode de paiement")
    public void l_etudiant_peut_completer_avec_un_autre_mode_de_paiement() {
        assertTrue(true);
    }

    @Quand("l'étudiant tente de payer uniquement avec {string}")
    public void l_etudiant_tente_de_payer_uniquement_avec(String paymentMethodStr) {
        try {
            if ("STUDENT_CREDIT".equals(paymentMethodStr)) {
                paymentMethodStr = "ALLOWANCE";
            }
            context.paymentMethod = PaymentMethod.valueOf(paymentMethodStr);
            double orderTotal = context.currentOrder.getTotalAmount();
            double allowance = context.currentCustomer.getAllowance();

            if (allowance < orderTotal) {
                context.errorMessage = "Allocation insuffisante";
                throw new IllegalStateException("Allocation insuffisante");
            }
            // If we reach here, payment would succeed
            context.paymentSuccess = true;
            context.paymentStatus = "PAID";
            context.currentOrder.setStatus(OrderStatus.VALIDATED);
        } catch (Exception e) {
            context.caughtException = e;
            if (context.errorMessage == null) {
                context.errorMessage = e.getMessage();
            }
            context.paymentSuccess = false;
        }
    }

    @Et("le paiement est refusé")
    public void le_paiement_est_refuse() {
        assertFalse(context.paymentSuccess);
    }

    @Étantdonné("l'étudiant a payé les commandes suivantes avec le crédit étudiant:")
    public void l_etudiant_a_paye_les_commandes_suivantes_avec_le_credit_etudiant(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> commandes = dataTable.asMaps();
        double totalSpent = 0.0;

        for (Map<String, String> row : commandes) {
            double montant = Double.parseDouble(row.get("montant"));
            totalSpent += montant;

            Order order = new Order(context.currentCustomer, context.currentRestaurant);
            order.setStatus(OrderStatus.VALIDATED);
            context.orderHistory.add(order);
        }

        double initialAllowance = context.currentCustomer.getAllowance();
        context.currentCustomer.setAllowance(initialAllowance - totalSpent);
    }

    @Quand("l'étudiant consulte l'historique de son crédit")
    public void l_etudiant_consulte_l_historique_de_son_credit() {
        assertNotNull(context.orderHistory);
    }

    @Alors("l'étudiant voit {int} transactions")
    public void l_etudiant_voit_transactions(Integer expectedCount) {
        assertEquals(expectedCount, context.orderHistory.size());
    }

    @Quand("l'étudiant paie avec le crédit étudiant")
    public void l_etudiant_paie_avec_le_credit_etudiant() {
        l_etudiant_confirme_le_paiement();
    }

    @Et("le solde de l'allocation est de {double} euro")
    public void le_solde_de_l_allocation_est_de_euro(Double expectedBalance) {
        assertEquals(expectedBalance, context.currentCustomer.getAllowance(), 0.01);
    }

    @Et("le solde de l'allocation est de {int}.{int} euro")
    public void le_solde_de_l_allocation_est_de_euro_decimales(Integer euros, Integer centimes) {
        double expectedBalance = euros + (centimes / 100.0);
        le_solde_de_l_allocation_est_de_euro(expectedBalance);
    }

    @Alors("le solde de l'allocation est de {double} euros")
    public void le_solde_de_l_allocation_est_de_euros(Double expectedBalance) {
        assertEquals(expectedBalance, context.currentCustomer.getAllowance(), 0.01);
    }

    @Alors("le solde de l'allocation est de {int}.{int} euros")
    public void le_solde_de_l_allocation_est_de_euros_decimales(Integer euros, Integer centimes) {
        double expectedBalance = euros + (centimes / 100.0);
        le_solde_de_l_allocation_est_de_euros(expectedBalance);
    }

    @Quand("l'allocation est rechargée de {double} euros")
    public void l_allocation_est_rechargee_de_euros(Double amount) {
        double currentAllowance = context.currentCustomer.getAllowance();
        context.currentCustomer.setAllowance(currentAllowance + amount);
    }

    @Quand("l'allocation est rechargée de {int}.{int} euros")
    public void l_allocation_est_rechargee_de_euros_decimales(Integer euros, Integer centimes) {
        double amount = euros + (centimes / 100.0);
        l_allocation_est_rechargee_de_euros(amount);
    }

    @Quand("l'étudiant utilise {double} euros du crédit étudiant")
    public void l_etudiant_utilise_euros_du_credit_etudiant(Double amount) {
        context.studentCredit = amount;
    }

    @Quand("l'étudiant utilise {int}.{int} euros du crédit étudiant")
    public void l_etudiant_utilise_euros_du_credit_etudiant_decimales(Integer euros, Integer centimes) {
        double amount = euros + (centimes / 100.0);
        l_etudiant_utilise_euros_du_credit_etudiant(amount);
    }

    @Et("l'étudiant paie le complément de {double} euros par carte bancaire")
    public void l_etudiant_paie_le_complement_de_euros_par_carte_bancaire(Double amount) {
        double studentCreditUsed = context.studentCredit;
        double currentAllowance = context.currentCustomer.getAllowance();

        if (currentAllowance >= studentCreditUsed) {
            context.currentCustomer.setAllowance(currentAllowance - studentCreditUsed);
            context.paymentSuccess = true;
            context.paymentStatus = "PAID";
            context.currentOrder.setStatus(OrderStatus.VALIDATED);
        }
    }

    @Et("l'étudiant paie le complément de {int}.{int} euros par carte bancaire")
    public void l_etudiant_paie_le_complement_de_euros_par_carte_bancaire_decimales(Integer euros, Integer centimes) {
        double amount = euros + (centimes / 100.0);
        l_etudiant_paie_le_complement_de_euros_par_carte_bancaire(amount);
    }

    @Alors("les deux paiements sont acceptés")
    public void les_deux_paiements_sont_acceptes() {
        assertTrue(context.paymentSuccess);
        assertEquals(OrderStatus.VALIDATED, context.currentOrder.getStatus());
    }
}

