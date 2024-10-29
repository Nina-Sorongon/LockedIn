package com.example.firstapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class AddTaskActivity extends AppCompatActivity {
    private ImageButton exitBtn;
    private Button createBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addtodo);

        exitBtn = findViewById(R.id.exitbtn);
        createBtn = findViewById(R.id.createtodobtn);

        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent exitIntent = new Intent(AddTaskActivity.this, TodoActivity.class);
                startActivity(exitIntent);
            }
        });

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createIntent = new Intent(AddTaskActivity.this, TodoActivity.class);
                startActivity(createIntent);
            }
        });
    }
}
