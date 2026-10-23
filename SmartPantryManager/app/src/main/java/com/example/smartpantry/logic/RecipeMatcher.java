package com.example.smartpantry.logic;

import com.example.smartpantry.logic.UnitConverter.Family;
import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * THE STRICT-MATCHING RULE (core business logic of the app).
 *
 * A recipe is "suggested" only if EVERY ingredient it needs is in the pantry in at
 * least the required quantity. One missing or short ingredient and the recipe is out.
 *
 * Robustness against real-world messiness:
 *   - names are normalised ("Tomatoes" == "tomato")                -> IngredientNormalizer
 *   - units are converted ("1 kg" covers "500 g", "1 cup" = 240 ml)  -> UnitConverter
 *   - the same ingredient entered twice is summed (200 ml + 300 ml milk)
 *   - optionally, expired pantry items are ignored
 *
 * This class has no Android dependencies, so it is covered by plain JUnit tests.
 */
public final class RecipeMatcher {

    /** Tolerance so floating point conversions (e.g. oz -> g) never cause a false "short". */
    private static final double EPSILON = 1e-6;

    private RecipeMatcher() { }

    /** A recipe together with the ingredients the pantry cannot currently cover. */
    public static class MatchResult {
        public final Recipe recipe;
        public final List<RecipeIngredient> missing;

        MatchResult(Recipe recipe, List<RecipeIngredient> missing) {
            this.recipe = recipe;
            this.missing = missing;
        }
    }

    /** Normalised ingredient name -> quantity available per unit family (in base units). */
    public static class Stock {
        private final Map<String, Map<Family, Double>> data = new HashMap<>();

        void add(String key, Family family, double baseQty) {
            Map<Family, Double> perFamily = data.get(key);
            if (perFamily == null) {
                perFamily = new EnumMap<>(Family.class);
                data.put(key, perFamily);
            }
            Double old = perFamily.get(family);
            perFamily.put(family, (old == null ? 0 : old) + baseQty);
        }

        double available(String key, Family family) {
            Map<Family, Double> perFamily = data.get(key);
            if (perFamily == null) return 0;
            Double qty = perFamily.get(family);
            return qty == null ? 0 : qty;
        }
    }

    // ------------------------------------------------------------------ stock

    /**
     * Collapses the pantry into a {@link Stock}.
     *
     * @param ignoreExpired if true, items whose expiry date is before today do not count
     * @param nowMillis     "current time" (injected so tests are deterministic)
     */
    public static Stock buildStock(List<PantryItem> pantry, boolean ignoreExpired, long nowMillis) {
        Stock stock = new Stock();
        for (PantryItem item : pantry) {
            if (item.getQuantity() <= 0) continue;
            if (ignoreExpired && isExpired(item, nowMillis)) continue;

            String key = IngredientNormalizer.normalize(item.getName());
            if (key.isEmpty()) continue;

            stock.add(key, UnitConverter.familyOf(item.getUnit()),
                    UnitConverter.toBase(item.getQuantity(), item.getUnit()));
        }
        return stock;
    }

    // ---------------------------------------------------------------- matching

    /** True if the stock covers this single recipe ingredient. */
    public static boolean isCovered(RecipeIngredient ing, Stock stock) {
        String key = IngredientNormalizer.normalize(ing.getName());
        Family family = UnitConverter.familyOf(ing.getUnit());
        double needed = UnitConverter.toBase(ing.getQuantity(), ing.getUnit());
        return stock.available(key, family) + EPSILON >= needed;
    }

    /** Ingredients of the recipe that the stock does NOT cover (empty list = can cook it). */
    public static List<RecipeIngredient> findMissing(Recipe recipe, Stock stock) {
        List<RecipeIngredient> missing = new ArrayList<>();
        for (RecipeIngredient ing : recipe.getIngredients()) {
            if (!isCovered(ing, stock)) missing.add(ing);
        }
        return missing;
    }

    /**
     * STRICT suggestions: only recipes where nothing is missing.
     * A recipe with no ingredients at all is never suggested.
     */
    public static List<Recipe> strictMatches(List<Recipe> recipes, Stock stock) {
        List<Recipe> result = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (!recipe.getIngredients().isEmpty() && findMissing(recipe, stock).isEmpty()) {
                result.add(recipe);
            }
        }
        return result;
    }

    /**
     * BONUS "Almost There" list: recipes missing exactly ONE ingredient.
     * Kept separate from {@link #strictMatches} and never mixed into it.
     */
    public static List<MatchResult> almostThere(List<Recipe> recipes, Stock stock) {
        List<MatchResult> result = new ArrayList<>();
        for (Recipe recipe : recipes) {
            List<RecipeIngredient> missing = findMissing(recipe, stock);
            if (missing.size() == 1) result.add(new MatchResult(recipe, missing));
        }
        return result;
    }

    // ------------------------------------------------------------------ dates

    /** Midnight at the start of the day containing {@code millis} (device time zone). */
    public static long startOfDay(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    /** An item is expired once its expiry day is before today (it is still fine ON the day). */
    public static boolean isExpired(PantryItem item, long nowMillis) {
        return item.hasExpiry() && item.getExpiryMillis() < startOfDay(nowMillis);
    }

    /** True if the item is not expired but expires within the next {@code days} days. */
    public static boolean isExpiringSoon(PantryItem item, long nowMillis, int days) {
        if (!item.hasExpiry() || isExpired(item, nowMillis)) return false;
        Calendar limit = Calendar.getInstance();
        limit.setTimeInMillis(startOfDay(nowMillis));
        limit.add(Calendar.DAY_OF_YEAR, days);   // calendar maths is DST-safe
        return item.getExpiryMillis() <= limit.getTimeInMillis();
    }
}
