package com.example.smartpantry.logic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Turns free-text ingredient names into a canonical key so that
 * "Tomatoes", "tomato " and "FRESH tomato" all compare equal.
 *
 * Steps: lower-case -> strip punctuation -> drop filler words -> singularise each word
 *        -> apply a small synonym table.
 * This is intentionally simple (no NLP), but it removes the trivial mismatches.
 */
public final class IngredientNormalizer {

    private static final Set<String> FILLER = new HashSet<>(Arrays.asList(
            "fresh", "large", "small", "medium", "ripe", "chopped", "diced", "minced",
            "sliced", "organic", "frozen", "of"));

    /** Words that end in s/es but are NOT plurals. */
    private static final Set<String> NOT_PLURAL = new HashSet<>(Arrays.asList(
            "hummus", "couscous", "asparagus", "molasses", "swiss", "citrus", "watercress",
            "grits", "lemongrass", "bass", "cress"));

    private static final Map<String, String> IRREGULAR = new HashMap<>();
    private static final Map<String, String> SYNONYMS = new HashMap<>();

    static {
        IRREGULAR.put("leaves", "leaf");
        IRREGULAR.put("loaves", "loaf");
        IRREGULAR.put("halves", "half");
        IRREGULAR.put("knives", "knife");

        // phrase -> canonical key (phrases are already singularised)
        String[][] syn = {
                {"scallion", "green onion"}, {"spring onion", "green onion"},
                {"capsicum", "bell pepper"}, {"sweet pepper", "bell pepper"},
                {"aubergine", "eggplant"}, {"courgette", "zucchini"},
                {"garbanzo bean", "chickpea"},
                {"spaghetti", "pasta"}, {"penne", "pasta"}, {"fusilli", "pasta"},
                {"macaroni", "pasta"}, {"tagliatelle", "pasta"},
                {"plain flour", "flour"}, {"all purpose flour", "flour"},
                {"caster sugar", "sugar"}, {"castor sugar", "sugar"}, {"white sugar", "sugar"},
                {"extra virgin olive oil", "olive oil"},
                {"sunflower oil", "vegetable oil"}, {"canola oil", "vegetable oil"},
                {"cooking oil", "vegetable oil"},
                {"vegetable broth", "vegetable stock"},
                {"garlic clove", "garlic"}, {"clove garlic", "garlic"},
                {"rolled oat", "oat"}, {"porridge oat", "oat"},
                {"tinned tuna", "tuna"}, {"canned tuna", "tuna"},
                {"cheddar", "cheese"}, {"cheddar cheese", "cheese"},
                {"mozzarella", "cheese"}, {"mozzarella cheese", "cheese"},
                {"chili flake", "chilli flake"}, {"chilli flake", "chilli flake"},
        };
        for (String[] s : syn) {
            SYNONYMS.put(s[0], s[1]);
        }
    }

    private IngredientNormalizer() { }

    /** Returns the canonical key for an ingredient name ("" if the name has no letters). */
    public static String normalize(String raw) {
        if (raw == null) return "";
        String s = raw.toLowerCase(Locale.ROOT).replaceAll("[^a-z]+", " ").trim();
        if (s.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (String word : s.split("\\s+")) {
            if (FILLER.contains(word)) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(singular(word));
        }
        String key = sb.toString().trim();
        String mapped = SYNONYMS.get(key);
        return mapped != null ? mapped : key;
    }

    /** Very small singulariser: handles the common English plural patterns. */
    static String singular(String w) {
        if (w.length() <= 3 || NOT_PLURAL.contains(w)) return w;
        String irregular = IRREGULAR.get(w);
        if (irregular != null) return irregular;

        if (w.endsWith("ies")) return w.substring(0, w.length() - 3) + "y";   // berries -> berry
        if (w.endsWith("oes")) return w.substring(0, w.length() - 2);          // tomatoes -> tomato
        if (w.endsWith("ches") || w.endsWith("shes") || w.endsWith("xes")
                || w.endsWith("sses")) {
            return w.substring(0, w.length() - 2);                             // peaches -> peach
        }
        if (w.endsWith("ss") || w.endsWith("us") || w.endsWith("is")) return w;
        if (w.endsWith("s")) return w.substring(0, w.length() - 1);            // eggs -> egg
        return w;
    }
}
