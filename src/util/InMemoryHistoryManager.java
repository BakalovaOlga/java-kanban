package util;

import tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private List<Task> historyList = new ArrayList<>();

    @Override
    public void add(Task task) {
        if (task == null){
            return;
        }
            Task savedTask = new Task(task.getTitle(), task.getDescription());
            savedTask.setStatus(task.getStatus());
            savedTask.setId(task.getId());
            historyList.add(savedTask);
            if (historyList.size() > 10){
                historyList.removeFirst();
            }

    }

    @Override
    public List<Task> getHistory(){
        return new ArrayList<>(historyList);
    }
}
