package com.example.smartpantry.ui;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.smartpantry.R;
import com.example.smartpantry.data.PantryDbHelper;
import com.example.smartpantry.logic.RecipeMatcher;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * Recipe Detail screen: full ingredient list (with a tick/cross showing whether the
 * pantry covers each one) and the numbered method.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setTitle(R.string.title_recipe);

        long id = getIntent().getLongExtra(SuggestedRecipesActivity.EXTRA_RECIPE_ID, -1);
        PantryDbHelper db = PantryDbHelper.getInstance(this);
        Recipe recipe = id == -1 ? null : db.getRecipe(id);
        if (recipe == null) {
            Toast.makeText(this, R.string.msg_recipe_missing, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ((TextView) findViewById(R.id.tvName)).setText(recipe.getName());
        ((TextView) findViewById(R.id.tvDesc)).setText(
                recipe.getDescription() + "  •  ⏱ " + recipe.getPrepMinutes() + " mins prep");

        RecipeMatcher.Stock stock = RecipeMatcher.buildStock(
                db.getAllItems(), AppSettings.ignoreExpired(this), System.currentTimeMillis());

        LinearLayout ingredients = findViewById(R.id.layoutIngredients);
        for (RecipeIngredient ing : recipe.getIngredients()) {
            boolean have = RecipeMatcher.isCovered(ing, stock);
            ingredients.addView(createIngredientRow(ing, have));
        }

        LinearLayout steps = findViewById(R.id.layoutSteps);
        String[] lines = recipe.getSteps().split("\n");
        int stepNumber = 1;
        for (String s : lines) {
            if (s.trim().isEmpty()) continue;
            steps.addView(createStepRow(stepNumber++, s.trim()));
        }
    }

    private View createIngredientRow(RecipeIngredient ing, boolean have) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 10, 0, 10);

        // Status Badge Circle
        FrameLayout badge = new FrameLayout(this);
        int badgeSize = (int) (28 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(badgeSize, badgeSize);
        badgeParams.setMarginEnd((int) (12 * getResources().getDisplayMetrics().density));
        badge.setLayoutParams(badgeParams);
        badge.setBackground(ContextCompat.getDrawable(this,
                have ? R.drawable.bg_chip_ok : R.drawable.bg_chip_expired));

        TextView tvBadgeText = new TextView(this);
        tvBadgeText.setText(have ? "✓" : "✗");
        tvBadgeText.setTextSize(14);
        tvBadgeText.setTypeface(null, android.graphics.Typeface.BOLD);
        tvBadgeText.setTextColor(ContextCompat.getColor(this, have ? R.color.ok_green : R.color.expired));
        tvBadgeText.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        badge.addView(tvBadgeText);

        row.addView(badge);

        // Ingredient details
        TextView tvDetails = new TextView(this);
        tvDetails.setText(PantryAdapter.formatQty(ing.getQuantity()) + " " + ing.getUnit() + "  " + ing.getName());
        tvDetails.setTextSize(15);
        tvDetails.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        tvDetails.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

        row.addView(tvDetails);
        return row;
    }

    private View createStepRow(int stepNumber, String instruction) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.TOP);
        row.setPadding(0, 12, 0, 12);

        // Step number badge
        TextView tvStepNum = new TextView(this);
        int size = (int) (28 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams numParams = new LinearLayout.LayoutParams(size, size);
        numParams.setMarginEnd((int) (12 * getResources().getDisplayMetrics().density));
        tvStepNum.setLayoutParams(numParams);
        tvStepNum.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_step_number));
        tvStepNum.setText(String.valueOf(stepNumber));
        tvStepNum.setTextColor(ContextCompat.getColor(this, R.color.white));
        tvStepNum.setTextSize(13);
        tvStepNum.setTypeface(null, android.graphics.Typeface.BOLD);
        tvStepNum.setGravity(Gravity.CENTER);

        row.addView(tvStepNum);

        // Instruction Text
        TextView tvInstruction = new TextView(this);
        tvInstruction.setText(instruction);
        tvInstruction.setTextSize(15);
        tvInstruction.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        tvInstruction.setLineSpacing(4, 1.1f);
        tvInstruction.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

        row.addView(tvInstruction);
        return row;
    }
}
