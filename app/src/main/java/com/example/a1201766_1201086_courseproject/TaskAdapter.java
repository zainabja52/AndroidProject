package com.example.a1201766_1201086_courseproject;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TaskAdapter extends BaseTaskAdapter<Task> {

    public TaskAdapter(Context context, Map<String, List<Task>> groupedTasks, TaskDatabaseHelper taskDatabaseHelper) {
        super(context, flattenTasks(groupedTasks), taskDatabaseHelper);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.item_task;
    }

    // Helper method to flatten Map<String, List<Task>> into List<Task>
    private static List<Task> flattenTasks(Map<String, List<Task>> groupedTasks) {
        List<Task> allTasks = new ArrayList<>();
        for (List<Task> tasks : groupedTasks.values()) {
            allTasks.addAll(tasks);
        }
        return allTasks;
    }

    public void updateGroupedTasks(Map<String, List<Task>> newGroupedTasks) {
        List<Task> updatedTasks = flattenTasks(newGroupedTasks);
        updateTaskList(updatedTasks); // Use BaseTaskAdapter's method to update the list
    }

}
