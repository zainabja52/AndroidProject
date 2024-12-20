package com.example.a1201766_1201086_courseproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;
    private String userEmail;
    private SwitchCompat themeToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize ThemeManager and apply the current theme
        ThemeManager.init(this);
        ThemeManager.setDarkMode(ThemeManager.isDarkMode());
        setContentView(R.layout.activity_home);
        getSupportActionBar().setTitle("Task Management App");

        userEmail = getIntentEmail();

        // Initialize DrawerLayout and NavigationView
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        // Set up the ActionBarDrawerToggle
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // Set the ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Access the header view of the NavigationView
        View headerView = navigationView.getHeaderView(0);
        themeToggle = headerView.findViewById(R.id.themeToggle);

        // Set the initial state of the toggle switch
        themeToggle.setChecked(ThemeManager.isDarkMode());

        // Handle theme toggle changes
        themeToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ThemeManager.setDarkMode(isChecked); // Save and apply theme
        });

        // Set the NavigationItemSelectedListener
        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_today:
                    loadFragment(new TodayFragment());
                    break;
                case R.id.nav_new_task:
                    loadFragment(new NewTaskFragment());
                    break;
                case R.id.nav_all_tasks:
                    loadFragment(new AllTasksFragment());
                    break;
                case R.id.nav_completed_tasks:
                    loadFragment(new CompletedTasksFragment());
                    break;
                case R.id.nav_search:
                    loadFragment(new SearchFragment());
                    break;
                case R.id.nav_profile:
                    ProfileFragment profileFragment = new ProfileFragment();
                    Bundle args = new Bundle();
                    args.putString("email", userEmail); // Pass the email of the logged-in user
                    profileFragment.setArguments(args);
                    loadFragment(profileFragment);
                    break;
                case R.id.nav_logout:
                    logout();
                    break;
                default:
                    break;
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Load the default fragment (Today)
        loadFragment(new TodayFragment());
    }

    private String getIntentEmail() {
        Intent intent = getIntent();
        return intent.getStringExtra("email");
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.contentFrame, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void logout() {
        // Redirect to MainActivity
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
