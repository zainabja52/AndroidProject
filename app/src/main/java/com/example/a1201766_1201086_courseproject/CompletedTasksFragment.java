package com.example.a1201766_1201086_courseproject;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_completed_tasks, container, false);

        // Initialize database helper and RecyclerView
        taskDatabaseHelper = new TaskDatabaseHelper(getContext());
        recyclerView = view.findViewById(R.id.ViewCompletedTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load completed tasks
        loadCompletedTasks();

        return view;
    }

    private void loadCompletedTasks() {
        Cursor cursor = taskDatabaseHelper.getCompletedTasksGroupedByDate();

        // Group tasks by due_date
        Map<String, List<Task>> groupedTasks = new HashMap<>();
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

            groupedTasks.putIfAbsent(dueDate, new ArrayList<>());
            groupedTasks.get(dueDate).add(task);
        }
        cursor.close();

        // Sort the grouped tasks by date
        List<String> dateKeys = new ArrayList<>(groupedTasks.keySet());
        dateKeys.sort(String::compareTo);

        // Bind grouped tasks to RecyclerView
        recyclerView.setAdapter(new TaskAdapter(groupedTasks, dateKeys));
    }
}
