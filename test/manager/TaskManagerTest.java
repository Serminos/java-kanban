package manager;

import enums.TaskStatus;
import models.Epic;
import models.SubTask;
import models.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    void init() {
        taskManager = Managers.getDefault();
    }

    @Test
    void createTask_shouldSaveTask() {
        // prepare
        Task task = new Task("Приготовить завтрак", "Сварить макароны и пожарить котлету", TaskStatus.NEW);

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
    void updateTask_shouldUpdateTaskWithSpecifiedId() {
        // prepare
        Task task = new Task("Приготовить завтрак", "Сварить макароны и пожарить котлету", TaskStatus.NEW);
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

    @Test
    void createEpic_shouldSaveEpic() {
        // prepare
        Epic epic = new Epic("Испечь торт", "Испечь торт Наполеон");

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
    void updateEpic_shouldUpdateEpicWithSpecifiedId() {
        // prepare
        Epic epic = new Epic("Испечь торт", "Испечь торт Наполеон");
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

    @Test
    void createSubTask_shouldCreateSubTaskAndUpdateEpic() {
        // prepare
        Epic epic = new Epic("Испечь торт", "Испечь торт Наполеон");
        final Long savedEpicId = taskManager.create(epic);
        SubTask subTask1 = new SubTask(savedEpicId, "Найти рецепт",
                "Выполнить поиск видео рецепта", TaskStatus.NEW);

        // do
        Long savedSubTask1Id = taskManager.create(subTask1);
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

    @Test
    void updateSubTask_shouldUpdateSubTaskWithSpecifiedIdAndUpdateEpic() {
        // prepare
        Epic epic = new Epic("Испечь торт", "Испечь торт Наполеон");
        final Long savedEpicId = taskManager.create(epic);
        SubTask subTask1 = new SubTask(savedEpicId, "Найти рецепт",
                "Выполнить поиск видео рецепта", TaskStatus.IN_PROGRESS);
        final Long savedSubTask1Id = taskManager.create(subTask1);
        SubTask updatedSubTask1 = new SubTask(savedEpicId, "Найти видео рецепт",
                "Выполнить поиск видео рецепта на youtube", TaskStatus.IN_PROGRESS);
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
        Assertions.assertEquals("Выполнить поиск видео рецепта на youtube", updatedSubTaskInMemory.getDescription());
        Assertions.assertEquals(TaskStatus.IN_PROGRESS, updatedSubTaskInMemory.getStatus());
        // Epic
        Assertions.assertEquals(savedEpicId, updatedEpicInMemory.getId());
        Assertions.assertEquals("Испечь торт", updatedEpicInMemory.getName());
        Assertions.assertEquals("Испечь торт Наполеон", updatedEpicInMemory.getDescription());
        Assertions.assertArrayEquals(List.of(savedSubTask1Id).toArray(), updatedEpicInMemory.getSubTaskIds().toArray());
        Assertions.assertEquals(TaskStatus.IN_PROGRESS, updatedEpicInMemory.getStatus());
    }

    @Test
    void getAllSubTasksByEpicId_shouldReturnSubTaskList() {
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
    void clearTasks_shouldClearTasksOnly() {
        // prepare
        Task task1 = new Task("Приготовить завтрак", "Сварить макароны и пожарить котлету", TaskStatus.NEW);
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
    void clearEpics_shouldClearEpicsAndSubTasksOnly() {
        // prepare
        Epic epic = new Epic("Испечь торт", "Испечь торт Наполеон");
        final Long savedEpicId = taskManager.create(epic);
        SubTask subTask1 = new SubTask(savedEpicId, "Найти рецепт",
                "Выполнить поиск видео рецепта", TaskStatus.NEW);
        Task task = new Task("Приготовить завтрак", "Сварить макароны и пожарить котлету", TaskStatus.NEW);
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
    void clearSubTasks_shouldClearTasksOnly() {
        // prepare
        Epic epic = new Epic("Испечь торт", "Испечь торт Наполеон");
        final Long savedEpicId = taskManager.create(epic);
        SubTask subTask1 = new SubTask(savedEpicId, "Найти рецепт",
                "Выполнить поиск видео рецепта", TaskStatus.NEW);
        Task task = new Task("Приготовить завтрак", "Сварить макароны и пожарить котлету", TaskStatus.NEW);
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
    void clear_shouldClearEpicsAndSubTasksAndTasks() {
        // prepare
        Epic epic = new Epic("Испечь торт", "Испечь торт Наполеон");
        final Long savedEpicId = taskManager.create(epic);
        SubTask subTask1 = new SubTask(savedEpicId, "Найти рецепт",
                "Выполнить поиск видео рецепта", TaskStatus.NEW);
        Task task = new Task("Приготовить завтрак", "Сварить макароны и пожарить котлету", TaskStatus.NEW);
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
    void removeTask_shouldRemoveTaskById() {
        // prepare
        Task task1 = new Task("Приготовить завтрак", "Сварить макароны и пожарить котлету", TaskStatus.NEW);
        final Long savedTask1Id = taskManager.create(task1);

        // do
        taskManager.removeTask(savedTask1Id);

        // check
        Assertions.assertNull(taskManager.getTask(savedTask1Id));
    }

    @Test
    void removeEpic_shouldRemoveEpicByIdAndHisSubTask() {
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
    void removeSubTask_shouldRemoveSubTaskButNotHisEpic() {
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
    void getAllTasksText_shouldReturnTestRepresentationForDifferentTypeTask() {
        // prepare
        Task task1 = new Task("Приготовить завтрак", "Сварить макароны и пожарить котлету", TaskStatus.NEW);
        final Long savedTask1Id = taskManager.create(task1);
        Epic epic = new Epic("Испечь торт", "Испечь торт Наполеон");
        final Long savedEpicId = taskManager.create(epic);
        SubTask subTask1 = new SubTask(savedEpicId, "Найти рецепт",
                "Выполнить поиск видео рецепта", TaskStatus.NEW);
        final Long savedSubTask1Id = taskManager.create(subTask1);
        List<String> expectedTasksText = new ArrayList<>();
        expectedTasksText.add("Task{id=1, name='Приготовить завтрак', description='Сварить макароны и пожарить котлету', status=NEW}");
        expectedTasksText.add("Epic{id=2, name='Испечь торт', description='Испечь торт Наполеон', status=NEW, subtaskIds=[3]}");
        expectedTasksText.add("SubTask{epicId=2, id=3, name='Найти рецепт', description='Выполнить поиск видео рецепта', status=NEW}");

        // do
        List<String> tasksText = taskManager.getAllTasksText();

        // check
        assertArrayEquals(expectedTasksText.toArray(), tasksText.toArray());
    }

    @Test
    void testEpicInEpic() {
        /*
        ¯\_(ツ)_/¯
         */
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
    void getById_shouldReturnTasksEpicsSubTaskById() {
        // prepare
        Task task = new Task("Приготовить завтрак", "Сварить макароны и пожарить котлету", TaskStatus.NEW);
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
        Task task = new Task("Приготовить завтрак", "Сварить макароны и пожарить котлету", TaskStatus.NEW);
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
        Task task = new Task("Приготовить завтрак", "Сварить макароны и пожарить котлету", TaskStatus.NEW);
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
        assertEquals(10, history.size());
    }

    @Test
    void getHistory_shouldSaveDuplicateAndReturnNewValueAfterUpdate() {
        // prepare
        Task task1 = new Task("Приготовить завтрак", "Сварить макароны и пожарить котлету", TaskStatus.NEW);
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
        assertEquals(10, history.size());
        for (int i = 0; i < 10; i++) {
            if (i == 9) {
                assertEquals("Task{id=1, name='Голодовка', description='Вода', status=NEW}",
                        history.get(i).toString());
            } else {
                assertEquals("Task{id=1, name='Приготовить завтрак', description='Сварить макароны и пожарить котлету', status=NEW}",
                        history.get(i).toString());
            }

        }
    }

}