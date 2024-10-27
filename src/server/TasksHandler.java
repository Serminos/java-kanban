package server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.TaskValidationException;
import manager.TaskManager;
import models.Task;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public TasksHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String[] path = exchange.getRequestURI().getPath().split("/");
        if (method.equals("GET") && path.length == 2) {
            getTasks(exchange);
        } else if (method.equals("GET") && path.length == 3) {
            Long id = Long.parseLong(path[2]);
            getTask(id, exchange);
        } else if (method.equals("POST") && path.length == 2) {
            Task task = parseTask(exchange);
            postTask(task, exchange);
        } else if (method.equals("DELETE") && path.length == 3) {
            Long id = Long.parseLong(path[2]);
            deleteTask(id, exchange);
        } else {
            sendResponse(exchange, "Bad Request", 400);
        }
    }

    private void postTask(Task task, HttpExchange exchange) {
        try {
            Long id = (taskManager.update(task)) ? task.getId() : taskManager.create(task);
            sendResponse(exchange, gson.toJson(Map.of("id", id)), 201);
        } catch (TaskValidationException e) {
            sendResponse(exchange, gson.toJson(e.getMessage()),406);
        }
    }

    private void deleteTask(Long id, HttpExchange exchange) {
        taskManager.removeTask(id);
        sendResponse(exchange, gson.toJson(Map.of("id", id)), 200);
    }

    private void getTask(Long id, HttpExchange exchange) {
        Task task = taskManager.getTask(id);
        if (task == null) {
            sendResponse(exchange, gson.toJson((Object) null), 404);
        }
        sendResponse(exchange, gson.toJson(task), 200);
    }

    private void getTasks(HttpExchange exchange) {
        List<Task> allTasks = taskManager.getTasks();
        if (allTasks.isEmpty()) {
            sendResponse(exchange, gson.toJson(allTasks), 200);

        }
        String jsonAllTasks = gson.toJson(allTasks);
        sendResponse(exchange, jsonAllTasks, 200);
    }

    private Task parseTask(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        Task task = gson.fromJson(requestBody, Task.class);

        JsonObject jsonObject = JsonParser.parseString(requestBody).getAsJsonObject();
        if (jsonObject.has("id")) {
            Long id = jsonObject.get("id").getAsLong();
            task.setId(id);
        }
        return task;
    }
}
