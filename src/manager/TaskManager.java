package manager;

import models.Epic;
import models.SubTask;
import models.Task;

import java.util.List;

public interface TaskManager {

    Long create(Task task);

    boolean update(Task task);

    Long create(Epic epic);

    boolean update(Epic epic);

    Long create(SubTask subTask);

    boolean update(SubTask subTask);

    Task getTask(Long id);

    Epic getEpic(Long id);

    SubTask getSubTask(Long id);

    List<SubTask> getAllSubTasksByEpicId(Long id);

    void clearTasks();

    void clearEpics();

    void clearSubTasks();

    void clear();

    void removeTask(Long id);

    void removeEpic(Long id);

    void removeSubTask(Long id);

    List<String> getAllTasksText();

    Task getById(Long id);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks(boolean sort);
}
