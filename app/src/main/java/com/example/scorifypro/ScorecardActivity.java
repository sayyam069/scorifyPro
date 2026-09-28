package com.example.scorifypro;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ScorecardActivity extends AppCompatActivity {

    private TextView tvMatchTitle, tvTeamAScore, tvTeamBScore, tvResult, tvPotmName;
    private TextView tvTeamANameShort, tvTeamBNameShort;
    private RecyclerView rvBattingA, rvBattingB, rvBowlingA, rvBowlingB;
    private Button btnSaveGallery, btnBack;
    private CardView cvPotm;
    private LinearLayout llScorecardContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scorecard);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initViews();

        String teamAName = getIntent().getStringExtra("teamA");
        String teamBName = getIntent().getStringExtra("teamB");
        int teamAScore = getIntent().getIntExtra("teamAScore", 0);
        int teamAWickets = getIntent().getIntExtra("teamAWickets", 0);
        String teamAOvers = getIntent().getStringExtra("teamAOvers");
        int teamBScore = getIntent().getIntExtra("teamBScore", 0);
        int teamBWickets = getIntent().getIntExtra("teamBWickets", 0);
        String teamBOvers = getIntent().getStringExtra("teamBOvers");
        String result = getIntent().getStringExtra("result");
        boolean isRuntime = getIntent().getBooleanExtra("isRuntime", false);
        boolean isFromHistory = getIntent().getBooleanExtra("isFromHistory", false);
        long matchId = getIntent().getLongExtra("matchId", -1);

        List<PlayerStats> battingStatsA, battingStatsB, bowlingStatsA, bowlingStatsB;

        if (isFromHistory && matchId != -1) {
            DatabaseHelper db = new DatabaseHelper(this);
            battingStatsA = db.getBattingStats(matchId, 1);
            battingStatsB = db.getBattingStats(matchId, 2);
            bowlingStatsA = db.getBowlingStats(matchId, 1);
            bowlingStatsB = db.getBowlingStats(matchId, 2);
        } else {
            battingStatsA = (List<PlayerStats>) getIntent().getSerializableExtra("battingStatsA");
            battingStatsB = (List<PlayerStats>) getIntent().getSerializableExtra("battingStatsB");
            bowlingStatsA = (List<PlayerStats>) getIntent().getSerializableExtra("bowlingStatsA");
            bowlingStatsB = (List<PlayerStats>) getIntent().getSerializableExtra("bowlingStatsB");
        }

        if (battingStatsA == null) battingStatsA = new ArrayList<>();
        if (battingStatsB == null) battingStatsB = new ArrayList<>();
        if (bowlingStatsA == null) bowlingStatsA = new ArrayList<>();
        if (bowlingStatsB == null) bowlingStatsB = new ArrayList<>();

        tvMatchTitle.setText(teamAName + " VS " + teamBName);
        tvTeamANameShort.setText(teamAName);
        tvTeamBNameShort.setText(teamBName);
        tvTeamAScore.setText(teamAScore + "/" + teamAWickets + " (" + teamAOvers + ")");
        tvTeamBScore.setText(teamBScore + "/" + teamBWickets + " (" + teamBOvers + ")");
        
        if (isRuntime) {
            tvResult.setText("🔴 LIVE PROGRESS");
            tvResult.setTextColor(Color.RED);
            btnSaveGallery.setVisibility(View.GONE); // Hide save button during match
        } else {
            tvResult.setText(result);
            tvResult.setTextColor(Color.parseColor("#FF9800"));
            btnSaveGallery.setVisibility(View.VISIBLE);
        }

        rvBattingA.setLayoutManager(new LinearLayoutManager(this));
        rvBattingB.setLayoutManager(new LinearLayoutManager(this));
        rvBowlingA.setLayoutManager(new LinearLayoutManager(this));
        rvBowlingB.setLayoutManager(new LinearLayoutManager(this));

        rvBattingA.setAdapter(new ScorecardAdapter(battingStatsA, "batting"));
        rvBattingB.setAdapter(new ScorecardAdapter(battingStatsB, "batting"));
        rvBowlingA.setAdapter(new ScorecardAdapter(bowlingStatsA, "bowling"));
        rvBowlingB.setAdapter(new ScorecardAdapter(bowlingStatsB, "bowling"));

        if (!isRuntime) {
            calculateAndShowPotm(battingStatsA, battingStatsB, bowlingStatsA, bowlingStatsB);
        }

        btnSaveGallery.setOnClickListener(v -> saveScorecardToGallery());
        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        tvMatchTitle = findViewById(R.id.tvMatchTitle);
        tvTeamAScore = findViewById(R.id.tvTeamAScore);
        tvTeamBScore = findViewById(R.id.tvTeamBScore);
        tvTeamANameShort = findViewById(R.id.tvTeamANameShort);
        tvTeamBNameShort = findViewById(R.id.tvTeamBNameShort);
        tvResult = findViewById(R.id.tvResult);
        tvPotmName = findViewById(R.id.tvPotmName);
        cvPotm = findViewById(R.id.cvPotm);
        rvBattingA = findViewById(R.id.rvBattingA);
        rvBattingB = findViewById(R.id.rvBattingB);
        rvBowlingA = findViewById(R.id.rvBowlingA);
        rvBowlingB = findViewById(R.id.rvBowlingB);
        btnSaveGallery = findViewById(R.id.btnSaveGallery);
        btnBack = findViewById(R.id.btnBack);
        llScorecardContent = findViewById(R.id.llScorecardContent);
    }

    private void calculateAndShowPotm(List<PlayerStats> bA, List<PlayerStats> bB, List<PlayerStats> bwA, List<PlayerStats> bwB) {
        String potmName = "N/A";
        double maxPoints = -1;

        List<PlayerStats> allPlayers = new ArrayList<>();
        allPlayers.addAll(bA);
        allPlayers.addAll(bB);
        allPlayers.addAll(bwA);
        allPlayers.addAll(bwB);

        // Simple algorithm: Runs * 1 + Wickets * 25
        for (PlayerStats p : allPlayers) {
            double points = (p.runs * 1) + (p.wickets * 25);
            if (points > maxPoints && points > 0) {
                maxPoints = points;
                potmName = p.name;
            }
        }

        if (maxPoints > 0) {
            tvPotmName.setText(potmName);
            cvPotm.setVisibility(View.VISIBLE);
        }
    }

    private void saveScorecardToGallery() {
        try {
            Bitmap bitmap = getBitmapFromView(llScorecardContent);
            String fileName = "Scorecard_" + System.currentTimeMillis() + ".png";
            
            OutputStream fos;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Scorify");
                Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                fos = getContentResolver().openOutputStream(imageUri);
            } else {
                String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                java.io.File image = new java.io.File(imagesDir, fileName);
                fos = new java.io.FileOutputStream(image);
            }

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            Snackbar.make(llScorecardContent, "✅ Scorecard saved to Gallery!", Snackbar.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving scorecard: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap getBitmapFromView(View view) {
        // Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.parseColor("#0A0A0A")); // Background color
        view.draw(canvas);
        return returnedBitmap;
    }

    public static class PlayerStats implements Serializable {
        public String name;
        public int runs, balls, fours, sixes, runsGiven, wickets;
        public float overs;

        public PlayerStats(String name) {
            this.name = name;
        }
    }
}