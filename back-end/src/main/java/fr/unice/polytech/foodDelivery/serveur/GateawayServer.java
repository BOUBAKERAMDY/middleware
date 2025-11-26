import fr.unice.polytech.foodDelivery.API.GateawayAPI;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public static void main(String[] args) throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
    server.createContext("/api", new GateawayAPI());
    server.setExecutor(null);
    server.start();
    System.out.println("Serveur démarré sur http://localhost:" + 8080);

}
