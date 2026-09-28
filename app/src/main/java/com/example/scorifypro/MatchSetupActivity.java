package com.example.scorifypro;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MatchSetupActivity extends AppCompatActivity {

    private EditText etTeamA, etTeamB, etOvers;
    private Button btnStartMatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_setup);

        // Fullscreen mode
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Initialize views
        etTeamA = findViewById(R.id.etTeamA);
        etTeamB = findViewById(R.id.etTeamB);
        etOvers = findViewById(R.id.etOvers);
        btnStartMatch = findViewById(R.id.btnStartMatch);

        // Start Match button click
        btnStartMatch.setOnClickListener(v -> {
            String teamA = etTeamA.getText().toString().trim();
            String teamB = etTeamB.getText().toString().trim();
            String oversStr = etOvers.getText().toString().trim();

            if (teamA.isEmpty() || teamB.isEmpty() || oversStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (teamA.equalsIgnoreCase(teamB)) {
                Toast.makeText(this, "Team names must be different", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int overs = Integer.parseInt(oversStr);
                if (overs <= 0) {
                    Toast.makeText(this, "Overs must be greater than 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Proceed to Player Selection
                Intent intent = new Intent(MatchSetupActivity.this, PlayerSelectionActivity.class);
                intent.putExtra("teamA", teamA);
                intent.putExtra("teamB", teamB);
                intent.putExtra("overs", overs);
                startActivity(intent);
                
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number of overs", Toast.LENGTH_SHORT).show();
            }
        });
    }
}