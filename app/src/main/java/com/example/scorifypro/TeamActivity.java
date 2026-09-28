package com.example.scorifypro;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class TeamActivity extends AppCompatActivity {

    private EditText etTeamName;
    private ListView lvTeams;
    private DatabaseHelper db;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team);

        db = new DatabaseHelper(this);
        etTeamName = findViewById(R.id.etTeamName);
        Button btnAdd = findViewById(R.id.btnAdd);
        lvTeams = findViewById(R.id.lvTeams);

        loadTeams();

        btnAdd.setOnClickListener(v -> {
            String name = etTeamName.getText().toString().trim();
            if (!name.isEmpty()) {
                if (db.addTeam(name)) {
                    Toast.makeText(this, "Team Added: " + name, Toast.LENGTH_SHORT).show();
                    etTeamName.setText("");
                    loadTeams();
                } else {
                    Toast.makeText(this, "Team already exists", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Enter team name", Toast.LENGTH_SHORT).show();
            }
        });

        lvTeams.setOnItemLongClickListener((parent, view, position, id) -> {
            String team = adapter.getItem(position);

            new AlertDialog.Builder(this)
                    .setTitle("Delete Team")
                    .setMessage("Are you sure you want to delete \"" + team + "\"?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        if (db.deleteTeam(team)) {
                            Toast.makeText(this, "Team Deleted: " + team, Toast.LENGTH_SHORT).show();
                            loadTeams();
                        } else {
                            Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

            return true;
        });
    }

    private void loadTeams() {
        List<String> teams = db.getAllTeamNames();  // ✅ Fixed: using getAllTeamNames()
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, teams);
        lvTeams.setAdapter(adapter);
    }
}