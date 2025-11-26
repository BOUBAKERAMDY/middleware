package fr.unice.polytech.foodDelivery.stepDefs.userStories;

import fr.unice.polytech.foodDelivery.stepDefs.userStories.context.UserStoriesContext;
import io.cucumber.java.fr.*;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalTime;
import java.util.*;
import fr.unice.polytech.foodDelivery.domain.model.*;
import fr.unice.polytech.foodDelivery.domain.ENUM.*;
import fr.unice.polytech.foodDelivery.domain.exception.*;

public class R5_RestaurantCapacitySchedulingSteps {

    private final UserStoriesContext context;
    private String currentManager;

    public R5_RestaurantCapacitySchedulingSteps(UserStoriesContext context) {
        this.context = context;
    }

    @Quand("le gestionnaire définit les créneaux de capacité suivants:")
    public void le_gestionnaire_definit_les_creneaux_de_capacite_suivants(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> creneaux = dataTable.asMaps();

        for (Map<String, String> row : creneaux) {
            String heure = row.get("heure");
            int capacite = Integer.parseInt(row.get("capacité"));

            String[] times = heure.split("-");
            LocalTime start = LocalTime.parse(times[0]);
            LocalTime end = LocalTime.parse(times[1]);

            String slotId = context.currentRestaurant.getName() + "_" + heure;
            java.time.DayOfWeek day = java.time.DayOfWeek.MONDAY; // Default day
            context.currentRestaurant.getPlanning().addTimeSlot(slotId, day, start, end, capacite);

            String key = context.currentRestaurant.getName() + "_" + heure;
            context.timeSlotCapacities.put(key, capacite);
            context.timeSlotReservations.putIfAbsent(key, 0);
        }
    }

    @Alors("le restaurant a {int} créneaux de capacité configurés")
    public void le_restaurant_a_creneaux_de_capacite_configures(Integer expectedCount) {
        assertEquals(expectedCount, context.currentRestaurant.getPlanning().getTimeSlots().size());
    }

    @Et("le créneau {string} a une capacité de {int} commandes")
    public void le_creneau_a_une_capacite_de_commandes(String creneau, Integer expectedCapacity) {
        String key = context.currentRestaurant.getName() + "_" + creneau;
        Integer capacity = context.timeSlotCapacities.get(key);
        assertNotNull(capacity);
        assertEquals(expectedCapacity, capacity);
    }

    @Étantdonnéque("le restaurant a les créneaux suivants:")
    public void le_restaurant_a_les_creneaux_suivants(io.cucumber.datatable.DataTable dataTable) {
        que_le_restaurant_a_les_creneaux_suivants(dataTable);
    }

    @Étantdonné("que le restaurant a les créneaux suivants:")
    public void que_le_restaurant_a_les_creneaux_suivants(io.cucumber.datatable.DataTable dataTable) {
        le_gestionnaire_definit_les_creneaux_de_capacite_suivants(dataTable);
    }

    @Quand("le gestionnaire met à jour le créneau {string} avec une capacité de {int}")
    public void le_gestionnaire_met_a_jour_le_creneau_avec_une_capacite_de(String creneau, Integer newCapacity) {
        String key = context.currentRestaurant.getName() + "_" + creneau;
        context.timeSlotCapacities.put(key, newCapacity);

        String[] times = creneau.split("-");
        LocalTime start = LocalTime.parse(times[0]);
        LocalTime end = LocalTime.parse(times[1]);

    }

    @Et("le créneau {string} reste à {int} commandes")
    public void le_creneau_reste_a_commandes(String creneau, Integer expectedCapacity) {
        le_creneau_a_une_capacite_de_commandes(creneau, expectedCapacity);
    }

    @Alors("tous les créneaux entre {string} et {string} ont une capacité de {int} commandes")
    public void tous_les_creneaux_entre_et_ont_une_capacite_de_commandes(String startTime, String endTime, Integer expectedCapacity) {
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);


        for (Planning.TimeSlot slot : context.currentRestaurant.getPlanning().getTimeSlots().values()) {
            if (!slot.getStartTime().isBefore(start) && !slot.getEndTime().isAfter(end)) {
                assertEquals(expectedCapacity, slot.getCapacity());
            }
        }
    }

    @Quand("le gestionnaire ajoute les créneaux suivants:")
    public void le_gestionnaire_ajoute_les_creneaux_suivants(io.cucumber.datatable.DataTable dataTable) {
        le_gestionnaire_definit_les_creneaux_de_capacite_suivants(dataTable);
    }

    @Quand("le gestionnaire supprime le créneau {string}")
    public void le_gestionnaire_supprime_le_creneau(String creneau) {
        String key = context.currentRestaurant.getName() + "_" + creneau;
        context.timeSlotCapacities.remove(key);
        context.timeSlotReservations.remove(key);
        String slotId = context.currentRestaurant.getName() + "_" + creneau;
        context.currentRestaurant.getPlanning().removeTimeSlot(slotId);
    }

    @Et("le restaurant n'a pas de créneau {string}")
    public void le_restaurant_n_a_pas_de_creneau(String creneau) {
        String key = context.currentRestaurant.getName() + "_" + creneau;
        assertFalse(context.timeSlotCapacities.containsKey(key));
    }

    @Quand("le gestionnaire consulte la capacité restante du créneau {string}")
    public void le_gestionnaire_consulte_la_capacite_restante_du_creneau(String creneau) {
        String key = context.currentRestaurant.getName() + "_" + creneau;
        Integer capacity = context.timeSlotCapacities.get(key);
        Integer reserved = context.timeSlotReservations.getOrDefault(key, 0);
        context.remainingCapacity = capacity - reserved;
    }

    @Alors("la capacité restante est de {int} commandes")
    public void la_capacite_restante_est_de_commandes(Integer expectedRemaining) {
        assertEquals(expectedRemaining, context.remainingCapacity);
    }

    @Quand("le gestionnaire définit les créneaux pour {string}:")
    public void le_gestionnaire_definit_les_creneaux_pour(String day, io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> creneaux = dataTable.asMaps();

        Map<String, Integer> dayCapacities = context.dailyCapacities.computeIfAbsent(day, k -> new HashMap<>());

        for (Map<String, String> row : creneaux) {
            String heure = row.get("heure");
            int capacite = Integer.parseInt(row.get("capacité"));
            dayCapacities.put(heure, capacite);
        }
    }

    @Alors("le créneau {string} du {string} a une capacité de {int} commandes")
    public void le_creneau_du_a_une_capacite_de_commandes(String creneau, String day, Integer expectedCapacity) {
        Map<String, Integer> dayCapacities = context.dailyCapacities.get(day);
        assertNotNull(dayCapacities);
        Integer capacity = dayCapacities.get(creneau);
        assertEquals(expectedCapacity, capacity);
    }

    @Quand("le gestionnaire tente de mettre à jour le créneau {string} avec une capacité de {int}")
    public void le_gestionnaire_tente_de_mettre_a_jour_le_creneau_avec_une_capacite_de(String creneau, Integer newCapacity) {
        try {
            String key = context.currentRestaurant.getName() + "_" + creneau;
            Integer reserved = context.timeSlotReservations.getOrDefault(key, 0);

            if (newCapacity < reserved) {
                throw new CapacityExceededException("Impossible de réduire la capacité en dessous du nombre de commandes validées");
            }

            context.timeSlotCapacities.put(key, newCapacity);
        } catch (Exception e) {
            context.caughtException = e;
            context.errorMessage = e.getMessage();
        }
    }

    @Et("le créneau {string} garde sa capacité de {int} commandes")
    public void le_creneau_garde_sa_capacite_de_commandes(String creneau, Integer expectedCapacity) {
        le_creneau_a_une_capacite_de_commandes(creneau, expectedCapacity);
    }

    @Et("la capacité totale journalière est de {int} commandes")
    public void la_capacite_totale_journaliere_est_de_commandes(Integer expectedTotal) {
        int total = 0;
        for (Integer capacity : context.timeSlotCapacities.values()) {
            total += capacity;
        }
        assertEquals(expectedTotal, total);
    }
}
