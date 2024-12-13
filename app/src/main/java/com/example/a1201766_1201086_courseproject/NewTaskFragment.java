package com.example.a1201766_1201086_courseproject;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import java.util.Calendar;

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
        String title = taskTitleField.getText().toString();
        String description = taskDescriptionField.getText().toString();
        String dueDate = dueDateField.getText().toString();
        String dueTime = dueTimeField.getText().toString();
        String priority = prioritySpinner.getSelectedItem() != null ? prioritySpinner.getSelectedItem().toString() : "";
        String status = completionStatusCheckbox.isChecked() ? "completed" : "pending";
        String reminder = "reminder"; // Placeholder for reminder logic

        if (title.isEmpty() || description.isEmpty() || dueDate.isEmpty() || dueTime.isEmpty() || priority.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isInserted = taskDatabaseHelper.insertTask(title, description, dueDate + " " + dueTime, priority, status, reminder);
        if (isInserted) {
            Toast.makeText(getContext(), "Task saved successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Failed to save task", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteTask() {
        // Placeholder: actual implementation will require task identification
        Toast.makeText(getContext(), "Delete feature not implemented", Toast.LENGTH_SHORT).show();
    }

    private void setReminder() {
        // Placeholder: actual implementation will need integration with notification system
        Toast.makeText(getContext(), "Reminder feature not implemented", Toast.LENGTH_SHORT).show();
    }
}
