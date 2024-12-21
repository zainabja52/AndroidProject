package com.example.a1201766_1201086_courseproject;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.widget.ArrayAdapter;
import android.widget.AdapterView;

public class NewTaskFragment extends Fragment {

    private EditText taskTitleField, taskDescriptionField, dueDateField, dueTimeField, searchTaskField;
    private Spinner prioritySpinner, statusSpinner;
    private Button saveTaskButton, deleteTaskButton, reminderButton, confirmDeleteButton;
    private TaskDatabaseHelper taskDatabaseHelper;
    private ListView taskListView;
    private CardView searchCardView, taskDetailsCard;
    private TextView taskDetailsText;
    private String selectedTaskTitle, selectedTaskDate;
    private boolean isReminderSet = false;
    private long tempReminderTime = -1;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_task, container, false);
        try {
        taskTitleField = view.findViewById(R.id.taskTitleField);
        taskDescriptionField = view.findViewById(R.id.taskDescriptionField);
        dueDateField = view.findViewById(R.id.dueDateField);
        dueTimeField = view.findViewById(R.id.dueTimeField);
        prioritySpinner = view.findViewById(R.id.prioritySpinner);
        statusSpinner = view.findViewById(R.id.statusSpinner);
        saveTaskButton = view.findViewById(R.id.saveTaskButton);
        deleteTaskButton = view.findViewById(R.id.deleteTaskButton);
        reminderButton = view.findViewById(R.id.reminderButton);


        searchCardView = view.findViewById(R.id.searchCardView);
        searchTaskField = view.findViewById(R.id.searchTaskField);
        taskListView = view.findViewById(R.id.taskListView);
        taskDetailsCard = view.findViewById(R.id.taskDetailsCard);
        taskDetailsText = view.findViewById(R.id.taskDetailsText);
        confirmDeleteButton = view.findViewById(R.id.confirmDeleteButton);

        taskDatabaseHelper = new TaskDatabaseHelper(getContext());

            ArrayAdapter<CharSequence> priorityAdapter = ArrayAdapter.createFromResource(
                    getContext(),
                    R.array.priority_options,
                    android.R.layout.simple_spinner_item
            );
            priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            prioritySpinner.setAdapter(priorityAdapter);

            ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(
                    getContext(),
                    R.array.status_options,
                    android.R.layout.simple_spinner_item
            );
            statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            statusSpinner.setAdapter(statusAdapter);

            prioritySpinner.setSelection(1);
            statusSpinner.setSelection(0);

        dueDateField.setOnClickListener(v -> showDatePickerDialog());
        dueTimeField.setOnClickListener(v -> showTimePickerDialog());
        saveTaskButton.setOnClickListener(v -> saveTask());
        reminderButton.setOnClickListener(v -> setReminder());
        deleteTaskButton.setOnClickListener(v -> {
            searchCardView.setVisibility(View.VISIBLE);
            searchTaskField.setText("");
            taskListView.setAdapter(null);
            taskListView.setVisibility(View.GONE);
            taskDetailsCard.setVisibility(View.GONE);

            Toast.makeText(getContext(), "Search and select a task to delete", Toast.LENGTH_SHORT).show();
        });


            setupSearch();

    } catch (Exception e) {
        Log.e("NewTaskFragment", "Error initializing views", e);
    }
        return view;
    }

    private void setupSearch() {
        searchTaskField.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    taskListView.setAdapter(null);
                    taskListView.setVisibility(View.GONE);
                    taskDetailsCard.setVisibility(View.GONE);
                } else {
                    filterTasks(s.toString());
                }
            }


            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        taskListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedTask = (String) parent.getItemAtPosition(position);
            String[] parts = selectedTask.split("\\|");
            selectedTaskTitle = parts[0].trim();
            selectedTaskDate = parts[2].trim();

            taskDetailsCard.setVisibility(View.VISIBLE); // Show details card
            taskDetailsText.setText("Title: " + selectedTaskTitle + "\nDescription: " +
                    parts[1].trim() + "\nDate: " + selectedTaskDate);

        });

        confirmDeleteButton.setOnClickListener(v -> confirmDeletion());
    }


    private void filterTasks(String query) {
        SharedPreferences preferences = getActivity().getSharedPreferences("USER_PREF", Context.MODE_PRIVATE);
        String userEmail = preferences.getString("email", "");

        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(getContext(), "Error: User email not found. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch tasks for the logged-in user
        Cursor cursor = taskDatabaseHelper.getAllTasks(userEmail);
        List<String> filteredTasks = new ArrayList<>();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("due_date"));

                // Add tasks that match the query
                if (title.toLowerCase().contains(query.toLowerCase()) ||
                        description.toLowerCase().contains(query.toLowerCase()) ||
                        date.contains(query)) {
                    filteredTasks.add(title + " | " + description + " | " + date);
                }
            }
            cursor.close();
        }

        // Update the ListView with filtered results
        if (!filteredTasks.isEmpty()) {
            // Create and set a new adapter with the filtered tasks
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_list_item_1, filteredTasks);
            taskListView.setAdapter(adapter);

            // Make sure the ListView is visible and hide task details
            taskListView.setVisibility(View.VISIBLE);
            taskDetailsCard.setVisibility(View.GONE);

        } else {
            // Hide ListView and show a message when no tasks match
            taskListView.setVisibility(View.GONE);
            taskDetailsCard.setVisibility(View.GONE);
            Toast.makeText(getContext(), "No tasks match your search.", Toast.LENGTH_SHORT).show();
        }
}


    private void confirmDeletion() {
        // Check if a task title and date are selected
        if (selectedTaskTitle != null && selectedTaskDate != null) {
            // Delete the selected task
            boolean isDeleted = taskDatabaseHelper.deleteTaskByTitleAndDate(selectedTaskTitle, selectedTaskDate);
            if (isDeleted) {
                Toast.makeText(getContext(), "Task deleted successfully", Toast.LENGTH_SHORT).show();

                // Clear the selection
                taskDetailsCard.setVisibility(View.GONE);
                selectedTaskTitle = null;
                selectedTaskDate = null;

                // Refresh the filtered list
                String query = searchTaskField.getText().toString(); // Get the current search query
                filterTasks(query); // Re-run the search to refresh ListView

            } else {
                Toast.makeText(getContext(), "Failed to delete task", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "No task selected to delete", Toast.LENGTH_SHORT).show();
        }
    }



    private void confirmTaskDeletion(String taskDetails) {
        String[] details = taskDetails.split("\\|");
        String title = details[0].trim();
        String date = details[2].trim();

        new AlertDialog.Builder(getContext())
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?\n" + taskDetails)
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean isDeleted = taskDatabaseHelper.deleteTaskByTitleAndDate(title, date);
                    if (isDeleted) {
                        Toast.makeText(getContext(), "Task deleted successfully", Toast.LENGTH_SHORT).show();
                        filterTasks(searchTaskField.getText().toString()); // Refresh the search results
                    } else {
                        Toast.makeText(getContext(), "Failed to delete task", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
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
        String status = (String) statusSpinner.getSelectedItem();
        Log.d("NewTaskFragment", "Saving task with status: " + status);


        if (title.isEmpty() || description.isEmpty() || dueDate.isEmpty() || dueTime.isEmpty() || priority.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor cursor = taskDatabaseHelper.getTaskByTitleAndDate(title, dueDate + " " + dueTime);
        if (cursor != null && cursor.getCount() > 0) {

            Toast.makeText(getContext(), "Task already exists", Toast.LENGTH_SHORT).show();
            cursor.close();
            return;
        }

        boolean isInserted = taskDatabaseHelper.insertTask(userEmail, title, description, dueDate + " " + dueTime, priority, status, isReminderSet ? "reminder" : "", null, 0, isReminderSet);
        if (isInserted) {
            Toast.makeText(getContext(), "Task saved successfully", Toast.LENGTH_SHORT).show();

            if (isReminderSet && tempReminderTime > 0) {
                scheduleNotification(title, "Task Reminder", tempReminderTime);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                String notificationTime = sdf.format(new Date(tempReminderTime));
                Toast.makeText(getContext(), "You will be notified by " + notificationTime, Toast.LENGTH_LONG).show();
            }

            //reset the fields
            taskTitleField.setText("");
            taskDescriptionField.setText("");
            dueDateField.setText("");
            dueTimeField.setText("");
            prioritySpinner.setSelection(1);
            statusSpinner.setSelection(0);
            isReminderSet = false;
            tempReminderTime = -1;
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
            statusSpinner.setSelection(0);
        } else {
            Toast.makeText(getContext(), "Failed to delete task. Task may not exist.", Toast.LENGTH_SHORT).show();
        }
    }


    private void setReminder() {
        String dueDate = dueDateField.getText().toString().trim();
        String dueTime = dueTimeField.getText().toString().trim();

        if (dueDate.isEmpty() || dueTime.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in the due date and time before setting a reminder.", Toast.LENGTH_SHORT).show();
            return;
        }

        long reminderTime = parseDateTimeToMillis(dueDate, dueTime);
        tempReminderTime = reminderTime;
        isReminderSet = true;
        Toast.makeText(getContext(), "Reminder set. Please save the task to finalize.", Toast.LENGTH_SHORT).show();
    }

    public static long parseDateTimeToMillis(String dueDate, String dueTime) {
        try {
            String dateTime = dueDate + " " + dueTime;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date parsedDate = sdf.parse(dateTime);
            if (parsedDate != null) {
                return parsedDate.getTime();
            } else {
                Log.e("ReminderReceiver", "Parsed date is null.");
                return -1;
            }
        } catch (Exception e) {
            Log.e("ReminderReceiver", "Error parsing date and time: " + e.getMessage());
            return -1;
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
