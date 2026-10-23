package com.example.smartpantry.model;

import java.util.ArrayList;
import java.util.List;

/** A recipe with its required ingredients and preparation steps. */
public class Recipe {

    private final long id;
    private final String name;
    private final String description;
    private final String steps;          // one step per line
    private final int prepMinutes;
    private final List<RecipeIngredient> ingredients = new ArrayList<>();

    public Recipe(long id, String name, String description, String steps, int prepMinutes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.steps = steps;
        this.prepMinutes = prepMinutes;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getSteps() { return steps; }
    public int getPrepMinutes() { return prepMinutes; }
    public List<RecipeIngredient> getIngredients() { return ingredients; }
}
