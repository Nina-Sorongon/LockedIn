package com.example.firstapp;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private final List<Map<String, Object>> tasks;
    private final String taskGroupId; // The ID of the task group this list belongs to
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final Context context;

    public TaskAdapter(List<Map<String, Object>> tasks, String taskGroupId, Context context) {
        this.tasks = tasks;
        this.taskGroupId = taskGroupId;
        this.context = context;
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

        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.put("status", isChecked);
            updateTaskInFirestore(position, task);
        });

        // Edit button functionality
        holder.editButton.setOnClickListener(view -> showEditDialog(position, holder.title));

        // Delete button functionality
        holder.deleteButton.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete this task?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        deleteTaskFromFirestore(position); // Delete from Firestore
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });

    }

    private void deleteTaskFromFirestore(int taskIndex) {
        db.collection("taskGroups").document(taskGroupId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> taskList = (List<Map<String, Object>>) documentSnapshot.get("tasks");
                        if (taskList != null && taskIndex < taskList.size()) {
                            taskList.remove(taskIndex);

                            db.collection("taskGroups").document(taskGroupId)
                                    .update("tasks", taskList)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("TaskAdapter", "Task deleted successfully.");
                                        tasks.clear();
                                        tasks.addAll(taskList);
                                        notifyDataSetChanged();
                                    })
                                    .addOnFailureListener(e -> Log.e("TaskAdapter", "Failed to delete task.", e));
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("TaskAdapter", "Failed to fetch task group.", e));
    }


    @Override
    public int getItemCount() {
        return tasks.size();
    }

    private void showEditDialog(int taskIndex, TextView titleView) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle("Edit Task Title");

        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(titleView.getText().toString());
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newTitle = input.getText().toString().trim();
            if (!newTitle.isEmpty()) {
                tasks.get(taskIndex).put("title", newTitle);
                updateTaskInFirestore(taskIndex, tasks.get(taskIndex));
                titleView.setText(newTitle);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }


    private void updateTaskInFirestore(int taskIndex, Map<String, Object> updatedTask) {
        db.collection("taskGroups").document(taskGroupId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> taskList = (List<Map<String, Object>>) documentSnapshot.get("tasks");
                        if (taskList != null && taskIndex < taskList.size()) {
                            taskList.set(taskIndex, updatedTask);

                            db.collection("taskGroups").document(taskGroupId)
                                    .update("tasks", taskList)
                                    .addOnSuccessListener(aVoid -> Log.d("TaskAdapter", "Task updated successfully."))
                                    .addOnFailureListener(e -> Log.e("TaskAdapter", "Failed to update task.", e));
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("TaskAdapter", "Failed to fetch task group.", e));
    }

    public void updateTaskList(List<Map<String, Object>> newTasks) {
        tasks.clear();
        tasks.addAll(newTasks);
        notifyDataSetChanged();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView deadline;
        CheckBox checkbox;
        TextView editButton;
        TextView deleteButton;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.taskTitle);
            deadline = itemView.findViewById(R.id.taskDeadline);
            checkbox = itemView.findViewById(R.id.taskCheckbox);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
