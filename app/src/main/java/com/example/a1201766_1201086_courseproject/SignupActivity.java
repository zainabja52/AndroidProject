package com.example.a1201766_1201086_courseproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    EditText emailField, firstNameField, lastNameField, passwordField, confirmPasswordField;
    Button registerButton;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Enable the back button in the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#8A9C6B")));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Sign Up"); // Optional: Set a title
        }

        // Initialize views
        emailField = findViewById(R.id.emailField);
        firstNameField = findViewById(R.id.firstNameField);
        lastNameField = findViewById(R.id.lastNameField);
        passwordField = findViewById(R.id.passwordField);
        confirmPasswordField = findViewById(R.id.confirmPasswordField);
        registerButton = findViewById(R.id.registerButton);

        dbHelper = new DatabaseHelper(this);

        registerButton.setOnClickListener(view -> {
            String email = emailField.getText().toString();
            String firstName = firstNameField.getText().toString();
            String lastName = lastNameField.getText().toString();
            String password = passwordField.getText().toString();
            String confirmPassword = confirmPasswordField.getText().toString();

            if (validateInputs(email, firstName, lastName, password, confirmPassword)) {
                if (dbHelper.insertUser(email, firstName, lastName, password)) {
                    SharedPreferences preferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.clear();
                    editor.apply();

                    Toast.makeText(SignupActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(SignupActivity.this, "Registration Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validateInputs(String email, String firstName, String lastName, String password, String confirmPassword) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.setError("Invalid email format");
            return false;
        }
        if (firstName.length() < 5 || firstName.length() > 20) {
            firstNameField.setError("First name must be between 5 and 20 characters");
            return false;
        }
        if (lastName.length() < 5 || lastName.length() > 20) {
            lastNameField.setError("Last name must be between 5 and 20 characters");
            return false;
        }
        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{6,12}$")) {
            passwordField.setError("Password must be 6-12 characters and include uppercase, lowercase, and numbers");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordField.setError("Passwords do not match");
            return false;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Navigate back when the up button is pressed
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
