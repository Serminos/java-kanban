package server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.TaskValidationException;
import manager.TaskManager;
import models.SubTask;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SubTasksHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager taskManager;

    public SubTasksHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String[] path = exchange.getRequestURI().getPath().split("/");
        if (method.equals("GET") && path.length == 2) {
            getSubTasks(exchange);
        } else if (method.equals("GET") && path.length == 3) {
            getSubTask(Long.parseLong(path[2]), exchange);
        } else if (method.equals("POST")  && path.length == 2) {
            SubTask subTask = parseSubTask(exchange);
            postSubTask(subTask, exchange);
        } else if (method.equals("DELETE") && path.length == 3) {
            deleteSubTask(Long.parseLong(path[2]), exchange);
        }
    }

    private void postSubTask(SubTask subTask, HttpExchange exchange) {
        try {
            Long id = taskManager.update(subTask) ? subTask.getId() : taskManager.create(subTask);
            sendText(exchange, gson.toJson(Map.of("id", id)), 201);
        } catch (TaskValidationException e) {
            sendHasInteractions(exchange, gson.toJson(e.getMessage()));
        } catch (IOException e) {
            sendInternalServerError(exchange, gson.toJson(e.getMessage()));
        }
    }

    private void getSubTask(Long id, HttpExchange exchange) {
        SubTask subTask = taskManager.getSubTask(id);
        if (subTask == null) {
            try {
                sendNotFound(exchange, gson.toJson((Object) null));
            } catch (IOException e) {
                sendInternalServerError(exchange, gson.toJson(e.getMessage()));
            }
        }
        try {
            sendText(exchange, gson.toJson(subTask), 200);
        } catch (IOException e) {
            sendInternalServerError(exchange, gson.toJson(e.getMessage()));
        }
    }

    private void getSubTasks(HttpExchange exchange) {
        List<SubTask> allSubTasks = taskManager.getSubTasks();
        if (allSubTasks == null) {
            try {
                sendNotFound(exchange, gson.toJson(allSubTasks));
            } catch (IOException e) {
                sendInternalServerError(exchange, gson.toJson(e.getMessage()));
            }
        }
        try {
            sendText(exchange, gson.toJson(allSubTasks), 200);
        } catch (IOException e) {
            sendInternalServerError(exchange, gson.toJson(e.getMessage()));
        }
    }

    private void deleteSubTask(Long id, HttpExchange exchange) {
        taskManager.removeSubTask(id);
        try {
            sendText(exchange, gson.toJson(id), 200);
        } catch (IOException e) {
            sendInternalServerError(exchange, e.getMessage());
        }
    }

    private SubTask parseSubTask(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes());
        SubTask subtask = gson.fromJson(body, SubTask.class);

        JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
        if (jsonObject.has("id")) {
            Long id = jsonObject.get("id").getAsLong();
            subtask.setId(id);
        }
        return subtask;
    }
}
