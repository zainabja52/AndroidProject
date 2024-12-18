package com.example.a1201766_1201086_courseproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.database.Cursor;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_tasks, container, false);

        taskDatabaseHelper = new TaskDatabaseHelper(getContext());
        recyclerView = view.findViewById(R.id.recyclerViewAllTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadTasks();

        return view;
    }

    private void loadTasks() {
        SharedPreferences preferences = getActivity().getSharedPreferences("USER_PREF", Context.MODE_PRIVATE);
        String userEmail = preferences.getString("email", ""); // Get user email
        Cursor cursor = taskDatabaseHelper.getAllTasks(userEmail);

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

        // Sort date keys for consistent display order
        List<String> dateKeys = new ArrayList<>(groupedTasks.keySet());
        dateKeys.sort(String::compareTo); // Sort dates chronologically

        // Set adapter to display the tasks
        recyclerView.setAdapter(new TaskAdapter(groupedTasks, dateKeys));
    }

}
