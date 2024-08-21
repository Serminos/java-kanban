package manager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void getDefault_shouldReturnInMemoryTaskManager() {
        // perform do
        TaskManager taskManager = Managers.getDefault();

        //check
        assertInstanceOf(InMemoryTaskManager.class, taskManager);
    }

    @Test
    void getDefaultHistory_shouldReturnInMemoryHistoryManager() {
        // perform do
        HistoryManager historyManager = Managers.getDefaultHistory();

        //check
        assertInstanceOf(InMemoryHistoryManager.class, historyManager);
    }
}