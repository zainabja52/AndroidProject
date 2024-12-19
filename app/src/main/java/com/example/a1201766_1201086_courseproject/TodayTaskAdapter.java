package com.example.a1201766_1201086_courseproject;

import android.content.Context;
import java.util.List;

public class TodayTaskAdapter extends BaseTaskAdapter<Task> {

    public TodayTaskAdapter(Context context, List<Task> taskList, TaskDatabaseHelper taskDatabaseHelper) {
        super(context, taskList, taskDatabaseHelper);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.item_task;
    }

    // Check if all tasks are completed
    public boolean areAllTasksCompleted() {
        for (Task task : getTaskList()) {
            if (!"completed".equalsIgnoreCase(task.getStatus())) {
                return false;
            }
        }
        return true;
    }
}
