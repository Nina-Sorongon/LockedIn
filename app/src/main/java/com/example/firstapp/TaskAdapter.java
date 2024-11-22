package com.example.firstapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private final List<Map<String, Object>> tasks;

    public TaskAdapter(List<Map<String, Object>> tasks) {
        this.tasks = tasks;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Map<String, Object> task = tasks.get(position);
        holder.title.setText(task.get("title").toString());

        long deadline = (long) task.get("deadline");
        String formattedDeadline = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(deadline);
        holder.deadline.setText("Due: " + formattedDeadline);

        boolean status = (boolean) task.get("status");
        holder.checkbox.setChecked(status);

        // Optional: Handle task completion toggling
        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.put("status", isChecked); // Update the status locally
            // TODO: Update Firestore status here
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView deadline;
        CheckBox checkbox;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.taskTitle);
            deadline = itemView.findViewById(R.id.taskDeadline);
            checkbox = itemView.findViewById(R.id.taskCheckbox);
        }
    }
}
