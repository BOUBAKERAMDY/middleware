package fr.unice.polytech.foodDelivery.serveur;

import com.sun.net.httpserver.HttpServer;
import fr.unice.polytech.foodDelivery.API.RestaurantAPI;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ServeurRestaurant {
    public static void main(String[] args) throws IOException {
        int port = 8082;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/restaurant", new RestaurantAPI());

        server.setExecutor(null);
        server.start();

        System.out.println("Serveur démarré sur http://localhost:" + port);
    }
}
