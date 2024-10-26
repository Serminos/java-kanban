package server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.TaskValidationException;
import manager.TaskManager;
import models.Task;

import java.io.IOException;
import java.util.HashMap;
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
            sendBadRequest(exchange);
        }
    }

    private void postTask(Task task, HttpExchange exchange) {
        try {
            Long id = (taskManager.update(task)) ? task.getId() : taskManager.create(task);
            sendText(exchange, gson.toJson(Map.of("id", id)), 201);
        } catch (TaskValidationException e) {
            sendHasInteractions(exchange, gson.toJson(e.getMessage()));
        } catch (IOException e) {
            sendInternalServerError(exchange, gson.toJson(e.getMessage()));
        }
    }

    private void deleteTask(Long id, HttpExchange exchange) {
        taskManager.removeTask(id);
        try {
            sendText(exchange, gson.toJson(Map.of("id", id)), 200);
        } catch (IOException e) {
            sendInternalServerError(exchange, e.getMessage());
        }
    }

    private void getTask(Long id, HttpExchange exchange) {
        Task task = taskManager.getTask(id);
        if (task == null) {
            try {
                sendNotFound(exchange, gson.toJson((Object) null));
            } catch (IOException e) {
                sendInternalServerError(exchange, gson.toJson(e.getMessage()));
            }
        }
        try {
            sendText(exchange, gson.toJson(task), 200);
        } catch (IOException e) {
            sendInternalServerError(exchange, gson.toJson(e.getMessage()));
        }
    }

    private void getTasks(HttpExchange exchange) {
        List<Task> allTasks = taskManager.getTasks();
        if (allTasks.isEmpty()) {
            try {
                sendText(exchange, gson.toJson(allTasks), 200);
            } catch (IOException e) {
                sendInternalServerError(exchange, gson.toJson(e.getMessage()));
            }
        }
        String jsonAllTasks = gson.toJson(allTasks);
        try {
            sendText(exchange, jsonAllTasks, 200);
        } catch (IOException e) {
            sendInternalServerError(exchange, gson.toJson(e.getMessage()));
        }
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
