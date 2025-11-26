package fr.unice.polytech.foodDelivery.API;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import fr.unice.polytech.foodDelivery.domain.model.CustomerAccount;
import fr.unice.polytech.foodDelivery.domain.model.Order;
import fr.unice.polytech.foodDelivery.domain.model.RestaurantAccount;
import fr.unice.polytech.foodDelivery.domain.model.Menu;
import fr.unice.polytech.foodDelivery.service.CustomerService;
import fr.unice.polytech.foodDelivery.service.OrderService;
import fr.unice.polytech.foodDelivery.service.RestaurantService;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CustomerAPI implements HttpHandler {
    CustomerService customerService = new CustomerService();
    RestaurantService restaurantService = new RestaurantService();
    OrderService orderService = new OrderService();
    ObjectMapper mapper = new ObjectMapper();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String[] pathSegments = path.split("/");

        int len = pathSegments.length;
        String response = "{\"message\": \"Bienvenue sur l'API Customer!\"}";
        InputStream is = exchange.getRequestBody();

        switch (method) {

            case "GET":
                if (len == 2) {
                    response = mapper.writeValueAsString(customerService.findAll());
                } else if (len == 3) {
                    UUID id = UUID.fromString(pathSegments[2]);
                    response = mapper.writeValueAsString(customerService.findById(id));
                } else if (len == 4 && pathSegments[3].equals("restaurants")) {
                    response = mapper.writeValueAsString(restaurantService.findAll());
                } else if (len == 6 && pathSegments[3].equals("restaurants") && pathSegments[5].equals("menu")) {
                    UUID restaurantId = UUID.fromString(pathSegments[4]);
                    Menu menu = restaurantService.getRestaurantMenu(restaurantId);
                    response = mapper.writeValueAsString(menu);
                } else if (len == 4 && pathSegments[3].equals("orders")) {
                    UUID customerId = UUID.fromString(pathSegments[2]);
                    List<Order> orders = orderService.getCustomerHistory(customerId);
                    response = mapper.writeValueAsString(orders);
                } else if (len == 5 && pathSegments[3].equals("orders")) {
                    UUID customerId = UUID.fromString(pathSegments[2]);
                    UUID orderId = UUID.fromString(pathSegments[4]);
                    List<Order> orders = orderService.getCustomerHistory(customerId);
                    Order order = orders.stream()
                            .filter(o -> o.getOrderId().equals(orderId))
                            .findFirst()
                            .orElse(null);
                    response = mapper.writeValueAsString(order);
                }
                break;

            case "POST":
                if (len == 2) {
                    CustomerAccount newCustomer = mapper.readValue(is, CustomerAccount.class);
                    customerService.registerCustomer(newCustomer);
                    response = "{\"message\": \"Client enregistré avec succès!\"}";
                } else if (len == 4 && pathSegments[3].equals("orders")) {
                    UUID customerId = UUID.fromString(pathSegments[2]);
                    Map<String, Object> orderData = mapper.readValue(is, Map.class);

                    CustomerAccount customer = customerService.findById(customerId);
                    UUID restaurantId = UUID.fromString((String) orderData.get("restaurantId"));
                    RestaurantAccount restaurant = restaurantService.findById(restaurantId);

                    Order order = orderService.createOrder(customer, restaurant);

                    List<String> mealIds = (List<String>) orderData.get("mealIds");
                    if (mealIds != null) {
                        for (String mealIdStr : mealIds) {
                            UUID mealId = UUID.fromString(mealIdStr);
                            orderService.addMealToOrder(order, restaurantService.getMealById(restaurantId, mealId));
                        }
                    }

                    response = mapper.writeValueAsString(order);
                }
                break;
        }

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }
}

