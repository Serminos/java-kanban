package manager;

import enums.TaskStatus;
import models.Epic;
import models.SubTask;
import models.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
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
        Task task = new Task(1L, "Приготовить завтрак",
                "Сварить макароны и пожарить котлету", TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 21, 19, 0), Duration.ofMinutes(60)
        );
        Epic epic = new Epic(2L, "Испечь торт", "Испечь торт Наполеон");
        SubTask subTask = new SubTask(3L, 2L, "Найти рецепт",
                "Выполнить поиск видео рецепта", TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 21, 19, 0), Duration.ofMinutes(60));
        epic.setSubTaskId(3L);

        // do
        inMemoryHistoryManager.add(task);
        inMemoryHistoryManager.add(epic);
        inMemoryHistoryManager.add(subTask);
        inMemoryHistoryManager.add(subTask);
        inMemoryHistoryManager.add(epic);
        inMemoryHistoryManager.add(task);

        //check
        assertEquals(3, inMemoryHistoryManager.getHistory().size());
        List<Task> history = inMemoryHistoryManager.getHistory();
        //SubTask
        assertInstanceOf(SubTask.class, history.get(0));
        SubTask subTaskInHistory = (SubTask) history.get(0);
        assertEquals(3L, subTaskInHistory.getId());
        assertEquals(2L, subTaskInHistory.getEpicId());
        assertEquals("Найти рецепт", subTaskInHistory.getName());
        assertEquals("Выполнить поиск видео рецепта", subTaskInHistory.getDescription());
        assertEquals(TaskStatus.NEW, subTaskInHistory.getStatus());
        //Epic
        assertInstanceOf(Epic.class, history.get(1));
        Epic epicInHistory = (Epic) history.get(1);
        assertEquals(2L, epicInHistory.getId());
        assertEquals("Испечь торт", epicInHistory.getName());
        assertEquals("Испечь торт Наполеон", epicInHistory.getDescription());
        assertEquals(null, epicInHistory.getStatus());
        Assertions.assertArrayEquals(List.of(3L).toArray(),
                epicInHistory.getSubTaskIds().toArray());
        //Task
        assertInstanceOf(Task.class, history.get(2));
        Task taskInHistory = history.get(2);
        assertEquals(1L, taskInHistory.getId());
        assertEquals("Приготовить завтрак", taskInHistory.getName());
        assertEquals("Сварить макароны и пожарить котлету", taskInHistory.getDescription());
        assertEquals(TaskStatus.NEW, taskInHistory.getStatus());
    }

    @Test
    void add_shouldNotAddNullValueTasksInHistory() {
        // prepare
        Task task = new Task(1L, "Приготовить завтрак",
                "Сварить макароны и пожарить котлету", TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 21, 19, 0), Duration.ofMinutes(60)
        );
        Epic epic = new Epic(2L, "Испечь торт", "Испечь торт Наполеон");
        SubTask subTask = new SubTask(3L, 2L, "Найти рецепт",
                "Выполнить поиск видео рецепта", TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 21, 19, 0), Duration.ofMinutes(60));
        epic.setSubTaskId(3L);

        // do
        inMemoryHistoryManager.add(task);
        inMemoryHistoryManager.add(epic);
        inMemoryHistoryManager.add(null);
        inMemoryHistoryManager.add(subTask);

        //check
        assertEquals(3, inMemoryHistoryManager.getHistory().size());
    }

    @Test
    void add_shouldSaveOneLastViewActionInHistory() {
        // prepare
        Task task = new Task(1L, "Приготовить завтрак",
                "Сварить макароны и пожарить котлету", TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 21, 19, 0), Duration.ofMinutes(60)
        );

        // do
        inMemoryHistoryManager.add(task);
        inMemoryHistoryManager.add(task);
        inMemoryHistoryManager.add(task);

        //check
        assertEquals(1, inMemoryHistoryManager.getHistory().size());
        List<Task> history = inMemoryHistoryManager.getHistory();
        //Task
        assertInstanceOf(Task.class, history.get(0));
        Task taskInHistory = history.get(0);
        assertEquals(1L, taskInHistory.getId());
        assertEquals("Приготовить завтрак", taskInHistory.getName());
        assertEquals("Сварить макароны и пожарить котлету", taskInHistory.getDescription());
        assertEquals(TaskStatus.NEW, taskInHistory.getStatus());
    }

    @Test
    void add_shouldReturnEmptyHistory() {
        // prepare
        Task task = new Task(1L, "Приготовить завтрак",
                "Сварить макароны и пожарить котлету", TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 21, 19, 0), Duration.ofMinutes(60)
        );
        Epic epic = new Epic(2L, "Испечь торт", "Испечь торт Наполеон");
        SubTask subTask = new SubTask(3L, 2L, "Найти рецепт",
                "Выполнить поиск видео рецепта", TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 21, 19, 0), Duration.ofMinutes(60));
        epic.setSubTaskId(3L);

        // do
        inMemoryHistoryManager.add(task);
        inMemoryHistoryManager.add(epic);
        inMemoryHistoryManager.add(subTask);
        inMemoryHistoryManager.add(subTask);
        inMemoryHistoryManager.add(epic);
        inMemoryHistoryManager.add(task);
        inMemoryHistoryManager.remove(task.getId());
        inMemoryHistoryManager.remove(epic.getId());
        inMemoryHistoryManager.remove(subTask.getId());

        //check
        assertEquals(0, inMemoryHistoryManager.getHistory().size());
    }

    @Test
    void add_shouldSaveUniqueTasksInHistory() {
        // prepare
        Task task = new Task(1L, "Приготовить завтрак",
                "Сварить макароны и пожарить котлету", TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 21, 19, 0), Duration.ofMinutes(60)
        );
        Epic epic = new Epic(2L, "Испечь торт", "Испечь торт Наполеон");
        SubTask subTask = new SubTask(3L, 2L, "Найти рецепт",
                "Выполнить поиск видео рецепта", TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 21, 19, 0), Duration.ofMinutes(60));
        epic.setSubTaskId(3L);

        // do
        inMemoryHistoryManager.add(task);
        inMemoryHistoryManager.add(task);
        inMemoryHistoryManager.add(epic);
        inMemoryHistoryManager.add(subTask);
        inMemoryHistoryManager.add(subTask);
        inMemoryHistoryManager.add(epic);
        inMemoryHistoryManager.add(task);
        inMemoryHistoryManager.add(subTask);
        inMemoryHistoryManager.add(epic);

        //check
        assertEquals(3, inMemoryHistoryManager.getHistory().size());
    }

    @Test
    void add_shouldRemoveStartTasksInHistory() {
        // prepare
        Task task = new Task(1L, "Приготовить завтрак",
                "Сварить макароны и пожарить котлету", TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 21, 19, 0), Duration.ofMinutes(60)
        );
        Epic epic = new Epic(2L, "Испечь торт", "Испечь торт Наполеон");
        SubTask subTask = new SubTask(3L, 2L, "Найти рецепт",
                "Выполнить поиск видео рецепта", TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 21, 19, 0), Duration.ofMinutes(60));
        epic.setSubTaskId(3L);

        // do
        inMemoryHistoryManager.add(task);
        inMemoryHistoryManager.add(epic);
        inMemoryHistoryManager.add(subTask);
        inMemoryHistoryManager.add(subTask);
        inMemoryHistoryManager.add(epic);
        inMemoryHistoryManager.add(task);
        inMemoryHistoryManager.remove(subTask.getId());//after then epic on start

        //check
        assertEquals(2, inMemoryHistoryManager.getHistory().size());
        List<Task> history = inMemoryHistoryManager.getHistory();
        //Epic
        assertInstanceOf(Epic.class, history.get(0));
        Epic epicInHistory = (Epic) history.get(0);
        assertEquals(2L, epicInHistory.getId());
        assertEquals("Испечь торт", epicInHistory.getName());
        assertEquals("Испечь торт Наполеон", epicInHistory.getDescription());
        assertEquals(null, epicInHistory.getStatus());
        Assertions.assertArrayEquals(List.of(3L).toArray(),
                epicInHistory.getSubTaskIds().toArray());
        //Task
        assertInstanceOf(Task.class, history.get(1));
        Task taskInHistory = history.get(1);
        assertEquals(1L, taskInHistory.getId());
        assertEquals("Приготовить завтрак", taskInHistory.getName());
        assertEquals("Сварить макароны и пожарить котлету", taskInHistory.getDescription());
        assertEquals(TaskStatus.NEW, taskInHistory.getStatus());
    }

    @Test
    void add_shouldRemoveMiddleTasksInHistory() {
        // prepare
        Task task = new Task(1L, "Приготовить завтрак",
                "Сварить макароны и пожарить котлету", TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 21, 19, 0), Duration.ofMinutes(60)
        );
        Epic epic = new Epic(2L, "Испечь торт", "Испечь торт Наполеон");
        SubTask subTask = new SubTask(3L, 2L, "Найти рецепт",
                "Выполнить поиск видео рецепта", TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 21, 19, 0), Duration.ofMinutes(60));
        epic.setSubTaskId(3L);

        // do
        inMemoryHistoryManager.add(task);
        inMemoryHistoryManager.add(epic);
        inMemoryHistoryManager.add(subTask);
        inMemoryHistoryManager.add(subTask);
        inMemoryHistoryManager.add(epic);
        inMemoryHistoryManager.add(task);
        inMemoryHistoryManager.remove(epic.getId());//after then subTask on start

        //check
        assertEquals(2, inMemoryHistoryManager.getHistory().size());
        List<Task> history = inMemoryHistoryManager.getHistory();
        //SubTask
        assertInstanceOf(SubTask.class, history.get(0));
        SubTask subTaskInHistory = (SubTask) history.get(0);
        assertEquals(3L, subTaskInHistory.getId());
        assertEquals(2L, subTaskInHistory.getEpicId());
        assertEquals("Найти рецепт", subTaskInHistory.getName());
        assertEquals("Выполнить поиск видео рецепта", subTaskInHistory.getDescription());
        assertEquals(TaskStatus.NEW, subTaskInHistory.getStatus());
        //Task
        assertInstanceOf(Task.class, history.get(1));
        Task taskInHistory = history.get(1);
        assertEquals(1L, taskInHistory.getId());
        assertEquals("Приготовить завтрак", taskInHistory.getName());
        assertEquals("Сварить макароны и пожарить котлету", taskInHistory.getDescription());
        assertEquals(TaskStatus.NEW, taskInHistory.getStatus());
    }

    @Test
    void add_shouldRemoveEndTasksInHistory() {
        // prepare
        Task task = new Task(1L, "Приготовить завтрак",
                "Сварить макароны и пожарить котлету", TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 21, 19, 0), Duration.ofMinutes(60)
        );
        Epic epic = new Epic(2L, "Испечь торт", "Испечь торт Наполеон");
        SubTask subTask = new SubTask(3L, 2L, "Найти рецепт",
                "Выполнить поиск видео рецепта", TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 21, 19, 0), Duration.ofMinutes(60));
        epic.setSubTaskId(3L);

        // do
        inMemoryHistoryManager.add(task);
        inMemoryHistoryManager.add(epic);
        inMemoryHistoryManager.add(subTask);
        inMemoryHistoryManager.add(subTask);
        inMemoryHistoryManager.add(epic);
        inMemoryHistoryManager.add(task);
        inMemoryHistoryManager.remove(task.getId());//after then epic on end

        //check
        assertEquals(2, inMemoryHistoryManager.getHistory().size());
        List<Task> history = inMemoryHistoryManager.getHistory();
        //SubTask
        assertInstanceOf(SubTask.class, history.get(0));
        SubTask subTaskInHistory = (SubTask) history.get(0);
        assertEquals(3L, subTaskInHistory.getId());
        assertEquals(2L, subTaskInHistory.getEpicId());
        assertEquals("Найти рецепт", subTaskInHistory.getName());
        assertEquals("Выполнить поиск видео рецепта", subTaskInHistory.getDescription());
        assertEquals(TaskStatus.NEW, subTaskInHistory.getStatus());
        //Epic
        assertInstanceOf(Epic.class, history.get(1));
        Epic epicInHistory = (Epic) history.get(1);
        assertEquals(2L, epicInHistory.getId());
        assertEquals("Испечь торт", epicInHistory.getName());
        assertEquals("Испечь торт Наполеон", epicInHistory.getDescription());
        assertEquals(null, epicInHistory.getStatus());
        Assertions.assertArrayEquals(List.of(3L).toArray(),
                epicInHistory.getSubTaskIds().toArray());
    }
}