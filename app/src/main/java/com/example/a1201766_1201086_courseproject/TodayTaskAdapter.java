package com.example.a1201766_1201086_courseproject;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

        Task task = getTaskList().get(holder.getAdapterPosition());

        // Set up Spinner for status options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                context,
                R.array.status_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.statusSpinner.setAdapter(adapter);

        // Set current status in the Spinner
        int spinnerPosition = task.getStatus().equalsIgnoreCase("completed") ? 1 : 0;
        holder.statusSpinner.setSelection(spinnerPosition);

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

                        // Trigger congratulatory check if all tasks are completed
                        if (fragment != null) {
                            fragment.checkAndShowCongratulations();
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
