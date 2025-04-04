package tasks;

import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Integer> subtaskId; //список Id подзадач

    public Epic(int id, String title, String description) {
        super(id, title, description);
        this.subtaskId = new ArrayList<>();
    }
    public Epic(String title, String description) {
        super(title, description);
        this.subtaskId = new ArrayList<>();
    }


    public ArrayList<Integer> getSubtaskId() {
        return subtaskId;
    }

    public void setSubtaskId(ArrayList<Integer> subtaskId) {
        this.subtaskId = subtaskId;
    }

    public void addSubtask(int subId) {
        if (subId == this.getId() || subtaskId.contains(subId)) {
            return;
        }
        subtaskId.add(subId);
    }

    @Override
    public String toString() {
        return "\nEpic{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", subtaskId=" + subtaskId +
                '}';
    }
}
