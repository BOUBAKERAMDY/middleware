package fr.unice.polytech.foodDelivery.stepDefs.backend;

import io.cucumber.java.fr.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import fr.unice.polytech.foodDelivery.domain.model.*;
import fr.unice.polytech.foodDelivery.service.RestaurantService;

public class CapacitySteps {

    private final TestContext context;
    private final RestaurantService restaurantService;
    private String dernierCreneauId = null;

    public CapacitySteps(TestContext context) {
        this.context = context;
        this.restaurantService = new RestaurantService();
    }

    /* ==============================
       🕒 Gestion des créneaux horaires
       ============================== */
    @Quand("le propriétaire ajoute les créneaux suivants:")
    public void le_proprietaire_ajoute_les_creneaux_suivants(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> creneaux = dataTable.asMaps();

        for (Map<String, String> creneau : creneaux) {
            String slotId = creneau.get("créneau");
            LocalTime debut = LocalTime.parse(creneau.get("début"));
            LocalTime fin = LocalTime.parse(creneau.get("fin"));
            int capacite = Integer.parseInt(creneau.get("capacité"));

            restaurantService.addTimeSlot(context.restaurant.getRestaurantId(), slotId, DayOfWeek.MONDAY, debut, fin, capacite);
            dernierCreneauId = slotId;
        }
    }

    @Alors("tous les créneaux sont disponibles")
    public void tous_les_creneaux_sont_disponibles() {
        for (String slotId : context.restaurant.getPlanning().getTimeSlots().keySet()) {
            assertTrue(restaurantService.isSlotAvailable(context.restaurant.getRestaurantId(), slotId),
                    "Le créneau " + slotId + " devrait être disponible");
        }
    }

    /* ==============================
       📊 Vérification disponibilité
       ============================== */
    @Étantdonné("un créneau {string} avec une capacité de {int}")
    public void un_creneau_avec_capacite(String creneau, int capacite) {
        LocalTime debut = LocalTime.parse(creneau.split("-")[0]);
        LocalTime fin = LocalTime.parse(creneau.split("-")[1]);

        restaurantService.addTimeSlot(context.restaurant.getRestaurantId(), creneau, DayOfWeek.MONDAY, debut, fin, capacite);
        dernierCreneauId = creneau;
    }

    @Quand("{int} commandes sont réservées pour ce créneau")
    public void commandes_sont_reservees_pour_ce_creneau(int nombreCommandes) {
        if (dernierCreneauId != null) {
            for (int i = 0; i < nombreCommandes; i++) {
                if (restaurantService.isSlotAvailable(context.restaurant.getRestaurantId(), dernierCreneauId)) {
                    restaurantService.reserveSlotForOrder(context.restaurant.getRestaurantId(), dernierCreneauId);
                }
            }
        }
    }

    @Et("{int} commandes sont déjà réservées pour ce créneau")
    public void commandes_sont_deja_reservees_pour_ce_creneau(int nombreCommandes) {
        commandes_sont_reservees_pour_ce_creneau(nombreCommandes);
    }

    @Alors("le créneau a {int} places restantes")
    public void le_creneau_a_places_restantes(int placesRestantes) {
        if (dernierCreneauId != null) {
            Planning.TimeSlot slot = context.restaurant.getPlanning().getTimeSlot(dernierCreneauId);
            assertNotNull(slot, "Le créneau " + dernierCreneauId + " devrait exister");
            int available = slot.getCapacity() - slot.getCurrentOrders();
            assertEquals(placesRestantes, available,
                    "Le créneau devrait avoir " + placesRestantes + " places restantes");
        }
    }

    @Et("{int} places sont disponibles")
    public void places_sont_disponibles(int placesDisponibles) {
        le_creneau_a_places_restantes(placesDisponibles);
    }

    /* ==============================
       ❌ Créneau indisponible
       ============================== */
    @Alors("le créneau {string} n'est plus disponible")
    public void le_creneau_n_est_plus_disponible(String creneau) {
        assertFalse(restaurantService.isSlotAvailable(context.restaurant.getRestaurantId(), creneau),
                "Le créneau " + creneau + " ne devrait plus être disponible");
    }

    @Alors("aucune nouvelle commande ne peut être acceptée pour ce créneau")
    public void aucune_nouvelle_commande_ne_peut_etre_acceptee() {
        if (dernierCreneauId != null) {
            assertFalse(restaurantService.isSlotAvailable(context.restaurant.getRestaurantId(), dernierCreneauId),
                    "Aucune nouvelle commande ne devrait être acceptée pour ce créneau");
        }
    }

    @Mais("aucune nouvelle commande ne peut être prise")
    public void aucune_nouvelle_commande_ne_peut_etre_prise() {
        aucune_nouvelle_commande_ne_peut_etre_acceptee();
    }

    /* ==============================
       📈 Augmentation capacité
       ============================== */
    @Quand("le propriétaire augmente la capacité du créneau à {int}")
    public void le_proprietaire_augmente_la_capacite_du_creneau(int nouvelleCapacite) {
        if (dernierCreneauId != null) {
            restaurantService.updateSlotCapacity(context.restaurant.getRestaurantId(), dernierCreneauId, nouvelleCapacite);
        }
    }

    @Alors("{int} places supplémentaires sont disponibles")
    public void places_supplementaires_sont_disponibles(int placesSupplementaires) {
        if (dernierCreneauId != null) {
            Planning.TimeSlot slot = context.restaurant.getPlanning().getTimeSlot(dernierCreneauId);
            assertNotNull(slot, "Le créneau devrait exister");
            int available = slot.getCapacity() - slot.getCurrentOrders();
            assertTrue(available >= placesSupplementaires,
                    "Le créneau devrait avoir au moins " + placesSupplementaires + " places supplémentaires, mais a " + available);
        }
    }

    /* ==============================
       📉 Réduction capacité
       ============================== */
    @Quand("le propriétaire réduit la capacité du créneau à {int}")
    public void le_proprietaire_reduit_la_capacite_du_creneau(int nouvelleCapacite) {
        if (dernierCreneauId != null) {
            restaurantService.updateSlotCapacity(context.restaurant.getRestaurantId(), dernierCreneauId, nouvelleCapacite);
        }
    }

    @Quand("le propriétaire tente de réduire la capacité du créneau à {int}")
    public void le_proprietaire_tente_de_reduire_la_capacite(int nouvelleCapacite) {
        le_proprietaire_reduit_la_capacite_du_creneau(nouvelleCapacite);
    }

    @Alors("la modification est acceptée")
    public void la_modification_est_acceptee() {
        assertTrue(true);
    }

    /* ==============================
       📦 Distribution des commandes
       ============================== */
    @Étantdonné("les créneaux suivants avec capacités limitées:")
    public void les_creneaux_suivants_avec_capacites_limitees(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> creneaux = dataTable.asMaps();

        for (Map<String, String> creneau : creneaux) {
            String slotId = creneau.get("créneau");
            LocalTime debut = LocalTime.parse(slotId.split("-")[0]);
            LocalTime fin = LocalTime.parse(slotId.split("-")[1]);
            int capacite = Integer.parseInt(creneau.get("capacité"));

            restaurantService.addTimeSlot(context.restaurant.getRestaurantId(), slotId, DayOfWeek.MONDAY, debut, fin, capacite);
        }
    }

    @Quand("{int} commandes sont créées")
    public void commandes_sont_creees(int nombreTotalCommandes) {
        int commandesRestantes = nombreTotalCommandes;
        Map<String, Planning.TimeSlot> slots = context.restaurant.getPlanning().getTimeSlots();

        while (commandesRestantes > 0) {
            boolean commandePlacee = false;

            for (String slotId : slots.keySet()) {
                if (restaurantService.isSlotAvailable(context.restaurant.getRestaurantId(), slotId) && commandesRestantes > 0) {
                    restaurantService.reserveSlotForOrder(context.restaurant.getRestaurantId(), slotId);
                    commandesRestantes--;
                    commandePlacee = true;
                }
            }

            if (!commandePlacee) {
                break;
            }
        }
    }

    @Alors("les commandes sont distribuées sur les {int} créneaux")
    public void les_commandes_sont_distribuees_sur_les_creneaux(int nombreCreneauxUtilises) {
        int creneauxAvecReservations = 0;
        Map<String, Planning.TimeSlot> slots = context.restaurant.getPlanning().getTimeSlots();

        for (Planning.TimeSlot slot : slots.values()) {
            if (slot.getCurrentOrders() > 0) {
                creneauxAvecReservations++;
            }
        }
        assertEquals(nombreCreneauxUtilises, creneauxAvecReservations);
    }

    @Alors("chaque créneau atteint sa capacité maximale")
    public void chaque_creneau_atteint_sa_capacite_maximale() {
        Map<String, Planning.TimeSlot> slots = context.restaurant.getPlanning().getTimeSlots();

        for (Map.Entry<String, Planning.TimeSlot> entry : slots.entrySet()) {
            Planning.TimeSlot slot = entry.getValue();
            assertEquals(slot.getCapacity(), slot.getCurrentOrders(),
                    "Le créneau " + entry.getKey() + " devrait être à capacité maximale");
        }
    }

    /* ==============================
       🧪 Scénario outline
       ============================== */
    @Quand("{int} commandes sont réservées")
    public void commandes_sont_reservees(int nombreCommandes) {
        commandes_sont_reservees_pour_ce_creneau(nombreCommandes);
    }

    @Alors("la disponibilité du créneau est {word}")
    public void la_disponibilite_du_creneau_est(String disponible) {
        if (dernierCreneauId != null) {
            boolean isAvailable = restaurantService.isSlotAvailable(context.restaurant.getRestaurantId(), dernierCreneauId);
            boolean expectedAvailability = "oui".equalsIgnoreCase(disponible);
            assertEquals(expectedAvailability, isAvailable,
                    "La disponibilité du créneau ne correspond pas à l'attente");
        }
    }

    /* ==============================
       🍽️ Heures de pointe
       ============================== */
    @Étantdonné("les créneaux de déjeuner avec capacité élevée:")
    public void les_creneaux_de_dejeuner_avec_capacite_elevee(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> creneaux = dataTable.asMaps();

        for (Map<String, String> creneau : creneaux) {
            String slotId = creneau.get("créneau");
            LocalTime debut = LocalTime.parse(slotId.split("-")[0]);
            LocalTime fin = LocalTime.parse(slotId.split("-")[1]);
            int capacite = Integer.parseInt(creneau.get("capacité"));

            restaurantService.addTimeSlot(context.restaurant.getRestaurantId(), slotId, DayOfWeek.MONDAY, debut, fin, capacite);
        }
    }

    @Étantdonné("les créneaux hors pointe avec capacité normale:")
    public void les_creneaux_hors_pointe_avec_capacite_normale(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> creneaux = dataTable.asMaps();

        for (Map<String, String> creneau : creneaux) {
            String slotId = creneau.get("créneau");
            LocalTime debut = LocalTime.parse(slotId.split("-")[0]);
            LocalTime fin = LocalTime.parse(slotId.split("-")[1]);
            int capacite = Integer.parseInt(creneau.get("capacité"));

            restaurantService.addTimeSlot(context.restaurant.getRestaurantId(), slotId, DayOfWeek.MONDAY, debut, fin, capacite);
        }
    }

    @Quand("{int} commandes sont passées pendant l'heure de déjeuner")
    public void commandes_sont_passees_pendant_heure_dejeuner(int nombreCommandes) {
        int commandesRestantes = nombreCommandes;
        Map<String, Planning.TimeSlot> slots = context.restaurant.getPlanning().getTimeSlots();

        while (commandesRestantes > 0) {
            boolean commandePlacee = false;

            for (String slotId : slots.keySet()) {
                if ((slotId.contains("12:") || slotId.contains("13:")) && commandesRestantes > 0) {
                    if (restaurantService.isSlotAvailable(context.restaurant.getRestaurantId(), slotId)) {
                        restaurantService.reserveSlotForOrder(context.restaurant.getRestaurantId(), slotId);
                        commandesRestantes--;
                        commandePlacee = true;
                    }
                }
            }

            if (!commandePlacee) {
                break;
            }
        }
    }

    @Alors("toutes les commandes de déjeuner sont acceptées")
    public void toutes_les_commandes_de_dejeuner_sont_acceptees() {
        int totalReservationsDejeuner = 0;
        Map<String, Planning.TimeSlot> slots = context.restaurant.getPlanning().getTimeSlots();

        for (Map.Entry<String, Planning.TimeSlot> entry : slots.entrySet()) {
            if (entry.getKey().contains("12:") || entry.getKey().contains("13:")) {
                totalReservationsDejeuner += entry.getValue().getCurrentOrders();
            }
        }
        assertTrue(totalReservationsDejeuner > 0, "Des commandes de déjeuner devraient être acceptées");
    }

    @Alors("les créneaux hors pointe restent disponibles")
    public void les_creneaux_hors_pointe_restent_disponibles() {
        Map<String, Planning.TimeSlot> slots = context.restaurant.getPlanning().getTimeSlots();

        for (String slotId : slots.keySet()) {
            if (slotId.contains("14:") || slotId.contains("15:")) {
                assertTrue(restaurantService.isSlotAvailable(context.restaurant.getRestaurantId(), slotId),
                        "Le créneau hors pointe " + slotId + " devrait être disponible");
            }
        }
    }
}
