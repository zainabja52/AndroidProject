package com.example.a1201766_1201086_courseproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.database.Cursor;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllTasksFragment extends Fragment {

    private TaskDatabaseHelper taskDatabaseHelper;
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private EditText searchBar;
    private List<Task> taskList;
    private Map<String, List<Task>> groupedTasks;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_tasks, container, false);
        Button importButton = view.findViewById(R.id.button_import_tasks);

        taskDatabaseHelper = new TaskDatabaseHelper(getContext());
        recyclerView = view.findViewById(R.id.recyclerViewAllTasks);
        searchBar = view.findViewById(R.id.searchBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        taskList = new ArrayList<>();
        groupedTasks = new HashMap<>();

        loadTasks();

        taskAdapter = new TaskAdapter(getContext(), groupedTasks, taskDatabaseHelper);
        recyclerView.setAdapter(taskAdapter);

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


    private void loadTasks() {
        SharedPreferences preferences = getActivity().getSharedPreferences("USER_PREF", Context.MODE_PRIVATE);
        String userEmail = preferences.getString("email", ""); // Get user email
        Cursor cursor = taskDatabaseHelper.getAllTasks(userEmail);

        groupedTasks.clear(); // Clear previous data
        taskList.clear(); // Clear previous task list

        while (cursor.moveToNext()) {
            String dueDate = cursor.getString(cursor.getColumnIndexOrThrow("due_date"));
            Task task = new Task(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    dueDate,
                    cursor.getString(cursor.getColumnIndexOrThrow("priority")),
                    cursor.getString(cursor.getColumnIndexOrThrow("status")),
                    cursor.getString(cursor.getColumnIndexOrThrow("reminder"))
            );

            // Add to flat task list for filtering
            taskList.add(task);

            // Add to grouped tasks map
            groupedTasks.putIfAbsent(dueDate, new ArrayList<>());
            groupedTasks.get(dueDate).add(task);
        }
        cursor.close();

        // Sort date keys for consistent display order
        List<String> dateKeys = new ArrayList<>(groupedTasks.keySet());
        dateKeys.sort(String::compareTo); // Sort dates chronologically

        // Set the adapter
        TaskAdapter taskAdapter = new TaskAdapter(getContext(), groupedTasks, taskDatabaseHelper);
        recyclerView.setAdapter(taskAdapter);

        // Set up the search bar
        setupSearch(taskAdapter);
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

    private void fetchTasksFromAPI(String apiURL) {
        SharedPreferences preferences = getActivity().getSharedPreferences("USER_PREF", Context.MODE_PRIVATE);
        String userEmail = preferences.getString("email", ""); // Get the current user's email

        ConnectionAsyncTask task = new ConnectionAsyncTask(tasks -> {
            if (tasks != null) {
                for (Task importedTask : tasks) {
                    taskDatabaseHelper.insertTask(
                            userEmail, // Pass the user's email
                            importedTask.getTitle(),
                            importedTask.getDescription(),
                            importedTask.getDueDate(),
                            importedTask.getPriority(),
                            importedTask.getStatus(),
                            importedTask.getReminder()
                    );
                }
                Toast.makeText(getContext(), "Tasks Imported Successfully!", Toast.LENGTH_SHORT).show();
                loadTasks(); // Reload tasks after import
            } else {
                Toast.makeText(getContext(), "Failed to fetch tasks.", Toast.LENGTH_SHORT).show();
            }
        });
        task.execute(apiURL);
    }


}