package com.example.firstapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Activity for adding a new task to a task group.
 * This activity allows users to set task details such as title and deadline,
 * and save them to a Firestore collection associated with a task group.
 */
public class AddTaskActivity extends AppCompatActivity {
    private ImageButton exitBtn;
    private Button createBtn;
    private ImageButton calendarBtn;
    private long selectedDeadline = 0;
    private String taskGroupId;
    private String taskGroupName;
    private EditText taskTitle;

    /**
     * Initializes the activity, retrieves the task group ID, and sets up event listeners for UI components.
     *
     * @param savedInstanceState The saved instance state of the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addtodo);

        // Initialize buttons and input fields
        exitBtn = findViewById(R.id.exitbtn);
        createBtn = findViewById(R.id.createtodobtn);
        calendarBtn = findViewById(R.id.calendarbtn);
        taskTitle = findViewById(R.id.edittitle);
        ImageButton dueTodayBtn = findViewById(R.id.duetodaybtn);
        ImageButton dueTomorrowBtn = findViewById(R.id.duetomorrowbtn);

        // Get the task group ID and name from the Intent
        taskGroupId = getIntent().getStringExtra("TASK_GROUP_ID");
        taskGroupName = getIntent().getStringExtra("TASK_GROUP_NAME");

        // Validate task group information
        if (taskGroupId == null || taskGroupId.isEmpty()) {
            Toast.makeText(this, "Task group not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set up the calendar button to select a deadline
        calendarBtn.setOnClickListener(view -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Deadline")
                    .build();

            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");

            datePicker.addOnPositiveButtonClickListener(selection -> {
                selectedDeadline = selection; // Store the selected date
                Toast.makeText(AddTaskActivity.this, "Deadline Set", Toast.LENGTH_SHORT).show();
            });
        });

        // Set the deadline to today
        dueTodayBtn.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            selectedDeadline = calendar.getTimeInMillis();
            Toast.makeText(this, "Due date set to today", Toast.LENGTH_SHORT).show();
        });

        // Set the deadline to tomorrow
        dueTomorrowBtn.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            selectedDeadline = calendar.getTimeInMillis();
            Toast.makeText(this, "Due date set to tomorrow", Toast.LENGTH_SHORT).show();
        });

        // Set up the exit button to navigate back to the task list
        exitBtn.setOnClickListener(view -> {
            Intent exitIntent = new Intent(AddTaskActivity.this, TodoActivity.class);
            exitIntent.putExtra("TASK_GROUP_ID", taskGroupId);
            exitIntent.putExtra("TASK_GROUP_NAME", taskGroupName);
            startActivity(exitIntent);
        });

        // Set up the create button to add a new task
        createBtn.setOnClickListener(view -> {
            if (selectedDeadline == 0) {
                Toast.makeText(this, "Please set a deadline", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create task data
            Map<String, Object> taskData = new HashMap<>();
            taskData.put("title", taskTitle.getText().toString());
            taskData.put("deadline", selectedDeadline);
            taskData.put("status", false); // Default status: not completed

            // Add task to Firestore
            addTaskToGroup(taskGroupId, taskData);
        });
    }

    /**
     * Adds a new task to the specified task group in Firestore.
     *
     * @param groupId  The ID of the task group to which the task will be added.
     * @param newTask  A map containing the task details (title, deadline, etc.).
     */
    private void addTaskToGroup(String groupId, Map<String, Object> newTask) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Generate a unique ID for the task
        String taskId = UUID.randomUUID().toString();
        newTask.put("id", taskId);

        db.collection("taskGroups")
                .document(groupId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> tasks = (List<Map<String, Object>>) documentSnapshot.get("tasks");
                        if (tasks == null) {
                            tasks = new ArrayList<>();
                        } else {
                            tasks = new ArrayList<>(tasks);
                        }

                        tasks.add(newTask);

                        db.collection("taskGroups")
                                .document(groupId)
                                .update("tasks", tasks)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Task added successfully", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(AddTaskActivity.this, TodoActivity.class);
                                    String groupName = getIntent().getStringExtra("TASK_GROUP_NAME");
                                    intent.putExtra("TASK_GROUP_ID", groupId);
                                    intent.putExtra("TASK_GROUP_NAME", groupName);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to add task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Task group not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load task group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
