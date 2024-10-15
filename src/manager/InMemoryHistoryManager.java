package manager;

import models.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    protected HashMap<Long, Node> history = new HashMap<>();
    private Node head;
    private Node tail;

    /**
     * @param task
     */
    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        final long id = task.getId();
        remove(id);
        linkLast(task);
        history.put(id, tail);
    }

    /**
     * @param id
     */
    @Override
    public void remove(Long id) {
        final Node node = history.remove(id);
        if (node == null) {
            return;
        }
        removeNode(node);
    }

    private void removeNode(Node node) {
        if (node.prev != null) {
            node.prev.next = node.next;
            if (node.next == null) {
                tail = node.prev;
            } else {
                node.next.prev = node.prev;
            }
        } else {
            head = node.next;
            if (head == null) {
                tail = null;
            } else {
                head.prev = null;
            }
        }
    }

    /**
     * @return List<Task>
     */
    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    private void linkLast(Task task) {
        final Node node = new Node(task, tail, null);
        if (head == null) {
            head = node;
        } else {
            tail.next = node;
        }
        tail = node;
    }

    /**
     * @return List<Task>
     */
    private List<Task> getTasks() {
        List<Task> listTasks = new ArrayList<>();
        if (!history.isEmpty()) {
            Node node = head;
            while (node.next != null) {
                listTasks.add(node.task);
                node = node.next;
            }
            listTasks.add(node.task);
        }
        return listTasks;
    }
}
