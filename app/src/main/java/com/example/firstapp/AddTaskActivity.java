package com.example.firstapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddTaskActivity extends AppCompatActivity {
    private ImageButton exitBtn;
    private Button createBtn;
    private ImageButton calendarBtn;
    private long selectedDeadline = 0;
    private String taskGroupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addtodo);

        // Initialize buttons
        exitBtn = findViewById(R.id.exitbtn);
        createBtn = findViewById(R.id.createtodobtn);
        calendarBtn = findViewById(R.id.calendarbtn);

        // Get the task group ID from Intent
        taskGroupId = getIntent().getStringExtra("TASK_GROUP_ID");
        if (taskGroupId == null || taskGroupId.isEmpty()) {
            Toast.makeText(this, "Task group not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Calendar button setup
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

        // Exit button setup
        exitBtn.setOnClickListener(view -> {
            Intent exitIntent = new Intent(AddTaskActivity.this, TodoActivity.class);
            exitIntent.putExtra("taskGroupId", taskGroupId); // Pass back the task group ID
            startActivity(exitIntent);
        });

        // Create button setup
        createBtn.setOnClickListener(view -> {
            // Validate inputs
            if (selectedDeadline == 0) {
                Toast.makeText(this, "Please set a deadline", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create task data
            Map<String, Object> taskData = new HashMap<>();
            taskData.put("title", "New Task"); // Replace with actual input from the UI
            taskData.put("deadline", selectedDeadline);
            taskData.put("status", false); // Default status: not completed
            taskData.put("groupId", taskGroupId);

            // Save task to Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("tasks")
                    .add(taskData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(AddTaskActivity.this, "Task created", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(AddTaskActivity.this, TodoActivity.class);
                        intent.putExtra("taskGroupId", taskGroupId); // Pass back the task group ID
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AddTaskActivity.this, "Failed to create task", Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
