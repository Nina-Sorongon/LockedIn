package com.example.firstapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * This activity manages tasks within a task group. It supports viewing, filtering,
 * deleting tasks, and displaying notifications for tasks due today.
 */
public class TodoActivity extends AppCompatActivity {
    private ImageButton backBtn;
    private ImageButton addtaskBtn;
    private TextView taskGroupNameTextView;
    private RecyclerView taskRecyclerView;
    private TaskAdapter taskAdapter;
    private List<Map<String, Object>> tasks;
    private List<Map<String, Object>> filteredTasks = new ArrayList<>();
    private boolean isFilterSpinnerInitialized = false;
    private NotificationHelper notificationHelper;

    /**
     * Initializes the activity and sets up UI components, intent data, and event listeners.
     *
     * @param savedInstanceState state information saved from a previous instance (if any).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);

        // Initialize views
        backBtn = findViewById(R.id.backbtn);
        addtaskBtn = findViewById(R.id.addtaskbtn);
        taskGroupNameTextView = findViewById(R.id.taskGroupNameTextView);
        taskRecyclerView = findViewById(R.id.taskRecyclerView);
        Spinner filterDropdown = findViewById(R.id.filterDropdown);
        ImageButton deleteTaskGroupBtn = findViewById(R.id.deleteTaskGroupBtn);

        // Get data from Intent
        Intent intent = getIntent();
        String taskGroupId = intent.getStringExtra("TASK_GROUP_ID");
        String taskGroupName = intent.getStringExtra("TASK_GROUP_NAME");

        // Set up RecyclerView
        tasks = new ArrayList<>();
        taskAdapter = new TaskAdapter(tasks, taskGroupId, this);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskRecyclerView.setAdapter(taskAdapter);

        if (taskGroupName != null) {
            taskGroupNameTextView.setText(taskGroupName);
        }

        // Load tasks from Firestore
        if (taskGroupId != null) {
            loadTasksFromFirestore(taskGroupId, "All");
        } else {
            Toast.makeText(this, "Task group not found", Toast.LENGTH_SHORT).show();
        }

        backBtn.setOnClickListener(view -> {
            Intent backIntent = new Intent(TodoActivity.this, ProjectsActivity.class);
            startActivity(backIntent);
        });

        addtaskBtn.setOnClickListener(view -> {
            Intent addtaskIntent = new Intent(TodoActivity.this, AddTaskActivity.class);
            addtaskIntent.putExtra("TASK_GROUP_ID", taskGroupId);
            addtaskIntent.putExtra("TASK_GROUP_NAME", taskGroupName);
            startActivity(addtaskIntent);
        });

        deleteTaskGroupBtn.setOnClickListener(view -> {
            if (taskGroupId != null) {
                showConfirmationDialog(taskGroupId);
            } else {
                Toast.makeText(this, "No task group to delete", Toast.LENGTH_SHORT).show();
            }
        });

        filterDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isFilterSpinnerInitialized) {
                    String selectedFilter = parent.getItemAtPosition(position).toString();
                    loadTasksFromFirestore(taskGroupId, selectedFilter);
                } else {
                    isFilterSpinnerInitialized = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                loadTasksFromFirestore(taskGroupId, "All");
            }
        });

        notificationHelper = new NotificationHelper(this);
    }

    /**
     * Loads tasks from Firestore, applies the selected filter, and sets up notifications for tasks due today.
     *
     * @param taskGroupId the ID of the task group to load tasks from.
     * @param filter      the filter to apply (e.g., "All", "Ongoing", "Completed").
     */
    private void loadTasksFromFirestore(String taskGroupId, String filter) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("taskGroups")
                .document(taskGroupId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        tasks.clear();
                        List<Map<String, Object>> taskList = (List<Map<String, Object>>) documentSnapshot.get("tasks");
                        if (taskList != null) {
                            tasks.addAll(taskList);
                        }

                        sortTasksByDeadline(tasks);
                        applyFilter(filter);

                        for (Map<String, Object> task : tasks) {
                            long deadline = (long) task.get("deadline");
                            boolean status = task.get("status") != null && (boolean) task.get("status");

                            if (!status && isTaskDueToday(deadline)) {
                                showTaskDueNotification(task);
                            }
                        }

                    } else {
                        Toast.makeText(this, "Task group document does not exist", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load tasks: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Checks if a task is due today.
     *
     * @param deadlineMillis the deadline in milliseconds.
     * @return true if the task is due today, false otherwise.
     */
    private boolean isTaskDueToday(long deadlineMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(deadlineMillis);

        Calendar today = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Shows a notification for a task that is due today.
     *
     * @param task the task to notify about.
     */
    private void showTaskDueNotification(Map<String, Object> task) {
        String taskTitle = (String) task.get("title");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(TodoActivity.this, "default")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Task Due Today!")
                .setContentText(taskTitle + " is due today!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(TodoActivity.this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    /**
     * Deletes a task group from Firestore.
     *
     * @param taskGroupId the ID of the task group to delete.
     */
    private void deleteTaskGroup(String taskGroupId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("taskGroups")
                .document(taskGroupId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Task group deleted", Toast.LENGTH_SHORT).show();
                    Intent backIntent = new Intent(TodoActivity.this, ProjectsActivity.class);
                    startActivity(backIntent);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete task group: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Applies the specified filter to the list of tasks and updates the RecyclerView.
     *
     * @param filter the filter to apply ("All", "Ongoing", or "Completed").
     */
    private void applyFilter(String filter) {
        filteredTasks.clear();

        if ("All".equals(filter)) {
            filteredTasks.addAll(tasks);
        } else {
            for (Map<String, Object> task : tasks) {
                boolean status = task.get("status") != null && (boolean) task.get("status");
                if ("Ongoing".equals(filter) && !status) {
                    filteredTasks.add(task);
                } else if ("Completed".equals(filter) && status) {
                    filteredTasks.add(task);
                }
            }
        }

        sortTasksByDeadline(filteredTasks);
        taskAdapter.updateTaskList(new ArrayList<>(filteredTasks));
    }

    /**
     * Displays a confirmation dialog to delete a task group.
     *
     * @param taskGroupId the ID of the task group to delete.
     */
    private void showConfirmationDialog(final String taskGroupId) {
        new AlertDialog.Builder(this)
                .setTitle("Are you sure?")
                .setMessage("This action will permanently delete the task group and all its tasks. Are you sure you want to proceed?")
                .setPositiveButton("Yes", (dialog, which) -> deleteTaskGroup(taskGroupId))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    /**
     * Sorts tasks by their deadline in ascending order.
     *
     * @param tasks the list of tasks to sort.
     */
    private void sortTasksByDeadline(List<Map<String, Object>> tasks) {
        Collections.sort(tasks, Comparator.comparingLong(task -> (long) task.get("deadline")));
    }
}
