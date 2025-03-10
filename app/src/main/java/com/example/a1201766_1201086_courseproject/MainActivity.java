package com.example.a1201766_1201086_courseproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText emailField, passwordField;
    CheckBox rememberMeCheckBox;
    Button signInButton, signUpButton;
    DatabaseHelper dbHelper;
    ImageView backgroundImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.init(this);
        ThemeManager.setDarkMode(ThemeManager.isDarkMode());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("Task Management App");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#8A9C6B")));


        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);
        signInButton = findViewById(R.id.signInButton);
        signUpButton = findViewById(R.id.signUpButton);
        backgroundImageView = findViewById(R.id.backgroundImageView);
        dbHelper = new DatabaseHelper(this);

        if (ThemeManager.isDarkMode()) {
            backgroundImageView.setImageResource(R.drawable.photo3); // Set dark mode background
        } else {
            backgroundImageView.setImageResource(R.drawable.photo1); // Set light mode background
        }


        SharedPreferences preferences = getSharedPreferences("USER_PREF", MODE_PRIVATE);
        emailField.setText(preferences.getString("email", ""));



        signInButton.setOnClickListener(view -> {
            String email = emailField.getText().toString();
            String password = passwordField.getText().toString();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(MainActivity.this, "Please enter all fields!", Toast.LENGTH_SHORT).show();
            } else if (dbHelper.checkUser(email, password)) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("email", email); // Save logged-in user's email
                editor.apply();
                Log.d("LOGIN_EMAIL", "User email saved: " + email);

                if (rememberMeCheckBox.isChecked()) {
                    editor.putString("email", email);
                    editor.putBoolean("rememberMe", true);
                }
                editor.apply();
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                intent.putExtra("email", email); // Pass the email to HomeActivity
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(MainActivity.this, "Invalid credentials!", Toast.LENGTH_SHORT).show();
            }
        });

        signUpButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }
}