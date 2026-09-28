package com.example.scorifypro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load theme before super.onCreate
        SharedPreferences pref = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        boolean isDarkMode = pref.getBoolean("isDarkMode", true);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Button btnStartMatch = findViewById(R.id.btnStartMatch);
        Button btnTeams = findViewById(R.id.btnTeams);
        Button btnHistory = findViewById(R.id.btnHistory);
        LinearLayout llThemeToggle = findViewById(R.id.llThemeToggle);
        TextView tvThemeName = findViewById(R.id.tvThemeName);
        ImageView ivThemeIcon = findViewById(R.id.ivThemeIcon);

        // Set initial toggle state
        if (isDarkMode) {
            tvThemeName.setText("DARK");
            ivThemeIcon.setImageResource(R.drawable.ic_moon);
        } else {
            tvThemeName.setText("WHITE");
            ivThemeIcon.setImageResource(R.drawable.ic_sun);
        }

        llThemeToggle.setOnClickListener(v -> {
            boolean currentMode = pref.getBoolean("isDarkMode", true);
            SharedPreferences.Editor editor = pref.edit();
            if (currentMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                editor.putBoolean("isDarkMode", false);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                editor.putBoolean("isDarkMode", true);
            }
            editor.apply();
            recreate();
        });

        btnStartMatch.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MatchSetupActivity.class);
            startActivity(intent);
        });

        btnTeams.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TeamActivity.class);
            startActivity(intent);
        });

        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });
    }
}