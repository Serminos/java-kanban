package manager;

import enums.TaskStatus;
import enums.TaskType;
import exception.ManagerSaveException;
import models.Epic;
import models.SubTask;
import models.Task;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {
    private final File file;
    private static final String CSV_HEADER = "id,type,name,status,description,epic";

    public FileBackedTaskManager(HistoryManager historyManager, File file) {
        super(historyManager);
        this.file = file;
    }

    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write(CSV_HEADER + "\n");
            for (Task task : getTasks()) {
                writer.write(toString(task) + "\n");
            }
            for (Epic epic : getEpics()) {
                writer.write(toString(epic) + "\n");
            }
            for (SubTask subtask : getSubTasks()) {
                writer.write(toString(subtask) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении данных", e);
        }
    }

    /**
     * @param task
     * @return String for CSV format
     */
    private String toString(Task task) {
        if (task instanceof SubTask subtask) {
            return String.format("%d,%s,%s,%s,%s,%d",
                    subtask.getId(),
                    TaskType.SUBTASK,
                    subtask.getName(),
                    subtask.getStatus(),
                    subtask.getDescription(),
                    subtask.getEpicId()
            );
        } else if (task instanceof Epic epic) {
            return String.format("%d,%s,%s,%s,%s,%s",
                    epic.getId(),
                    TaskType.EPIC,
                    epic.getName(),
                    epic.getStatus(),
                    epic.getDescription(),
                    ""
            );
        } else {
            return String.format("%d,%s,%s,%s,%s,%s",
                    task.getId(),
                    TaskType.TASK,
                    task.getName(),
                    task.getStatus(),
                    task.getDescription(),
                    ""
            );
        }
    }

    private static Task fromString(String value) {
        String[] fields = value.split(",");

        Long id = Long.parseLong(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        TaskStatus taskStatus = TaskStatus.valueOf(fields[3]);
        String description = fields[4];

        switch (type) {
            case TASK:
                return new Task(id, name, description, taskStatus);
            case EPIC:
                return new Epic(id, name, description, taskStatus);
            case SUBTASK:
                String epicIdString = fields[5];
                Long epicId = Long.parseLong(epicIdString);
                return new SubTask(id, epicId, name, description, taskStatus);
            default:
                throw new ManagerSaveException("Неизвестный тип задачи: " + type, null);
        }
    }

    /**
     * @param file
     * @return
     */
    public static FileBackedTaskManager loadFromFile(File file) {
        HistoryManager historyManager = Managers.getDefaultHistory();
        FileBackedTaskManager manager = new FileBackedTaskManager(historyManager, file);
        try {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            for (int i = 1; i < lines.size(); i++) {
                Task task = fromString(lines.get(i));
                if (task instanceof SubTask subtask) {
                    manager.create(subtask);
                } else if (task instanceof Epic epic) {
                    manager.create(epic);
                } else {
                    manager.create(task);
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки файла", e);
        }
        return manager;
    }

    /**
     * @param task
     * @return
     */
    @Override
    public Long create(Task task) {
        Long id = super.create(task);
        save();
        return id;
    }

    /**
     * @param task
     * @return
     */
    @Override
    public boolean update(Task task) {
        boolean result = super.update(task);
        save();
        return result;
    }

    /**
     * @param epic
     * @return
     */
    @Override
    public Long create(Epic epic) {
        Long id = super.create(epic);
        save();
        return id;
    }

    /**
     * @param epic
     * @return
     */
    @Override
    public boolean update(Epic epic) {
        boolean result = super.update(epic);
        save();
        return result;
    }

    /**
     * @param subTask
     * @return
     */
    @Override
    public Long create(SubTask subTask) {
        Long id = super.create(subTask);
        save();
        return id;
    }

    /**
     * @param subTask
     * @return
     */
    @Override
    public boolean update(SubTask subTask) {
        boolean result = super.update(subTask);
        save();
        return result;
    }

    /**
     *
     */
    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    /**
     *
     */
    @Override
    public void clearEpics() {
        super.clearEpics();
        save();
    }

    /**
     *
     */
    @Override
    public void clearSubTasks() {
        super.clearSubTasks();
        save();
    }

    /**
     *
     */
    @Override
    public void clear() {
        super.clear();
        save();
    }

    /**
     * @param id
     */
    @Override
    public void removeTask(Long id) {
        super.removeTask(id);
        save();
    }

    /**
     * @param id
     */
    @Override
    public void removeEpic(Long id) {
        super.removeEpic(id);
        save();
    }

    /**
     * @param subtaskId
     */
    @Override
    public void removeSubTask(Long subtaskId) {
        super.removeSubTask(subtaskId);
        save();
    }
}
