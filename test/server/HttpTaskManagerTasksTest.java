package server;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import enums.TaskStatus;
import manager.InMemoryTaskManager;
import manager.Managers;
import manager.TaskManager;
import models.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.adapters.DurationAdapter;
import server.adapters.LocalDateTimeAdapter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerTasksTest {
    TaskManager manager = Managers.getDefault();
    HttpTaskServer taskServer = new HttpTaskServer(manager, port);
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .setPrettyPrinting()
            .create();
    private static final int port = 8089;
    private static final URI urlTask = URI.create(String.format("http://localhost:%s/tasks", port));
    Task task1 = new Task("Test 1", "Testing task 1",
            TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
    Task task2 = new Task("Test 2", "Testing task 2",
            TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));

    public HttpTaskManagerTasksTest() throws IOException {
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
    public void testCreateTask() throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient("POST", urlTask, gson.toJson(task1));

        assertEquals(201, response.statusCode());
        JsonObject jsonObject = gson.fromJson(response.body(), JsonObject.class);
        assertEquals(true, jsonObject.has("id"));
        assertEquals(1, jsonObject.get("id").getAsLong());
        List<Task> tasksFromManager = manager.getTasks();
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 1", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void testUpdateTask() throws IOException, InterruptedException {
        HttpResponse<String> response1 = httpClient("POST", urlTask, gson.toJson(task1));
        Task task3 = new Task(1L, "Test 3", "Testing task 3",
                TaskStatus.IN_PROGRESS, LocalDateTime.now(), Duration.ofMinutes(5));

        HttpResponse<String> response3 = httpClient("POST", urlTask, gson.toJson(task3));

        assertEquals(201, response3.statusCode());
        JsonObject jsonObject = gson.fromJson(response3.body(), JsonObject.class);
        assertEquals(true, jsonObject.has("id"));
        assertEquals(1, jsonObject.get("id").getAsLong());
        List<Task> tasksFromManager = manager.getTasks();
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 3", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
        assertEquals("Testing task 3", tasksFromManager.get(0).getDescription(), "Некорректное описание задачи");
        assertEquals(TaskStatus.IN_PROGRESS, tasksFromManager.get(0).getStatus(), "Некорректный статус задачи");
    }

    @Test
    public void testGetTaskByIdShouldReturnCode200AndEntity() throws IOException, InterruptedException {
        HttpResponse<String> response3 = httpClient("POST", urlTask, gson.toJson(task1));

        HttpResponse<String> responseTask = httpClient("GET",
                URI.create(String.format("http://localhost:%s/tasks" + "/" + 1L, port)),
                null);

        assertEquals(200, responseTask.statusCode());
        JsonElement jsonElement = JsonParser.parseString(responseTask.body());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        assertEquals(true, jsonObject.has("id"));
        assertEquals("Test 1", jsonObject.get("name").getAsString());
    }

    @Test
    public void testGetTasksByWrongIdShouldReturnCode404() throws IOException, InterruptedException {
        HttpResponse<String> response3 = httpClient("POST", urlTask, gson.toJson(task1));

        HttpResponse<String> responseTask = httpClient("GET",
                URI.create(String.format("http://localhost:%s/tasks" + "/" + 100L, port)),
                null);

        assertEquals(404, responseTask.statusCode());
    }

    @Test
    public void testGetTasksShouldReturnCode406AndFirstEntity() throws IOException, InterruptedException {
        Task task2 = new Task(1L, "Test 2", "Testing task 2",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        Task task3 = new Task(2L, "Test 3", "Testing task 3",
                TaskStatus.IN_PROGRESS, LocalDateTime.now(), Duration.ofMinutes(5));

        HttpResponse<String> response2 = httpClient("POST", urlTask, gson.toJson(task2));
        HttpResponse<String> response3 = httpClient("POST", urlTask, gson.toJson(task3));
        HttpResponse<String> responseTasks = httpClient("GET", urlTask, null);

        assertEquals(201, response2.statusCode());
        assertEquals(406, response3.statusCode(), "Задача не должна была добавиться. Наложение времени");
        assertEquals(200, responseTasks.statusCode());
        JsonElement jsonElement = JsonParser.parseString(responseTasks.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        assertEquals(1, jsonArray.size());
        assertEquals(true, jsonArray.get(0).toString().contains("Test 2"));
    }

    @Test
    public void testGetTasksShouldReturnCode200And2Entity() throws IOException, InterruptedException {
        Task task2 = new Task(1L, "Test 2", "Testing task 2",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        Task task3 = new Task(2L, "Test 3", "Testing task 3",
                TaskStatus.IN_PROGRESS, LocalDateTime.now().plusMinutes(100), Duration.ofMinutes(5));

        HttpResponse<String> response2 = httpClient("POST", urlTask, gson.toJson(task2));
        HttpResponse<String> response3 = httpClient("POST", urlTask, gson.toJson(task3));
        HttpResponse<String> responseTasks = httpClient("GET", urlTask, null);

        assertEquals(200, responseTasks.statusCode());
        JsonElement jsonElement = JsonParser.parseString(responseTasks.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        assertEquals(2, jsonArray.size());
    }

    @Test
    public void testGetTasksReturnCode200AndEmptyList() throws IOException, InterruptedException {
        HttpResponse<String> responseTasks = httpClient("GET", urlTask, null);

        assertEquals(200, responseTasks.statusCode());
        JsonElement jsonElement = JsonParser.parseString(responseTasks.body());
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