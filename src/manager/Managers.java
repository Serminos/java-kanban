package manager;

/**
 * Утилитарный класс управления.
 */
public class Managers {
    public static TaskManager getDefault() {
        HistoryManager historyManager = getDefaultHistory();
        return new InMemoryTaskManager(historyManager);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
