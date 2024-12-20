package com.example.a1201766_1201086_courseproject;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.List;

public class TodayTaskAdapter extends BaseTaskAdapter<Task> {
    private TodayFragment fragment; // Reference to TodayFragment

    public TodayTaskAdapter(Context context, List<Task> taskList, TaskDatabaseHelper taskDatabaseHelper, TodayFragment fragment) {
        super(context, taskList, taskDatabaseHelper);
        this.fragment = fragment; // Initialize the fragment reference
    }

    @Override
    protected int getLayoutId() {
        return R.layout.item_task;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        holder.markCompleteButton.setOnClickListener(v -> {
            Task task = getTaskList().get(position);
            task.setStatus("completed");

            // Update the task status in the database
            boolean updated = taskDatabaseHelper.updateTaskStatus(task.getId(), "completed");

            if (updated) {
                Toast.makeText(context, "Task marked as completed", Toast.LENGTH_SHORT).show();
                notifyItemChanged(position);

                // Trigger congratulatory check
                if (fragment != null) {
                    fragment.checkAndShowCongratulations();
                }
            }
        });
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

    // Method to update and sort the task list
    public void sortTasksByPriority(boolean isAscending) {
        // Sort tasks by priority in ascending or descending order
        getTaskList().sort((task1, task2) -> {
            int priority1 = getPriorityValue(task1.getPriority());
            int priority2 = getPriorityValue(task2.getPriority());
            return isAscending ? Integer.compare(priority1, priority2) : Integer.compare(priority2, priority1);
        });

        // Notify the adapter about data changes
        notifyDataSetChanged();
    }

    // Helper method to convert priority string to a numeric value
    private int getPriorityValue(String priority) {
        switch (priority.toLowerCase()) {
            case "high":
                return 1;
            case "medium":
                return 2;
            case "low":
                return 3;
            default:
                return 4; // For any unexpected priority
        }
    }

}
