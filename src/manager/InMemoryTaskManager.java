package manager;

import enums.TaskStatus;
import models.Epic;
import models.SubTask;
import models.Task;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected long id;
    protected final HistoryManager historyManager;
    protected final Map<Long, Task> tasks = new HashMap<>();
    protected final Map<Long, Epic> epics = new HashMap<>();
    protected final Map<Long, SubTask> subTasks = new HashMap<>();

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    public long nextId() {
        return ++id;
    }

    @Override
    public Long create(Task task) {
        if (task.getId() == null) {
            long newTaskId = nextId();
            task.setId(newTaskId);
        }
        tasks.put(task.getId(), task);
        return task.getId();
    }

    @Override
    public boolean update(Task task) {
        if (task.getId() != null) {
            tasks.put(task.getId(), task);
            return true;
        }
        return false;
    }

    @Override
    public Long create(Epic epic) {
        if (epic.getId() == null) {
            long newTaskId = nextId();
            epic.setId(newTaskId);
        }
        epics.put(epic.getId(), epic);
        return epic.getId();
    }

    @Override
    public boolean update(Epic epic) {
        if (epic.getId() != null) {
            epics.put(epic.getId(), epic);
            return true;
        }
        return false;
    }

    @Override
    public Long create(SubTask subTask) {
        long newSubTaskId;
        if (subTask.getId() == null) {
            newSubTaskId = nextId();
            subTask.setId(newSubTaskId);
        }
        newSubTaskId = subTask.getId();
        Epic epic = epics.get(subTask.getEpicId());
        if (epic != null) {
            subTasks.put(newSubTaskId, subTask);
            epic.setSubTaskIds(newSubTaskId);
            updateEpicStatus(epic);
            return newSubTaskId;
        }
        return null;
    }

    @Override
    public boolean update(SubTask subTask) {
        if (subTask.getId() != null) {
            long subTaskId = subTask.getId();
            Epic epic = epics.get(subTask.getEpicId());
            if (epic != null) {
                subTasks.put(subTaskId, subTask);
                epic.setSubTaskIds(subTaskId);
                updateEpicStatus(epic);
                return true;
            }
        }
        return false;
    }

    public List<Task> getTasks() {
        return tasks.values().stream().toList();
    }

    public List<Epic> getEpics() {
        return epics.values().stream().toList();
    }

    public List<SubTask> getSubTasks() {
        return subTasks.values().stream().toList();
    }

    @Override
    public Task getTask(Long id) {
        Task task = tasks.getOrDefault(id, null);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Epic getEpic(Long id) {
        Epic epic = epics.getOrDefault(id, null);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public List<SubTask> getAllSubTasksByEpicId(Long id) {
        Epic epic = getEpic(id);
        List<SubTask> epicSubTasks = new ArrayList<>();
        if (epic != null) {
            epic.getSubTaskIds().forEach(subTaskId -> epicSubTasks.add(subTasks.get(subTaskId)));
            return epicSubTasks;
        }
        return epicSubTasks;
    }

    @Override
    public SubTask getSubTask(Long id) {
        SubTask subTask = subTasks.getOrDefault(id, null);
        if (subTask != null) {
            historyManager.add(subTask);
        }
        return subTask;
    }

    @Override
    public void clearTasks() {
        tasks.clear();
    }

    public void clearEpics() {
        epics.clear();
        subTasks.clear();
    }

    @Override
    public void clearSubTasks() {
        subTasks.clear();
    }

    @Override
    public void clear() {
        clearTasks();
        clearEpics();
        clearSubTasks();
        id = 0;
    }

    @Override
    public void removeTask(Long id) {
        tasks.remove(id);
    }

    @Override
    public void removeEpic(Long id) {
        Epic epic = this.getEpic(id);
        if (epic != null) {
            for (Long subtaskId : epic.getSubTaskIds()) {
                subTasks.remove(subtaskId);
            }
            epics.remove(id);
        }
    }

    @Override
    public void removeSubTask(Long id) {
        SubTask subTask = subTasks.get(id);
        if (subTask != null) {
            Epic epic = getEpic(subTask.getEpicId());
            subTasks.remove(id);
            epic.getSubTaskIds().remove(subTask.getId());
            updateEpicStatus(epic);
        }
    }

    public void updateEpicStatus(Epic epic) {
        if (!epics.containsKey(epic.getId())) {
            return;
        }
        long epicSubTaskSize = epic.getSubTaskIds().size();
        if (epicSubTaskSize == 0) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }
        List<SubTask> epicSubTasks = new ArrayList<>();
        epic.getSubTaskIds().forEach(subTaskId -> epicSubTasks.add(subTasks.get(subTaskId)));

        long countInDone = epicSubTasks.stream().filter(subTask -> subTask.getStatus() == TaskStatus.DONE).count();
        if (countInDone == epicSubTaskSize) {
            epic.setStatus(TaskStatus.DONE);
            return;
        }

        long countInNew = epicSubTasks.stream().filter(subTask -> subTask.getStatus() == TaskStatus.NEW).count();
        if (countInNew == epicSubTaskSize) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        epic.setStatus(TaskStatus.IN_PROGRESS);
    }

    @Override
    public List<String> getAllTasksText() {
        List<String> resultText = new ArrayList<>();
        this.getTasks().forEach(v -> resultText.add(v.toString()));
        this.getEpics().forEach(v -> resultText.add(v.toString()));
        this.getSubTasks().forEach(v -> resultText.add(v.toString()));
        return resultText;
    }

    @Override
    public Task getById(Long id) {
        if (this.getTask(id) != null) return this.getTask(id);
        if (this.getEpic(id) != null) return this.getEpic(id);
        if (this.getSubTask(id) != null) return this.getSubTask(id);
        return null;
    }

    /**
     * @return Task
     */
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}
