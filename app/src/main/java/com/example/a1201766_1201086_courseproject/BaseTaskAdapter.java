package com.example.a1201766_1201086_courseproject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public abstract class BaseTaskAdapter<T extends Task> extends RecyclerView.Adapter<BaseTaskAdapter.ViewHolder> {

    private final List<Task> taskList;
    private final TaskDatabaseHelper taskDatabaseHelper;
    private final Context context;

    private final int[] colorPalette = {
            0xFFF5D6C3,
            0xFFF8B6AD,
            0xFFC7745E,
            0xFF8A9C6B,
            0xFFFFFACD
    };

    public BaseTaskAdapter(Context context, List<Task> taskList, TaskDatabaseHelper taskDatabaseHelper) {
        this.context = context;
        this.taskList = taskList;
        this.taskDatabaseHelper = taskDatabaseHelper;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(getLayoutId(), parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.taskTitle.setText(task.getTitle());
        holder.taskDescription.setText(task.getDescription());
        holder.taskDueTime.setText(formatTime(task.getDueDate()));

        // Assign a random color for the card background
        int randomColor = colorPalette[new Random().nextInt(colorPalette.length)];
        holder.cardView.setCardBackgroundColor(randomColor);

        // Edit task
        holder.editButton.setOnClickListener(v -> editTask(task, position));

        // Mark task as complete
        holder.markCompleteButton.setOnClickListener(v -> {
            task.setStatus("completed");
            taskDatabaseHelper.updateTaskStatus(task.getId(), "completed");
            Toast.makeText(context, "Task marked as completed", Toast.LENGTH_SHORT).show();
            notifyItemChanged(position);
        });

        // Share task
        holder.shareButton.setOnClickListener(v -> shareTaskViaEmail(task));

        // Delete task
        holder.deleteButton.setOnClickListener(v -> {
            taskDatabaseHelper.deleteTask(task.getId());
            taskList.remove(position);
            Toast.makeText(context, "Task deleted successfully", Toast.LENGTH_SHORT).show();
            notifyItemRemoved(position);
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    // Abstract method to provide layout ID
    protected abstract int getLayoutId();

    // Helper to format time
    private String formatTime(String dueDate) {
        try {
            String[] parts = dueDate.split(" ");
            return parts.length > 1 ? parts[1] : dueDate;
        } catch (Exception e) {
            return dueDate;
        }
    }

    private void editTask(Task task, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_edit_task, null);
        builder.setView(view);

        EditText editTitle = view.findViewById(R.id.editTitle);
        EditText editDescription = view.findViewById(R.id.editDescription);
        EditText editDueDate = view.findViewById(R.id.editDueDate);
        EditText editDueTime = view.findViewById(R.id.editDueTime);

        editTitle.setText(task.getTitle());
        editDescription.setText(task.getDescription());

        String[] dueDateParts = task.getDueDate().split(" ");
        if (dueDateParts.length == 2) {
            editDueDate.setText(dueDateParts[0]);
            editDueTime.setText(dueDateParts[1]);
        }

        builder.setTitle("Edit Task")
                .setPositiveButton("Update", (dialog, which) -> {
                    String updatedTitle = editTitle.getText().toString().trim();
                    String updatedDescription = editDescription.getText().toString().trim();
                    String updatedDueDate = editDueDate.getText().toString().trim();
                    String updatedDueTime = editDueTime.getText().toString().trim();

                    if (updatedTitle.isEmpty() || updatedDescription.isEmpty() || updatedDueDate.isEmpty() || updatedDueTime.isEmpty()) {
                        Toast.makeText(context, "All fields are required!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!isValidDate(updatedDueDate) || !isValidTime(updatedDueTime)) {
                        Toast.makeText(context, "Invalid date or time format. Date: yyyy-MM-dd, Time: HH:mm", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String updatedDueDateTime = updatedDueDate + " " + updatedDueTime;
                    boolean isUpdated = taskDatabaseHelper.updateTask(
                            task.getId(),
                            updatedTitle,
                            updatedDescription,
                            updatedDueDateTime,
                            task.getPriority(),
                            task.getStatus(),
                            task.getReminder()
                    );

                    if (isUpdated) {
                        task.setTitle(updatedTitle);
                        task.setDescription(updatedDescription);
                        task.setDueDate(updatedDueDateTime);
                        if (!updatedDueDate.equals(getTodayDate())) {
                            taskList.remove(task);
                            notifyItemRemoved(position);
                        } else {
                            notifyItemChanged(position);
                        }
                        Toast.makeText(context, "Task updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to update task", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private boolean isValidDate(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            sdf.setLenient(false);
            sdf.parse(date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidTime(String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            sdf.setLenient(false);
            sdf.parse(time);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new java.util.Date());
    }

    private void shareTaskViaEmail(Task task) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Task: " + task.getTitle());
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Task Details:\n" +
                "Title: " + task.getTitle() + "\n" +
                "Description: " + task.getDescription() + "\n" +
                "Due: " + task.getDueDate());
        try {
            context.startActivity(Intent.createChooser(emailIntent, "Share task via email"));
        } catch (Exception e) {
            Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle, taskDescription, taskDueTime;
        TextView editButton, markCompleteButton, shareButton, deleteButton;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskDescription = itemView.findViewById(R.id.taskDescription);
            taskDueTime = itemView.findViewById(R.id.taskDueTime);

            editButton = itemView.findViewById(R.id.editButton);
            markCompleteButton = itemView.findViewById(R.id.markCompleteButton);
            shareButton = itemView.findViewById(R.id.shareButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
