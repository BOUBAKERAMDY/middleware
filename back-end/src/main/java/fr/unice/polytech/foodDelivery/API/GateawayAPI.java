package fr.unice.polytech.foodDelivery.API;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GateawayAPI implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String targetUrl;

        if (path.startsWith("/api/customers")){
            targetUrl = "http://localhost:8081" + path.replace("/api/customers", "");
        }

        else if (path.startsWith("/api/restaurants")){
            targetUrl = "http://localhost:8082" + path.replace("/api/restaurants","");
        }
        else {
            String error = "{\"error\":\"Unknown endpoint\"}";
            exchange.sendResponseHeaders(404, error.length());
            exchange.getResponseBody().write(error.getBytes());
            exchange.close();
            return;
        }

        URL url = new URL(targetUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        if (method.equals("POST") || method.equals("PUT") || method.equals("PATCH")) {
            conn.setDoOutput(true);
            exchange.getRequestBody().transferTo(conn.getOutputStream());
        }

        int status = conn.getResponseCode();
        InputStream serviceResponseStream = (status >= 400)
                ? conn.getErrorStream()
                : conn.getInputStream();

        String response = "";
        if (serviceResponseStream != null) {
            response = new String(serviceResponseStream.readAllBytes());
        }

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, response.getBytes().length);

        if (!response.isEmpty()) {
            exchange.getResponseBody().write(response.getBytes());
        }

        exchange.close();

    }
}
