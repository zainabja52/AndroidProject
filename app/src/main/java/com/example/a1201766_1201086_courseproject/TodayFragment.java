package com.example.a1201766_1201086_courseproject;

import android.content.Context;
import android.content.SharedPreferences;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TodayFragment extends Fragment {

    private RecyclerView recyclerView;
    private TodayTaskAdapter taskAdapter;
    private TaskDatabaseHelper taskDatabaseHelper;
    private List<Task> todayTasks;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_today, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerViewTodayTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize helper and task list
        taskDatabaseHelper = new TaskDatabaseHelper(getContext());
        todayTasks = new ArrayList<>();

        // Load today's tasks
        loadTodayTasks();

        // Set up adapter
        taskAdapter = new TodayTaskAdapter(getContext(), todayTasks, taskDatabaseHelper);
        recyclerView.setAdapter(taskAdapter);

        return view;
    }

    private void loadTodayTasks() {
        // Get logged-in user's email
        SharedPreferences preferences = getActivity().getSharedPreferences("USER_PREF", Context.MODE_PRIVATE);
        String userEmail = preferences.getString("email", "");

        if (userEmail == null || userEmail.isEmpty()) return;

        // Get today's date
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Query tasks for today
        Cursor cursor = taskDatabaseHelper.getTasksForToday(userEmail, todayDate);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Task task = new Task(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        cursor.getString(cursor.getColumnIndexOrThrow("due_date")),
                        cursor.getString(cursor.getColumnIndexOrThrow("priority")),
                        cursor.getString(cursor.getColumnIndexOrThrow("status")),
                        cursor.getString(cursor.getColumnIndexOrThrow("reminder"))
                );
                todayTasks.add(task);
            }
            cursor.close();
        }
    }
}
