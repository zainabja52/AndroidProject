package com.example.a1201766_1201086_courseproject;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    private EditText emailField, firstNameField, lastNameField;
    private Button saveProfileButton, createNewPasswordButton;
    private ImageView editEmailIcon;
    private DatabaseHelper databaseHelper;

    private String currentUserEmail;
    private boolean isEmailBeingEdited = false;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        emailField = view.findViewById(R.id.emailField);
        firstNameField = view.findViewById(R.id.firstNameField);
        lastNameField = view.findViewById(R.id.lastNameField);
        saveProfileButton = view.findViewById(R.id.saveProfileButton);
        createNewPasswordButton = view.findViewById(R.id.createNewPasswordButton);
        editEmailIcon = view.findViewById(R.id.editEmailIcon);

        databaseHelper = new DatabaseHelper(getContext());

        Bundle args = getArguments();
        if (args != null) {
            currentUserEmail = args.getString("email");
        }
        if (currentUserEmail == null) {
            Toast.makeText(getContext(), "No user email found. Please log in again.", Toast.LENGTH_SHORT).show();
            return view;
        }

        loadUserProfile();

        editEmailIcon.setOnClickListener(v -> {
            isEmailBeingEdited = true;
            emailField.setEnabled(true);
        });

        createNewPasswordButton.setOnClickListener(v -> showCreateNewPasswordDialog());

        saveProfileButton.setOnClickListener(v -> {
            if (isEmailBeingEdited) {
                showEmailConfirmationDialog();
            } else {
                Toast.makeText(getContext(), "No changes to save.", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    private void loadUserProfile() {
        Cursor cursor = databaseHelper.getUserDetails(currentUserEmail);

        if (cursor != null && cursor.moveToFirst()) {
            firstNameField.setText(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
            lastNameField.setText(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
            emailField.setText(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            currentUserEmail = emailField.getText().toString().trim();
            cursor.close();
        } else {
            Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEmailConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Confirm Email Update");

        // Inflate the custom dialog layout
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_confirm_password, null);
        builder.setView(dialogView);

        EditText currentPasswordField = dialogView.findViewById(R.id.currentPasswordField);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String currentPassword = currentPasswordField.getText().toString().trim();

            if (TextUtils.isEmpty(currentPassword)) {
                Toast.makeText(getContext(), "Current password is required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!databaseHelper.checkUser(currentUserEmail, currentPassword)) {
                Toast.makeText(getContext(), "Incorrect current password", Toast.LENGTH_SHORT).show();
                return;
            }

            saveUserProfile(currentPassword, null); // Update email only
            isEmailBeingEdited = false; // Reset email editing flag
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // Cancel email editing
            isEmailBeingEdited = false;
            emailField.setText(currentUserEmail);
            emailField.setEnabled(false);
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showCreateNewPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Create New Password");

        // Inflate the custom dialog layout
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_new_password, null);
        builder.setView(dialogView);

        EditText currentPasswordField = dialogView.findViewById(R.id.currentPasswordField);
        EditText newPasswordField = dialogView.findViewById(R.id.newPasswordField);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String currentPassword = currentPasswordField.getText().toString().trim();
            String newPassword = newPasswordField.getText().toString().trim();

            if (TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword)) {
                Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!databaseHelper.checkUser(currentUserEmail, currentPassword)) {
                Toast.makeText(getContext(), "Incorrect current password", Toast.LENGTH_SHORT).show();
                return;
            }

            saveUserProfile(currentPassword, newPassword); // Update password only
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void saveUserProfile(String currentPassword, String newPassword) {
        String newEmail = emailField.getText().toString().trim();

        if (newPassword == null) {
            Cursor cursor = databaseHelper.getUserDetails(currentUserEmail);
            if (cursor != null && cursor.moveToFirst()) {
                newPassword = cursor.getString(cursor.getColumnIndexOrThrow("password"));
                cursor.close();
            }
        }

        boolean isUpdated = databaseHelper.updateUserProfile(currentUserEmail, newEmail, newPassword);

        if (isUpdated) {
            Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
            currentUserEmail = newEmail; // Update email reference
            emailField.setEnabled(false);
        } else {
            Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
        }
    }
}