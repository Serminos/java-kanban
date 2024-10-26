package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;

import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager taskManager;

    public PrioritizedHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            sendText(exchange, gson.toJson(taskManager.getPrioritizedTasks(true)), 200);
        } catch (IOException e) {
            sendInternalServerError(exchange, gson.toJson(e.getMessage()));
        }
    }
}
