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

    void sendResponse(HttpExchange exchange, String text, int responseCode) {
        try (OutputStream os = exchange.getResponseBody()) {
            byte[] response = text.getBytes(DEFAULT_CHARSET);
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(responseCode, response.length);
            os.write(response);
        } catch (IOException e) {
            try (OutputStream os = exchange.getResponseBody()) {
                byte[] response = e.getMessage().getBytes(DEFAULT_CHARSET);
                exchange.sendResponseHeaders(500, e.getMessage().length());
                os.write(response);
            } catch (IOException ex) {
            }
        }
    }
}
