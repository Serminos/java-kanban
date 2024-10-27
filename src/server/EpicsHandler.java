package server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.TaskValidationException;
import manager.TaskManager;
import models.Epic;
import models.SubTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager taskManager;

    public EpicsHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String[] path = exchange.getRequestURI().getPath().split("/");
        long id;
        if (method.equals("GET") && path.length == 2) {
            getEpics(exchange);
        } else if (method.equals("GET") && path.length == 3) {
            id = Long.parseLong(path[2]);
            getEpic(id, exchange);
        } else if (method.equals("GET") && path.length == 4) {
            id = Long.parseLong(path[2]);
            getEpicSubTasks(id, exchange);
        } else if (method.equals("POST") && path.length == 2) {
            Epic epic = parseEpic(exchange);
            postEpic(epic, exchange);
        } else if (method.equals("DELETE") && path.length == 3) {
            id = Long.parseLong(path[2]);
            deleteEpic(id, exchange);
        } else {
            sendResponse(exchange, "Bad Request", 400);
        }
    }

    private void postEpic(Epic epic, HttpExchange exchange) {
        try {
            Long id = taskManager.update(epic) ? epic.getId() : taskManager.create(epic);
            sendResponse(exchange, gson.toJson(Map.of("id", id)), 201);
        } catch (TaskValidationException e) {
            sendResponse(exchange, gson.toJson(e.getMessage()), 406);
        }
    }

    private void deleteEpic(Long id, HttpExchange exchange) {
        taskManager.removeEpic(id);
        sendResponse(exchange, gson.toJson(Map.of("id", id)), 200);
    }

    private void getEpic(Long id, HttpExchange exchange) {
        Epic epic = taskManager.getEpic(id);
        if (epic == null) {
            sendResponse(exchange, gson.toJson((Object) null), 404);
        }
        sendResponse(exchange, gson.toJson(epic), 200);
    }

    private void getEpicSubTasks(Long id, HttpExchange exchange) {
        Epic epic = taskManager.getEpic(id);
        if (epic == null) {
            sendResponse(exchange, gson.toJson((Object) null), 404);
        }
        List<Long> subtasksId = epic.getSubTaskIds();
        if (subtasksId.isEmpty()) {
            sendResponse(exchange, gson.toJson(subtasksId), 404);
        }
        List<SubTask> subTasks = new ArrayList<>();
        for (Long sub : subtasksId) {
            subTasks.add(taskManager.getSubTask(sub));
        }
        sendResponse(exchange, gson.toJson(subTasks), 200);
    }

    private void getEpics(HttpExchange exchange) {
        List<Epic> allEpics = taskManager.getEpics();
        if (allEpics == null) {
            sendResponse(exchange, gson.toJson((Object) null), 404);
        }
        sendResponse(exchange, gson.toJson(allEpics), 200);
    }

    private Epic parseEpic(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes());
        Epic epic = gson.fromJson(body, Epic.class);

        JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
        if (jsonObject.has("id")) {
            Long id = jsonObject.get("id").getAsLong();
            epic.setId(id);
        }
        return epic;
    }
}
