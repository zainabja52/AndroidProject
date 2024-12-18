package com.example.a1201766_1201086_courseproject;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.widget.ArrayAdapter;
import android.widget.AdapterView;


public class NewTaskFragment extends Fragment {

    private EditText taskTitleField, taskDescriptionField, dueDateField, dueTimeField;
    private Spinner prioritySpinner;
    private CheckBox completionStatusCheckbox;
    private Button saveTaskButton, deleteTaskButton, reminderButton;
    private TaskDatabaseHelper taskDatabaseHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_task, container, false);

        taskTitleField = view.findViewById(R.id.taskTitleField);
        taskDescriptionField = view.findViewById(R.id.taskDescriptionField);
        dueDateField = view.findViewById(R.id.dueDateField);
        dueTimeField = view.findViewById(R.id.dueTimeField);
        prioritySpinner = view.findViewById(R.id.prioritySpinner);
        completionStatusCheckbox = view.findViewById(R.id.completionStatusCheckbox);
        saveTaskButton = view.findViewById(R.id.saveTaskButton);
        deleteTaskButton = view.findViewById(R.id.deleteTaskButton);
        reminderButton = view.findViewById(R.id.reminderButton);

        taskDatabaseHelper = new TaskDatabaseHelper(getContext());

        // Set up Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.priority_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(adapter);

        // Set default selection to "Medium"
        prioritySpinner.setSelection(1);

        dueDateField.setOnClickListener(v -> showDatePickerDialog());
        dueTimeField.setOnClickListener(v -> showTimePickerDialog());
        saveTaskButton.setOnClickListener(v -> saveTask());
        deleteTaskButton.setOnClickListener(v -> deleteTask());
        reminderButton.setOnClickListener(v -> setReminder());

        return view;
    }

    private void showDatePickerDialog() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            String date = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth);
            dueDateField.setText(date);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePickerDialog() {
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
            String time = String.format("%02d:%02d", hourOfDay, minute);
            dueTimeField.setText(time);
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
    }

    private void saveTask() {
        SharedPreferences preferences = getActivity().getSharedPreferences("USER_PREF", Context.MODE_PRIVATE);
        String userEmail = preferences.getString("email", ""); // To Get the email
        Log.d("DEBUG_EMAIL", "User email: " + userEmail);
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(getContext(), "Error: User email not found. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }
        String title = taskTitleField.getText().toString();
        String description = taskDescriptionField.getText().toString();
        String dueDate = dueDateField.getText().toString();
        String dueTime = dueTimeField.getText().toString();
        String priority = (String) prioritySpinner.getSelectedItem();
        if (priority == null) {
            priority = "Medium"; // Fallback if no selection
        }
        String status = completionStatusCheckbox.isChecked() ? "completed" : "pending";
        String reminder = "reminder"; // Placeholder for reminder logic

        if (title.isEmpty() || description.isEmpty() || dueDate.isEmpty() || dueTime.isEmpty() || priority.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check for duplicates
        Cursor cursor = taskDatabaseHelper.getTaskByTitleAndDate(title, dueDate + " " + dueTime);
        if (cursor != null && cursor.getCount() > 0) {

            Toast.makeText(getContext(), "Task already exists", Toast.LENGTH_SHORT).show();
            cursor.close();
            return;
        }

        boolean isInserted = taskDatabaseHelper.insertTask(userEmail, title, description, dueDate + " " + dueTime, priority, status, reminder);
        if (isInserted) {
            Toast.makeText(getContext(), "Task saved successfully", Toast.LENGTH_SHORT).show();

            //reset the fields
            taskTitleField.setText("");
            taskDescriptionField.setText("");
            dueDateField.setText("");
            dueTimeField.setText("");
            prioritySpinner.setSelection(1);
            completionStatusCheckbox.setChecked(false);
        } else {
            Toast.makeText(getContext(), "Failed to save task", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteTask() {
        String title = taskTitleField.getText().toString().trim();
        String dueDate = dueDateField.getText().toString().trim();
        String dueTime = dueTimeField.getText().toString().trim();

        if (title.isEmpty() || dueDate.isEmpty() || dueTime.isEmpty()) {
            Toast.makeText(getContext(), "Please provide the task title and due date to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isDeleted = taskDatabaseHelper.deleteTaskByTitleAndDate(title, dueDate + " " + dueTime);
        if (isDeleted) {
            Toast.makeText(getContext(), "Task deleted successfully", Toast.LENGTH_SHORT).show();
            taskTitleField.setText("");
            taskDescriptionField.setText("");
            dueDateField.setText("");
            dueTimeField.setText("");
            prioritySpinner.setSelection(1);
            completionStatusCheckbox.setChecked(false);
        } else {
            Toast.makeText(getContext(), "Failed to delete task. Task may not exist.", Toast.LENGTH_SHORT).show();
        }
    }


    private void setReminder() {
        String title = taskTitleField.getText().toString().trim();
        String dueDate = dueDateField.getText().toString().trim();
        String dueTime = dueTimeField.getText().toString().trim();

        if (title.isEmpty() || dueDate.isEmpty() || dueTime.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in the task title, due date, and time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse date and time into a timestamp
        long reminderTime = parseDateTimeToMillis(dueDate, dueTime);
        //long reminderTime = System.currentTimeMillis() + 60 * 1000;
        if (reminderTime <= System.currentTimeMillis()) {
            Toast.makeText(getContext(), "Reminder time must be in the future", Toast.LENGTH_SHORT).show();
            return;
        }

        scheduleNotification(title, "Task Reminder", reminderTime);
        Toast.makeText(getContext(), "Reminder set successfully", Toast.LENGTH_SHORT).show();
    }

    private long parseDateTimeToMillis(String date, String time) {
        try {
            String dateTime = date + " " + time;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            return sdf.parse(dateTime).getTime();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void scheduleNotification(String title, String message, long reminderTime) {

        Log.d("Reminder", "Scheduling reminder for: " + title + " at " + new java.util.Date(reminderTime));

        Intent intent = new Intent(getContext(), ReminderReceiver .class);
        intent.putExtra("title", title);
        intent.putExtra("message", message);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getContext(),
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
    }

}
