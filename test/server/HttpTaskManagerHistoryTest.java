package server;

import com.google.gson.*;
import enums.TaskStatus;
import manager.Managers;
import manager.TaskManager;
import models.Epic;
import models.SubTask;
import models.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.adapters.DurationAdapter;
import server.adapters.LocalDateTimeAdapter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpTaskManagerHistoryTest {
    TaskManager manager = Managers.getDefault();
    HttpTaskServer taskServer = new HttpTaskServer(manager, port);
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .setPrettyPrinting()
            .create();
    private static final int port = 8089;
    private static final URI urlEpic = URI.create(String.format("http://localhost:%s/epics", port));
    private static final URI urlTask = URI.create(String.format("http://localhost:%s/tasks", port));
    private static final URI urlSubTask = URI.create(String.format("http://localhost:%s/subtasks", port));
    private static final URI urlHistory = URI.create(String.format("http://localhost:%s/history", port));
    static Task task1 = new Task(1L, "Test 2", "Testing task 2",
            TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
    static Epic epic1 = new Epic(1L, "Испечь торт 1", "Испечь торт Наполеон 1");
    static SubTask subTask1 = new SubTask(epic1.getId(), "Найти рецепт 1",
            "Выполнить поиск видео рецепта 1", TaskStatus.NEW,
            LocalDateTime.of(2024, 10, 21, 19, 0), Duration.ofMinutes(60));
    static SubTask subTask2 = new SubTask(epic1.getId(), "Найти рецепт 2",
            "Выполнить поиск видео рецепта 2", TaskStatus.IN_PROGRESS,
            LocalDateTime.of(2024, 10, 21, 20, 0), Duration.ofMinutes(60));

    public HttpTaskManagerHistoryTest() throws IOException {
    }

    @BeforeEach
    public void setUp() throws IOException {
        manager.clearTasks();
        manager.clearSubTasks();
        manager.clearEpics();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testGetHistory_ShouldReturn200AndListHistory() throws IOException, InterruptedException {
        HttpResponse<String> response1 = httpClient("POST", urlTask, gson.toJson(task1));
        HttpResponse<String> response2 = httpClient("POST", urlEpic, gson.toJson(epic1));
        HttpResponse<String> response3 = httpClient("POST", urlSubTask, gson.toJson(subTask1));
        HttpResponse<String> response4 = httpClient("POST", urlSubTask, gson.toJson(subTask2));
        HttpResponse<String> response5 = httpClient("GET",
                URI.create(String.format("http://localhost:%s/tasks" + "/" + 1L, port)),
                null);

        HttpResponse<String> responseHistory = httpClient("GET",
                urlHistory,
                null);

        assertEquals(200, responseHistory.statusCode());
        JsonElement jsonElement = JsonParser.parseString(responseHistory.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        assertEquals(1, jsonArray.size());
        assertEquals(true, jsonArray.get(0).toString().contains("Testing task 2"));
    }

    @Test
    public void testGetHistory_ShouldReturn200AndEmptyHistory() throws IOException, InterruptedException {
        HttpResponse<String> response1 = httpClient("POST", urlTask, gson.toJson(task1));
        HttpResponse<String> response2 = httpClient("POST", urlEpic, gson.toJson(epic1));
        HttpResponse<String> response3 = httpClient("POST", urlSubTask, gson.toJson(subTask1));
        HttpResponse<String> response4 = httpClient("POST", urlSubTask, gson.toJson(subTask2));

        HttpResponse<String> responseHistory = httpClient("GET",
                urlHistory,
                null);

        assertEquals(200, responseHistory.statusCode());
        JsonElement jsonElement = JsonParser.parseString(responseHistory.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        assertEquals(0, jsonArray.size());
    }

    public HttpResponse<String> httpClient(String method, URI uri, String body) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        if (method.equals("GET")) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } else if (method.equals("POST")) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } else if (method.equals("DELETE")) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .DELETE()
                    .build();
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } else {
            return null;
        }
    }
}