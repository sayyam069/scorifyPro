package com.example.scorifypro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import android.graphics.Color;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;

public class ScoringActivity extends AppCompatActivity {

    private TextView tvTeamName, tvScore, tvWickets, tvOvers, tvRunRate, tvTarget, tvRequired;
    private TextView tvStriker, tvNonStriker, tvBowler, tvFreeHitIndicator;
    private LinearLayout llTarget, llFreeHit;
    private Button btn0, btn1, btn2, btn3, btn4, btn5, btn6, btnWide, btnNoBall, btnWicket, btnUndo, btnEndInnings, btnChangeBowler, btnScorecard;
    private LinearLayout llCurrentOver;
    private List<String> currentOverBalls = new ArrayList<>();

    private int runs = 0;
    private int wickets = 0;
    private int balls = 0;
    private int oversLimit = 0;
    private int target = 0;
    private boolean isSecondInnings = false;
    private int firstInningsScore = 0;
    private int firstInningsBalls = 0;
    private int wicketsBeforeSecond = 0;
    private boolean isMatchEnded = false;
    private boolean isFreeHit = false;

    private String teamAName, teamBName;
    private int currentStrikerIndex = 0;
    private int currentNonStrikerIndex = 1;
    private int currentBowlerIndex = 0;

    private List<Player> battingPlayers = new ArrayList<>();
    private List<Player> bowlingPlayers = new ArrayList<>();
    private List<Ball> ballHistory = new ArrayList<>();

    // For Scorecard
    private List<ScorecardActivity.PlayerStats> battingStatsA = new ArrayList<>();
    private List<ScorecardActivity.PlayerStats> battingStatsB = new ArrayList<>();
    private List<ScorecardActivity.PlayerStats> bowlingStatsA = new ArrayList<>();
    private List<ScorecardActivity.PlayerStats> bowlingStatsB = new ArrayList<>();

    private static class Player {
        String name;
        int runs, balls, fours, sixes;
        float overs;
        int runsGiven, wickets;

        Player(String name) {
            this.name = name;
            this.runs = 0;
            this.balls = 0;
            this.fours = 0;
            this.sixes = 0;
            this.overs = 0;
            this.runsGiven = 0;
            this.wickets = 0;
        }
    }

    private static class Ball {
        int runsAdded, wicketAdded, ballsAdded, strikerIndex, nonStrikerIndex, bowlerIndex;
        int strikerRunsBefore, strikerBallsBefore, nonStrikerRunsBefore, nonStrikerBallsBefore;
        int bowlerRunsBefore, bowlerWicketsBefore, runsBefore, wicketsBefore, ballsBefore;
        float bowlerOversBefore;
        boolean freeHitBefore;
        String ballLabel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoring);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        teamAName = getIntent().getStringExtra("teamA");
        teamBName = getIntent().getStringExtra("teamB");
        oversLimit = getIntent().getIntExtra("overs", 2);
        String strikerName = getIntent().getStringExtra("strikerName");
        String nonStrikerName = getIntent().getStringExtra("nonStrikerName");
        String bowlerName = getIntent().getStringExtra("bowlerName");

        battingPlayers.add(new Player(strikerName));
        battingPlayers.add(new Player(nonStrikerName));
        for (int i = 3; i <= 11; i++) {
            battingPlayers.add(new Player("Batsman " + i));
        }

        bowlingPlayers.add(new Player(bowlerName));
        for (int i = 2; i <= 5; i++) {
            bowlingPlayers.add(new Player("Bowler " + i));
        }

        initViews();
        tvTeamName.setText(teamAName + " vs " + teamBName);
        tvOvers.setText("Overs: 0.0/" + oversLimit);

        updateBatsmanDisplay();
        setupClickListeners();
    }

    private void initViews() {
        tvTeamName = findViewById(R.id.tvTeamName);
        tvScore = findViewById(R.id.tvScore);
        tvWickets = findViewById(R.id.tvWickets);
        tvOvers = findViewById(R.id.tvOvers);
        tvRunRate = findViewById(R.id.tvRunRate);
        tvTarget = findViewById(R.id.tvTarget);
        tvRequired = findViewById(R.id.tvRequired);
        tvStriker = findViewById(R.id.tvStriker);
        tvNonStriker = findViewById(R.id.tvNonStriker);
        tvBowler = findViewById(R.id.tvBowler);
        tvFreeHitIndicator = findViewById(R.id.tvFreeHitIndicator);
        llTarget = findViewById(R.id.llTarget);
        llFreeHit = findViewById(R.id.llFreeHit);

        btn0 = findViewById(R.id.btn0);
        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);
        btn4 = findViewById(R.id.btn4);
        btn5 = findViewById(R.id.btn5);
        btn6 = findViewById(R.id.btn6);
        btnWide = findViewById(R.id.btnWide);
        btnNoBall = findViewById(R.id.btnNoBall);
        btnWicket = findViewById(R.id.btnWicket);
        btnUndo = findViewById(R.id.btnUndo);
        btnEndInnings = findViewById(R.id.btnEndInnings);
        btnChangeBowler = findViewById(R.id.btnChangeBowler);
        btnScorecard = findViewById(R.id.btnScorecard);
        llCurrentOver = findViewById(R.id.llCurrentOver);
    }

    private void setupClickListeners() {
        btn0.setOnClickListener(v -> addRuns(0));
        btn1.setOnClickListener(v -> addRuns(1));
        btn2.setOnClickListener(v -> addRuns(2));
        btn3.setOnClickListener(v -> addRuns(3));
        btn4.setOnClickListener(v -> addRuns(4));
        btn5.setOnClickListener(v -> addRuns(5));
        btn6.setOnClickListener(v -> addRuns(6));
        btnWide.setOnClickListener(v -> showWideBallDialog());
        btnNoBall.setOnClickListener(v -> showNoBallDialog());
        btnWicket.setOnClickListener(v -> addWicket());
        btnUndo.setOnClickListener(v -> undoLastBall());
        btnEndInnings.setOnClickListener(v -> endInnings());
        btnChangeBowler.setOnClickListener(v -> showAddBowlerDialog());
        btnScorecard.setOnClickListener(v -> showCurrentScorecard());
    }

    private void showCurrentScorecard() {
        saveInningsStats(!isSecondInnings);
        Intent intent = new Intent(ScoringActivity.this, ScorecardActivity.class);
        intent.putExtra("teamA", teamAName);
        intent.putExtra("teamB", teamBName);
        
        if (!isSecondInnings) {
            intent.putExtra("teamAScore", runs);
            intent.putExtra("teamAWickets", wickets);
            intent.putExtra("teamAOvers", String.format("%.1f", (float) balls / 6));
            intent.putExtra("teamBScore", 0);
            intent.putExtra("teamBWickets", 0);
            intent.putExtra("teamBOvers", "0.0");
            intent.putExtra("result", "First Innings in Progress");
        } else {
            intent.putExtra("teamAScore", firstInningsScore);
            intent.putExtra("teamAWickets", wicketsBeforeSecond);
            intent.putExtra("teamAOvers", String.format("%.1f", (float) firstInningsBalls / 6));
            intent.putExtra("teamBScore", runs);
            intent.putExtra("teamBWickets", wickets);
            intent.putExtra("teamBOvers", String.format("%.1f", (float) balls / 6));
            intent.putExtra("result", "Second Innings in Progress");
        }

        intent.putExtra("battingStatsA", (ArrayList) battingStatsA);
        intent.putExtra("battingStatsB", (ArrayList) battingStatsB);
        intent.putExtra("bowlingStatsA", (ArrayList) bowlingStatsA);
        intent.putExtra("bowlingStatsB", (ArrayList) bowlingStatsB);
        intent.putExtra("isRuntime", true);
        startActivity(intent);
    }

    private boolean isOverComplete() {
        return balls >= oversLimit * 6;
    }

    private void saveBallState(int runsAdded, int wicketAdded, int ballsAdded,
                               int strikerIdx, int nonStrikerIdx, int bowlerIdx, String label) {
        Ball ball = new Ball();
        ball.runsAdded = runsAdded;
        ball.wicketAdded = wicketAdded;
        ball.ballsAdded = ballsAdded;
        ball.strikerIndex = strikerIdx;
        ball.nonStrikerIndex = nonStrikerIdx;
        ball.bowlerIndex = bowlerIdx;
        ball.ballLabel = label;

        ball.strikerRunsBefore = battingPlayers.get(strikerIdx).runs;
        ball.strikerBallsBefore = battingPlayers.get(strikerIdx).balls;
        ball.nonStrikerRunsBefore = battingPlayers.get(nonStrikerIdx).runs;
        ball.nonStrikerBallsBefore = battingPlayers.get(nonStrikerIdx).balls;
        ball.bowlerRunsBefore = bowlingPlayers.get(bowlerIdx).runsGiven;
        ball.bowlerWicketsBefore = bowlingPlayers.get(bowlerIdx).wickets;
        ball.bowlerOversBefore = bowlingPlayers.get(bowlerIdx).overs;
        ball.runsBefore = runs;
        ball.wicketsBefore = wickets;
        ball.ballsBefore = balls;
        ball.freeHitBefore = isFreeHit;
        ballHistory.add(ball);
        
        if (label != null) {
            currentOverBalls.add(label);
        }
    }

    private void addRuns(int runsToAdd) {
        if (isMatchEnded) return;
        if (isOverComplete()) {
            showSnackbar("Overs completed! Press End Innings");
            return;
        }

        Player striker = battingPlayers.get(currentStrikerIndex);
        Player bowler = bowlingPlayers.get(currentBowlerIndex);

        saveBallState(runsToAdd, 0, 1, currentStrikerIndex, currentNonStrikerIndex, currentBowlerIndex, String.valueOf(runsToAdd));

        runs += runsToAdd;
        balls += 1;

        striker.runs += runsToAdd;
        striker.balls += 1;
        bowler.runsGiven += runsToAdd;

        if (runsToAdd == 4) striker.fours++;
        if (runsToAdd == 6) striker.sixes++;

        bowler.overs = (float) balls / 6;

        if (runsToAdd % 2 == 1) {
            int temp = currentStrikerIndex;
            currentStrikerIndex = currentNonStrikerIndex;
            currentNonStrikerIndex = temp;
        }

        isFreeHit = false;
        updateFreeHitIndicator();

        updateUI();
        updateBatsmanDisplay();

        if (balls % 6 == 0 && balls > 0 && !isMatchEnded && balls < oversLimit * 6) {
            currentOverBalls.clear(); // New over starts
            showAddBowlerDialog();
        }
    }

    private void showWideBallDialog() {
        if (isMatchEnded) return;
        if (isOverComplete()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_wide_ball, null);
        builder.setView(dialogView);
        builder.setTitle("Wide Ball");
        builder.setCancelable(true);

        Button btnWide0 = dialogView.findViewById(R.id.btnWide0);
        Button btnWide1 = dialogView.findViewById(R.id.btnWide1);
        Button btnWide2 = dialogView.findViewById(R.id.btnWide2);
        Button btnWide3 = dialogView.findViewById(R.id.btnWide3);
        Button btnWide4 = dialogView.findViewById(R.id.btnWide4);
        Button btnWide5 = dialogView.findViewById(R.id.btnWide5);
        Button btnWide6 = dialogView.findViewById(R.id.btnWide6);
        Button btnWideRunOut = dialogView.findViewById(R.id.btnWideRunOut);

        AlertDialog dialog = builder.create();

        View.OnClickListener listener = v -> {
            int extraRuns = 0;
            boolean isRunOut = false;

            if (v.getId() == R.id.btnWide0) extraRuns = 0;
            else if (v.getId() == R.id.btnWide1) extraRuns = 1;
            else if (v.getId() == R.id.btnWide2) extraRuns = 2;
            else if (v.getId() == R.id.btnWide3) extraRuns = 3;
            else if (v.getId() == R.id.btnWide4) extraRuns = 4;
            else if (v.getId() == R.id.btnWide5) extraRuns = 5;
            else if (v.getId() == R.id.btnWide6) extraRuns = 6;
            else if (v.getId() == R.id.btnWideRunOut) {
                extraRuns = 0;
                isRunOut = true;
            }

            Player bowler = bowlingPlayers.get(currentBowlerIndex);
            int totalRunsOnThisBall = 1 + extraRuns;
            String label = "Wd" + (extraRuns > 0 ? "+" + extraRuns : "");

            saveBallState(totalRunsOnThisBall, isRunOut ? 1 : 0, 0,
                    currentStrikerIndex, currentNonStrikerIndex, currentBowlerIndex, label);

            runs += totalRunsOnThisBall;
            bowler.runsGiven += totalRunsOnThisBall;

            if (isRunOut) {
                wickets++;
                showSnackbar("RUN OUT on Wide Ball!");
                showNewBatsmanDialog();
            }

            if (extraRuns % 2 == 1 && !isRunOut) {
                int temp = currentStrikerIndex;
                currentStrikerIndex = currentNonStrikerIndex;
                currentNonStrikerIndex = temp;
            }

            updateUI();
            updateBatsmanDisplay();
            showSnackbar("Wide ball! +" + totalRunsOnThisBall + " runs");
            updateFreeHitIndicator();

            dialog.dismiss();
        };

        btnWide0.setOnClickListener(listener);
        btnWide1.setOnClickListener(listener);
        btnWide2.setOnClickListener(listener);
        btnWide3.setOnClickListener(listener);
        btnWide4.setOnClickListener(listener);
        btnWide5.setOnClickListener(listener);
        btnWide6.setOnClickListener(listener);
        btnWideRunOut.setOnClickListener(listener);

        dialog.show();
    }

    private void showNoBallDialog() {
        if (isMatchEnded) return;
        if (isOverComplete()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_no_ball, null);
        builder.setView(dialogView);
        builder.setTitle("No Ball");
        builder.setCancelable(true);

        Button btn0 = dialogView.findViewById(R.id.btnNoBall0);
        Button btn1 = dialogView.findViewById(R.id.btnNoBall1);
        Button btn2 = dialogView.findViewById(R.id.btnNoBall2);
        Button btn3 = dialogView.findViewById(R.id.btnNoBall3);
        Button btn4 = dialogView.findViewById(R.id.btnNoBall4);
        Button btn5 = dialogView.findViewById(R.id.btnNoBall5);
        Button btn6 = dialogView.findViewById(R.id.btnNoBall6);
        Button btnRunOut = dialogView.findViewById(R.id.btnRunOut);

        AlertDialog dialog = builder.create();

        View.OnClickListener listener = v -> {
            int runsScored = 0;
            boolean isRunOut = false;

            if (v.getId() == R.id.btnNoBall0) runsScored = 0;
            else if (v.getId() == R.id.btnNoBall1) runsScored = 1;
            else if (v.getId() == R.id.btnNoBall2) runsScored = 2;
            else if (v.getId() == R.id.btnNoBall3) runsScored = 3;
            else if (v.getId() == R.id.btnNoBall4) runsScored = 4;
            else if (v.getId() == R.id.btnNoBall5) runsScored = 5;
            else if (v.getId() == R.id.btnNoBall6) runsScored = 6;
            else if (v.getId() == R.id.btnRunOut) {
                runsScored = 0;
                isRunOut = true;
            }

            Player striker = battingPlayers.get(currentStrikerIndex);
            Player bowler = bowlingPlayers.get(currentBowlerIndex);
            String label = "Nb" + (runsScored > 0 ? "+" + runsScored : "");

            saveBallState(1 + runsScored, isRunOut ? 1 : 0, 0,
                    currentStrikerIndex, currentNonStrikerIndex, currentBowlerIndex, label);

            runs += 1 + runsScored;
            striker.runs += runsScored;
            bowler.runsGiven += 1 + runsScored;

            if (runsScored == 4) striker.fours++;
            if (runsScored == 6) striker.sixes++;

            if (isRunOut) {
                wickets++;
                showSnackbar("RUN OUT on No Ball!");
                showNewBatsmanDialog();
            }

            if (runsScored % 2 == 1 && !isRunOut) {
                int temp = currentStrikerIndex;
                currentStrikerIndex = currentNonStrikerIndex;
                currentNonStrikerIndex = temp;
            }

            isFreeHit = true;
            updateFreeHitIndicator();
            showSnackbar("⚡ FREE HIT! Next ball ⚡");

            updateUI();
            updateBatsmanDisplay();

            dialog.dismiss();
        };

        btn0.setOnClickListener(listener);
        btn1.setOnClickListener(listener);
        btn2.setOnClickListener(listener);
        btn3.setOnClickListener(listener);
        btn4.setOnClickListener(listener);
        btn5.setOnClickListener(listener);
        btn6.setOnClickListener(listener);
        btnRunOut.setOnClickListener(listener);

        dialog.show();
    }

    private void addWicket() {
        if (isMatchEnded) return;
        if (isOverComplete()) return;
        if (wickets >= 10) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("☝️ Select Wicket Type");
        
        String[] types;
        if (isFreeHit) {
            types = new String[]{"Run Out (Legal on Free Hit)"};
        } else {
            types = new String[]{"Bowled / Caught / LBW", "Run Out"};
        }

        builder.setItems(types, (dialog, which) -> {
            if (isFreeHit || which == 1) {
                showRunOutDialog();
            } else {
                processNormalWicket();
            }
        });
        builder.show();
    }

    private void processNormalWicket() {
        Player striker = battingPlayers.get(currentStrikerIndex);
        Player bowler = bowlingPlayers.get(currentBowlerIndex);

        saveBallState(0, 1, 1, currentStrikerIndex, currentNonStrikerIndex, currentBowlerIndex, "W");

        wickets++;
        balls++;
        striker.balls += 1;
        bowler.wickets++;
        bowler.overs = (float) balls / 6;

        updateUI();
        showNewBatsmanDialog();

        if (wickets == 10) {
            endInnings();
        }
    }

    private void showRunOutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        // Custom Red Header
        TextView header = new TextView(this);
        header.setText("RUN OUT");
        header.setBackgroundColor(Color.parseColor("#D32F2F")); // Professional Red
        header.setTextColor(Color.WHITE);
        header.setTextSize(20);
        header.setTypeface(null, android.graphics.Typeface.BOLD);
        header.setGravity(Gravity.CENTER);
        header.setPadding(20, 30, 20, 30);
        builder.setCustomTitle(header);
        
        View view = getLayoutInflater().inflate(R.layout.dialog_wide_ball, null); // Reuse wide layout for runs selection
        builder.setView(view);
        
        TextView tvMsg = new TextView(this);
        tvMsg.setText("Select runs completed before Run Out:");
        tvMsg.setPadding(40, 20, 40, 10);
        tvMsg.setTextColor(Color.WHITE);
        ((LinearLayout)view).addView(tvMsg, 0);

        AlertDialog dialog = builder.create();

        View.OnClickListener runListener = v -> {
            int runsCompleted = 0;
            if (v.getId() == R.id.btnWide0) runsCompleted = 0;
            else if (v.getId() == R.id.btnWide1) runsCompleted = 1;
            else if (v.getId() == R.id.btnWide2) runsCompleted = 2;
            else if (v.getId() == R.id.btnWide3) runsCompleted = 3;
            else if (v.getId() == R.id.btnWide4) runsCompleted = 4;
            
            final int finalRuns = runsCompleted;
            dialog.dismiss();
            
            // Ask who is out
            new AlertDialog.Builder(this)
                .setTitle("Who got out?")
                .setItems(new String[]{"Striker (" + battingPlayers.get(currentStrikerIndex).name + ")", 
                                     "Non-Striker (" + battingPlayers.get(currentNonStrikerIndex).name + ")"}, (d, whichOut) -> {
                    processRunOut(finalRuns, whichOut == 0);
                }).show();
        };

        view.findViewById(R.id.btnWide0).setOnClickListener(runListener);
        view.findViewById(R.id.btnWide1).setOnClickListener(runListener);
        view.findViewById(R.id.btnWide2).setOnClickListener(runListener);
        view.findViewById(R.id.btnWide3).setOnClickListener(runListener);
        view.findViewById(R.id.btnWide4).setOnClickListener(runListener);
        view.findViewById(R.id.btnWide5).setVisibility(View.GONE);
        view.findViewById(R.id.btnWide6).setVisibility(View.GONE);
        view.findViewById(R.id.btnWideRunOut).setVisibility(View.GONE);

        dialog.show();
    }

    private void processRunOut(int runsCompleted, boolean isStrikerOut) {
        Player striker = battingPlayers.get(currentStrikerIndex);
        Player nonStriker = battingPlayers.get(currentNonStrikerIndex);
        Player bowler = bowlingPlayers.get(currentBowlerIndex);

        saveBallState(runsCompleted, 1, 1, currentStrikerIndex, currentNonStrikerIndex, currentBowlerIndex, "W(RO)");

        runs += runsCompleted;
        wickets++;
        balls++;
        
        striker.runs += runsCompleted;
        striker.balls += 1;
        bowler.runsGiven += runsCompleted;
        bowler.overs = (float) balls / 6;

        // Rotation logic
        if (runsCompleted % 2 == 1) {
            int temp = currentStrikerIndex;
            currentStrikerIndex = currentNonStrikerIndex;
            currentNonStrikerIndex = temp;
        }

        updateUI();
        
        // Handle new batsman
        if (isStrikerOut) {
            showNewBatsmanDialogForRunOut(true);
        } else {
            showNewBatsmanDialogForRunOut(false);
        }

        isFreeHit = false;
        updateFreeHitIndicator();

        if (wickets == 10) {
            endInnings();
        }
    }

    private void showNewBatsmanDialogForRunOut(boolean wasStrikerOut) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🏏 New Batsman");
        builder.setMessage("Enter new batsman name:");
        final EditText input = new EditText(this);
        input.setTextColor(Color.WHITE);
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = input.getText().toString().trim();
            Player p = new Player(name.isEmpty() ? "Batsman " + (battingPlayers.size() + 1) : name);
            battingPlayers.add(p);
            if (wasStrikerOut) {
                currentStrikerIndex = battingPlayers.size() - 1;
            } else {
                currentNonStrikerIndex = battingPlayers.size() - 1;
            }
            updateBatsmanDisplay();
            updateUI();
        });
        builder.show();
    }

    private void showNewBatsmanDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🏏 New Batsman");
        builder.setMessage("Enter new batsman name:");

        final EditText input = new EditText(this);
        input.setHint("e.g., Babar Azam");
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.GRAY);
        input.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        builder.setView(input);

        builder.setPositiveButton("Add Batsman", (dialog, which) -> {
            String batsmanName = input.getText().toString().trim();
            if (batsmanName.isEmpty()) {
                showSnackbar("Please enter batsman name");
                showNewBatsmanDialog();
            } else {
                // Check if batsman already exists
                int existingIndex = -1;
                for (int i = 0; i < battingPlayers.size(); i++) {
                    if (battingPlayers.get(i).name.equalsIgnoreCase(batsmanName)) {
                        existingIndex = i;
                        break;
                    }
                }

                if (existingIndex != -1) {
                    currentStrikerIndex = existingIndex;
                    showSnackbar("Batsman " + batsmanName + " returned to crease");
                } else {
                    Player newBatsman = new Player(batsmanName);
                    battingPlayers.add(newBatsman);
                    currentStrikerIndex = battingPlayers.size() - 1;
                    showSnackbar("New Batsman: " + batsmanName);
                }
                updateBatsmanDisplay();
                updateUI();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            Player defaultBatsman = new Player("Batsman " + (battingPlayers.size() + 1));
            battingPlayers.add(defaultBatsman);
            currentStrikerIndex = battingPlayers.size() - 1;
            updateBatsmanDisplay();
            updateUI();
        });

        builder.show();
    }

    private void showAddBowlerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🎯 New Bowler");
        builder.setMessage("Enter bowler name for next over:");

        final EditText input = new EditText(this);
        input.setHint("e.g., Shaheen Afridi");
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.GRAY);
        input.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        builder.setView(input);

        builder.setPositiveButton("Start Over", (dialog, which) -> {
            String bowlerName = input.getText().toString().trim();
            if (bowlerName.isEmpty()) {
                showSnackbar("Please enter bowler name");
                showAddBowlerDialog();
            } else {
                // Check if bowler already exists
                int existingIndex = -1;
                for (int i = 0; i < bowlingPlayers.size(); i++) {
                    if (bowlingPlayers.get(i).name.equalsIgnoreCase(bowlerName)) {
                        existingIndex = i;
                        break;
                    }
                }

                if (existingIndex != -1) {
                    currentBowlerIndex = existingIndex;
                    showSnackbar("Bowler " + bowlerName + " is back for another over");
                } else {
                    Player newBowler = new Player(bowlerName);
                    bowlingPlayers.add(newBowler);
                    currentBowlerIndex = bowlingPlayers.size() - 1;
                    showSnackbar("New Bowler: " + bowlerName);
                }
                updateBatsmanDisplay();
            }
        });

        builder.setNegativeButton("Same Bowler", (dialog, which) -> {
            showSnackbar("Continuing with same bowler");
        });

        builder.show();
    }

    private void undoLastBall() {
        if (isMatchEnded) {
            showSnackbar("Match ended, cannot undo");
            return;
        }

        if (ballHistory.isEmpty()) {
            showSnackbar("Nothing to undo");
            return;
        }

        Ball last = ballHistory.remove(ballHistory.size() - 1);
        if (!currentOverBalls.isEmpty()) {
            currentOverBalls.remove(currentOverBalls.size() - 1);
        }

        runs = last.runsBefore;
        wickets = last.wicketsBefore;
        balls = last.ballsBefore;
        currentStrikerIndex = last.strikerIndex;
        currentNonStrikerIndex = last.nonStrikerIndex;
        currentBowlerIndex = last.bowlerIndex;
        isFreeHit = last.freeHitBefore;

        battingPlayers.get(currentStrikerIndex).runs = last.strikerRunsBefore;
        battingPlayers.get(currentStrikerIndex).balls = last.strikerBallsBefore;
        battingPlayers.get(currentNonStrikerIndex).runs = last.nonStrikerRunsBefore;
        battingPlayers.get(currentNonStrikerIndex).balls = last.nonStrikerBallsBefore;
        bowlingPlayers.get(currentBowlerIndex).runsGiven = last.bowlerRunsBefore;
        bowlingPlayers.get(currentBowlerIndex).wickets = last.bowlerWicketsBefore;
        bowlingPlayers.get(currentBowlerIndex).overs = last.bowlerOversBefore;

        updateUI();
        updateBatsmanDisplay();
        updateFreeHitIndicator();

        showSnackbar("↩️ Undo successful");
    }

    private void updateFreeHitIndicator() {
        if (isFreeHit && !isMatchEnded) {
            llFreeHit.setVisibility(View.VISIBLE);
            tvFreeHitIndicator.setText("⚡ FREE HIT ⚡");
        } else {
            llFreeHit.setVisibility(View.GONE);
        }
    }

    private void updateUI() {
        tvScore.setText(String.valueOf(runs));
        tvWickets.setText("/" + wickets);

        int oversComplete = balls / 6;
        int ballsRemaining = balls % 6;
        tvOvers.setText("Overs: " + oversComplete + "." + ballsRemaining + "/" + oversLimit);

        float runRate = balls > 0 ? (float) runs / ((float) balls / 6) : 0;
        tvRunRate.setText(String.format("Run Rate: %.2f", runRate));

        if (isSecondInnings) {
            int required = target - runs;
            int wicketsLeft = 10 - wickets;
            tvTarget.setText("TARGET: " + target);
            tvRequired.setText("Need " + required + " runs | " + wicketsLeft + " wkts left");
            llTarget.setVisibility(View.VISIBLE);

            if (runs >= target) {
                matchEnded(teamAName + " won by " + wicketsLeft + " wickets");
            } else if (wickets >= 10) {
                matchEnded(teamBName + " won by " + (target - runs) + " runs");
            } else if (balls >= oversLimit * 6 && !isMatchEnded) {
                matchEnded(teamBName + " won by " + (target - runs) + " runs");
            }
        }

        updateFreeHitIndicator();
        refreshOverView();
    }

    private void refreshOverView() {
        if (llCurrentOver == null) return;
        llCurrentOver.removeAllViews();
        for (String ball : currentOverBalls) {
            addBallToOverView(ball);
        }
    }

    private void addBallToOverView(String label) {
        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(14);
        tv.setPadding(20, 10, 20, 10);
        tv.setGravity(Gravity.CENTER);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);

        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(100);

        if (label.contains("Wd") || label.contains("Nb")) {
            shape.setColor(Color.parseColor("#FF9800")); // Orange for extras
            tv.setTextColor(Color.BLACK);
        } else if (label.equals("W")) {
            shape.setColor(Color.parseColor("#D32F2F")); // Red for wicket
        } else if (label.equals("4") || label.equals("6")) {
            shape.setColor(Color.parseColor("#1B5E20")); // Green for boundaries
        } else {
            shape.setColor(Color.parseColor("#333333")); // Dark for dots/singles
        }

        tv.setBackground(shape);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(6, 0, 6, 0);
        tv.setLayoutParams(params);

        llCurrentOver.addView(tv);
    }

    private void updateBatsmanDisplay() {
        Player striker = battingPlayers.get(currentStrikerIndex);
        Player nonStriker = battingPlayers.get(currentNonStrikerIndex);
        Player bowler = bowlingPlayers.get(currentBowlerIndex);

        int strikerSR = striker.balls > 0 ? (striker.runs * 100 / striker.balls) : 0;
        int nonStrikerSR = nonStriker.balls > 0 ? (nonStriker.runs * 100 / nonStriker.balls) : 0;

        tvStriker.setText(striker.name + "  " + striker.runs + "(" + striker.balls + ")  SR:" + strikerSR);
        tvNonStriker.setText(nonStriker.name + "  " + nonStriker.runs + "(" + nonStriker.balls + ")  SR:" + nonStrikerSR);

        int oversB = (int) bowler.overs;
        int ballsB = (int) ((bowler.overs - oversB) * 10);
        tvBowler.setText("🎯 " + bowler.name + "  " + oversB + "." + ballsB + "-" + bowler.wickets + "-" + bowler.runsGiven);
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    private void endInnings() {
        if (isMatchEnded) return;

        if (!isSecondInnings) {
            new AlertDialog.Builder(this)
                .setTitle("End First Innings?")
                .setMessage(teamAName + " scored " + runs + "/" + wickets + ". Start second innings?")
                .setPositiveButton("Start Next Innings", (dialog, which) -> {
                    firstInningsScore = runs;
                    firstInningsBalls = balls;
                    wicketsBeforeSecond = wickets;
                    target = firstInningsScore + 1;
                    isSecondInnings = true;

                    saveInningsStats(true);

                    // Reset for second innings
                    runs = 0;
                    wickets = 0;
                    balls = 0;
                    isFreeHit = false;
                    ballHistory.clear();
                    currentOverBalls.clear(); // Clear current over for new innings
                    
                    // Clear current lists for new team
                    battingPlayers.clear();
                    bowlingPlayers.clear();
                    
                    showSecondInningsStartDialog();
                })
                .setNegativeButton("Cancel", null)
                .show();
        } else {
            saveInningsStats(false);
            if (runs >= target) {
                matchEnded(teamAName + " won by " + (10 - wickets) + " wickets");
            } else {
                matchEnded(teamBName + " won by " + (target - runs) + " runs");
            }
        }
    }

    private void showSecondInningsStartDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_start_innings, null);
        builder.setView(view);
        builder.setCancelable(false);

        TextView tvTitle = view.findViewById(R.id.tvInningsTitle);
        EditText etStriker = view.findViewById(R.id.etInningsStriker);
        EditText etNonStriker = view.findViewById(R.id.etInningsNonStriker);
        EditText etBowler = view.findViewById(R.id.etInningsBowler);
        Button btnStart = view.findViewById(R.id.btnInningsStart);

        tvTitle.setText("Second Innings: " + teamBName + " Batting");
        
        AlertDialog dialog = builder.create();
        
        btnStart.setOnClickListener(v -> {
            String s = etStriker.getText().toString().trim();
            String ns = etNonStriker.getText().toString().trim();
            String b = etBowler.getText().toString().trim();

            if (s.isEmpty() || ns.isEmpty() || b.isEmpty()) {
                showSnackbar("Please enter all names");
                return;
            }

            battingPlayers.add(new Player(s));
            battingPlayers.add(new Player(ns));
            for (int i = 3; i <= 11; i++) battingPlayers.add(new Player("Batsman " + i));

            bowlingPlayers.add(new Player(b));
            for (int i = 2; i <= 5; i++) bowlingPlayers.add(new Player("Bowler " + i));

            currentStrikerIndex = 0;
            currentNonStrikerIndex = 1;
            currentBowlerIndex = 0;

            String temp = teamAName;
            teamAName = teamBName;
            teamBName = temp;

            tvTeamName.setText(teamAName + " vs " + teamBName);
            updateUI();
            updateBatsmanDisplay();
            updateFreeHitIndicator();
            showSnackbar("Second Innings Started! Target: " + target);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void saveInningsStats(boolean isFirstInnings) {
        List<ScorecardActivity.PlayerStats> battingStats = new ArrayList<>();
        List<ScorecardActivity.PlayerStats> bowlingStats = new ArrayList<>();

        for (Player p : battingPlayers) {
            // Include if they have played or are currently at crease
            if (p.runs > 0 || p.balls > 0 || battingPlayers.indexOf(p) == currentStrikerIndex || battingPlayers.indexOf(p) == currentNonStrikerIndex) {
                ScorecardActivity.PlayerStats stats = new ScorecardActivity.PlayerStats(p.name);
                stats.runs = p.runs;
                stats.balls = p.balls;
                stats.fours = p.fours;
                stats.sixes = p.sixes;
                battingStats.add(stats);
            }
        }

        for (Player p : bowlingPlayers) {
            if (p.runsGiven > 0 || p.wickets > 0 || p.overs > 0 || bowlingPlayers.indexOf(p) == currentBowlerIndex) {
                ScorecardActivity.PlayerStats stats = new ScorecardActivity.PlayerStats(p.name);
                stats.overs = p.overs;
                stats.runsGiven = p.runsGiven;
                stats.wickets = p.wickets;
                bowlingStats.add(stats);
            }
        }

        if (isFirstInnings) {
            battingStatsA = battingStats;
            bowlingStatsA = bowlingStats;
        } else {
            battingStatsB = battingStats;
            bowlingStatsB = bowlingStats;
        }
    }

    private void matchEnded(String result) {
        if (isMatchEnded) return;
        isMatchEnded = true;

        saveInningsStats(false); // Save current (second) innings stats

        // Calculate POTM for database
        String potm = "N/A";
        double maxPoints = -1;
        List<ScorecardActivity.PlayerStats> all = new ArrayList<>();
        if (battingStatsA != null) all.addAll(battingStatsA);
        if (battingStatsB != null) all.addAll(battingStatsB);
        if (bowlingStatsA != null) all.addAll(bowlingStatsA);
        if (bowlingStatsB != null) all.addAll(bowlingStatsB);

        for (ScorecardActivity.PlayerStats p : all) {
            double pts = (p.runs * 1) + (p.wickets * 25);
            if (pts > maxPoints && pts > 0) { maxPoints = pts; potm = p.name; }
        }

        // Save match to database with full scorecard
        DatabaseHelper db = new DatabaseHelper(this);
        db.addMatchExtended(teamAName, teamBName, firstInningsScore, runs, 
                    wicketsBeforeSecond, wickets, 
                    String.format("%.1f", (float) firstInningsBalls / 6), 
                    String.format("%.1f", (float) balls / 6), 
                    result, potm,
                    battingStatsA, battingStatsB,
                    bowlingStatsA, bowlingStatsB);

        Intent intent = new Intent(ScoringActivity.this, ScorecardActivity.class);
        intent.putExtra("teamA", teamAName);
        intent.putExtra("teamB", teamBName);
        intent.putExtra("teamAScore", firstInningsScore);
        intent.putExtra("teamAWickets", wicketsBeforeSecond);
        intent.putExtra("teamAOvers", String.format("%.1f", (float) firstInningsBalls / 6));
        intent.putExtra("teamBScore", runs);
        intent.putExtra("teamBWickets", wickets);
        intent.putExtra("teamBOvers", String.format("%.1f", (float) balls / 6));
        intent.putExtra("result", result);

        intent.putExtra("battingStatsA", (ArrayList) battingStatsA);
        intent.putExtra("battingStatsB", (ArrayList) battingStatsB);
        intent.putExtra("bowlingStatsA", (ArrayList) bowlingStatsA);
        intent.putExtra("bowlingStatsB", (ArrayList) bowlingStatsB);

        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (isMatchEnded) {
            finish();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Match in Progress")
                .setMessage("If you go back, match progress will be lost. Continue?")
                .setPositiveButton("Exit Match", (d, w) -> finish())
                .setNegativeButton("Stay", null)
                .show();
    }
}