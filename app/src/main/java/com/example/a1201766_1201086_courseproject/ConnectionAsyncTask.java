package com.example.a1201766_1201086_courseproject;

import android.os.AsyncTask;
import java.util.List;

public class ConnectionAsyncTask extends AsyncTask<String, Void, List<Task>> {
    private OnTaskFetchListener listener;

    public interface OnTaskFetchListener {
        void onTaskFetchComplete(List<Task> tasks);
    }

    public ConnectionAsyncTask(OnTaskFetchListener listener) {
        this.listener = listener;
    }

    @Override
    protected List<Task> doInBackground(String... params) {
        String response = HTTPManager.getData(params[0]);
        if (response != null) {
            return JSONToModelClass.parseJSON(response);
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<Task> tasks) {
        if (listener != null) {
            listener.onTaskFetchComplete(tasks);
        }
    }
}
