package com.example.scorifypro;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ScorecardAdapter extends RecyclerView.Adapter<ScorecardAdapter.ViewHolder> {

    private List<ScorecardActivity.PlayerStats> statsList;
    private String type;

    public ScorecardAdapter(List<ScorecardActivity.PlayerStats> statsList, String type) {
        this.statsList = statsList;
        this.type = type;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_scorecard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScorecardActivity.PlayerStats stats = statsList.get(position);
        holder.tvName.setText(stats.name);

        if ("batting".equals(type)) {
            holder.llBatting.setVisibility(View.VISIBLE);
            holder.llBowling.setVisibility(View.GONE);

            holder.tvRuns.setText(String.valueOf(stats.runs));
            holder.tvBalls.setText(String.valueOf(stats.balls));
            holder.tvFours.setText(String.valueOf(stats.fours));
            holder.tvSixes.setText(String.valueOf(stats.sixes));
            int sr = stats.balls > 0 ? (stats.runs * 100 / stats.balls) : 0;
            holder.tvSR.setText(String.valueOf(sr));
        } else {
            holder.llBatting.setVisibility(View.GONE);
            holder.llBowling.setVisibility(View.VISIBLE);

            holder.tvOvers.setText(String.format("%.1f", stats.overs));
            holder.tvRunsGiven.setText(String.valueOf(stats.runsGiven));
            holder.tvWickets.setText(String.valueOf(stats.wickets));
        }
    }

    @Override
    public int getItemCount() {
        return statsList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRuns, tvBalls, tvFours, tvSixes, tvSR, tvOvers, tvRunsGiven, tvWickets;
        LinearLayout llBatting, llBowling;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPlayerName);
            tvRuns = itemView.findViewById(R.id.tvRuns);
            tvBalls = itemView.findViewById(R.id.tvBalls);
            tvFours = itemView.findViewById(R.id.tvFours);
            tvSixes = itemView.findViewById(R.id.tvSixes);
            tvSR = itemView.findViewById(R.id.tvSR);
            tvOvers = itemView.findViewById(R.id.tvOvers);
            tvRunsGiven = itemView.findViewById(R.id.tvRunsGiven);
            tvWickets = itemView.findViewById(R.id.tvWickets);
            llBatting = itemView.findViewById(R.id.llBattingStats);
            llBowling = itemView.findViewById(R.id.llBowlingStats);
        }
    }
}