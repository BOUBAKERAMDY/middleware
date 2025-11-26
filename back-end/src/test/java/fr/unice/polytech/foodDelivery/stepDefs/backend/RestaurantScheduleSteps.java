package fr.unice.polytech.foodDelivery.stepDefs.backend;

import io.cucumber.java.fr.*;
import static org.junit.jupiter.api.Assertions.*;

import fr.unice.polytech.foodDelivery.domain.model.*;
import fr.unice.polytech.foodDelivery.domain.ENUM.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;

public class RestaurantScheduleSteps {

    private final TestContext context;

    public RestaurantScheduleSteps(TestContext context) {
        this.context = context;
    }


    @Etantdonné("un restaurant {string} avec un repas {string} au prix de {string}")
    public void creer_restaurant_pour_planning(String nom, String mealName, String priceStr) {
        double price = Double.parseDouble(priceStr.replace(",", "."));
        Meal meal = new Meal.Builder(mealName, price).build();
        context.restaurant = new RestaurantAccount(
                nom,
                new Menu(Arrays.asList(meal)),
                Arrays.asList(CuisineType.FRENCH),
                RestaurantType.RESTAURANT,
                new int[]{(int) price, (int) price + 10}
        );
        assertNotNull(context.restaurant);
    }

    @Quand("le restaurant modifie ses horaires d'ouverture pour {string} de {string} à {string}")
    public void restaurant_modifie_horaires(String jour, String heureDebut, String heureFin) {

        assertNotNull(context.restaurant, "Le restaurant ne devrait pas être null");

        DayOfWeek day = DayOfWeek.valueOf(jour.toUpperCase());
        LocalTime startTime = LocalTime.parse(heureDebut);
        LocalTime endTime = LocalTime.parse(heureFin);

        String slotId = jour.toLowerCase() + "_" + heureDebut.replace(":", "") + "_" + heureFin.replace(":", "");


        context.restaurant.addToPlanning(slotId, day, startTime, endTime, 50);
    }

    @Alors("les horaires du restaurant doivent indiquer qu'il est ouvert le {string} de {string} à {string}")
    public void verifier_horaires_restaurant(String jour, String heureDebut, String heureFin) {
        assertNotNull(context.restaurant, "Le restaurant ne devrait pas être null");

        DayOfWeek day = DayOfWeek.valueOf(jour.toUpperCase());
        LocalTime startTime = LocalTime.parse(heureDebut);
        LocalTime endTime = LocalTime.parse(heureFin);

        String slotId = jour.toLowerCase() + "_" + heureDebut.replace(":", "") + "_" + heureFin.replace(":", "");
        Planning planning = context.restaurant.getPlanning();

        assertNotNull(planning, "Le planning ne devrait pas être null");

        Planning.TimeSlot timeSlot = planning.getTimeSlot(slotId);
        assertNotNull(timeSlot, "Le créneau horaire devrait exister pour le slot: " + slotId);
        assertEquals(day, timeSlot.getDay());
        assertEquals(startTime, timeSlot.getStartTime());
        assertEquals(endTime, timeSlot.getEndTime());
    }
}