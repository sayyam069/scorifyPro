package com.example.scorifypro;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private TextView tvEmpty;
    private ImageButton btnBack;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        rvHistory = findViewById(R.id.rvHistory);
        tvEmpty = findViewById(R.id.tvEmptyHistory);
        btnBack = findViewById(R.id.btnHistoryBack);
        db = new DatabaseHelper(this);

        btnBack.setOnClickListener(v -> finish());

        loadHistory();
    }

    private void loadHistory() {
        List<DatabaseHelper.MatchHistory> historyList = db.getAllMatches();
        if (historyList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvHistory.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvHistory.setVisibility(View.VISIBLE);
            rvHistory.setLayoutManager(new LinearLayoutManager(this));
            rvHistory.setAdapter(new HistoryAdapter(historyList));
        }
    }

    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private List<DatabaseHelper.MatchHistory> list;

        HistoryAdapter(List<DatabaseHelper.MatchHistory> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DatabaseHelper.MatchHistory m = list.get(position);
            holder.tvDate.setText(m.date);
            holder.tvTeamA.setText(m.teamA);
            holder.tvTeamB.setText(m.teamB);
            holder.tvScoreA.setText(m.scoreA + "/" + m.wicketsA + " (" + m.oversA + ")");
            holder.tvScoreB.setText(m.scoreB + "/" + m.wicketsB + " (" + m.oversB + ")");
            holder.tvResult.setText(m.result);
            holder.tvPotm.setText("🌟 POTM: " + m.potm);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), ScorecardActivity.class);
                intent.putExtra("matchId", (long) m.id);
                intent.putExtra("teamA", m.teamA);
                intent.putExtra("teamB", m.teamB);
                intent.putExtra("teamAScore", m.scoreA);
                intent.putExtra("teamAWickets", m.wicketsA);
                intent.putExtra("teamAOvers", m.oversA);
                intent.putExtra("teamBScore", m.scoreB);
                intent.putExtra("teamBWickets", m.wicketsB);
                intent.putExtra("teamBOvers", m.oversB);
                intent.putExtra("result", m.result);
                intent.putExtra("isFromHistory", true);
                v.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDate, tvTeamA, tvTeamB, tvScoreA, tvScoreB, tvResult, tvPotm;
            ViewHolder(View v) {
                super(v);
                tvDate = v.findViewById(R.id.tvHistoryDate);
                tvTeamA = v.findViewById(R.id.tvHistoryTeamA);
                tvTeamB = v.findViewById(R.id.tvHistoryTeamB);
                tvScoreA = v.findViewById(R.id.tvHistoryScoreA);
                tvScoreB = v.findViewById(R.id.tvHistoryScoreB);
                tvResult = v.findViewById(R.id.tvHistoryResult);
                tvPotm = v.findViewById(R.id.tvHistoryPotm);
            }
        }
    }
}