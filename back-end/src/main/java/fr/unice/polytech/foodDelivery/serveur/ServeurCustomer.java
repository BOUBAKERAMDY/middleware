package fr.unice.polytech.foodDelivery.serveur;

import com.sun.net.httpserver.HttpServer;
import fr.unice.polytech.foodDelivery.API.CustomerAPI;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ServeurCustomer {
    public static void main(String[] args) throws IOException {
        int port = 8081;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/customer", new CustomerAPI());

        server.setExecutor(null);
        server.start();

        System.out.println("Serveur démarré sur http://localhost:" + port);
    }
}
