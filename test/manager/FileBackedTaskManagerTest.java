package manager;

import enums.TaskStatus;
import models.Epic;
import models.SubTask;
import models.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class FileBackedTaskManagerTest {
    private File file;
    private FileBackedTaskManager fileBackedTaskManager;

    @BeforeEach
    void beforeEachTest() {
        try {
            file = File.createTempFile("test", ".csv");
            file.deleteOnExit();
            fileBackedTaskManager = new FileBackedTaskManager(Managers.getDefaultHistory(), file);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать файл", e);
        }
    }

    @Test
    void shouldSaveAndLoadEmptyTasks() {
        // prepare
        Task task = new Task("Приготовить завтрак", "Сварить макароны и пожарить котлету", TaskStatus.NEW);
        Epic epic = new Epic("Испечь торт", "Испечь торт Наполеон");
        final Long savedTaskId = fileBackedTaskManager.create(task);
        final Long savedEpicId = fileBackedTaskManager.create(epic);
        SubTask subTask = new SubTask(savedEpicId, "Найти рецепт",
                "Выполнить поиск видео рецепта", TaskStatus.NEW);
        final Long savedSubTaskId = fileBackedTaskManager.create(subTask);
        fileBackedTaskManager.clear();

        // do
        FileBackedTaskManager testManager = FileBackedTaskManager.loadFromFile(file);

        //check
        assertEquals(0, testManager.getTasks().size());
        assertEquals(0, testManager.getEpics().size());
        assertEquals(0, testManager.getSubTasks().size());
    }


    @Test
    void shouldSaveAndLoadDifferentTasks() {
        // prepare
        Task task = new Task("Приготовить завтрак", "Сварить макароны и пожарить котлету", TaskStatus.NEW);
        Epic epic = new Epic("Испечь торт", "Испечь торт Наполеон");
        final Long savedTaskId = fileBackedTaskManager.create(task);
        final Long savedEpicId = fileBackedTaskManager.create(epic);
        SubTask subTask = new SubTask(savedEpicId, "Найти рецепт",
                "Выполнить поиск видео рецепта", TaskStatus.NEW);
        final Long savedSubTaskId = fileBackedTaskManager.create(subTask);

        // do
        FileBackedTaskManager testManager = FileBackedTaskManager.loadFromFile(file);

        //check
        assertEquals(fileBackedTaskManager.getTasks().size(), testManager.getTasks().size());
        assertEquals(fileBackedTaskManager.getTasks().get(0).toString(), testManager.getTasks().get(0).toString());
        assertEquals(fileBackedTaskManager.getEpics().size(), testManager.getEpics().size());
        assertEquals(fileBackedTaskManager.getEpics().get(0).toString(), testManager.getEpics().get(0).toString());
        assertEquals(fileBackedTaskManager.getSubTasks().size(), testManager.getSubTasks().size());
        assertEquals(fileBackedTaskManager.getSubTasks().get(0).toString(), testManager.getSubTasks().get(0).toString());
    }
}