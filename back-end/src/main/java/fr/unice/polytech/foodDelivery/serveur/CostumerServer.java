package fr.unice.polytech.foodDelivery.serveur;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import fr.unice.polytech.foodDelivery.API.CustomerAPI;

public class CostumerServer {
    public static void main(String[] args) throws IOException {
        int port = 8081;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", new CustomerAPI());

        server.setExecutor(null);
        server.start();

        System.out.println("Serveur Customer démarré sur http://localhost:" + port);
    }
}
