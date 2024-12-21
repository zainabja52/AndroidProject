package com.example.a1201766_1201086_courseproject;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TaskAdapter extends BaseTaskAdapter<Task> {
    private AllTasksFragment allTasksFragment;
    private CompletedTasksFragment completedTasksFragment;

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

    private static List<Task> flattenTasks(Map<String, List<Task>> groupedTasks) {
        List<Task> allTasks = new ArrayList<>();
        groupedTasks.forEach((date, tasks) -> {
            tasks.forEach(task -> {
                System.out.println("Flattening task: " + task.getTitle() + " | Date: " + date);
            });
            allTasks.addAll(tasks);
        });
        return allTasks;
    }


    public void updateGroupedTasks(Map<String, List<Task>> newGroupedTasks) {
        List<Task> updatedTasks = flattenTasks(newGroupedTasks);
        updateTaskList(updatedTasks);
        System.out.println("TaskAdapter: Updated grouped tasks. Total tasks: " + updatedTasks.size());
        notifyDataSetChanged();
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        Task task = getTaskList().get(holder.getAdapterPosition());


        ArrayList<String> statusOptions = new ArrayList<>();
        statusOptions.add("pending");
        statusOptions.add("completed");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_item,
                statusOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.statusSpinner.setAdapter(adapter);

        Log.d("TaskAdapter", "Spinner adapter set with options: " + Arrays.toString(context.getResources().getStringArray(R.array.status_options)));


        String status = task.getStatus();
        if (status == null || status.isEmpty()) {
            status = "pending";
        } else {
            status = status.toLowerCase();
        }

        int spinnerPosition = adapter.getPosition(status);
        if (spinnerPosition == AdapterView.INVALID_POSITION) {
            Log.e("TaskAdapter", "Invalid status: " + status + " for Task ID: " + task.getId());
            spinnerPosition = 0;
        }
        holder.statusSpinner.setSelection(spinnerPosition);



        holder.statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selectedStatus = parent.getItemAtPosition(pos).toString();
                if (!selectedStatus.equalsIgnoreCase(task.getStatus())) {
                    task.setStatus(selectedStatus);
                    boolean updated = taskDatabaseHelper.updateTaskStatus(task.getId(), selectedStatus);

                    if (updated) {
                        Toast.makeText(context, "Task status updated to " + selectedStatus, Toast.LENGTH_SHORT).show();

                        taskList.set(holder.getAdapterPosition(), task);
                        notifyItemChanged(holder.getAdapterPosition());

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
    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

}
