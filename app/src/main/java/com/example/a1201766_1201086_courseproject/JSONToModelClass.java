package com.example.a1201766_1201086_courseproject;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JSONToModelClass {
    public static List<Task> parseJSON(String jsonResponse) {
        List<Task> taskList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Task task = new Task();
                task.setId(jsonObject.getInt("id"));
                task.setTitle(jsonObject.getString("title"));
                task.setDescription(jsonObject.getString("description"));
                task.setDueDate(jsonObject.getString("due_date"));
                task.setPriority(jsonObject.getString("priority"));
                task.setStatus(jsonObject.getString("status"));
                task.setReminder(jsonObject.getString("reminder"));
                taskList.add(task);
                Log.d("Task", "Parsed Task: " + task.getStatus() + ", " + task.getReminder());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return taskList;
    }

}
