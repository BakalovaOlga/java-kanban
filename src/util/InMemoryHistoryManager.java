package util;

import tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    List<Task> historyList = new ArrayList<>();

    @Override
    public void add(Task task) {
        if (task != null){
            Task savedTask = new Task(task.getTitle(), task.getDescription());
            savedTask.setStatus(task.getStatus());
            savedTask.setId(task.getId());
            if (historyList.size() < 10){
                historyList.add(savedTask);
            } else {
                historyList.removeFirst();
                historyList.add(savedTask);
            }
        }
    }

    @Override
    public List<Task> getHistory(){
        return new ArrayList<>(historyList);
    }
}
