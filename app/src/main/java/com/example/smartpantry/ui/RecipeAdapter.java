package com.example.smartpantry.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.logic.RecipeMatcher;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for recipe cards. Works for both lists:
 *  - strict suggestions: no "missing" text shown
 *  - "Almost there": shows the single missing ingredient
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    public interface Listener {
        void onRecipeClick(Recipe recipe);
    }

    private final List<Recipe> recipes = new ArrayList<>();
    private final List<RecipeIngredient> missing = new ArrayList<>();   // parallel to recipes, null entries allowed
    private final Listener listener;

    public RecipeAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setStrict(List<Recipe> list) {
        recipes.clear();
        missing.clear();
        recipes.addAll(list);
        for (int i = 0; i < list.size(); i++) missing.add(null);
        notifyDataSetChanged();
    }

    public void setAlmost(List<RecipeMatcher.MatchResult> list) {
        recipes.clear();
        missing.clear();
        for (RecipeMatcher.MatchResult r : list) {
            recipes.add(r.recipe);
            missing.add(r.missing.get(0));
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        final Recipe r = recipes.get(position);
        RecipeIngredient miss = missing.get(position);

        h.tvName.setText(r.getName());
        h.tvDesc.setText(r.getDescription());
        if (h.tvTime != null) {
            h.tvTime.setText("⏱ " + r.getPrepMinutes() + " min");
        }
        h.tvMeta.setText("🥗 " + r.getIngredients().size() + " Ingredients");

        if (miss == null) {
            h.tvMissing.setVisibility(View.GONE);
        } else {
            h.tvMissing.setVisibility(View.VISIBLE);
            h.tvMissing.setText("🛒 Missing 1 item: " + PantryAdapter.formatQty(miss.getQuantity())
                    + " " + miss.getUnit() + " " + miss.getName());
        }
        h.itemView.setOnClickListener(v -> listener.onRecipeClick(r));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName, tvDesc, tvTime, tvMeta, tvMissing;

        ViewHolder(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tvRecipeName);
            tvDesc = v.findViewById(R.id.tvRecipeDesc);
            tvTime = v.findViewById(R.id.tvRecipeTime);
            tvMeta = v.findViewById(R.id.tvRecipeMeta);
            tvMissing = v.findViewById(R.id.tvMissing);
        }
    }
}
