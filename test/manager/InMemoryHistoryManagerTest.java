package manager;

import enums.TaskStatus;
import models.Epic;
import models.SubTask;
import models.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    InMemoryHistoryManager inMemoryHistoryManager;

    @BeforeEach
    void beforeEachTest() {
        inMemoryHistoryManager = new InMemoryHistoryManager();
    }

    @Test
    void add_shouldAddDifferentTasksInHistory() {
        // prepare
        Task task = new Task(1L, "Приготовить завтрак", "Сварить макароны и пожарить котлету", TaskStatus.NEW);
        Epic epic = new Epic(2L, "Испечь торт", "Испечь торт Наполеон");
        SubTask subTask = new SubTask(3L, 2L, "Найти рецепт",
                "Выполнить поиск видео рецепта", TaskStatus.NEW);
        epic.setSubTaskIds(3L);

        // do
        inMemoryHistoryManager.add(task);
        inMemoryHistoryManager.add(epic);
        inMemoryHistoryManager.add(subTask);
        inMemoryHistoryManager.add(subTask);
        inMemoryHistoryManager.add(epic);
        inMemoryHistoryManager.add(task);

        //check
        assertEquals(6, inMemoryHistoryManager.getHistory().size());
        List<Task> history = inMemoryHistoryManager.getHistory();
        //Task
        assertInstanceOf(Task.class, history.get(0));
        Task taskInHistory = history.get(0);
        assertEquals(1L, taskInHistory.getId());
        assertEquals("Приготовить завтрак", taskInHistory.getName());
        assertEquals("Сварить макароны и пожарить котлету", taskInHistory.getDescription());
        assertEquals(TaskStatus.NEW, taskInHistory.getStatus());
        //Epic
        assertInstanceOf(Epic.class, history.get(1));
        Epic epicInHistory = (Epic) history.get(1);
        assertEquals(2L, epicInHistory.getId());
        assertEquals("Испечь торт", epicInHistory.getName());
        assertEquals("Испечь торт Наполеон", epicInHistory.getDescription());
        assertEquals(null, epicInHistory.getStatus());
        Assertions.assertArrayEquals(List.of(3L).toArray(),
                epicInHistory.getSubTaskIds().toArray());
        //SubTask
        assertInstanceOf(SubTask.class, history.get(2));
        SubTask subTaskInHistory = (SubTask) history.get(2);
        assertEquals(3L, subTaskInHistory.getId());
        assertEquals(2L, subTaskInHistory.getEpicId());
        assertEquals("Найти рецепт", subTaskInHistory.getName());
        assertEquals("Выполнить поиск видео рецепта", subTaskInHistory.getDescription());
        assertEquals(TaskStatus.NEW, subTaskInHistory.getStatus());

    }
}