package com.example.a1201766_1201086_courseproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.database.Cursor;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AllTasksFragment extends Fragment {

    private TaskDatabaseHelper taskDatabaseHelper;
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private EditText searchBar;
    private ImageView sortIcon;
    private List<Task> taskList;
    private Map<String, List<Task>> groupedTasks;
    private boolean isAscending = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_tasks, container, false);
        Button importButton = view.findViewById(R.id.button_import_tasks);
        sortIcon = view.findViewById(R.id.sortIcon);

        taskDatabaseHelper = new TaskDatabaseHelper(getContext());
        recyclerView = view.findViewById(R.id.recyclerViewAllTasks);
        searchBar = view.findViewById(R.id.searchBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        taskList = new ArrayList<>();
        groupedTasks = new HashMap<>();

        taskAdapter = new TaskAdapter(getContext(), groupedTasks, taskDatabaseHelper,this,null);
        recyclerView.setAdapter(taskAdapter);

        loadTasks();

        sortIcon.setOnClickListener(v -> toggleSorting());

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTasks(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        importButton.setOnClickListener(v -> fetchTasksFromAPI("https://mocki.io/v1/fd97a2bd-1c26-43d0-8841-e8d8808f49d0"));

        return view;
    }


    public void loadTasks() {
        SharedPreferences preferences = getActivity().getSharedPreferences("USER_PREF", Context.MODE_PRIVATE);
        String userEmail = preferences.getString("email", "");
        Cursor cursor = taskDatabaseHelper.getAllTasks(userEmail);

        groupedTasks.clear();
        taskList.clear();

        while (cursor != null && cursor.moveToNext()) {
            String dueDate = cursor.getString(cursor.getColumnIndexOrThrow("due_date"));
            Task task = new Task(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    dueDate,
                    cursor.getString(cursor.getColumnIndexOrThrow("priority")),
                    cursor.getString(cursor.getColumnIndexOrThrow("status")),
                    cursor.getString(cursor.getColumnIndexOrThrow("reminder")),
                    cursor.getString(cursor.getColumnIndexOrThrow("custom_notification_time")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("snooze_duration")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("default_reminder_enabled")) == 1

            );

            taskList.add(task);

            groupedTasks.putIfAbsent(dueDate, new ArrayList<>());
            groupedTasks.get(dueDate).add(task);
        }


        if (cursor != null) cursor.close();

        getActivity().runOnUiThread(() -> taskAdapter.updateGroupedTasks(groupedTasks));


    }

    private void toggleSorting() {
        isAscending = !isAscending;

        groupedTasks = new TreeMap<>(groupedTasks);
        groupedTasks.forEach((date, tasks) -> tasks.sort((task1, task2) -> {
            int priority1 = getPriorityValue(task1.getPriority());
            int priority2 = getPriorityValue(task2.getPriority());
            return isAscending ? Integer.compare(priority1, priority2) : Integer.compare(priority2, priority1);
        }));

        groupedTasks = new TreeMap<>(groupedTasks);

        System.out.println("Grouped tasks after sorting:");
        groupedTasks.forEach((date, tasks) -> {
            System.out.println("Date: " + date);
            tasks.forEach(task -> System.out.println("  Task: " + task.getTitle() + " | Priority: " + task.getPriority()));
        });

        getActivity().runOnUiThread(() -> {
            taskAdapter.updateGroupedTasks(groupedTasks);
        });

        Toast.makeText(getContext(), isAscending ? "Sorted by Ascending Priority" : "Sorted by Descending Priority", Toast.LENGTH_SHORT).show();
    }



    private int getPriorityValue(String priority) {
        switch (priority.toLowerCase()) {
            case "high":
                return 1;
            case "medium":
                return 2;
            case "low":
                return 3;
            default:
                return 4;
        }
    }
    private void setupSearch(TaskAdapter taskAdapter) {
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
        Log.d("FilterTasks", "Query: " + query);

        if (query == null || query.trim().isEmpty()) {
            Log.d("FilterTasks", "Resetting to original groupedTasks.");
            taskAdapter.updateGroupedTasks(groupedTasks);
            return;
        }

        Map<String, List<Task>> filteredGroupedTasks = new HashMap<>();

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

        Log.d("FilterTasks", "FilteredGroupedTasks size: " + filteredGroupedTasks.size());
        filteredGroupedTasks.forEach((date, tasks) -> {
            Log.d("FilterTasks", "Date: " + date + ", Tasks: " + tasks.size());
            tasks.forEach(task -> Log.d("FilterTasks", "Task: " + task.getTitle()));
        });

        taskAdapter.updateGroupedTasks(filteredGroupedTasks);
    }


    private void fetchTasksFromAPI(String apiURL) {
        SharedPreferences preferences = getActivity().getSharedPreferences("USER_PREF", Context.MODE_PRIVATE);
        String userEmail = preferences.getString("email", ""); // Get the current user's email

        ConnectionAsyncTask task = new ConnectionAsyncTask(tasks -> {
            if (tasks != null) {
                for (Task importedTask : tasks) {
                    boolean isInserted = taskDatabaseHelper.insertTask(
                            userEmail, // Pass the user's email
                            importedTask.getTitle() != null ? importedTask.getTitle() : "Untitled Task",
                            importedTask.getDescription() != null ? importedTask.getDescription() : "No Description",
                            importedTask.getDueDate() != null ? importedTask.getDueDate() : "",
                            importedTask.getPriority() != null ? importedTask.getPriority() : "Medium",
                            importedTask.getStatus() != null ? importedTask.getStatus() : "pending",
                            importedTask.getReminder() != null ? importedTask.getReminder() : "None",
                            importedTask.getCustomNotificationTime() != null ? importedTask.getCustomNotificationTime() : null,
                            importedTask.getSnoozeDuration() > 0 ? importedTask.getSnoozeDuration() : 0,
                            importedTask.getDefaultReminderEnabled()
                    );

                    if (!isInserted) {
                        Toast.makeText(getContext(), "Failed to import task: " + importedTask.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                }
                Toast.makeText(getContext(), "Tasks Imported Successfully!", Toast.LENGTH_SHORT).show();
                loadTasks();
            } else {
                Toast.makeText(getContext(), "Failed to fetch tasks.", Toast.LENGTH_SHORT).show();
            }
        });
        task.execute(apiURL);
    }


}