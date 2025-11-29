package fr.unice.polytech.foodDelivery.API;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import fr.unice.polytech.foodDelivery.domain.ENUM.CuisineType;
import fr.unice.polytech.foodDelivery.domain.ENUM.RestaurantType;
import fr.unice.polytech.foodDelivery.domain.model.Meal;
import fr.unice.polytech.foodDelivery.domain.model.Menu;
import fr.unice.polytech.foodDelivery.domain.model.Planning;
import fr.unice.polytech.foodDelivery.domain.model.RestaurantAccount;
import fr.unice.polytech.foodDelivery.service.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RestaurantAPITest {

    private RestaurantAPI api;
    private RestaurantService service;
    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        api = new RestaurantAPI();
        service = api.restaurantService;
        service.clearData();
    }

    private HttpExchange mockRequest(String method, String path, String body) throws IOException {
        HttpExchange exchange = mock(HttpExchange.class);

        // méthode et URI
        when(exchange.getRequestMethod()).thenReturn(method);
        when(exchange.getRequestURI()).thenReturn(java.net.URI.create(path));

        // corps de la requête
        if (body != null) {
            InputStream is = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
            when(exchange.getRequestBody()).thenReturn(is);
        } else {
            when(exchange.getRequestBody()).thenReturn(InputStream.nullInputStream());
        }

        // 👇 stub des Headers pour éviter la NPE
        Headers headers = new Headers();
        when(exchange.getResponseHeaders()).thenReturn(headers);

        // 👇 stub du OutputStream pour capturer la réponse
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        when(exchange.getResponseBody()).thenReturn(os);

        return exchange;
    }


    @Test
    void testAddRestaurant() throws IOException {
        RestaurantAccount r = new RestaurantAccount("PizzaHut");

        HttpExchange exchange = mockRequest("POST", "/restaurants","{\"name\":\"PizzaHut\"}");

        api.handle(exchange);

        assertEquals(1, service.findAll().size());
        assertEquals("PizzaHut", service.findAll().get(0).getName());
    }

    @Test
    void testGetAllRestaurants() throws Exception {
        service.registerRestaurant(new RestaurantAccount("KFC", new Menu()));
        service.registerRestaurant(new RestaurantAccount("McDo", new Menu()));

        HttpExchange exchange = mockRequest("GET", "/restaurants", null);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        when(exchange.getResponseBody()).thenReturn(os);

        api.handle(exchange);

        List<?> list = mapper.readValue(os.toByteArray(), List.class);
        assertEquals(2, list.size());
    }

    @Test
    void testGetRestaurantByName() throws Exception {
        RestaurantAccount r = new RestaurantAccount("SushiBar", new Menu());
        service.registerRestaurant(r);

        HttpExchange exchange = mockRequest("GET", "/restaurants/" + r.getName(), null);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        when(exchange.getResponseBody()).thenReturn(os);

        api.handle(exchange);

        RestaurantAccount result = mapper.readValue(os.toByteArray(), RestaurantAccount.class);
        assertEquals("SushiBar", result.getName());
    }

    @Test
    void testAddMealToMenu() throws Exception {
        RestaurantAccount r = new RestaurantAccount("KFC", new Menu());
        service.registerRestaurant(r);

        Meal m = new Meal.Builder("Bucket", 15.0).build();

        HttpExchange exchange = mockRequest("POST",
                "/restaurants/" + r.getRestaurantId() + "/menu/meals",
                "{\"name\":\"Bucket\", \"price\":\"15.0\"}");

        api.handle(exchange);
        System.out.println(service.getMealByName(r.getRestaurantId(),"Bucket").getPrice());
        assertTrue(service.getMealByName(r.getRestaurantId(),"Bucket").getPrice() == 15.0);
    }

    @Test
    void testDeleteMealFromMenu() throws Exception {
        RestaurantAccount r = new RestaurantAccount("BurgerKing", new Menu());
        Meal whopper = new Meal.Builder("Whopper", 8.5).build();
        service.registerRestaurant(r);
        service.addMealToMenu(r.getRestaurantId(), whopper);

        HttpExchange exchange = mockRequest("DELETE",
                "/restaurants/" + r.getRestaurantId() + "/menu/meals/Whopper",
                null);

        api.handle(exchange);

        assertFalse(service.isMealInMenu(r.getRestaurantId(), whopper));
    }

    @Test
    void testAddTimeSlot() throws Exception {
        RestaurantAccount r = new RestaurantAccount("PizzaHut");
        service.registerRestaurant(r);
        UUID id = r.getRestaurantId();
        Planning.TimeSlot timeSlot = new Planning.TimeSlot( DayOfWeek.MONDAY, LocalTime.of(12,0), LocalTime.of(14,0), 10);
        String jsonBody = mapper.writeValueAsString(timeSlot);
        HttpExchange exchange = mockRequest("POST", "/restaurants/" + id + "/timeslots", jsonBody);

        api.handle(exchange);

        // Vérifie que le planning a été ajouté
        assertEquals(1, service.getRestaurantMemory().findById(id).get().getPlanning().getTimeSlots().size());
    }

    @Test
    void testUpdateRestaurant() throws Exception {
        RestaurantAccount r = new RestaurantAccount("PizzaHut");
        service.registerRestaurant(r);
        UUID id = r.getRestaurantId();

        Menu menu = new Menu();
        menu.addMeal(new Meal.Builder("Pizza Margherita", 10.0f).build());
        menu.addMeal(new Meal.Builder("Pasta Carbonara", 12.5f).build());

        List<CuisineType> cuisineTypes = Arrays.asList(CuisineType.ITALIAN);
        int[] priceRange = new int[] { 8, 25 };

        RestaurantAccount updated = new RestaurantAccount(
                "PizzaHutUpdated",
                menu,
                cuisineTypes,
                RestaurantType.RESTAURANT,
                priceRange
        );


        String jsonBody = mapper.writeValueAsString(updated);

        HttpExchange exchange = mockRequest("PUT", "/restaurants/" + id, jsonBody);

        api.handle(exchange);

        assertEquals("PizzaHutUpdated", service.findAll().get(0).getName());
    }

    @Test
    void testUpdateMealInMenu() throws Exception {
        RestaurantAccount r = new RestaurantAccount("PizzaHut");
        Meal oldMeal = new Meal.Builder("Pasta", 10).build();
        r.getMenu().addMeal(oldMeal);
        service.registerRestaurant(r);
        UUID id = r.getRestaurantId();

        Meal newMeal = new Meal.Builder("Pasta", 12).build();
        String jsonBody = mapper.writeValueAsString(newMeal);

        HttpExchange exchange = mockRequest("PUT", "/restaurants/" + id + "/menu/meals/Pasta", jsonBody);
        api.handle(exchange);

        Meal updated = service.getMealByName(id, "Pasta");
        assertEquals(12, updated.getPrice());
    }

    @Test
    void testUpdateTimeSlotCapacity() throws Exception {
        RestaurantAccount r = new RestaurantAccount("PizzaHut");
        r.getPlanning().addTimeSlot("slot1", DayOfWeek.MONDAY, LocalTime.of(12,0), LocalTime.of(14,0), 10);
        service.registerRestaurant(r);
        UUID id = r.getRestaurantId();

        String jsonBody = mapper.writeValueAsString(20); // nouvelle capacité
        HttpExchange exchange = mockRequest("PUT", "/restaurants/" + id + "/timeslots/slot1", jsonBody);

        api.handle(exchange);

        int capacity = r.getPlanning().getTimeSlot("slot1").getCapacity();
        assertEquals(20, capacity);
    }
}
