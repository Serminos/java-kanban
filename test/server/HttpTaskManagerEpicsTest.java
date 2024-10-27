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

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerEpicsTest {
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
    static Epic epic2 = new Epic(2L, "Испечь торт 2", "Испечь торт Наполеон 2");

    public HttpTaskManagerEpicsTest() throws IOException {
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
    public void testCreateEpic() throws IOException, InterruptedException {

        HttpResponse<String> response = httpClient("POST", urlEpic, gson.toJson(epic1));

        assertEquals(201, response.statusCode());
        JsonObject jsonObject = gson.fromJson(response.body(), JsonObject.class);
        assertEquals(true, jsonObject.has("id"));
        assertEquals(1, jsonObject.get("id").getAsLong());
        List<Epic> tasksFromManager = manager.getEpics();
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Испечь торт 1", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void testUpdateEpic() throws IOException, InterruptedException {
        Epic epicToUpdate = new Epic(epic1.getId(), epic2.getName(), epic2.getDescription());
        HttpResponse<String> response1 = httpClient("POST", urlEpic, gson.toJson(epic1));
        HttpResponse<String> response2 = httpClient("POST", urlEpic, gson.toJson(epicToUpdate));

        assertEquals(201, response2.statusCode());
        JsonObject jsonObject = gson.fromJson(response2.body(), JsonObject.class);
        assertEquals(true, jsonObject.has("id"));
        assertEquals(1, jsonObject.get("id").getAsLong());
        List<Epic> tasksFromManager = manager.getEpics();
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Испечь торт 2", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
        assertEquals("Испечь торт Наполеон 2", tasksFromManager.get(0).getDescription(), "Некорректное описание задачи");
        assertEquals(null, tasksFromManager.get(0).getStatus(), "Некорректный статус задачи");
        assertEquals(null, tasksFromManager.get(0).getStartTime(), "Некорректное время начала задачи");
        assertEquals(null, tasksFromManager.get(0).getEndTime(), "Некорректный время окончания задачи");
    }

    @Test
    public void testGetEpicByIdShouldReturnCode200AndEntity() throws IOException, InterruptedException {
        HttpResponse<String> response1 = httpClient("POST", urlEpic, gson.toJson(epic1));
        HttpResponse<String> responseTask = httpClient("GET",
                URI.create(String.format("http://localhost:%s/epics" + "/" + 1L, port)), null);

        assertEquals(200, responseTask.statusCode());
        JsonElement jsonElement = JsonParser.parseString(responseTask.body());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        assertEquals(true, jsonObject.has("id"));
        assertEquals("Испечь торт 1", jsonObject.get("name").getAsString());
    }

    @Test
    public void testGetEpicByWrongIdShouldReturnCode404() throws IOException, InterruptedException {
        HttpResponse<String> response1 = httpClient("POST", urlEpic, gson.toJson(epic1));

        HttpResponse<String> responseEpic = httpClient("GET",
                URI.create(String.format("http://localhost:%s/epics" + "/" + 100L, port)), null);

        assertEquals(404, responseEpic.statusCode());
    }

    @Test
    public void testGetEpicsShouldReturnCode200And2Entity() throws IOException, InterruptedException {
        HttpResponse<String> response1 = httpClient("POST", urlEpic, gson.toJson(epic1));
        HttpResponse<String> response2 = httpClient("POST", urlEpic, gson.toJson(epic2));

        HttpResponse<String> responseTasks = httpClient("GET", urlEpic, null);

        assertEquals(200, responseTasks.statusCode());
        JsonElement jsonElement = JsonParser.parseString(responseTasks.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        assertEquals(2, jsonArray.size());
    }

    @Test
    public void testGetEpicsReturnCode200AndEmptyList() throws IOException, InterruptedException {
        HttpResponse<String> responseTasks = httpClient("GET", urlEpic, null);

        assertEquals(200, responseTasks.statusCode());
        JsonElement jsonElement = JsonParser.parseString(responseTasks.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        assertEquals(0, jsonArray.size());
    }

    @Test
    public void testUpdateEpicWithSubTask() throws IOException, InterruptedException {
        SubTask subtask1 = new SubTask(1L, "Найти рецепт",
                "Выполнить поиск видео рецепта", TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 21, 19, 0), Duration.ofMinutes(60));
        SubTask subtask2 = new SubTask(1L, "Найти рецепт",
                "Выполнить поиск видео рецепта", TaskStatus.IN_PROGRESS,
                LocalDateTime.of(2024, 10, 21, 20, 0), Duration.ofMinutes(60));
        HttpResponse<String> response1 = httpClient("POST", urlEpic, gson.toJson(epic1));
        HttpResponse<String> response2 = httpClient("POST", urlSubTask, gson.toJson(subtask1));
        HttpResponse<String> response3 = httpClient("POST", urlSubTask, gson.toJson(subtask2));

        assertEquals(201, response1.statusCode());
        assertEquals(201, response2.statusCode());
        assertEquals(201, response3.statusCode());
        JsonObject jsonObject = gson.fromJson(response1.body(), JsonObject.class);
        assertEquals(true, jsonObject.has("id"));
        assertEquals(1, jsonObject.get("id").getAsLong());
        List<Epic> tasksFromManager = manager.getEpics();
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Испечь торт 1", tasksFromManager.get(0).getName(), "Испечь торт Наполеон 1");
        assertEquals("Испечь торт Наполеон 1", tasksFromManager.get(0).getDescription(),
                "Некорректное описание задачи");
        assertEquals(TaskStatus.IN_PROGRESS, tasksFromManager.get(0).getStatus(), "Некорректный статус задачи");
        assertEquals(LocalDateTime.of(2024, 10, 21, 19, 0),
                tasksFromManager.get(0).getStartTime(), "Некорректное время начала задачи");
        assertEquals(LocalDateTime.of(2024, 10, 21, 21, 0),
                tasksFromManager.get(0).getEndTime(), "Некорректный время окончания задачи");
    }

    @Test
    public void testGetEpicSubTasks_ShouldReturnCode200AndSubTasks() throws IOException, InterruptedException {
        SubTask subtask1 = new SubTask(epic1.getId(), "Найти рецепт 1",
                "Выполнить поиск видео рецепта 1", TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 21, 19, 0), Duration.ofMinutes(60));
        SubTask subtask2 = new SubTask(epic1.getId(), "Найти рецепт 2",
                "Выполнить поиск видео рецепта 2", TaskStatus.IN_PROGRESS,
                LocalDateTime.of(2024, 10, 21, 20, 0), Duration.ofMinutes(60));
        HttpResponse<String> response1 = httpClient("POST", urlEpic, gson.toJson(epic1));
        HttpResponse<String> response2 = httpClient("POST", urlSubTask, gson.toJson(subtask1));
        HttpResponse<String> response3 = httpClient("POST", urlSubTask, gson.toJson(subtask2));

        HttpResponse<String> responseSubTasks = httpClient("GET",
                URI.create(String.format("http://localhost:%s/epics/%s/subtasks", port, epic1.getId())),
                gson.toJson(subtask2));

        assertEquals(200, responseSubTasks.statusCode());
        JsonElement jsonElement = JsonParser.parseString(responseSubTasks.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        assertEquals(2, jsonArray.size());
        assertEquals(true, jsonArray.get(0).toString().contains("Найти рецепт 1"));
        assertEquals(true, jsonArray.get(0).toString().contains("Выполнить поиск видео рецепта 1"));
        assertEquals(true, jsonArray.get(1).toString().contains("Найти рецепт 2"));
        assertEquals(true, jsonArray.get(1).toString().contains("Выполнить поиск видео рецепта 2"));

        List<Epic> epicsFromManager = manager.getEpics();
        assertEquals(1, epicsFromManager.size());
        assertEquals(LocalDateTime.of(2024, 10, 21, 19, 0),
                epicsFromManager.get(0).getStartTime());
        assertEquals(LocalDateTime.of(2024, 10, 21, 21, 0),
                epicsFromManager.get(0).getEndTime());
    }

    @Test
    public void testGetEpicSubTasks_ShouldReturnCode404IfEpicIdWrong() throws IOException, InterruptedException {
        SubTask subtask1 = new SubTask(epic1.getId(), "Найти рецепт 1",
                "Выполнить поиск видео рецепта 1", TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 21, 19, 0), Duration.ofMinutes(60));
        SubTask subtask2 = new SubTask(epic1.getId(), "Найти рецепт 2",
                "Выполнить поиск видео рецепта 2", TaskStatus.IN_PROGRESS,
                LocalDateTime.of(2024, 10, 21, 20, 0), Duration.ofMinutes(60));
        HttpResponse<String> response1 = httpClient("POST", urlEpic, gson.toJson(epic1));
        HttpResponse<String> response2 = httpClient("POST", urlSubTask, gson.toJson(subtask1));
        HttpResponse<String> response3 = httpClient("POST", urlSubTask, gson.toJson(subtask2));

        HttpResponse<String> responseSubTasks = httpClient("GET",
                URI.create(String.format("http://localhost:%s/epics/%s/subtasks", port, 100L)),
                gson.toJson(subtask2));

        assertEquals(404, responseSubTasks.statusCode());
        assertTrue(responseSubTasks.body().equals("null"));
    }

    @Test
    public void testDeleteEpic_ShouldReturnCode200() throws IOException, InterruptedException {
        SubTask subTask1 = new SubTask(epic1.getId(), "Найти рецепт 1",
                "Выполнить поиск видео рецепта 1", TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 21, 19, 0), Duration.ofMinutes(60));
        SubTask subTask2 = new SubTask(epic1.getId(), "Найти рецепт 2",
                "Выполнить поиск видео рецепта 2", TaskStatus.IN_PROGRESS,
                LocalDateTime.of(2024, 10, 21, 20, 0), Duration.ofMinutes(60));
        HttpResponse<String> response1 = httpClient("POST", urlEpic, gson.toJson(epic1));
        HttpResponse<String> response2 = httpClient("POST", urlSubTask, gson.toJson(subTask1));
        HttpResponse<String> response3 = httpClient("POST", urlSubTask, gson.toJson(subTask2));

        HttpResponse<String> responseSubTasks = httpClient("DELETE",
                URI.create(String.format("http://localhost:%s/epics/%s", port, epic1.getId())),
                null);

        assertEquals(200, responseSubTasks.statusCode());
        JsonObject jsonObject = gson.fromJson(responseSubTasks.body(), JsonObject.class);
        assertEquals(true, jsonObject.has("id"));
        assertEquals(epic1.getId(), jsonObject.get("id").getAsLong());
        List<Epic> epicsFromManager = manager.getEpics();
        assertEquals(0, epicsFromManager.size());
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