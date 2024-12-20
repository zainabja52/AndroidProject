package com.example.a1201766_1201086_courseproject;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TaskAdapter extends BaseTaskAdapter<Task> {
    private AllTasksFragment allTasksFragment; // Reference to AllTasksFragment
    private CompletedTasksFragment completedTasksFragment; // Reference to CompletedTasksFragment

    public TaskAdapter(Context context, Map<String, List<Task>> groupedTasks, TaskDatabaseHelper taskDatabaseHelper,
                       AllTasksFragment allTasksFragment, CompletedTasksFragment completedTasksFragment) {
        super(context, flattenTasks(groupedTasks), taskDatabaseHelper);
        this.allTasksFragment = allTasksFragment;
        this.completedTasksFragment = completedTasksFragment;
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
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        Task task = getTaskList().get(holder.getAdapterPosition());

        // Set up Spinner for status options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                context,
                R.array.status_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.statusSpinner.setAdapter(adapter);

        // Validate task status and set spinner position
        String status = task.getStatus();
        if (status == null || status.isEmpty()) {
            status = "pending"; // Default to a valid value
        }

        int spinnerPosition = adapter.getPosition(status);
        if (spinnerPosition != AdapterView.INVALID_POSITION) {
            holder.statusSpinner.setSelection(spinnerPosition);
        } else {
            Log.e("TaskAdapter", "Invalid status: " + status + " for Task ID: " + task.getId());
        }

        // Handle status changes dynamically
        holder.statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selectedStatus = parent.getItemAtPosition(pos).toString();
                if (!selectedStatus.equalsIgnoreCase(task.getStatus())) {
                    task.setStatus(selectedStatus);
                    boolean updated = taskDatabaseHelper.updateTaskStatus(task.getId(), selectedStatus);

                    if (updated) {
                        Toast.makeText(context, "Task status updated to " + selectedStatus, Toast.LENGTH_SHORT).show();
                        notifyItemChanged(holder.getAdapterPosition());

                        // Trigger updates in AllTasksFragment and CompletedTasksFragment
                        if (allTasksFragment != null) {
                            allTasksFragment.loadTasks();
                        }

                        if (completedTasksFragment != null) {
                            completedTasksFragment.loadCompletedTasks();
                        }
                    } else {
                        Toast.makeText(context, "Failed to update task status", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

}
