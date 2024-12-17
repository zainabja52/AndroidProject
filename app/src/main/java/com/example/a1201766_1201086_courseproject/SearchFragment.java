package com.example.a1201766_1201086_courseproject;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.widget.ArrayAdapter;

public class SearchFragment extends Fragment {

    private TextView startDateField, endDateField, searchKeywordField;
    private Button searchButton, clearButton;
    private ListView searchResultsListView;
    private TaskDatabaseHelper taskDatabaseHelper;
    private Calendar calendar;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Initialize views
        startDateField = view.findViewById(R.id.startDateField);
        endDateField = view.findViewById(R.id.endDateField);
        searchKeywordField = view.findViewById(R.id.searchKeywordField);
        searchButton = view.findViewById(R.id.searchButton);
        clearButton = view.findViewById(R.id.clearButton);
        searchResultsListView = view.findViewById(R.id.searchResultsListView);
        taskDatabaseHelper = new TaskDatabaseHelper(getContext());
        calendar = Calendar.getInstance();

        // Set up date pickers
        startDateField.setOnClickListener(v -> showDatePickerDialog(startDateField));
        endDateField.setOnClickListener(v -> showDatePickerDialog(endDateField));

        // Set up buttons
        searchButton.setOnClickListener(v -> performSearch());
        clearButton.setOnClickListener(v -> clearSearch());

        return view;
    }

    private void showDatePickerDialog(TextView dateField) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(getContext(), (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
            String date = String.format("%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
            dateField.setText(date);
        }, year, month, day).show();
    }

    private void performSearch() {
        String startDate = startDateField.getText().toString();
        String endDate = endDateField.getText().toString();
        String keyword = searchKeywordField.getText().toString().trim();

        // Check if all filters are empty
        if (startDate.isEmpty() && endDate.isEmpty() && keyword.isEmpty()) {
            Toast.makeText(getContext(), "Please select at least one filter or keyword", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build the dynamic SQL query
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM tasks WHERE 1=1");
        List<String> queryArgs = new ArrayList<>();

        // Add Start Date condition
        if (!startDate.isEmpty()) {
            queryBuilder.append(" AND due_date >= ?");
            queryArgs.add(startDate);
        }

        // Add End Date condition
        if (!endDate.isEmpty()) {
            queryBuilder.append(" AND due_date <= ?");
            queryArgs.add(endDate);
        }

        // Add Keyword condition
        if (!keyword.isEmpty()) {
            queryBuilder.append(" AND (title LIKE ? OR description LIKE ?)");
            queryArgs.add("%" + keyword + "%"); // Wildcards for partial matching
            queryArgs.add("%" + keyword + "%");
        }

        // Execute the query
        Cursor cursor = taskDatabaseHelper.getReadableDatabase().rawQuery(
                queryBuilder.toString(),
                queryArgs.toArray(new String[0])
        );

        // Process the results
        List<String> tasks = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                String dueDate = cursor.getString(cursor.getColumnIndexOrThrow("due_date"));
                tasks.add("Title: " + title + "\nDescription: " + description + "\nDue: " + dueDate);
            }
            cursor.close();
        }

        // Display the results
        if (!tasks.isEmpty()) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, tasks);
            searchResultsListView.setAdapter(adapter);
            searchResultsListView.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(getContext(), "No tasks match your search criteria", Toast.LENGTH_SHORT).show();
            searchResultsListView.setAdapter(null);
            searchResultsListView.setVisibility(View.GONE);
        }
    }


    private void clearSearch() {
        startDateField.setText("");
        endDateField.setText("");
        searchKeywordField.setText("");
        searchResultsListView.setAdapter(null);
        searchResultsListView.setVisibility(View.GONE);
    }
}
