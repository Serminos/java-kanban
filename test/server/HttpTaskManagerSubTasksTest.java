package server;

import com.google.gson.*;
import enums.TaskStatus;
import manager.Managers;
import manager.TaskManager;
import models.Epic;
import models.SubTask;
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

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerSubTasksTest {
    TaskManager manager = Managers.getDefault();
    HttpTaskServer taskServer = new HttpTaskServer(manager, port);
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .setPrettyPrinting()
            .create();
    private static final int port = 8089;
    private static final URI urlEpic = URI.create(String.format("http://localhost:%s/epics", port));
    private static final URI urlSubTask = URI.create(String.format("http://localhost:%s/subtasks", port));
    static Epic epic1 = new Epic(1L, "Испечь торт 1", "Испечь торт Наполеон 1");
    static SubTask subTask1 = new SubTask(epic1.getId(), "Найти рецепт 1",
            "Выполнить поиск видео рецепта 1", TaskStatus.NEW,
            LocalDateTime.of(2024, 10, 21, 19, 0), Duration.ofMinutes(60));
    static SubTask subTask2 = new SubTask(epic1.getId(), "Найти рецепт 2",
            "Выполнить поиск видео рецепта 2", TaskStatus.IN_PROGRESS,
            LocalDateTime.of(2024, 10, 21, 20, 0), Duration.ofMinutes(60));

    public HttpTaskManagerSubTasksTest() throws IOException {
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
    public void testCreateSubTask_ShouldReturn201() throws IOException, InterruptedException {
        HttpResponse<String> response1 = httpClient("POST", urlEpic, gson.toJson(epic1));
        HttpResponse<String> response2 = httpClient("POST", urlSubTask, gson.toJson(subTask1));

        assertEquals(201, response2.statusCode());
        JsonObject jsonObject = gson.fromJson(response2.body(), JsonObject.class);
        assertEquals(true, jsonObject.has("id"));
        assertEquals(1, jsonObject.get("id").getAsLong());
        List<SubTask> tasksFromManager = manager.getSubTasks();
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество подзадач");
        assertEquals("Найти рецепт 1", tasksFromManager.get(0).getName(), "Некорректное имя подзадачи");
    }

    @Test
    public void testUpdateSubTask_ShouldReturn201() throws IOException, InterruptedException {
        HttpResponse<String> response1 = httpClient("POST", urlEpic, gson.toJson(epic1));
        HttpResponse<String> response2 = httpClient("POST", urlSubTask, gson.toJson(subTask1));
        SubTask subTask3 = new SubTask(1L, epic1.getId(), "Найти рецепт 2",
                "Выполнить поиск видео рецепта 2", TaskStatus.IN_PROGRESS,
                LocalDateTime.of(2024, 10, 21, 20, 0), Duration.ofMinutes(60));
        HttpResponse<String> response3 = httpClient("POST", urlSubTask, gson.toJson(subTask3));

        assertEquals(201, response3.statusCode());
        JsonObject jsonObject = gson.fromJson(response3.body(), JsonObject.class);
        assertEquals(true, jsonObject.has("id"));
        assertEquals(1, jsonObject.get("id").getAsLong());
        List<SubTask> tasksFromManager = manager.getSubTasks();
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Найти рецепт 2", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
        assertEquals("Выполнить поиск видео рецепта 2", tasksFromManager.get(0).getDescription(), "Некорректное описание задачи");
        assertEquals(TaskStatus.IN_PROGRESS, tasksFromManager.get(0).getStatus(), "Некорректный статус задачи");
        assertEquals(LocalDateTime.of(2024, 10, 21, 20, 0),
                tasksFromManager.get(0).getStartTime(), "Некорректное время начала задачи");
        assertEquals(LocalDateTime.of(2024, 10, 21, 20, 0).plusMinutes(60),
                tasksFromManager.get(0).getEndTime(), "Некорректный время окончания задачи");
    }

    @Test
    public void testCreateSubTask_ShouldReturn406IfTermIntercept() throws IOException, InterruptedException {
        HttpResponse<String> response1 = httpClient("POST", urlEpic, gson.toJson(epic1));
        HttpResponse<String> response2 = httpClient("POST", urlSubTask, gson.toJson(subTask1));
        SubTask subTask3 = new SubTask(2L, 1L, "Найти рецепт 2",
                "Выполнить поиск видео рецепта 2", TaskStatus.IN_PROGRESS,
                LocalDateTime.of(2024, 10, 21, 19, 30), Duration.ofMinutes(60));
        HttpResponse<String> response3 = httpClient("POST", urlSubTask, gson.toJson(subTask3));

        assertEquals(406, response3.statusCode());
    }

    @Test
    public void testGetSubTaskById_ShouldReturn200() throws IOException, InterruptedException {
        HttpResponse<String> response1 = httpClient("POST", urlEpic, gson.toJson(epic1));
        HttpResponse<String> response2 = httpClient("POST", urlSubTask, gson.toJson(subTask1));
        HttpResponse<String> response4 = httpClient("GET",
                URI.create(String.format("http://localhost:%s/subtasks" + "/" + 1L, port)),
                null);

        assertEquals(200, response4.statusCode());
        JsonElement jsonElement = JsonParser.parseString(response4.body());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        assertEquals(true, jsonObject.has("id"));
        assertEquals(1L, jsonObject.get("id").getAsLong());
        assertEquals("Найти рецепт 1", jsonObject.get("name").getAsString());
    }

    @Test
    public void testGetSubTaskById_ShouldReturn404IfWrongId() throws IOException, InterruptedException {
        HttpResponse<String> response1 = httpClient("POST", urlEpic, gson.toJson(epic1));
        HttpResponse<String> response2 = httpClient("POST", urlSubTask, gson.toJson(subTask1));
        HttpResponse<String> response4 = httpClient("GET",
                URI.create(String.format("http://localhost:%s/subtasks" + "/" + 100L, port)),
                null);
        assertEquals(404, response4.statusCode());
    }

    @Test
    public void testDeleteSubTaskById_ShouldReturn200() throws IOException, InterruptedException {
        HttpResponse<String> response1 = httpClient("POST", urlEpic, gson.toJson(epic1));
        HttpResponse<String> response2 = httpClient("POST", urlSubTask, gson.toJson(subTask1));
        HttpResponse<String> response3 = httpClient("POST", urlSubTask, gson.toJson(subTask2));

        HttpResponse<String> response4 = httpClient("DELETE",
                URI.create(String.format("http://localhost:%s/subtasks" + "/" + 1L, port)),
                null);

        assertEquals(200, response4.statusCode());
        List<SubTask> tasksFromManager = manager.getSubTasks();
        assertEquals(1, tasksFromManager.size());
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