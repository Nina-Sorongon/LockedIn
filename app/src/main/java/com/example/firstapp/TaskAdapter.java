package com.example.firstapp;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private final List<Map<String, Object>> tasks;
    private final String taskGroupId; // The ID of the task group this list belongs to
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public TaskAdapter(List<Map<String, Object>> tasks, String taskGroupId) {
        this.tasks = tasks;
        this.taskGroupId = taskGroupId;
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

        String title = task.get("title") != null ? task.get("title").toString() : "Unnamed Task";
        holder.title.setText(title);

        long deadline = task.get("deadline") != null ? (long) task.get("deadline") : 0;
        if (deadline > 0) {
            String formattedDeadline = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(deadline);
            holder.deadline.setText("Due: " + formattedDeadline);
        } else {
            holder.deadline.setText("No deadline");
        }

        boolean status = task.get("status") != null && (boolean) task.get("status");
        holder.checkbox.setChecked(status);

        // Optional: Handle task completion toggling
        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.put("status", isChecked); // Update the status locally
            updateTaskInFirestore(position, isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    private void updateTaskInFirestore(int taskIndex, boolean newStatus) {
        db.collection("taskGroups").document(taskGroupId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> taskList = (List<Map<String, Object>>) documentSnapshot.get("tasks");
                        if (taskList != null && taskIndex < taskList.size()) {
                            taskList.get(taskIndex).put("status", newStatus);

                            db.collection("taskGroups").document(taskGroupId)
                                    .update("tasks", taskList)
                                    .addOnSuccessListener(aVoid -> Log.d("TaskAdapter", "Task status updated successfully."))
                                    .addOnFailureListener(e -> Log.e("TaskAdapter", "Failed to update task status.", e));
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("TaskAdapter", "Failed to fetch task group.", e));
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
