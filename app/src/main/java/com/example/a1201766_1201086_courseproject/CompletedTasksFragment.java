package com.example.a1201766_1201086_courseproject;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompletedTasksFragment extends Fragment {

    private TaskDatabaseHelper taskDatabaseHelper;
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private EditText searchBar;
    private Map<String, List<Task>> groupedTasks;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_completed_tasks, container, false);

        // Initialize database helper, RecyclerView, and search bar
        taskDatabaseHelper = new TaskDatabaseHelper(getContext());
        recyclerView = view.findViewById(R.id.ViewCompletedTasks);
        searchBar = view.findViewById(R.id.searchBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        groupedTasks = new HashMap<>();

        // Load completed tasks
        loadCompletedTasks();

        // Set up search bar
        setupSearch();

        return view;
    }

    private void loadCompletedTasks() {
        Cursor cursor = taskDatabaseHelper.getCompletedTasksGroupedByDate();

        groupedTasks.clear(); // Clear previous data

        // Group tasks by due_date
        while (cursor.moveToNext()) {
            String dueDate = cursor.getString(cursor.getColumnIndexOrThrow("due_date"));
            Task task = new Task(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    dueDate,
                    cursor.getString(cursor.getColumnIndexOrThrow("priority")),
                    cursor.getString(cursor.getColumnIndexOrThrow("status")),
                    cursor.getString(cursor.getColumnIndexOrThrow("reminder")),
                    cursor.isNull(cursor.getColumnIndexOrThrow("custom_notification_time")) ? null : cursor.getString(cursor.getColumnIndexOrThrow("custom_notification_time")),
                    cursor.isNull(cursor.getColumnIndexOrThrow("snooze_duration")) ? 0 : cursor.getInt(cursor.getColumnIndexOrThrow("snooze_duration")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("default_reminder_enabled")) == 1
            );

            groupedTasks.putIfAbsent(dueDate, new ArrayList<>());
            groupedTasks.get(dueDate).add(task);
        }
        cursor.close();

        // Sort the grouped tasks by date
        List<String> dateKeys = new ArrayList<>(groupedTasks.keySet());
        dateKeys.sort(String::compareTo);

        // Bind grouped tasks to RecyclerView
        taskAdapter = new TaskAdapter(getContext(), groupedTasks, taskDatabaseHelper);
        recyclerView.setAdapter(taskAdapter);
    }

    private void setupSearch() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTasks(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterTasks(String query) {
        Map<String, List<Task>> filteredGroupedTasks = new HashMap<>();

        // Iterate through all tasks and filter by title or description
        for (Map.Entry<String, List<Task>> entry : groupedTasks.entrySet()) {
            List<Task> filteredList = new ArrayList<>();
            for (Task task : entry.getValue()) {
                if (task.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        task.getDescription().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(task);
                }
            }
            if (!filteredList.isEmpty()) {
                filteredGroupedTasks.put(entry.getKey(), filteredList);
            }
        }

        // Update the adapter with the filtered tasks
        taskAdapter.updateGroupedTasks(filteredGroupedTasks);
    }
}
