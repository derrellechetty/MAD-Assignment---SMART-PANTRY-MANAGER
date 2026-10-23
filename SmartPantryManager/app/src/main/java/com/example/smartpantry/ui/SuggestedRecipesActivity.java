package com.example.smartpantry.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.data.PantryDbHelper;
import com.example.smartpantry.logic.RecipeMatcher;
import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.List;

/**
 * Suggested Recipes screen. Runs the STRICT matcher against the current pantry every
 * time it comes to the front (onResume), so adding/removing an ingredient is reflected
 * immediately. The optional "Almost there" list is a separate toggle and is never
 * mixed into the strict list.
 */
public class SuggestedRecipesActivity extends AppCompatActivity implements RecipeAdapter.Listener {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    private PantryDbHelper db;
    private RecipeAdapter adapter;
    private TextView tvEmpty, tvNote;
    private MaterialButtonToggleGroup toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);
        db = PantryDbHelper.getInstance(this);

        tvEmpty = findViewById(R.id.tvEmpty);
        tvNote = findViewById(R.id.tvNote);
        toggle = findViewById(R.id.toggleGroup);

        RecyclerView recycler = findViewById(R.id.recyclerRecipes);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this);
        recycler.setAdapter(adapter);

        toggle.check(R.id.btnReady);
        toggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) refresh();
        });

        NavHelper.setup(this, R.id.nav_recipes);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
        ((BottomNavigationView) findViewById(R.id.bottomNav))
                .getMenu().findItem(R.id.nav_recipes).setChecked(true);
    }

    private void refresh() {
        List<PantryItem> pantry = db.getAllItems();
        List<Recipe> recipes = db.getAllRecipes();
        RecipeMatcher.Stock stock = RecipeMatcher.buildStock(
                pantry, AppSettings.ignoreExpired(this), System.currentTimeMillis());

        boolean almostMode = toggle.getCheckedButtonId() == R.id.btnAlmost;
        int count;
        View cardNote = findViewById(R.id.cardNote);
        View layoutEmpty = findViewById(R.id.layoutEmpty);
        if (almostMode) {
            List<RecipeMatcher.MatchResult> almost = RecipeMatcher.almostThere(recipes, stock);
            adapter.setAlmost(almost);
            count = almost.size();
            tvNote.setText(R.string.almost_explainer);
            tvNote.setVisibility(View.VISIBLE);
            if (cardNote != null) cardNote.setVisibility(View.VISIBLE);
            tvEmpty.setText(R.string.empty_almost);
        } else {
            List<Recipe> strict = RecipeMatcher.strictMatches(recipes, stock);
            adapter.setStrict(strict);
            count = strict.size();
            tvNote.setVisibility(View.GONE);
            if (cardNote != null) cardNote.setVisibility(View.GONE);
            tvEmpty.setText(R.string.empty_suggested);
        }
        if (layoutEmpty != null) layoutEmpty.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
        else tvEmpty.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
