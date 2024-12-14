package com.example.a1201766_1201086_courseproject;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    private final Map<String, List<Task>> groupedTasks;
    private final List<String> dateKeys;

    private final int[] colorPalette = {
            Color.parseColor("#D0F0C0"), // Light Green
            Color.parseColor("#FFDD99"), // Light Orange
            Color.parseColor("#FFFACD"), // Light Yellow
            Color.parseColor("#FFB6C1"), // Light Pink
            Color.parseColor("#D1C4E9")  // Light Purple
    };

    public TaskAdapter(Map<String, List<Task>> groupedTasks, List<String> dateKeys) {
        this.groupedTasks = groupedTasks;
        this.dateKeys = dateKeys;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_group, parent, false);
        return new ViewHolder(view);
    }

    private String formatDateTime(String dueDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
            return outputFormat.format(inputFormat.parse(dueDate));
        } catch (Exception e) {
            e.printStackTrace();
            return dueDate;
        }
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String date = dateKeys.get(position);
        List<Task> tasks = groupedTasks.get(date);

        holder.dateTextView.setText(formatDateTime(date));
        holder.tasksTextView.setText(formatTasks(tasks));

        int randomColor = colorPalette[new Random().nextInt(colorPalette.length)];
        holder.cardView.setCardBackgroundColor(randomColor);
    }

    @Override
    public int getItemCount() {
        return dateKeys.size();
    }

    private String formatTasks(List<Task> tasks) {
        StringBuilder sb = new StringBuilder();
        for (Task task : tasks) {
            sb.append("• ").append(task.getTitle()).append("\n");
        }
        return sb.toString();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView, tasksTextView;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            dateTextView = itemView.findViewById(R.id.textViewDate);
            tasksTextView = itemView.findViewById(R.id.textViewTasks);
        }
    }
}
