package manager;

import enums.TaskStatus;
import exception.TaskValidationException;
import models.Epic;
import models.SubTask;
import models.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {
    T taskManager;
    HistoryManager historyManager;
    static Task task = new Task("Приготовить завтрак",
            "Сварить макароны и пожарить котлету", TaskStatus.NEW);
    static Epic epic = new Epic("Испечь торт", "Испечь торт Наполеон");
    static SubTask subtask = new SubTask(1L, "Найти рецепт",
            "Выполнить поиск видео рецепта", TaskStatus.NEW,
            LocalDateTime.of(2024, 10, 21, 19, 0), Duration.ofMinutes(60));

   /* @BeforeEach
    void init() {
    }*/

    static Stream<Task> getTestTask() {
        return Stream.of(task);
    }

    static Stream<Epic> getTestEpic() {
        return Stream.of(epic);
    }

    static Stream<SubTask> getTestSubTask() {
        return Stream.of(subtask);
    }

    @ParameterizedTest
    @MethodSource("getTestTask")
    void create(Task task) {
        // do
        Long savedTaskId = taskManager.create(task);
        Task taskInMemory = taskManager.getTask(savedTaskId);

        // check
        Assertions.assertNotNull(savedTaskId);
        Assertions.assertEquals("Приготовить завтрак", taskInMemory.getName());
        Assertions.assertEquals("Сварить макароны и пожарить котлету", taskInMemory.getDescription());
        Assertions.assertEquals(TaskStatus.NEW, taskInMemory.getStatus());
    }

    @ParameterizedTest
    @MethodSource("getTestTask")
    void update(Task task) {
        // prepare
        Long savedTaskId = taskManager.create(task);
        Task updatedTask = new Task("Приготовить легкий завтрак", "Сварить кашу", TaskStatus.IN_PROGRESS);
        updatedTask.setId(savedTaskId);

        // do
        boolean isUpdated = taskManager.update(updatedTask);

        // check
        Assertions.assertTrue(isUpdated, "Обновление Task не работает");
        Assertions.assertEquals("Приготовить легкий завтрак", taskManager.getTask(savedTaskId).getName());
        Assertions.assertEquals("Сварить кашу", taskManager.getTask(savedTaskId).getDescription());
    }

    @ParameterizedTest
    @MethodSource("getTestEpic")
    void create(Epic epic) {
        // do
        Long savedEpicId = taskManager.create(epic);
        Epic epicInMemory = taskManager.getEpic(savedEpicId);

        // check
        Assertions.assertNotNull(savedEpicId);
        Assertions.assertEquals("Испечь торт", epicInMemory.getName());
        Assertions.assertEquals("Испечь торт Наполеон", epicInMemory.getDescription());
        Assertions.assertEquals(null, epicInMemory.getStatus());
    }

    @ParameterizedTest
    @MethodSource("getTestEpic")
    void update(Epic epic) {
        // prepare
        final Long savedEpicId = taskManager.create(epic);
        Epic updatedEpic = new Epic("Испечь необычный торт", "Испечь торт Красный бархат");
        updatedEpic.setId(savedEpicId);

        // do
        boolean isUpdated = taskManager.update(updatedEpic);
        Epic updatedEpicInMemory = taskManager.getEpic(savedEpicId);

        // check
        Assertions.assertTrue(isUpdated, "Обновление Epic не работает");
        Assertions.assertEquals("Испечь необычный торт", updatedEpicInMemory.getName());
        Assertions.assertEquals("Испечь торт Красный бархат", updatedEpicInMemory.getDescription());
        assertNull(updatedEpicInMemory.getStatus(), "Epic не должен сам устанавливать статус");
    }

    @ParameterizedTest
    @MethodSource("getTestSubTask")
    void create(SubTask subTask) {
        // prepare
        Epic epic = new Epic(1L, "Испечь торт", "Испечь торт Наполеон");
        final Long savedEpicId = taskManager.create(epic);

        // do
        Long savedSubTask1Id = taskManager.create(subTask);
        SubTask updatedSubTaskInMemory = taskManager.getSubTask(savedSubTask1Id);
        Epic updatedEpicInMemory = taskManager.getEpic(savedEpicId);

        // check
        // SubTask
        Assertions.assertNotNull(savedSubTask1Id);
        Assertions.assertEquals(savedSubTask1Id, updatedSubTaskInMemory.getId());
        Assertions.assertEquals(savedEpicId, updatedSubTaskInMemory.getEpicId());
        Assertions.assertEquals("Найти рецепт", updatedSubTaskInMemory.getName());
        Assertions.assertEquals("Выполнить поиск видео рецепта", updatedSubTaskInMemory.getDescription());
        Assertions.assertEquals(TaskStatus.NEW, updatedSubTaskInMemory.getStatus());
        // Epic
        Assertions.assertEquals(savedEpicId, updatedEpicInMemory.getId());
        Assertions.assertEquals("Испечь торт", updatedEpicInMemory.getName());
        Assertions.assertEquals("Испечь торт Наполеон", updatedEpicInMemory.getDescription());
        Assertions.assertArrayEquals(List.of(savedSubTask1Id).toArray(), updatedEpicInMemory.getSubTaskIds().toArray());
        Assertions.assertEquals(TaskStatus.NEW, updatedEpicInMemory.getStatus());
    }

    @ParameterizedTest
    @MethodSource("getTestSubTask")
    void update(SubTask subTask) {
        // prepare
        Epic epic = new Epic(1L, "Испечь торт", "Испечь торт Наполеон");
        final Long savedEpicId = taskManager.create(epic);
        final Long savedSubTask1Id = taskManager.create(subTask);
        SubTask updatedSubTask1 = new SubTask(savedEpicId, "Найти видео рецепт",
                "Выполнить поиск видео рецепта на youtube", TaskStatus.IN_PROGRESS,
                LocalDateTime.of(2024, 12, 21, 19, 0), Duration.ofMinutes(60));
        updatedSubTask1.setId(savedSubTask1Id);

        // do
        boolean isUpdated = taskManager.update(updatedSubTask1);
        SubTask updatedSubTaskInMemory = taskManager.getSubTask(savedSubTask1Id);
        Epic updatedEpicInMemory = taskManager.getEpic(savedEpicId);

        // check
        // SubTask
        Assertions.assertTrue(isUpdated, "Обновление SubTask не работает");
        Assertions.assertNotNull(savedSubTask1Id);
        Assertions.assertEquals(savedSubTask1Id, updatedSubTaskInMemory.getId());
        Assertions.assertEquals(savedEpicId, updatedSubTaskInMemory.getEpicId());
        Assertions.assertEquals("Найти видео рецепт", updatedSubTaskInMemory.getName());
        Assertions.assertEquals("Выполнить поиск видео рецепта на youtube",
                updatedSubTaskInMemory.getDescription());
        Assertions.assertEquals(TaskStatus.IN_PROGRESS, updatedSubTaskInMemory.getStatus());
        // Epic
        Assertions.assertEquals(savedEpicId, updatedEpicInMemory.getId());
        Assertions.assertEquals("Испечь торт", updatedEpicInMemory.getName());
        Assertions.assertEquals("Испечь торт Наполеон", updatedEpicInMemory.getDescription());
        Assertions.assertArrayEquals(List.of(savedSubTask1Id).toArray(), updatedEpicInMemory.getSubTaskIds().toArray());
        Assertions.assertEquals(TaskStatus.IN_PROGRESS, updatedEpicInMemory.getStatus());
    }

    @Test
    void getTask() {
        // do
        Long savedTaskId = taskManager.create(task);
        Task taskInMemory = taskManager.getTask(savedTaskId);

        // check
        Assertions.assertNotNull(savedTaskId);
        Assertions.assertEquals("Приготовить завтрак", taskInMemory.getName());
        Assertions.assertEquals("Сварить макароны и пожарить котлету", taskInMemory.getDescription());
        Assertions.assertEquals(TaskStatus.NEW, taskInMemory.getStatus());
    }

    @Test
    void getEpic() {
        // do
        Long savedEpicId = taskManager.create(epic);
        Epic epicInMemory = taskManager.getEpic(savedEpicId);

        // check
        Assertions.assertNotNull(savedEpicId);
        Assertions.assertEquals("Испечь торт", epicInMemory.getName());
        Assertions.assertEquals("Испечь торт Наполеон", epicInMemory.getDescription());
        Assertions.assertEquals(null, epicInMemory.getStatus());
    }

    @Test
    void getSubTask() {
        // prepare
        Epic epic = new Epic(1L, "Испечь торт", "Испечь торт Наполеон");
        final Long savedEpicId = taskManager.create(epic);
        SubTask subTask = new SubTask(savedEpicId, "Найти рецепт",
                "Выполнить поиск видео рецепта", TaskStatus.IN_PROGRESS);
        final Long savedSubTask1Id = taskManager.create(subTask);


        // do
        SubTask updatedSubTaskInMemory = taskManager.getSubTask(savedSubTask1Id);

        // check
        // SubTask
        Assertions.assertNotNull(savedSubTask1Id);
        Assertions.assertEquals(savedSubTask1Id, updatedSubTaskInMemory.getId());
        Assertions.assertEquals(savedEpicId, updatedSubTaskInMemory.getEpicId());
        Assertions.assertEquals("Найти рецепт", updatedSubTaskInMemory.getName());
        Assertions.assertEquals("Выполнить поиск видео рецепта", updatedSubTaskInMemory.getDescription());
        Assertions.assertEquals(TaskStatus.IN_PROGRESS, updatedSubTaskInMemory.getStatus());
    }

    @Test
    void testSubTask_shouldNotReturnHisEpic() {
        // prepare
        Epic epic = new Epic("Испечь торт", "Испечь торт Наполеон");
        final Long savedEpicId = taskManager.create(epic);
        SubTask subTask1 = new SubTask(savedEpicId, "Найти рецепт",
                "Выполнить поиск видео рецепта", TaskStatus.NEW);
        final Long savedSubTask1Id = taskManager.create(subTask1);

        //do
        SubTask subTask2 = new SubTask(savedSubTask1Id, "Найти рецепт",
                "Выполнить поиск видео рецепта", TaskStatus.NEW);
        final Long savedSubTask2Id = taskManager.create(subTask2);

        //check
        Assertions.assertNull(savedSubTask2Id);
    }

    @Test
    void getAllSubTasksByEpicId() {
        // prepare
        Epic epic = new Epic("Испечь торт", "Испечь торт Наполеон");
        final Long savedEpicId = taskManager.create(epic);
        SubTask subTask1 = new SubTask(savedEpicId, "Найти рецепт",
                "Выполнить поиск видео рецепта", TaskStatus.IN_PROGRESS);
        final Long savedSubTask1Id = taskManager.create(subTask1);
        SubTask subTask2 = new SubTask(savedEpicId, "Найти рецепт",
                "Выполнить поиск текстового рецепта", TaskStatus.DONE);
        final Long savedSubTask2Id = taskManager.create(subTask2);

        // do
        List<SubTask> subTasks = taskManager.getAllSubTasksByEpicId(savedEpicId);

        // check
        // SubTask
        Assertions.assertNotNull(subTasks);
        Assertions.assertEquals(2, subTasks.size());
        List<Long> idSubTasks = new ArrayList<>();
        for (SubTask subTask : subTasks) {
            idSubTasks.add(subTask.getId());
        }
        Assertions.assertArrayEquals(List.of(savedSubTask1Id, savedSubTask2Id).toArray(), idSubTasks.toArray());
        // SubTask 1
        Assertions.assertEquals(2, subTasks.get(0).getId());
        Assertions.assertEquals(savedEpicId, subTasks.get(0).getEpicId());
        Assertions.assertEquals("Найти рецепт", subTasks.get(0).getName());
        Assertions.assertEquals("Выполнить поиск видео рецепта", subTasks.get(0).getDescription());
        Assertions.assertEquals(TaskStatus.IN_PROGRESS, subTasks.get(0).getStatus());
        // SubTask 2
        Assertions.assertEquals(3, subTasks.get(1).getId());
        Assertions.assertEquals(savedEpicId, subTasks.get(1).getEpicId());
        Assertions.assertEquals("Найти рецепт", subTasks.get(1).getName());
        Assertions.assertEquals("Выполнить поиск текстового рецепта", subTasks.get(1).getDescription());
        Assertions.assertEquals(TaskStatus.DONE, subTasks.get(1).getStatus());
        // Epic
        Epic updatedEpicInMemory = taskManager.getEpic(savedEpicId);
        Assertions.assertEquals(savedEpicId, updatedEpicInMemory.getId());
        Assertions.assertEquals("Испечь торт", updatedEpicInMemory.getName());
        Assertions.assertEquals("Испечь торт Наполеон", updatedEpicInMemory.getDescription());
        Assertions.assertArrayEquals(List.of(savedSubTask1Id, savedSubTask2Id).toArray(),
                updatedEpicInMemory.getSubTaskIds().toArray());
        Assertions.assertEquals(TaskStatus.IN_PROGRESS, updatedEpicInMemory.getStatus());
    }

    @Test
    void clearTasks() {
        // prepare
        Task task1 = new Task("Приготовить завтрак",
                "Сварить макароны и пожарить котлету", TaskStatus.NEW);
        Task task2 = new Task("Приготовить обед", "Сварить пельмени", TaskStatus.NEW);
        Epic epic = new Epic("Испечь торт", "Испечь торт Наполеон");
        Long savedTask1Id = taskManager.create(task1);
        Long savedTask2Id = taskManager.create(task2);
        Long savedEpicId = taskManager.create(epic);

        // do
        taskManager.clearTasks();

        // check
        Assertions.assertNull(taskManager.getTask(savedTask1Id));
        Assertions.assertNull(taskManager.getTask(savedTask2Id));
        Assertions.assertNotNull(taskManager.getEpic(savedEpicId));
    }

    @Test
    void clearEpics() {
        // prepare
        Epic epic = new Epic("Испечь торт", "Испечь торт Наполеон");
        final Long savedEpicId = taskManager.create(epic);
        SubTask subTask1 = new SubTask(savedEpicId, "Найти рецепт",
                "Выполнить поиск видео рецепта", TaskStatus.NEW);
        Task task = new Task("Приготовить завтрак",
                "Сварить макароны и пожарить котлету", TaskStatus.NEW);
        final Long savedSubTask12Id = taskManager.create(subTask1);
        final Long savedTaskId = taskManager.create(task);

        // do
        taskManager.clearEpics();

        // check
        Assertions.assertNull(taskManager.getEpic(savedEpicId));
        Assertions.assertNull(taskManager.getSubTask(savedSubTask12Id));
        Assertions.assertNotNull(taskManager.getTask(savedTaskId));
    }

    @Test
    void clearSubTasks() {
        // prepare
        Epic epic = new Epic("Испечь торт", "Испечь торт Наполеон");
        final Long savedEpicId = taskManager.create(epic);
        SubTask subTask1 = new SubTask(savedEpicId, "Найти рецепт",
                "Выполнить поиск видео рецепта", TaskStatus.NEW);
        Task task = new Task("Приготовить завтрак",
                "Сварить макароны и пожарить котлету", TaskStatus.NEW);
        final Long savedSubTask12Id = taskManager.create(subTask1);
        final Long savedTaskId = taskManager.create(task);

        // do
        taskManager.clearSubTasks();

        // check
        Assertions.assertNotNull(taskManager.getEpic(savedEpicId));
        Assertions.assertNull(taskManager.getSubTask(savedSubTask12Id));
        Assertions.assertNotNull(taskManager.getTask(savedTaskId));
    }

    @Test
    void clear() {
        // prepare
        Epic epic = new Epic("Испечь торт", "Испечь торт Наполеон");
        final Long savedEpicId = taskManager.create(epic);
        SubTask subTask1 = new SubTask(savedEpicId, "Найти рецепт",
                "Выполнить поиск видео рецепта", TaskStatus.NEW);
        Task task = new Task("Приготовить завтрак",
                "Сварить макароны и пожарить котлету", TaskStatus.NEW);
        final Long savedSubTask12Id = taskManager.create(subTask1);
        final Long savedTaskId = taskManager.create(task);

        // do
        taskManager.clear();

        // check
        Assertions.assertNull(taskManager.getEpic(savedEpicId));
        Assertions.assertNull(taskManager.getSubTask(savedSubTask12Id));
        Assertions.assertNull(taskManager.getTask(savedTaskId));
    }

    @Test
    void removeTask() {
        // prepare
        Task task1 = new Task("Приготовить завтрак",
                "Сварить макароны и пожарить котлету", TaskStatus.NEW);
        final Long savedTask1Id = taskManager.create(task1);

        // do
        taskManager.removeTask(savedTask1Id);

        // check
        Assertions.assertNull(taskManager.getTask(savedTask1Id));
    }

    @Test
    void removeEpic() {
        // prepare
        Epic epic = new Epic("Испечь торт", "Испечь торт Наполеон");
        final Long savedEpicId = taskManager.create(epic);
        SubTask subTask1 = new SubTask(savedEpicId, "Найти рецепт",
                "Выполнить поиск видео рецепта", TaskStatus.NEW);
        final Long savedSubTask1Id = taskManager.create(subTask1);

        // do
        taskManager.removeEpic(savedEpicId);

        // check
        Assertions.assertNull(taskManager.getEpic(savedEpicId));
        Assertions.assertNull(taskManager.getSubTask(savedSubTask1Id));
    }

    @Test
    void removeSubTask() {
        // prepare
        Epic epic = new Epic("Испечь торт", "Испечь торт Наполеон");
        final Long savedEpicId = taskManager.create(epic);
        SubTask subTask1 = new SubTask(savedEpicId, "Найти рецепт",
                "Выполнить поиск видео рецепта", TaskStatus.NEW);
        final Long savedSubTask1Id = taskManager.create(subTask1);

        // do
        taskManager.removeSubTask(savedSubTask1Id);

        // check
        Assertions.assertNotNull(taskManager.getEpic(savedEpicId));
        Assertions.assertEquals(0, taskManager.getEpic(savedEpicId).getSubTaskIds().size());
        Assertions.assertNull(taskManager.getSubTask(savedSubTask1Id));
    }

    @Test
    void getAllTasksText() {
        // prepare
        Task task1 = new Task("Приготовить завтрак",
                "Сварить макароны и пожарить котлету", TaskStatus.NEW);
        final Long savedTask1Id = taskManager.create(task1);
        Epic epic = new Epic("Испечь торт", "Испечь торт Наполеон");
        final Long savedEpicId = taskManager.create(epic);
        SubTask subTask1 = new SubTask(savedEpicId, "Найти рецепт",
                "Выполнить поиск видео рецепта", TaskStatus.NEW);
        final Long savedSubTask1Id = taskManager.create(subTask1);
        List<String> expectedTasksText = new ArrayList<>();
        expectedTasksText.add("Task{id=1, name='Приготовить завтрак', " +
                "description='Сварить макароны и пожарить котлету', status=NEW}");
        expectedTasksText.add("Epic{id=2, name='Испечь торт', " +
                "description='Испечь торт Наполеон', status=NEW, subtaskIds=[3]}");
        expectedTasksText.add("SubTask{epicId=2, id=3, name='Найти рецепт', " +
                "description='Выполнить поиск видео рецепта', status=NEW}");

        // do
        List<String> tasksText = taskManager.getAllTasksText();

        // check
        assertArrayEquals(expectedTasksText.toArray(), tasksText.toArray());
    }

    @Test
    void getById() {
        // prepare
        Task task = new Task("Приготовить завтрак",
                "Сварить макароны и пожарить котлету", TaskStatus.NEW);
        Epic epic = new Epic("Испечь торт", "Испечь торт Наполеон");
        final Long savedTaskId = taskManager.create(task);
        final Long savedEpicId = taskManager.create(epic);
        SubTask subTask = new SubTask(savedEpicId, "Найти рецепт",
                "Выполнить поиск видео рецепта", TaskStatus.NEW);
        final Long savedSubTaskId = taskManager.create(subTask);

        //do
        Task taskInMemory = taskManager.getById(savedTaskId);
        Task epicInMemory = taskManager.getById(savedEpicId);
        Task subTaskInMemory = taskManager.getById(savedSubTaskId);

        //check
        assertInstanceOf(Task.class, taskInMemory);
        assertInstanceOf(Epic.class, epicInMemory);
        assertInstanceOf(SubTask.class, subTaskInMemory);
    }

    @Test
    void getHistory_shouldReturn3DifferentTaskType() {
        // prepare
        Task task = new Task("Приготовить завтрак",
                "Сварить макароны и пожарить котлету", TaskStatus.NEW);
        Epic epic = new Epic("Испечь торт", "Испечь торт Наполеон");
        final Long savedTaskId = taskManager.create(task);
        final Long savedEpicId = taskManager.create(epic);
        SubTask subTask = new SubTask(savedEpicId, "Найти рецепт",
                "Выполнить поиск видео рецепта", TaskStatus.NEW);
        final Long savedSubTaskId = taskManager.create(subTask);

        //do
        taskManager.getTask(savedTaskId);
        taskManager.getEpic(savedEpicId);
        taskManager.getSubTask(savedSubTaskId);
        List<Task> history = taskManager.getHistory();

        //check
        assertEquals(3, history.size());
        //Task
        assertInstanceOf(Task.class, history.get(0));
        Task taskInHistory = history.get(0);
        assertEquals(savedTaskId, taskInHistory.getId());
        assertEquals("Приготовить завтрак", taskInHistory.getName());
        assertEquals("Сварить макароны и пожарить котлету", taskInHistory.getDescription());
        assertEquals(TaskStatus.NEW, taskInHistory.getStatus());
        //Epic
        assertInstanceOf(Epic.class, history.get(1));
        Epic epicInHistory = (Epic) history.get(1);
        assertEquals(savedEpicId, epicInHistory.getId());
        assertEquals("Испечь торт", epicInHistory.getName());
        assertEquals("Испечь торт Наполеон", epicInHistory.getDescription());
        assertEquals(TaskStatus.NEW, epicInHistory.getStatus());
        Assertions.assertArrayEquals(List.of(savedSubTaskId).toArray(),
                epicInHistory.getSubTaskIds().toArray());
        //SubTask
        assertInstanceOf(SubTask.class, history.get(2));
        SubTask subTaskInHistory = (SubTask) history.get(2);
        assertEquals(savedSubTaskId, subTaskInHistory.getId());
        assertEquals(savedEpicId, subTaskInHistory.getEpicId());
        assertEquals("Найти рецепт", subTaskInHistory.getName());
        assertEquals("Выполнить поиск видео рецепта", subTaskInHistory.getDescription());
        assertEquals(TaskStatus.NEW, subTaskInHistory.getStatus());
    }

    @Test
    void getHistory_shouldReturnOnly10DifferentTask() {
        // prepare
        Task task = new Task("Приготовить завтрак",
                "Сварить макароны и пожарить котлету", TaskStatus.NEW);
        Epic epic = new Epic("Испечь торт", "Испечь торт Наполеон");
        final Long savedTaskId = taskManager.create(task);
        final Long savedEpicId = taskManager.create(epic);
        SubTask subTask = new SubTask(savedEpicId, "Найти рецепт",
                "Выполнить поиск видео рецепта", TaskStatus.NEW);
        final Long savedSubTaskId = taskManager.create(subTask);

        //do
        for (int i = 0; i < 100; i++) {
            taskManager.getTask(savedTaskId);
            taskManager.getEpic(savedEpicId);
            taskManager.getSubTask(savedSubTaskId);
        }
        List<Task> history = taskManager.getHistory();

        //check
        assertEquals(3, history.size());
    }

    @Test
    void getHistory_shouldReplaceDuplicateAndReturnOneNewValueAfterUpdate() {
        // prepare
        Task task1 = new Task("Приготовить завтрак",
                "Сварить макароны и пожарить котлету", TaskStatus.NEW);
        final Long savedTask1Id = taskManager.create(task1);
        Task task2 = new Task("Голодовка", "Вода", TaskStatus.NEW);
        task2.setId(savedTask1Id);

        //do
        for (int i = 0; i < 11; i++) {
            if (i == 10) {
                taskManager.update(task2);
            }
            taskManager.getTask(savedTask1Id);
        }
        List<Task> history = taskManager.getHistory();

        //check
        assertEquals(1, history.size());
        assertEquals("Task{id=1, name='Голодовка', description='Вода', status=NEW}",
                history.get(0).toString());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getPrioritizedTasks(boolean sort) {
        // prepare
        List<Long> tasksIds = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Task task = new Task("Приготовить завтрак", "Сварить макароны и пожарить котлету",
                    TaskStatus.NEW,
                    LocalDateTime.of(2000 + i, 1, 1, 1, 1), Duration.ofMinutes(15));
            tasksIds.add(taskManager.create(task));
        }

        //do
        List<Task> listPrioritizedTasks = taskManager.getPrioritizedTasks(sort);

        //check
        assertEquals(10, listPrioritizedTasks.size());
        if (sort) {
            assertEquals(tasksIds.get(0),
                    listPrioritizedTasks.get(0).getId());
            assertEquals(LocalDateTime.of(2001, 1, 1, 1, 1),
                    listPrioritizedTasks.get(0).getStartTime()
            );
        } else {
            assertEquals(tasksIds.get(tasksIds.size() - 1),
                    listPrioritizedTasks.get(0).getId());
            assertEquals(LocalDateTime.of(2010, 1, 1, 1, 1),
                    listPrioritizedTasks.get(0).getStartTime()
            );
        }
    }

    @Test
    void updateEpicStatus_shouldUpdateEpicStatusForNew() {
        // prepare
        Epic epic = new Epic(1L, "Испечь торт", "Испечь торт Наполеон");
        final Long savedEpicId = taskManager.create(epic);
        SubTask subTask1 = new SubTask(savedEpicId, "Найти рецепт1",
                "Выполнить поиск видео рецепта", TaskStatus.NEW);
        SubTask subTask2 = new SubTask(savedEpicId, "Найти рецепт1",
                "Выполнить поиск видео рецепта", TaskStatus.NEW);

        // do
        Long savedSubTask1Id = taskManager.create(subTask1);
        Long savedSubTask2Id = taskManager.create(subTask2);
        Epic updatedEpicInMemory = taskManager.getEpic(savedEpicId);

        // check
        // Epic
        Assertions.assertEquals(savedEpicId, updatedEpicInMemory.getId());
        Assertions.assertEquals(2, updatedEpicInMemory.getSubTaskIds().size());
        Assertions.assertEquals(TaskStatus.NEW, updatedEpicInMemory.getStatus());
    }

    @Test
    void updateEpicStatus_shouldUpdateEpicStatusForDone() {
        // prepare
        Epic epic = new Epic(1L, "Испечь торт", "Испечь торт Наполеон");
        final Long savedEpicId = taskManager.create(epic);
        SubTask subTask1 = new SubTask(savedEpicId, "Найти рецепт1",
                "Выполнить поиск видео рецепта", TaskStatus.DONE);
        SubTask subTask2 = new SubTask(savedEpicId, "Найти рецепт1",
                "Выполнить поиск видео рецепта", TaskStatus.DONE);

        // do
        Long savedSubTask1Id = taskManager.create(subTask1);
        Long savedSubTask2Id = taskManager.create(subTask2);
        Epic updatedEpicInMemory = taskManager.getEpic(savedEpicId);

        // check
        // Epic
        Assertions.assertEquals(savedEpicId, updatedEpicInMemory.getId());
        Assertions.assertEquals(2, updatedEpicInMemory.getSubTaskIds().size());
        Assertions.assertEquals(TaskStatus.DONE, updatedEpicInMemory.getStatus());
    }

    @Test
    void updateEpicStatus_shouldUpdateEpicStatusForInProgressByDifferentSubTaskStatus() {
        // prepare
        Epic epic = new Epic(1L, "Испечь торт", "Испечь торт Наполеон");
        final Long savedEpicId = taskManager.create(epic);
        SubTask subTask1 = new SubTask(savedEpicId, "Найти рецепт1",
                "Выполнить поиск видео рецепта", TaskStatus.NEW);
        SubTask subTask2 = new SubTask(savedEpicId, "Найти рецепт1",
                "Выполнить поиск видео рецепта", TaskStatus.DONE);

        // do
        Long savedSubTask1Id = taskManager.create(subTask1);
        Long savedSubTask2Id = taskManager.create(subTask2);
        Epic updatedEpicInMemory = taskManager.getEpic(savedEpicId);

        // check
        // Epic
        Assertions.assertEquals(savedEpicId, updatedEpicInMemory.getId());
        Assertions.assertEquals(2, updatedEpicInMemory.getSubTaskIds().size());
        Assertions.assertEquals(TaskStatus.IN_PROGRESS, updatedEpicInMemory.getStatus());
    }

    @Test
    void updateEpicStatus_shouldUpdateEpicStatusForInProgress() {
        // prepare
        Epic epic = new Epic(1L, "Испечь торт", "Испечь торт Наполеон");
        final Long savedEpicId = taskManager.create(epic);
        SubTask subTask1 = new SubTask(savedEpicId, "Найти рецепт1",
                "Выполнить поиск видео рецепта", TaskStatus.IN_PROGRESS);
        SubTask subTask2 = new SubTask(savedEpicId, "Найти рецепт1",
                "Выполнить поиск видео рецепта", TaskStatus.IN_PROGRESS);

        // do
        Long savedSubTask1Id = taskManager.create(subTask1);
        Long savedSubTask2Id = taskManager.create(subTask2);
        Epic updatedEpicInMemory = taskManager.getEpic(savedEpicId);

        // check
        // Epic
        Assertions.assertEquals(savedEpicId, updatedEpicInMemory.getId());
        Assertions.assertEquals(2, updatedEpicInMemory.getSubTaskIds().size());
        Assertions.assertEquals(TaskStatus.IN_PROGRESS, updatedEpicInMemory.getStatus());
    }

    @Test
    void termIntercept_shouldGenerateExceptionWhenInterceptTermFromDifferentTasks() {
        // prepare
        Task task = new Task("Приготовить завтрак", "Сварить макароны и пожарить котлету",
                TaskStatus.NEW, LocalDateTime.of(2000, 1, 1, 1, 1),
                Duration.ofMinutes(100));
        final Long savedtaskId = taskManager.create(task);
        Epic epic = new Epic("Испечь торт", "Испечь торт Наполеон");
        final Long savedEpicId = taskManager.create(epic);
        SubTask subTask1 = new SubTask(savedEpicId, "Найти рецепт1",
                "Выполнить поиск видео рецепта",
                TaskStatus.IN_PROGRESS, LocalDateTime.of(2010, 1, 1, 1, 0),
                Duration.ofMinutes(100));
        final Long savedSubTask1Id = taskManager.create(subTask1);

        // do // check
        assertThrows(TaskValidationException.class, () -> {
            taskManager.create(new Task("Приготовить завтрак1",
                    "Сварить макароны и пожарить котлету1", TaskStatus.NEW,
                    LocalDateTime.of(2000, 1, 1, 1, 50), Duration.ofMinutes(100)));
        }, "Указанное время уже занято, задачей: " + task);
        assertThrows(TaskValidationException.class, () -> {
            taskManager.create(new SubTask(savedEpicId, "Найти рецепт1",
                    "Выполнить поиск видео рецепта", TaskStatus.NEW,
                    LocalDateTime.of(2000, 1, 1, 1, 50), Duration.ofMinutes(100)));
        }, "Указанное время уже занято, задачей: " + task);
        Assertions.assertEquals(LocalDateTime.of(2010, 1, 1, 1, 0),
                taskManager.getEpic(savedEpicId).getStartTime());
        assertThrows(TaskValidationException.class, () -> {
            taskManager.create(new SubTask(savedEpicId, "Найти рецепт2",
                    "Выполнить поиск видео рецепта",
                    TaskStatus.IN_PROGRESS, LocalDateTime.of(2010, 1, 1, 0, 59),
                    Duration.ofMinutes(15)));
        }, "Указанное время уже занято, задачей: " + subTask1);
    }

    @Test
    void updateEpicTerm_shouldUpdateEpicTermFormSubTasks() {
        // prepare
        Epic epic = new Epic("Испечь торт", "Испечь торт Наполеон");
        final Long savedEpicId = taskManager.create(epic);
        SubTask subTask1 = new SubTask(savedEpicId, "Найти рецепт1",
                "Выполнить поиск видео рецепта",
                TaskStatus.IN_PROGRESS, LocalDateTime.of(2010, 1, 1, 1, 59),
                Duration.ofMinutes(100));
        SubTask subTask2 = new SubTask(savedEpicId, "Найти рецепт2",
                "Выполнить поиск видео рецепта",
                TaskStatus.IN_PROGRESS, LocalDateTime.of(2010, 1, 2, 1, 1),
                Duration.ofMinutes(33));

        // do
        final Long savedSubTask1Id = taskManager.create(subTask1);
        final Long savedSubTask2Id = taskManager.create(subTask2);

        // check
        Assertions.assertEquals(LocalDateTime.of(2010, 1, 1, 1, 59),
                taskManager.getEpic(savedEpicId).getStartTime());
        Assertions.assertEquals(LocalDateTime.of(2010, 1, 2, 1, 34),
                taskManager.getEpic(savedEpicId).getEndTime());

    }


    @Test
    void testEpicInEpic() {
        /*
        ¯\_(ツ)_/¯
         */
    }
}