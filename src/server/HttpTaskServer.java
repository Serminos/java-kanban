package server;

import com.sun.net.httpserver.HttpServer;
import manager.Managers;
import manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private final TaskManager taskManager;
    private HttpServer httpServer;
    private final int port;

    private void createEachContext(HttpServer httpServer, TaskManager taskManager) {
        httpServer.createContext("/tasks", new TasksHandler(taskManager));
        httpServer.createContext("/subtasks", new SubTasksHandler(taskManager));
        httpServer.createContext("/epics", new EpicsHandler(taskManager));
        httpServer.createContext("/history", new HistoryHandler(taskManager));
        httpServer.createContext("/prioritized", new PrioritizedHandler(taskManager));
    }

    public HttpTaskServer(TaskManager taskManager, int port) {
        this.taskManager = taskManager;
        this.port = port;
    }

    public static void main(String[] args) throws IOException {
        new HttpTaskServer(Managers.getDefault(), 8089).start();
    }

    public void start() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        createEachContext(httpServer, taskManager);
        httpServer.start();
        //System.out.println("HTTP-сервер запущен, по адресу: http://127.0.0.1:" + port);
    }

    public void stop() {
        httpServer.stop(0);
    }
}
