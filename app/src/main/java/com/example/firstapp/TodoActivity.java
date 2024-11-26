package com.example.firstapp;

import android.app.NotificationManager;
import android.content.Context;
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

        // Set task group name in TextView
        if (taskGroupName != null) {
            taskGroupNameTextView.setText(taskGroupName);
        }

        // Load tasks from Firestore with default "All" filter
        if (taskGroupId != null) {
            loadTasksFromFirestore(taskGroupId, "All");
        } else {
            Toast.makeText(this, "Task group not found", Toast.LENGTH_SHORT).show();
        }

        // Handle back button click
        backBtn.setOnClickListener(view -> {
            Intent backIntent = new Intent(TodoActivity.this, ProjectsActivity.class);
            startActivity(backIntent);
        });

        // Handle add task button click
        addtaskBtn.setOnClickListener(view -> {
            Intent addtaskIntent = new Intent(TodoActivity.this, AddTaskActivity.class);
            addtaskIntent.putExtra("TASK_GROUP_ID", taskGroupId);
            addtaskIntent.putExtra("TASK_GROUP_NAME", taskGroupName);
            startActivity(addtaskIntent);
        });

        // Handle delete task group button click
        deleteTaskGroupBtn.setOnClickListener(view -> {
            if (taskGroupId != null) {
                showConfirmationDialog(taskGroupId);
            } else {
                Toast.makeText(this, "No task group to delete", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle dropdown selection for filtering
        filterDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isFilterSpinnerInitialized) {
                    String selectedFilter = parent.getItemAtPosition(position).toString();
                    // Reload tasks with the selected filter
                    loadTasksFromFirestore(taskGroupId, selectedFilter);
                } else {
                    isFilterSpinnerInitialized = true; // Ignore the initial selection
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Default to "All" filter
                loadTasksFromFirestore(taskGroupId, "All");
            }
        });

        notificationHelper = new NotificationHelper(this);
    }

    private void loadTasksFromFirestore(String taskGroupId, String filter) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("taskGroups")
                .document(taskGroupId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        tasks.clear(); // Clear existing tasks
                        List<Map<String, Object>> taskList = (List<Map<String, Object>>) documentSnapshot.get("tasks");
                        if (taskList != null) {
                            tasks.addAll(taskList); // Populate tasks
                        }

                        sortTasksByDeadline(tasks); // Sort tasks by deadline

                        // Apply the selected filter
                        applyFilter(filter);

                        // Check each task's due date and notify if necessary
                        for (Map<String, Object> task : tasks) {
                            long deadline = (long) task.get("deadline");
                            boolean status = task.get("status") != null && (boolean) task.get("status");

                            // Check if task is due today and not completed
                            if (!status && isTaskDueToday(deadline)) {
                                showTaskDueNotification(task); // Notify the user
                            }
                        }

                    } else {
                        Toast.makeText(this, "Task group document does not exist", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load tasks: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private boolean isTaskDueToday(long deadlineMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(deadlineMillis);

        Calendar today = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH);
    }

    private void showTaskDueNotification(Map<String, Object> task) {
        String taskTitle = (String) task.get("title");

        // Create a notification here
        NotificationCompat.Builder builder = new NotificationCompat.Builder(TodoActivity.this, "default")  // Use 'this' for context
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Use your own icon
                .setContentTitle("Task Due Today!")
                .setContentText(taskTitle + " is due today!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        // NotificationManager to display the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(TodoActivity.this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void deleteTaskGroup(String taskGroupId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Delete the task group document from Firestore
        db.collection("taskGroups")
                .document(taskGroupId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Task group deleted successfully, navigate back to ProjectsActivity
                    Toast.makeText(this, "Task group deleted", Toast.LENGTH_SHORT).show();
                    Intent backIntent = new Intent(TodoActivity.this, ProjectsActivity.class);
                    startActivity(backIntent);
                })
                .addOnFailureListener(e -> {
                    // If deletion fails, show an error message
                    Toast.makeText(this, "Failed to delete task group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

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
        taskAdapter.updateTaskList(new ArrayList<>(filteredTasks)); // Pass a new instance
    }

    private void showConfirmationDialog(final String taskGroupId) {
        new AlertDialog.Builder(this)
                .setTitle("Are you sure?")
                .setMessage("This action will permanently delete the task group and all its tasks. Are you sure you want to proceed?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // User confirmed, proceed to delete
                    deleteTaskGroup(taskGroupId);
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // User canceled, just dismiss the dialog
                    dialog.dismiss();
                })
                .setCancelable(false) // Prevent dismissing the dialog by clicking outside
                .show();
    }

    private void sortTasksByDeadline(List<Map<String, Object>> tasks) {
        Collections.sort(tasks, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> task1, Map<String, Object> task2) {
                Long deadline1 = (Long) task1.get("deadline");
                Long deadline2 = (Long) task2.get("deadline");

                return deadline1.compareTo(deadline2);
            }
        });
    }
}
