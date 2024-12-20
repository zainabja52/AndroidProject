package com.example.a1201766_1201086_courseproject;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public abstract class BaseTaskAdapter<T extends Task> extends RecyclerView.Adapter<BaseTaskAdapter.ViewHolder> {

    protected final List<Task> taskList;
    protected final TaskDatabaseHelper taskDatabaseHelper;
    protected final Context context;

    private final int[] colorPalette = {
            0xFFF5D6C3,
            0xFFF8B6AD,
            0xFFC7745E,
            0xFF8A9C6B,
    };

    public BaseTaskAdapter(Context context, List<Task> taskList, TaskDatabaseHelper taskDatabaseHelper) {
        this.context = context;
        this.taskList = taskList;
        this.taskDatabaseHelper = taskDatabaseHelper;
    }

    public List<T> getTaskList() {
        return (List<T>) taskList;
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

        holder.notificationIcon.setOnClickListener(v -> showNotificationDialog(task));

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

    protected abstract int getLayoutId();

    private String formatTime(String dueDate) {
        try {
            String[] parts = dueDate.split(" ");
            return parts.length > 1 ? parts[1] : dueDate;
        } catch (Exception e) {
            return dueDate;
        }
    }

    private void showNotificationDialog(Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Notification Settings");

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_notification_settings, null);
        builder.setView(dialogView);

        Spinner notificationTypeSpinner = dialogView.findViewById(R.id.notificationTypeSpinner);
        LinearLayout customNotificationSection = dialogView.findViewById(R.id.customNotificationSection);
        LinearLayout predefinedOptionsSection = dialogView.findViewById(R.id.predefinedOptionsSection);

        CheckBox enableDefaultReminderCheckbox = dialogView.findViewById(R.id.enableDefaultReminderCheckbox);
        CheckBox notificationStatusCheckbox = dialogView.findViewById(R.id.notificationStatusCheckbox);
        EditText customNotificationTimeField = dialogView.findViewById(R.id.customNotificationTimeField);
        Button customNotificationTimePickerButton = dialogView.findViewById(R.id.customNotificationTimePickerButton);
        Spinner predefinedOptionsSpinner = dialogView.findViewById(R.id.predefinedOptionsSpinner);
        EditText predefinedOptionsValue = dialogView.findViewById(R.id.predefinedOptionsValue);

        CheckBox enableSnoozeCheckbox = dialogView.findViewById(R.id.enableSnoozeCheckbox);
        LinearLayout snoozeOptionsSection = dialogView.findViewById(R.id.snoozeOptionsSection);
        Spinner snoozeDurationSpinner = dialogView.findViewById(R.id.snoozeDurationSpinner);

        notificationStatusCheckbox.setChecked(task.getReminder() != null && !task.getReminder().isEmpty());
        customNotificationTimeField.setText(task.getCustomNotificationTime());
        snoozeOptionsSection.setVisibility(View.GONE);

        enableDefaultReminderCheckbox.setChecked(task.getDefaultReminderEnabled());
        notificationStatusCheckbox.setChecked(task.getReminder() != null && !task.getReminder().isEmpty());
        customNotificationTimeField.setText(task.getCustomNotificationTime());


        enableDefaultReminderCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setDefaultReminderEnabled(isChecked);
            if (isChecked) {
                long defaultReminderTime = ReminderReceiver.parseDateTimeToMillis(task.getDueDate(), task.getDueTime());
                if (defaultReminderTime != -1) {
                    scheduleNotification(task.getId(), task.getTitle(), "Default Reminder", defaultReminderTime, 0);
                    Toast.makeText(context, "Default reminder enabled.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to enable default reminder.", Toast.LENGTH_SHORT).show();
                }
            } else {
                ReminderReceiver.cancelNotification(context, task.getId());
                Toast.makeText(context, "Default reminder disabled.", Toast.LENGTH_SHORT).show();
            }

            taskDatabaseHelper.updateTask(
                    task.getId(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getDueDate(),
                    task.getPriority(),
                    task.getStatus(),
                    isChecked ? "reminder" : "",
                    task.getCustomNotificationTime(),
                    task.getSnoozeDuration(),
                    isChecked
            );
        });



        notificationStatusCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                ReminderReceiver.cancelNotification(context, task.getId());
                task.setReminder("");
                task.setCustomNotificationTime(null);
                task.setSnoozeDuration(0);
                Toast.makeText(context, "Notification disabled.", Toast.LENGTH_SHORT).show();
            }

            taskDatabaseHelper.updateTask(
                    task.getId(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getDueDate(),
                    task.getPriority(),
                    task.getStatus(),
                    isChecked ? "Enabled" : "",
                    isChecked ? customNotificationTimeField.getText().toString() : null,
                    task.getSnoozeDuration(),
                    task.getDefaultReminderEnabled()
            );
        });


        notificationTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) { // Custom Date and Time
                    customNotificationSection.setVisibility(View.VISIBLE);
                    predefinedOptionsSection.setVisibility(View.GONE);
                } else if (position == 1) { // Predefined Options
                    customNotificationSection.setVisibility(View.GONE);
                    predefinedOptionsSection.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        predefinedOptionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedOption = predefinedOptionsSpinner.getSelectedItem().toString();

                if ("Same Day".equals(selectedOption)) {
                    predefinedOptionsValue.setVisibility(View.GONE);
                } else {
                    predefinedOptionsValue.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        enableSnoozeCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                snoozeOptionsSection.setVisibility(View.VISIBLE);
            } else {
                snoozeOptionsSection.setVisibility(View.GONE);
            }
        });

        customNotificationTimePickerButton.setOnClickListener(v -> showDateTimePicker(customNotificationTimeField));

        builder.setPositiveButton("Save", null);
        AlertDialog alertDialog = builder.create();

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());


        alertDialog.setOnShowListener(dialogInterface -> {
            Button saveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(v -> {
                boolean isNotificationEnabled = notificationStatusCheckbox.isChecked();
                String customTime = customNotificationTimeField.getText().toString().trim();
                String predefinedOption = predefinedOptionsSpinner.getSelectedItem().toString();
                String predefinedValue = predefinedOptionsValue.getText().toString().trim();

                String snoozeDurationString = snoozeDurationSpinner.getSelectedItem().toString();
                int snoozeDuration = parseSnoozeDuration(snoozeDurationString);

                String selectedType = notificationTypeSpinner.getSelectedItem().toString();

                switch (selectedType) {
                    case "Custom Date and Time":
                        if (customTime.isEmpty() || !validateCustomDateTime(customTime, task.getDueDate())) {
                            Toast.makeText(context, "Please select a valid date/time in the future and before the due date.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        predefinedOptionsValue.setText("");
                        task.setCustomNotificationTime(customTime);
                        task.setSnoozeDuration(enableSnoozeCheckbox.isChecked() ? snoozeDuration : 0);
                        long reminderTime = ReminderReceiver.parseDateTimeToMillis(task.getDueDate(), customTime);
                        if (reminderTime > 0) {
                            scheduleNotification(task.getId(), task.getTitle(), "Custom Reminder", reminderTime, snoozeDuration);
                        }
                        break;

                    case "Predefined Options":
                        if ("Same Day".equals(predefinedOption)) {
                            // Set the notification time to the start of the due date
                            customTime = task.getDueDate().split(" ")[0] + " 00:00";

                            if (!validateCustomDateTime(customTime, task.getDueDate())) {
                                Toast.makeText(context, "Invalid predefined option time.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            customNotificationTimeField.setText("");
                            task.setCustomNotificationTime(customTime);
                            task.setSnoozeDuration(enableSnoozeCheckbox.isChecked() ? snoozeDuration : 0);
                            reminderTime = ReminderReceiver.parseDateTimeToMillis(task.getDueDate(), customTime);
                            if (reminderTime > 0) {
                                scheduleNotification(task.getId(), task.getTitle(), "Custom Reminder", reminderTime, snoozeDuration);
                            }
                        } else {
                            if (predefinedValue.isEmpty()) {
                                Toast.makeText(context, "Please enter a value for predefined options.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            try {
                                int value = Integer.parseInt(predefinedValue.trim());
                                if (value <= 0) {
                                    Toast.makeText(context, "Value must be greater than 0.", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                customTime = predefinedOption.equals("Days Before")
                                        ? calculateTimeBefore(task.getDueDate(), value * 24)
                                        : calculateTimeBefore(task.getDueDate(), value);

                                if (!validateCustomDateTime(customTime, task.getDueDate())) {
                                    Toast.makeText(context, "Invalid predefined option time.", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                customNotificationTimeField.setText("");
                                task.setCustomNotificationTime(customTime);
                                task.setSnoozeDuration(enableSnoozeCheckbox.isChecked() ? snoozeDuration : 0);
                                 reminderTime = ReminderReceiver.parseDateTimeToMillis(task.getDueDate(), customTime);
                                if (reminderTime > 0) {
                                    scheduleNotification(task.getId(), task.getTitle(), "Custom Reminder", reminderTime, snoozeDuration);
                                }                            } catch (NumberFormatException e) {
                                Toast.makeText(context, "Invalid number format for predefined options.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        break;

                    default:
                        Toast.makeText(context, "Please select a valid notification type.", Toast.LENGTH_SHORT).show();
                        return;
                }

                task.setReminder(isNotificationEnabled ? "Enabled" : "");
                boolean isUpdated = taskDatabaseHelper.updateTask(
                        task.getId(),
                        task.getTitle(),
                        task.getDescription(),
                        task.getDueDate(),
                        task.getPriority(),
                        task.getStatus(),
                        task.getReminder(),
                        task.getCustomNotificationTime(),
                        task.getSnoozeDuration(),
                        task.getDefaultReminderEnabled()
                );

                if (isUpdated) {
                    Toast.makeText(context, "Notification settings saved.", Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                } else {
                    Toast.makeText(context, "Failed to save notification settings.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        alertDialog.show();
    }

    private int parseSnoozeDuration(String durationString) {
        try {
            return Integer.parseInt(durationString.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private boolean validateCustomDateTime(String customTime, String taskDueDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            sdf.setLenient(false);

            Date customDate = sdf.parse(customTime);
            Date dueDate = sdf.parse(taskDueDate);

            if (customDate == null || dueDate == null) {
                Log.e("ValidateDateTime", "Parsed date is null. customTime: " + customTime + ", taskDueDate: " + taskDueDate);
                return false;
            }

            if (customDate.before(new Date())) {
                Log.e("ValidateDateTime", "Custom time is in the past.");
                return false;
            }

            if (customDate.after(dueDate)) {
                Log.e("ValidateDateTime", "Custom time is after the due date.");
                return false;
            }

            return true;

        } catch (Exception e) {
            Log.e("ValidateDateTime", "Error validating custom date/time: " + e.getMessage(), e);
            return false;
        }
    }

    private void scheduleNotification(int taskId, String taskTitle, String message, long reminderTime, int snoozeDuration) {
        try {
            if (reminderTime > System.currentTimeMillis()) {
                Intent intent = new Intent(context, ReminderReceiver.class);
                intent.putExtra("taskId", taskId);
                intent.putExtra("title", taskTitle);
                intent.putExtra("message", message);
                intent.putExtra("snoozeDuration", snoozeDuration);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context,
                        taskId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);

                    Log.d("Notification", "Scheduled at: " + new Date(reminderTime) + " for task: " + taskTitle);
                    Toast.makeText(context, "Notification scheduled successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("Notification", "AlarmManager is null. Cannot schedule notification.");
                }
            } else {
                Log.e("Notification", "Invalid reminder time. It must be in the future.");
                Toast.makeText(context, "Invalid reminder time. It must be in the future.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("Notification", "Error scheduling notification: " + e.getMessage(), e);
        }
    }

    private void showDateTimePicker(EditText customNotificationTimeField) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            TimePickerDialog timePickerDialog = new TimePickerDialog(context, (view1, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                customNotificationTimeField.setText(sdf.format(calendar.getTime()));

            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);

            timePickerDialog.show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private String calculateTimeBefore(String dueDate, int hoursBefore) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date dueDateObj = sdf.parse(dueDate);

            if (dueDateObj == null) {
                Log.e("CalculateTimeBefore", "Due date is null.");
                return "";
            }

            Calendar calendar = Calendar.getInstance();
            if (dueDateObj != null) {
                calendar.setTime(dueDateObj);
                calendar.add(Calendar.HOUR, -hoursBefore);
            }

            return sdf.format(calendar.getTime());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
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
                            task.getReminder(),
                            task.getCustomNotificationTime(),
                            task.getSnoozeDuration(),
                            task.getDefaultReminderEnabled()
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
        ImageView notificationIcon;
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
            notificationIcon = itemView.findViewById(R.id.notificationIcon);
        }
    }

    public void updateTaskList(List<Task> updatedTasks) {
        this.taskList.clear();
        this.taskList.addAll(updatedTasks);
        notifyDataSetChanged();
    }
}
