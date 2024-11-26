package com.example.firstapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

/**
 * Handles user registration functionality.
 */
public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailField, passwordField, confirmPasswordField;
    private Button signupBtn;

    /**
     * Initializes the activity, setting up Firebase authentication and user interface.
     *
     * @param savedInstanceState Bundle containing the saved state of the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth and UI components
        mAuth = FirebaseAuth.getInstance();
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        confirmPasswordField = findViewById(R.id.confirm_password);
        signupBtn = findViewById(R.id.signupbtn);
        TextView loginBtn = findViewById(R.id.loginbtn);

        // Signup button logic
        signupBtn.setOnClickListener(view -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();
            String confirmPassword = confirmPasswordField.getText().toString().trim();

            if (password.equals(confirmPassword)) {
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(SignUpActivity.this, "Registration successful.", Toast.LENGTH_SHORT).show();
                                Intent login = new Intent(SignUpActivity.this, LoginActivity.class);
                                login.putExtra("email", emailField.getText().toString());
                                startActivity(login);
                                finish();
                            } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(SignUpActivity.this, "User already exists.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SignUpActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            } else {
                Toast.makeText(SignUpActivity.this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            }
        });

        // Redirect to login
        loginBtn.setOnClickListener(view -> startActivity(new Intent(SignUpActivity.this, LoginActivity.class)));
    }

    public FirebaseAuth getmAuth() {
        return mAuth;
    }
}
