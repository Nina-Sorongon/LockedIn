package com.example.firstapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity for adding a new task group.
 * This activity allows the user to create a new task group and store it in Firestore.
 */
public class AddNewGroupActivity extends AppCompatActivity {
    private EditText groupNameInput;
    private Button createGroupButton;
    private ImageButton exitBtn;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    /**
     * Initializes the activity and sets up UI elements and Firebase instances.
     *
     * @param savedInstanceState The saved instance state of the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addnewgroup);

        groupNameInput = findViewById(R.id.edittitle);
        createGroupButton = findViewById(R.id.creategroupbtn);
        exitBtn = findViewById(R.id.exitbtn);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Set up the create group button
        createGroupButton.setOnClickListener(view -> createTaskGroup());

        // Set up the exit button
        exitBtn.setOnClickListener(view -> {
            Intent exitIntent = new Intent(AddNewGroupActivity.this, ProjectsActivity.class);
            startActivity(exitIntent);
        });
    }

    /**
     * Handles the creation of a new task group.
     * Validates user input, retrieves the current user, and saves the group to Firestore.
     */
    private void createTaskGroup() {
        String groupName = groupNameInput.getText().toString().trim();

        // Check if the group name is empty
        if (TextUtils.isEmpty(groupName)) {
            Toast.makeText(this, "Group name cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the user is logged in
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();

        // Prepare task group data
        Map<String, Object> taskGroup = new HashMap<>();
        taskGroup.put("name", groupName);
        taskGroup.put("userId", userId);
        taskGroup.put("email", user.getEmail());
        taskGroup.put("tasks", null); // No tasks initially

        // Add task group to Firestore
        db.collection("taskGroups")
                .add(taskGroup)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Task group created!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    Intent i = new Intent(AddNewGroupActivity.this, ProjectsActivity.class);
                    startActivity(i);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error creating task group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
