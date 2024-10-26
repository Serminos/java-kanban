package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager taskManager;

    public HistoryHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            sendText(exchange, gson.toJson(taskManager.getHistory()), 200);
        } catch (IOException e) {
            sendInternalServerError(exchange, gson.toJson(e.getMessage()));
        }
    }
}
