package com.example.smartpantry.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.logic.RecipeMatcher;
import com.example.smartpantry.model.PantryItem;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Custom RecyclerView adapter for the pantry list.
 * The ViewHolder caches the row's views so findViewById runs once per row, not per bind.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    /** Callbacks implemented by the Activity (keeps the adapter free of navigation code). */
    public interface Listener {
        void onItemClick(PantryItem item);
        void onDeleteClick(PantryItem item);
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final Listener listener;

    public PantryAdapter(Listener listener) {
        this.listener = listener;
    }

    /** Replaces the displayed data and refreshes the list. */
    public void setItems(List<PantryItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        final PantryItem item = items.get(position);
        Context c = h.itemView.getContext();

        h.tvName.setText(item.getName());
        h.tvQuantity.setText(String.format(Locale.getDefault(), "%s %s",
                formatQty(item.getQuantity()), item.getUnit()));

        long now = System.currentTimeMillis();
        if (!item.hasExpiry()) {
            h.tvExpiry.setText("No Expiry Date");
            h.tvExpiry.setBackground(ContextCompat.getDrawable(c, R.drawable.bg_quantity_badge));
            h.tvExpiry.setTextColor(ContextCompat.getColor(c, R.color.text_secondary));
        } else if (RecipeMatcher.isExpired(item, now)) {
            h.tvExpiry.setText("⚠️ Expired " + DateFormat.getDateInstance().format(new Date(item.getExpiryMillis())));
            h.tvExpiry.setBackground(ContextCompat.getDrawable(c, R.drawable.bg_chip_expired));
            h.tvExpiry.setTextColor(ContextCompat.getColor(c, R.color.expired));
        } else if (RecipeMatcher.isExpiringSoon(item, now, AppSettings.EXPIRING_SOON_DAYS)) {
            h.tvExpiry.setText("⏳ Expires Soon: " + DateFormat.getDateInstance().format(new Date(item.getExpiryMillis())));
            h.tvExpiry.setBackground(ContextCompat.getDrawable(c, R.drawable.bg_chip_warning));
            h.tvExpiry.setTextColor(ContextCompat.getColor(c, R.color.warning_text));
        } else {
            h.tvExpiry.setText("✓ Expires " + DateFormat.getDateInstance().format(new Date(item.getExpiryMillis())));
            h.tvExpiry.setBackground(ContextCompat.getDrawable(c, R.drawable.bg_chip_ok));
            h.tvExpiry.setTextColor(ContextCompat.getColor(c, R.color.ok_green));
        }

        h.itemView.setOnClickListener(v -> listener.onItemClick(item));
        h.btnDelete.setOnClickListener(v -> listener.onDeleteClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** 2.0 -> "2", 0.5 -> "0.5". */
    static String formatQty(double q) {
        return q == Math.rint(q) ? String.valueOf((long) q) : String.valueOf(q);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName, tvQuantity, tvExpiry;
        final ImageButton btnDelete;

        ViewHolder(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tvName);
            tvQuantity = v.findViewById(R.id.tvQuantity);
            tvExpiry = v.findViewById(R.id.tvExpiry);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
