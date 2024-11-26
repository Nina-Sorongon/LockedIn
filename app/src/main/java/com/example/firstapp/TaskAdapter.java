package com.example.firstapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private final List<Map<String, Object>> tasks;
    private final String taskGroupId; // The ID of the task group this list belongs to
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final Context context;
    private NotificationHelper notificationHelper;

    public TaskAdapter(List<Map<String, Object>> tasks, String taskGroupId, Context context) {
        this.tasks = tasks;
        this.taskGroupId = taskGroupId;
        this.context = context;
        this.notificationHelper = new NotificationHelper(context);
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

        // Remove existing listener to prevent triggering during programmatic updates
        holder.checkbox.setOnCheckedChangeListener(null);

        // Update checkbox state
        holder.checkbox.setChecked(status);

        // Reattach listener for user interactions
        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.put("status", isChecked);
            updateTaskInFirestore(task); // Corrected method call
        });

        // Edit button functionality
        holder.editButton.setOnClickListener(view -> showEditDialog(position, holder.title));

        // Delete button functionality
        holder.deleteButton.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete this task?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        String taskId = (String) tasks.get(position).get("id"); // Retrieve task ID
                        deleteTaskFromFirestore(taskId); // Pass the task ID
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        // Check the due date and send notifications if necessary
        checkDueDateAndNotify(task);
    }

    // Check if the task's due date is today and show notification if necessary
    private void checkDueDateAndNotify(Map<String, Object> task) {
        long deadline = task.get("deadline") != null ? (long) task.get("deadline") : 0;
        if (deadline > 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(deadline);

            // Check if the due date is today
            if (calendar.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR) &&
                    calendar.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) &&
                    calendar.get(Calendar.DAY_OF_MONTH) == Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) {

                // Only show notification if the task is not completed
                boolean isCompleted = task.get("status") != null && (boolean) task.get("status");
                if (!isCompleted) {
                    NotificationHelper notificationHelper = new NotificationHelper(context);
                    notificationHelper.showNotification(task.get("title").toString());
                }
            }
        }
    }



    private void deleteTaskFromFirestore(String taskId) {
        db.collection("taskGroups").document(taskGroupId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> taskList = (List<Map<String, Object>>) documentSnapshot.get("tasks");
                        if (taskList != null) {
                            taskList.removeIf(task -> taskId.equals(task.get("id")));

                            db.collection("taskGroups").document(taskGroupId)
                                    .update("tasks", taskList)
                                    .addOnSuccessListener(aVoid -> {
                                        tasks.removeIf(task -> taskId.equals(task.get("id")));
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
        builder.setTitle("Edit Task");

        // Container for title input and deadline selection
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(20, 20, 20, 20);

        // Input field for task title
        EditText inputTitle = new EditText(context);
        inputTitle.setInputType(InputType.TYPE_CLASS_TEXT);
        inputTitle.setText(titleView.getText().toString());
        inputTitle.setHint("Task Title");
        container.addView(inputTitle);

        // TextView for deadline
        TextView deadlineView = new TextView(context);
        long currentDeadline = tasks.get(taskIndex).get("deadline") != null ? (long) tasks.get(taskIndex).get("deadline") : 0;

        String formattedDeadline = currentDeadline > 0 ? new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(currentDeadline) : "No deadline set";
        deadlineView.setText("Due: " + formattedDeadline);
        deadlineView.setPadding(0, 20, 0, 0);
        container.addView(deadlineView);

        // Open date picker when the deadline is clicked
        deadlineView.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            if (currentDeadline > 0) {
                calendar.setTimeInMillis(currentDeadline);
            }

            new DatePickerDialog(context, (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                String selectedDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(calendar.getTime());
                deadlineView.setText("Due: " + selectedDate);
                tasks.get(taskIndex).put("deadline", calendar.getTimeInMillis());
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        builder.setView(container);

        // Save button
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newTitle = inputTitle.getText().toString().trim();
            boolean hasChanges = false;

            // Check if the title has changed
            if (!newTitle.isEmpty() && !newTitle.equals(tasks.get(taskIndex).get("title"))) {
                tasks.get(taskIndex).put("title", newTitle);
                titleView.setText(newTitle);
                hasChanges = true;
            }

            // Check if the deadline has changed
            long newDeadline = (long) tasks.get(taskIndex).get("deadline");
            if (newDeadline != currentDeadline) {
                hasChanges = true;
            }

            if (hasChanges) {
                updateTaskInFirestore(tasks.get(taskIndex));
                sortTasksByDeadline();
                notifyDataSetChanged();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void sortTasksByDeadline() {
        tasks.sort((task1, task2) -> {
            long deadline1 = task1.get("deadline") != null ? (long) task1.get("deadline") : Long.MAX_VALUE;
            long deadline2 = task2.get("deadline") != null ? (long) task2.get("deadline") : Long.MAX_VALUE;
            return Long.compare(deadline1, deadline2);
        });
    }


    private void updateTaskInFirestore(Map<String, Object> updatedTask) {
        String taskId = (String) updatedTask.get("id");
        db.collection("taskGroups").document(taskGroupId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> taskList = (List<Map<String, Object>>) documentSnapshot.get("tasks");
                        if (taskList != null) {
                            for (int i = 0; i < taskList.size(); i++) {
                                Map<String, Object> task = taskList.get(i);
                                if (taskId.equals(task.get("id"))) {
                                    taskList.set(i, updatedTask);
                                    break;
                                }
                            }

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
