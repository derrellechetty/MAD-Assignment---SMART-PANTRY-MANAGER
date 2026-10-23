package com.example.smartpantry.logic;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Converts units into a common base so quantities can be compared.
 *
 * Units fall into three families, each with its own base unit:
 *   MASS   -> grams        (g, kg, oz, lb)
 *   VOLUME -> millilitres  (ml, l, tsp, tbsp, cup)
 *   COUNT  -> pieces       (pcs, piece, clove, slice, can, ...)
 *
 * We deliberately do NOT convert between families (e.g. grams of flour vs cups of flour)
 * because that needs a density table per ingredient. Quantities in different families are
 * therefore treated as "cannot be compared" and the recipe is NOT suggested (strict rule).
 */
public final class UnitConverter {

    public enum Family { MASS, VOLUME, COUNT }

    /** Units shown in the Add/Edit spinner. */
    public static final String[] UI_UNITS =
            {"g", "kg", "oz", "lb", "ml", "l", "tsp", "tbsp", "cup", "pcs"};

    private static final Map<String, Family> FAMILY = new HashMap<>();
    private static final Map<String, Double> TO_BASE = new HashMap<>();

    private static void def(Family f, double factor, String... aliases) {
        for (String a : aliases) {
            FAMILY.put(a, f);
            TO_BASE.put(a, factor);
        }
    }

    static {
        def(Family.MASS, 1, "g", "gram", "grams", "gr");
        def(Family.MASS, 1000, "kg", "kgs", "kilogram", "kilograms");
        def(Family.MASS, 28.3495, "oz", "ounce", "ounces");
        def(Family.MASS, 453.592, "lb", "lbs", "pound", "pounds");

        def(Family.VOLUME, 1, "ml", "milliliter", "milliliters", "millilitre", "millilitres");
        def(Family.VOLUME, 1000, "l", "liter", "liters", "litre", "litres");
        def(Family.VOLUME, 5, "tsp", "teaspoon", "teaspoons");
        def(Family.VOLUME, 15, "tbsp", "tablespoon", "tablespoons");
        def(Family.VOLUME, 240, "cup", "cups");

        def(Family.COUNT, 1, "pcs", "pc", "piece", "pieces", "unit", "units", "whole",
                "clove", "cloves", "slice", "slices", "can", "cans", "");
    }

    private UnitConverter() { }

    private static String clean(String unit) {
        return unit == null ? "" : unit.trim().toLowerCase(Locale.ROOT).replace(".", "");
    }

    /** Unknown units are treated as plain counts. */
    public static Family familyOf(String unit) {
        Family f = FAMILY.get(clean(unit));
        return f == null ? Family.COUNT : f;
    }

    /** Converts quantity in the given unit to the base unit of its family. */
    public static double toBase(double quantity, String unit) {
        Double factor = TO_BASE.get(clean(unit));
        return quantity * (factor == null ? 1.0 : factor);
    }
}
