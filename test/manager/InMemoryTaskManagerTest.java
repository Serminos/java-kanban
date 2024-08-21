package manager;

import enums.TaskStatus;
import models.Epic;
import models.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InMemoryTaskManagerTest {

    private TaskManager taskManager;

    @BeforeEach
    void init(){
        taskManager = Managers.getDefault();
    }

    @Test
    void createTask_shouldSaveTask() {
        Task task = new Task("задача_1","описание_1", TaskStatus.NEW);

        Long savedTaskId = taskManager.create(task);

        Assertions.assertNotNull(savedTaskId);
    }

    @Test
    void createEpic_shouldSaveEpic() {
        Epic epic = new Epic("задача_1","описание_1");

        Long savedEpicId = taskManager.create(epic);

        Assertions.assertNotNull(savedEpicId);
    }
}
