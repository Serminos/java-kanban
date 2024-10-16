package manager;

import models.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    protected HashMap<Long, Node> history = new HashMap<>();
    private Node head;
    private Node tail;

    private static class Node {
        public Task task;
        public Node prev;
        public Node next;

        public Node(Task task, Node prev, Node next) {
            this.task = task;
            this.prev = prev;
            this.next = next;
        }
    }

    /**
     * @param task
     */
    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        remove(task.getId());
        linkLast(task);
    }

    /**
     * @param id
     */
    @Override
    public void remove(Long id) {
        if (history.containsKey(id)) {
            final Node node = history.get(id);
            removeNode(node);
            history.remove(id);
        }
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
        history.put(task.getId(), node);
    }

    /**
     * @return List<Task>
     */
    private List<Task> getTasks() {
        List<Task> listTasks = new ArrayList<>();
        if (head != null) {
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
