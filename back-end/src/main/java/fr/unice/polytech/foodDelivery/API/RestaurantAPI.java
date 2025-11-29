package fr.unice.polytech.foodDelivery.API;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import fr.unice.polytech.foodDelivery.domain.model.Meal;
import fr.unice.polytech.foodDelivery.domain.model.Planning;
import fr.unice.polytech.foodDelivery.domain.model.RestaurantAccount;
import fr.unice.polytech.foodDelivery.service.RestaurantService;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class RestaurantAPI implements HttpHandler {
    RestaurantService restaurantService = new RestaurantService();
    ObjectMapper mapper = new ObjectMapper();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        System.out.println(method);
        String path = exchange.getRequestURI().getPath();
        String[] pathSegments = path.split("/");

        // Normalisation des paths : on ignore le premier segment vide
        // Ex: "/", "/restaurants", "/restaurants/123" etc.
        int len = pathSegments.length;
        String response = "{\"message\": \"Bienvenue sur l'API Restaurant!\"}";
        InputStream is = exchange.getRequestBody();

        switch (method) {

            case "GET":
                System.out.println("youpi");
                // GET /restaurants
                if (len == 2) {
                    response = mapper.writeValueAsString(restaurantService.findAll());
                }
                // GET /restaurants/{name}
                else if (len == 3) {
                    String name = pathSegments[2];
                    response = mapper.writeValueAsString(restaurantService.findByName(name));
                }
                break;

            case "POST":
                // POST /restaurants
                if (len == 2) {

                    RestaurantAccount newRestaurant = mapper.readValue(is, RestaurantAccount.class);
                    restaurantService.registerRestaurant(newRestaurant);
                    response = "{\"message\": \"Restaurant enregistré avec succès!\"}";
                }
                // POST /restaurants/{id}/timeslots
                else if (len == 4 && pathSegments[3].equals("timeslots")) {
                    UUID id = UUID.fromString(pathSegments[2]);
                    Planning newTimeslot = mapper.readValue(is, Planning.class);
                    restaurantService.addTimeSlot(id, newTimeslot);
                    response = "{\"message\": \"Créneau horaire ajouté avec succès!\"}";
                }
                // POST /restaurants/{id}/menu/meals
                else if (len == 5 && pathSegments[3].equals("menu") && pathSegments[4].equals("meals")) {
                    UUID id = UUID.fromString(pathSegments[2]);
                    Meal newMeal = mapper.readValue(is, Meal.class);
                    restaurantService.addMealToMenu(id, newMeal);
                    response = "{\"message\": \"Repas ajouté avec succès au menu!\"}";
                }
                break;

            case "PUT":
                // PUT /restaurants/{id}
                if (len == 3) {
                    UUID id = UUID.fromString(pathSegments[2]);
                    restaurantService.updateRestaurant(id, mapper.readValue(is, RestaurantAccount.class));
                    response = "{\"message\": \"Restaurant mis à jour avec succès!\"}";
                }
                // PUT /restaurants/{id}/menu/meals/{mealName}
                else if (len == 6 && pathSegments[3].equals("menu") && pathSegments[4].equals("meals")) {
                    UUID id = UUID.fromString(pathSegments[2]);
                    String mealName = pathSegments[5];
                    Meal updateMeal = mapper.readValue(is, Meal.class);
                    restaurantService.updateMeal(id, restaurantService.getMealByName(id, mealName), updateMeal);
                    response = "{\"message\": \"Repas mis à jour avec succès dans le menu!\"}";
                }
                // PUT /restaurants/{id}/timeslots/{slotId}
                else if (len == 5 && pathSegments[3].equals("timeslots")) {
                    UUID id = UUID.fromString(pathSegments[2]);
                    String slotId = pathSegments[4];
                    int newSlotCapacity = mapper.readValue(is, Integer.class);
                    restaurantService.updateSlotCapacity(id, slotId, newSlotCapacity);
                    response = "{\"message\": \"Capacité du créneau horaire mise à jour avec succès!\"}";
                }
                break;

            case "DELETE":
                // DELETE /restaurants/{id}/menu/meals/{mealName}
                if (len == 6 && pathSegments[3].equals("menu") && pathSegments[4].equals("meals")) {
                    UUID id = UUID.fromString(pathSegments[2]);
                    String mealName = pathSegments[5];
                    restaurantService.getRestaurantMenu(id).removeMeal(restaurantService.getMealByName(id, mealName));
                    response = "{\"message\": \"Repas supprimé avec succès du menu!\"}";
                }
                // DELETE /restaurants/{id}/timeslots/{slotId}
                else if (len == 5 && pathSegments[3].equals("timeslots")) {
                    UUID id = UUID.fromString(pathSegments[2]);
                    String slotId = pathSegments[4];
                    restaurantService.removeTimeSlot(id, slotId);
                    response = "{\"message\": \"Créneau horaire supprimé avec succès!\"}";
                }
                break;
        }

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
        exchange.close();
    }
}
