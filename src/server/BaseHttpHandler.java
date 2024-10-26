package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import server.adapters.DurationAdapter;
import server.adapters.LocalDateTimeAdapter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public class BaseHttpHandler {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .setPrettyPrinting()
            .create();

    void sendText(HttpExchange exchange, String text, int responseCode) throws IOException {
        try (OutputStream os = exchange.getResponseBody()) {
            byte[] response = text.getBytes(DEFAULT_CHARSET);
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(responseCode, response.length);
            os.write(response);
        }
    }

    void sendNotFound(HttpExchange exchange, String message) throws IOException {
        try (OutputStream os = exchange.getResponseBody()) {
            byte[] response = message.getBytes(DEFAULT_CHARSET);
            exchange.sendResponseHeaders(404, 0);
            os.write(response);
        }
    }

    void sendBadRequest(HttpExchange exchange) throws IOException {
        try (OutputStream os = exchange.getResponseBody()) {
            byte[] response = "Bad Request".getBytes(DEFAULT_CHARSET);
            exchange.sendResponseHeaders(400, 0);
            os.write(response);
        }
    }

    void sendHasInteractions(HttpExchange exchange, String message) {
        try (OutputStream os = exchange.getResponseBody()) {
            byte[] response = message.getBytes(DEFAULT_CHARSET);
            exchange.sendResponseHeaders(406, 0);
            os.write(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void sendInternalServerError(HttpExchange exchange, String message) {
        try (OutputStream os = exchange.getResponseBody()) {
            byte[] response = message.getBytes(DEFAULT_CHARSET);
            exchange.sendResponseHeaders(500, 0);
            os.write(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
