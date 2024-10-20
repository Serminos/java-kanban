package manager;

import enums.TaskStatus;
import exception.ManagerSaveException;
import models.Epic;
import models.SubTask;
import models.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static junit.framework.TestCase.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class FileBackedTaskManagerTest {
    private File file;
    private FileBackedTaskManager fileBackedTaskManager;
    private static final String CSV_HEADER = "id,type,name,status,description,epic";

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
    void shouldExceptionWhenLoadEmptyFile() {
        // do
        try {
            FileBackedTaskManager testManager = FileBackedTaskManager.loadFromFile(file);
            Assertions.fail("Проверка должна выдать исключение");
        } catch (ManagerSaveException e) {
            //check
            assertEquals("Размер указанного файла равен нулю: " + file.getAbsolutePath(), e.getMessage());
        }
    }

    @Test
    void shouldExceptionWhenLoadIfFileNotExist() {
        // do
        try {
            file.delete();
            FileBackedTaskManager testManager = FileBackedTaskManager.loadFromFile(file);
            Assertions.fail("Проверка должна выдать исключение");
        } catch (ManagerSaveException e) {
            //check
            assertEquals("Не удается прочитать указанный файл: " + file.getAbsolutePath(), e.getMessage());
        }
    }

    @Test
    void shouldExceptionWhenLoadIfWrongHeaderFormat() {
        // do
        try {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writer.write(CSV_HEADER + ",wrongcolumn" + "\n");
                writer.write("1,TASK,Приготовить завтрак,NEW,Сварить макароны и пожарить котлету," + "\n");
            } catch (Exception ignored) {
            }
            FileBackedTaskManager testManager = FileBackedTaskManager.loadFromFile(file);
            Assertions.fail("Проверка должна выдать исключение");
        } catch (ManagerSaveException e) {
            //check
            assertEquals("Формат CSV-файла не соответствует: " + file.getAbsolutePath()
                    + " . Ожидается:" + CSV_HEADER, e.getMessage());
        }
    }

    @Test
    void shouldExceptionWhenLoadIfWrongDataFormat() {
        // do
        try {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writer.write(CSV_HEADER + "\n");
                writer.write("1,TASK123,Приготовить завтрак,NEW,Сварить макароны и пожарить котлету," + "\n");
            } catch (Exception ignored) {
            }
            FileBackedTaskManager testManager = FileBackedTaskManager.loadFromFile(file);
            Assertions.fail("Проверка должна выдать исключение");
        } catch (ManagerSaveException e) {
            //check
            assertEquals("При чтении файла: " + file.getAbsolutePath()
                    + " . Возникла ошибка при разборе строки № 1", e.getMessage());
        }
    }

    @Test
    void shouldExceptionWhenSaveWrongPath() {
        try {
            // prepare
            file = File.createTempFile("test", ".csv");
            file.delete();
            fileBackedTaskManager = new FileBackedTaskManager(Managers.getDefaultHistory(), file);
            Task task = new Task("Приготовить завтрак", "Сварить макароны и пожарить котлету", TaskStatus.NEW);

            // do
            final Long savedTaskId = fileBackedTaskManager.create(task);
            Assertions.fail("Проверка должна выдать исключение");
        } catch (ManagerSaveException e) {
            //check
            assertEquals("Не удается сохранить файл по указанному пути: " + file.getAbsolutePath(), e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldNotExceptionIfDoesNotHaveData() {
        // do
        try {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writer.write(CSV_HEADER + "\n");
            } catch (Exception ignored) {
            }
            FileBackedTaskManager testManager = FileBackedTaskManager.loadFromFile(file);
        } catch (ManagerSaveException e) {
            //check
            Assertions.fail("Проверка не должна выдать исключение");
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
        assertEquals(fileBackedTaskManager.getTasks().get(0), testManager.getTasks().get(0));
        assertEquals(fileBackedTaskManager.getEpics().size(), testManager.getEpics().size());
        assertEquals(fileBackedTaskManager.getEpics().get(0), testManager.getEpics().get(0));
        assertEquals(fileBackedTaskManager.getSubTasks().size(), testManager.getSubTasks().size());
        assertEquals(fileBackedTaskManager.getSubTasks().get(0), testManager.getSubTasks().get(0));
    }
}