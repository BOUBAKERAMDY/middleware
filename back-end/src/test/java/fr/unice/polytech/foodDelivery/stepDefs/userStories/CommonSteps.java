package fr.unice.polytech.foodDelivery.stepDefs.userStories;

import fr.unice.polytech.foodDelivery.stepDefs.userStories.context.UserStoriesContext;
import io.cucumber.java.fr.*;
import static org.junit.jupiter.api.Assertions.*;


public class CommonSteps {

    private final UserStoriesContext context;

    public CommonSteps(UserStoriesContext context) {
        this.context = context;
    }

    @Alors("l'utilisateur voit {int} restaurants")
    public void l_utilisateur_voit_restaurants(Integer expectedCount) {
        // This step can be used for both browsed and filtered restaurants
        int actualCount = context.filteredRestaurants.isEmpty()
            ? context.restaurants.size()
            : context.filteredRestaurants.size();
        assertEquals(expectedCount, actualCount);
    }

    @Alors("l'utilisateur voit {int} restaurant")
    public void l_utilisateur_voit_restaurant(Integer expectedCount) {
        assertEquals(expectedCount, context.filteredRestaurants.size());
    }

    @Et("la liste contient {string}")
    public void la_liste_contient(String restaurantName) {
        boolean found = context.filteredRestaurants.stream()
                .anyMatch(r -> r.getName().equals(restaurantName));
        assertTrue(found, "Restaurant not found in filtered list: " + restaurantName);
    }
}

