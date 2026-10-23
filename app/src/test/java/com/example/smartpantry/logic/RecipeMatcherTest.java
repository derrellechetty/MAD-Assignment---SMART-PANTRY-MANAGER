package com.example.smartpantry.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** JVM unit tests for the strict-matching rule (Section 2.3 of the brief). */
public class RecipeMatcherTest {

    private static final long NOW = System.currentTimeMillis();
    private static final long DAY = 24L * 60 * 60 * 1000;

    private static Recipe recipe(String name, RecipeIngredient... ings) {
        Recipe r = new Recipe(1, name, "", "", 10);
        r.getIngredients().addAll(Arrays.asList(ings));
        return r;
    }

    private static RecipeIngredient ing(String n, double q, String u) {
        return new RecipeIngredient(n, q, u);
    }

    private static PantryItem item(String n, double q, String u) {
        return new PantryItem(n, q, u, 0);
    }

    private static List<Recipe> suggested(Recipe r, PantryItem... items) {
        RecipeMatcher.Stock stock = RecipeMatcher.buildStock(Arrays.asList(items), true, NOW);
        return RecipeMatcher.strictMatches(Collections.singletonList(r), stock);
    }

    @Test
    public void allIngredientsPresent_isSuggested() {
        Recipe r = recipe("Omelette", ing("eggs", 3, "pcs"), ing("cheese", 50, "g"));
        assertEquals(1, suggested(r, item("eggs", 6, "pcs"), item("cheese", 100, "g")).size());
    }

    @Test
    public void fourOfFiveIngredients_isNotSuggested() {
        Recipe r = recipe("Five", ing("a", 1, "pcs"), ing("b", 1, "pcs"), ing("c", 1, "pcs"),
                ing("d", 1, "pcs"), ing("e", 1, "pcs"));
        assertTrue(suggested(r, item("a", 1, "pcs"), item("b", 1, "pcs"),
                item("c", 1, "pcs"), item("d", 1, "pcs")).isEmpty());
    }

    @Test
    public void addingTheMissingIngredient_makesRecipeAppear() {
        Recipe r = recipe("Two", ing("eggs", 2, "pcs"), ing("butter", 10, "g"));
        assertTrue(suggested(r, item("eggs", 2, "pcs")).isEmpty());
        assertEquals(1, suggested(r, item("eggs", 2, "pcs"), item("butter", 10, "g")).size());
    }

    @Test
    public void insufficientQuantity_isNotSuggested() {
        Recipe r = recipe("Pancakes", ing("flour", 150, "g"));
        assertTrue(suggested(r, item("flour", 100, "g")).isEmpty());
    }

    @Test
    public void exactQuantity_isSuggested() {
        Recipe r = recipe("Pancakes", ing("flour", 150, "g"));
        assertEquals(1, suggested(r, item("flour", 150, "g")).size());
    }

    @Test
    public void singularVersusPlural_matches() {
        Recipe r = recipe("Soup", ing("tomatoes", 2, "pcs"), ing("potato", 1, "pcs"));
        assertEquals(1, suggested(r, item("Tomato", 3, "pcs"), item("Potatoes", 2, "pcs")).size());
    }

    @Test
    public void caseSpacingAndFillerWords_areIgnored() {
        Recipe r = recipe("Salad", ing("Tomatoes", 1, "pcs"));
        assertEquals(1, suggested(r, item("  FRESH tomato ", 1, "pcs")).size());
    }

    @Test
    public void unitConversion_kgCoversGrams() {
        Recipe r = recipe("Rice", ing("rice", 200, "g"));
        assertEquals(1, suggested(r, item("rice", 1, "kg")).size());
    }

    @Test
    public void unitConversion_cupsAndMillilitres() {
        Recipe r = recipe("Pancakes", ing("milk", 250, "ml"));
        assertEquals(1, suggested(r, item("milk", 2, "cup")).size());     // 480 ml
        assertTrue(suggested(r, item("milk", 1, "cup")).isEmpty());       // 240 ml < 250 ml
    }

    @Test
    public void incompatibleUnitFamilies_areNotSuggested() {
        Recipe r = recipe("Cake", ing("flour", 200, "g"));
        assertTrue(suggested(r, item("flour", 3, "cup")).isEmpty());
    }

    @Test
    public void duplicatePantryEntries_areSummed() {
        Recipe r = recipe("Mash", ing("milk", 400, "ml"));
        assertEquals(1, suggested(r, item("milk", 200, "ml"), item("Milk", 0.3, "l")).size());
    }

    @Test
    public void synonyms_match() {
        Recipe r = recipe("Noodles", ing("green onions", 2, "pcs"), ing("pasta", 100, "g"));
        assertEquals(1, suggested(r, item("spring onion", 2, "pcs"),
                item("spaghetti", 500, "g")).size());
    }

    @Test
    public void expiredItems_areIgnoredWhenSettingIsOn() {
        Recipe r = recipe("Omelette", ing("eggs", 2, "pcs"));
        PantryItem expired = new PantryItem("eggs", 6, "pcs", NOW - 3 * DAY);
        RecipeMatcher.Stock on = RecipeMatcher.buildStock(Arrays.asList(expired), true, NOW);
        RecipeMatcher.Stock off = RecipeMatcher.buildStock(Arrays.asList(expired), false, NOW);
        assertTrue(RecipeMatcher.strictMatches(Collections.singletonList(r), on).isEmpty());
        assertEquals(1, RecipeMatcher.strictMatches(Collections.singletonList(r), off).size());
    }

    @Test
    public void itemExpiringToday_stillCounts() {
        Recipe r = recipe("Omelette", ing("eggs", 2, "pcs"));
        PantryItem today = new PantryItem("eggs", 6, "pcs", RecipeMatcher.startOfDay(NOW));
        RecipeMatcher.Stock s = RecipeMatcher.buildStock(Arrays.asList(today), true, NOW);
        assertEquals(1, RecipeMatcher.strictMatches(Collections.singletonList(r), s).size());
    }

    @Test
    public void emptyPantry_suggestsNothing() {
        Recipe r = recipe("Omelette", ing("eggs", 2, "pcs"));
        assertTrue(suggested(r).isEmpty());
    }

    @Test
    public void recipeWithoutIngredients_isNeverSuggested() {
        assertTrue(suggested(recipe("Empty"), item("eggs", 2, "pcs")).isEmpty());
    }

    @Test
    public void almostThere_listsOnlyRecipesMissingExactlyOne_andIsSeparate() {
        Recipe missingOne = recipe("One", ing("a", 1, "pcs"), ing("b", 1, "pcs"));
        Recipe missingTwo = recipe("Two", ing("c", 1, "pcs"), ing("d", 1, "pcs"));
        Recipe complete = recipe("Full", ing("a", 1, "pcs"));
        List<Recipe> all = Arrays.asList(missingOne, missingTwo, complete);
        RecipeMatcher.Stock stock =
                RecipeMatcher.buildStock(Arrays.asList(item("a", 1, "pcs")), true, NOW);

        List<Recipe> strict = RecipeMatcher.strictMatches(all, stock);
        List<RecipeMatcher.MatchResult> almost = RecipeMatcher.almostThere(all, stock);

        assertEquals(1, strict.size());
        assertEquals("Full", strict.get(0).getName());
        assertEquals(1, almost.size());
        assertEquals("One", almost.get(0).recipe.getName());
        assertEquals("b", almost.get(0).missing.get(0).getName());
    }

    @Test
    public void normalizer_handlesCommonPlurals() {
        assertEquals("tomato", IngredientNormalizer.normalize("Tomatoes"));
        assertEquals("potato", IngredientNormalizer.normalize("potatoes"));
        assertEquals("berry", IngredientNormalizer.normalize("berries"));
        assertEquals("peach", IngredientNormalizer.normalize("peaches"));
        assertEquals("egg", IngredientNormalizer.normalize("Eggs"));
        assertEquals("hummus", IngredientNormalizer.normalize("hummus"));
        assertEquals("asparagus", IngredientNormalizer.normalize("asparagus"));
        assertEquals("", IngredientNormalizer.normalize("123"));
        assertFalse(IngredientNormalizer.normalize("rice").isEmpty());
    }

    @Test
    public void expiringSoon_detection() {
        PantryItem soon = new PantryItem("milk", 1, "l", RecipeMatcher.startOfDay(NOW) + DAY);
        PantryItem far = new PantryItem("milk", 1, "l", NOW + 30 * DAY);
        PantryItem gone = new PantryItem("milk", 1, "l", NOW - 2 * DAY);
        assertTrue(RecipeMatcher.isExpiringSoon(soon, NOW, 3));
        assertFalse(RecipeMatcher.isExpiringSoon(far, NOW, 3));
        assertFalse(RecipeMatcher.isExpiringSoon(gone, NOW, 3));
        assertTrue(RecipeMatcher.isExpired(gone, NOW));
        new ArrayList<>(); // keep import used if tests are trimmed
    }
}
