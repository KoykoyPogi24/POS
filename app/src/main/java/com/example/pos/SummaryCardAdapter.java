package com.example.pos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

// Model for summary card
class SummaryCard {
    String title;
    String value;
    int iconResId;
    public SummaryCard(String title, String value, int iconResId) {
        this.title = title;
        this.value = value;
        this.iconResId = iconResId;
    }
}

public class SummaryCardAdapter extends RecyclerView.Adapter<SummaryCardAdapter.SummaryCardViewHolder> {
    private List<SummaryCard> summaryCards;

    public SummaryCardAdapter(List<SummaryCard> summaryCards) {
        this.summaryCards = summaryCards;
    }

    @NonNull
    @Override
    public SummaryCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_summary_card, parent, false);
        return new SummaryCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SummaryCardViewHolder holder, int position) {
        SummaryCard card = summaryCards.get(position);
        holder.tvTitle.setText(card.title);
        holder.tvValue.setText(card.value);
        // TODO: Set icon if needed
    }

    @Override
    public int getItemCount() {
        return summaryCards.size();
    }

    static class SummaryCardViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvValue;
        // ImageView ivIcon;
        public SummaryCardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvValue = itemView.findViewById(R.id.tvValue);
            // ivIcon = itemView.findViewById(R.id.ivIcon);
        }
    }
} 