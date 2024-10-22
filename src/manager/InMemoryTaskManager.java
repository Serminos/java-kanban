package manager;

import enums.TaskStatus;
import exception.TaskValidationException;
import models.Epic;
import models.SubTask;
import models.Task;

import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected long id;
    protected final HistoryManager historyManager;
    protected final Map<Long, Task> tasks = new HashMap<>();
    protected final Map<Long, Epic> epics = new HashMap<>();
    protected final Map<Long, SubTask> subTasks = new HashMap<>();
    protected final TreeSet<Task> sortedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime,
            Comparator.nullsLast(Comparator.naturalOrder())));

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
        if (task.getStartTime() != null && task.getDuration() != null) {
            Optional<Task> taskIntercepted = sortedTasks.stream()
                    .filter(existingTask -> isTaskTermIntercept(existingTask, task)).findFirst();
            if (taskIntercepted.isPresent()) {
                throw new TaskValidationException("Указанное время уже занято, задачей: " + taskIntercepted.get());
            }
            sortedTasks.add(task);
        }
        tasks.put(task.getId(), task);
        return task.getId();
    }

    @Override
    public boolean update(Task task) {
        if (task.getId() != null) {
            if (task.getStartTime() != null && task.getDuration() != null) {
                Optional<Task> taskIntercepted = sortedTasks.stream()
                        .filter(existingTask -> isTaskTermIntercept(existingTask, task)).findFirst();
                if (taskIntercepted.isPresent()) {
                    throw new TaskValidationException("Указанное время уже занято, задачей: " + taskIntercepted.get());
                }
                sortedTasks.add(task);
            }
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
        if (epic.getStartTime() != null && epic.getDuration() != null) {
            Optional<Task> taskIntercepted = sortedTasks.stream()
                    .filter(existingTask -> isTaskTermIntercept(existingTask, epic)).findFirst();
            if (taskIntercepted.isPresent()) {
                throw new TaskValidationException("Указанное время уже занято, задачей: " + taskIntercepted.get());
            }
            sortedTasks.add(epic);
        }
        epics.put(epic.getId(), epic);
        return epic.getId();
    }

    @Override
    public boolean update(Epic epic) {
        if (epic.getId() != null) {
            if (epic.getStartTime() != null && epic.getDuration() != null) {
                Optional<Task> taskIntercepted = sortedTasks.stream()
                        .filter(existingTask -> isTaskTermIntercept(existingTask, epic)).findFirst();
                if (taskIntercepted.isPresent()) {
                    throw new TaskValidationException("Указанное время уже занято, задачей: " + taskIntercepted.get());
                }
                sortedTasks.add(epic);
            }
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
            if (subTask.getStartTime() != null && subTask.getDuration() != null) {
                Optional<Task> taskIntercepted = sortedTasks.stream()
                        .filter(existingTask -> isTaskTermIntercept(existingTask, subTask))
                        .filter(existingTask -> existingTask.getId() != subTask.getEpicId())//epic exclude
                        .findFirst();
                if (taskIntercepted.isPresent()) {
                    throw new TaskValidationException("Указанное время уже занято, задачей: " + taskIntercepted.get());
                }
                sortedTasks.add(subTask);
            }
            subTasks.put(newSubTaskId, subTask);
            epic.setSubTaskId(newSubTaskId);
            updateEpicAfterSubTaskChange(epic);
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
                if (subTask.getStartTime() != null && subTask.getDuration() != null) {
                    Optional<Task> taskIntercepted = sortedTasks.stream()
                            .filter(existingTask -> isTaskTermIntercept(existingTask, subTask)).findFirst();
                    if (taskIntercepted.isPresent()) {
                        throw new TaskValidationException("Указанное время уже занято, задачей: " + taskIntercepted.get());
                    }
                    sortedTasks.add(subTask);
                }
                subTasks.put(subTaskId, subTask);
                epic.setSubTaskId(subTaskId);
                updateEpicAfterSubTaskChange(epic);
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
        tasks.values().forEach(task -> {
            historyManager.remove(task.getId());
            sortedTasks.remove(task);
        });
        tasks.clear();
    }

    public void clearEpics() {
        epics.values().forEach(epic -> {
            historyManager.remove(epic.getId());
            sortedTasks.remove(epic);
        });
        subTasks.values().forEach(subTask -> {
            historyManager.remove(subTask.getId());
            sortedTasks.remove(subTask);
        });
        epics.clear();
        subTasks.clear();
    }

    @Override
    public void clearSubTasks() {
        subTasks.values().forEach(subTask -> {
            historyManager.remove(subTask.getId());
            sortedTasks.remove(subTask);
        });
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
        tasks.values().stream()
                .filter(task -> task.getId().equals(id))
                .forEach(task -> {
                    historyManager.remove(task.getId());
                    sortedTasks.remove(task);
                });
        tasks.remove(id);
    }

    @Override
    public void removeEpic(Long id) {
        Epic epic = this.getEpic(id);
        if (epic != null) {
            epic.getSubTaskIds().forEach(subtaskId -> {
                sortedTasks.remove(subTasks.get(subtaskId));
                subTasks.remove(subtaskId);
                historyManager.remove(subtaskId);
            });
            epics.remove(id);
            historyManager.remove(id);
            if (sortedTasks.contains(epic)) {
                sortedTasks.remove(epic);
            }
        }
    }

    @Override
    public void removeSubTask(Long subtaskId) {
        SubTask subTask = subTasks.get(subtaskId);
        if (subTask != null) {
            Epic epic = getEpic(subTask.getEpicId());
            subTasks.remove(subtaskId);
            historyManager.remove(subtaskId);
            sortedTasks.remove(subTask);
            epic.getSubTaskIds().remove(subTask.getId());
            updateEpicAfterSubTaskChange(epic);
        }
    }

    private void updateEpicAfterSubTaskChange(Epic epic) {
        if (!epics.containsKey(epic.getId())) {
            return;
        }
        updateEpicStatus(epic);
        updateEpicTerm(epic);
    }

    public void updateEpicTerm(Epic epic) {
        List<Long> epicSubTasksId = epic.getSubTaskIds();
        if (epicSubTasksId.size() == 0) {
            epic.setDuration(0L);
            return;
        }
        LocalDateTime epicStartTime = null;
        LocalDateTime epicEndTime = null;
        long duration = 0L;
        for (Long id : epicSubTasksId) {
            SubTask subTask = subTasks.get(id);
            if (subTask.getStartTime() == null || subTask.getEndTime() == null) {
                continue;
            }
            if (epicStartTime != null) {
                epicStartTime = subTask.getStartTime().isBefore(epicStartTime) ? subTask.getStartTime() : epicStartTime;
            } else {
                epicStartTime = subTask.getStartTime();
            }
            if (epicEndTime != null) {
                epicEndTime = subTask.getEndTime().isAfter(epicEndTime) ? subTask.getEndTime() : epicEndTime;
            } else {
                epicEndTime = subTask.getEndTime();
            }
            duration += subTask.getDuration().toMinutes();
        }
        epic.setDuration(duration);
        epic.setStartTime(epicStartTime);
        epic.setEndTime(epicEndTime);
    }

    public void updateEpicStatus(Epic epic) {
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
        return List.copyOf(historyManager.getHistory());
    }

    /**
     * @return Task
     */
    @Override
    public List<Task> getPrioritizedTasks(boolean asc) {
        return asc ? List.copyOf(sortedTasks) : List.copyOf(sortedTasks.descendingSet());
    }

    private boolean isTaskTermIntercept(Task task1, Task task2) {
        if (task1.getStartTime().isBefore(task2.getEndTime()) && task2.getStartTime().isBefore(task1.getEndTime())) {
            return true;
        }
        return false;
    }
}
