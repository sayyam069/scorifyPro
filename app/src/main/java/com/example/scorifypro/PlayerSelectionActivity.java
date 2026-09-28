package com.example.scorifypro;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PlayerSelectionActivity extends AppCompatActivity {

    private EditText etStriker, etNonStriker, etBowler;
    private Button btnStartMatch;
    private String teamA, teamB;
    private int overs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_selection);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Get data from intent
        teamA = getIntent().getStringExtra("teamA");
        teamB = getIntent().getStringExtra("teamB");
        overs = getIntent().getIntExtra("overs", 10);

        initViews();

        btnStartMatch.setOnClickListener(v -> {
            String strikerName = etStriker.getText().toString().trim();
            String nonStrikerName = etNonStriker.getText().toString().trim();
            String bowlerName = etBowler.getText().toString().trim();

            if (strikerName.isEmpty() || nonStrikerName.isEmpty() || bowlerName.isEmpty()) {
                Toast.makeText(this, "Please enter all player names", Toast.LENGTH_SHORT).show();
            } else if (strikerName.equals(nonStrikerName)) {
                Toast.makeText(this, "Striker and Non-Striker cannot be same", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(PlayerSelectionActivity.this, ScoringActivity.class);
                intent.putExtra("teamA", teamA);
                intent.putExtra("teamB", teamB);
                intent.putExtra("overs", overs);
                intent.putExtra("strikerName", strikerName);
                intent.putExtra("nonStrikerName", nonStrikerName);
                intent.putExtra("bowlerName", bowlerName);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initViews() {
        etStriker = findViewById(R.id.etStriker);
        etNonStriker = findViewById(R.id.etNonStriker);
        etBowler = findViewById(R.id.etBowler);
        btnStartMatch = findViewById(R.id.btnStartMatch);
    }
}