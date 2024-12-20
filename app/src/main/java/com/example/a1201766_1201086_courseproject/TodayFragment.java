package com.example.a1201766_1201086_courseproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    private EditText searchBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_today, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerViewTodayTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchBar = view.findViewById(R.id.searchBar); // Add search bar

        // Initialize helper and task list
        taskDatabaseHelper = new TaskDatabaseHelper(getContext());
        todayTasks = new ArrayList<>();

        // Load today's tasks
        loadTodayTasks();

        // Set up adapter
        taskAdapter = new TodayTaskAdapter(getContext(), todayTasks, taskDatabaseHelper,this);
        recyclerView.setAdapter(taskAdapter);

        // Set up search bar
        setupSearch();

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

        todayTasks.clear(); // Clear the list before loading

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

        // Notify the adapter about data changes
        if (taskAdapter != null) {
            taskAdapter.notifyDataSetChanged();

            // Check if all tasks are completed
            checkAndShowCongratulations();
        }
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
        List<Task> filteredTasks = new ArrayList<>();

        // Iterate through all tasks and filter by title or description
        for (Task task : todayTasks) {
            if (task.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    task.getDescription().toLowerCase().contains(query.toLowerCase())) {
                filteredTasks.add(task);
            }
        }

        // Update the adapter with the filtered tasks
        taskAdapter.updateTaskList(filteredTasks);
    }

    void checkAndShowCongratulations() {
        boolean allCompleted = true;

        for (Task task : todayTasks) {
            if (!"completed".equalsIgnoreCase(task.getStatus())) {
                allCompleted = false;
                break; // No need to check further
            }
        }

        if (allCompleted) {
            showCongratulations();
        } else {
            // Debugging to ensure correct task statuses
            for (Task task : todayTasks) {
                System.out.println("Task: " + task.getTitle() + " | Status: " + task.getStatus());
            }
        }
    }


    public void showCongratulations() {
        // Toast message for quick feedback
        Toast.makeText(getContext(), "Congratulations! All tasks for today are completed!", Toast.LENGTH_LONG).show();

        // Create a dialog for the celebratory animation
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_congratulations, null);
        builder.setView(dialogView);

        android.app.AlertDialog dialog = builder.create();

        // Find and set up animation/image
        ImageView celebrationImage = dialogView.findViewById(R.id.celebrationImage);
        TextView messageText = dialogView.findViewById(R.id.messageText);
        messageText.setText("Well Done! All tasks for today are completed!");

        // Optional: Add custom animation to the image
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.bounce);
        celebrationImage.startAnimation(animation);

        // Show the dialog
        dialog.show();

        // Automatically dismiss dialog after 3 seconds
        new Handler().postDelayed(dialog::dismiss, 3000);
    }
}
