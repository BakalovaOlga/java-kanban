package util;

import tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private Map<Integer, Node> historyNodeMap = new HashMap<>();
    private Node head;
    private Node tail;

    private void linkLast(Node lastNode) {
        if (tail == null) {
            tail = lastNode;
            head = lastNode;
        } else {
            tail.next = lastNode;
            lastNode.prev = tail;
            tail = lastNode;
        }
    }

    private void removeNode(Node node) {
        if (node == null) return;
        //для головы
        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }
        //для хвоста
        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        int id = task.getId();
        remove(id);
        Task copyOfTask = Task.copyTask(task);
        Node newNode = new Node(copyOfTask);
        linkLast(newNode);
        historyNodeMap.put(id, newNode);
    }

    @Override
    public void remove(int id) {
        if (historyNodeMap.containsKey(id)) {
            Node node = historyNodeMap.get(id);
            removeNode(node);
            historyNodeMap.remove(id);
        }
    }

    @Override
    public List<Task> getHistory() {
        List<Task> historyList = new ArrayList<>();
        Node current = head;

        while (current != null) {
            historyList.add(current.task);
            current = current.next;
        }
        return historyList;
    }


    private static class Node {
        Task task;
        Node prev; //предыдущий узел
        Node next; //следующий узел

        Node(Task task) {
            this.task = task;
        }
    }
}



